package dev.iliv007.ivai.provider.openai

import dev.iliv007.ivai.provider.ChatProvider
import dev.iliv007.ivai.provider.CredentialReference
import dev.iliv007.ivai.provider.NormalizedProviderError
import dev.iliv007.ivai.provider.ProviderChatRequest
import dev.iliv007.ivai.provider.ProviderConnectionValidation
import dev.iliv007.ivai.provider.ProviderErrorKind
import dev.iliv007.ivai.provider.ProviderId
import dev.iliv007.ivai.provider.ProviderModelDescriptor
import dev.iliv007.ivai.provider.ProviderStreamEvent
import dev.iliv007.ivai.provider.gemini.SafeNetworkDiagnostic
import dev.iliv007.ivai.provider.gemini.SafeNetworkDiagnostics
import dev.iliv007.ivai.security.EncryptedSecretVault
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext

/**
 * One foreground-only gate for a user-approved OpenAI Chat Completions-compatible connection.
 * It resolves a vault reference immediately before the HTTP call and stores neither credential nor
 * prompt in diagnostics. A custom endpoint has already passed [ProviderEndpointPolicy] validation.
 */
class OpenAiCompatibleNetworkGate(
    private val providerId: ProviderId,
    private val baseUrl: String,
    private val vault: EncryptedSecretVault,
    private val transport: OpenAiCompatibleHttpTransport = HttpUrlConnectionOpenAiCompatibleTransport(),
    private val diagnostics: SafeNetworkDiagnostics = SafeNetworkDiagnostics.None,
    private val nowEpochMs: () -> Long = System::currentTimeMillis
) {
    init {
        val uri = URI(baseUrl)
        require(uri.scheme == "https" && !uri.host.isNullOrBlank()) { "OpenAI-compatible base URL must be HTTPS" }
    }

    suspend fun validateStoredCredential(reference: CredentialReference): ProviderConnectionValidation {
        val exists = !vault.read(reference.value).isNullOrBlank()
        return if (exists) {
            ProviderConnectionValidation(providerId, isUsable = true)
        } else {
            ProviderConnectionValidation(
                providerId,
                isUsable = false,
                error = NormalizedProviderError(
                    kind = ProviderErrorKind.AUTHENTICATION,
                    safeMessage = "No usable credential is stored for this provider."
                )
            )
        }
    }

    fun stream(request: ProviderChatRequest): Flow<ProviderStreamEvent> = flow {
        val credential = vault.read(request.credentialReference.value)
        if (credential.isNullOrBlank()) {
            emit(ProviderStreamEvent.Failed(NormalizedProviderError(
                kind = ProviderErrorKind.AUTHENTICATION,
                safeMessage = "No usable credential is stored for this provider."
            )))
            return@flow
        }

        val startedAt = nowEpochMs()
        var status: Int? = null
        var terminalError: NormalizedProviderError? = null
        var exchange: OpenAiCompatibleHttpExchange? = null
        try {
            exchange = withContext(Dispatchers.IO) {
                transport.open(
                    url = "$baseUrl/chat/completions",
                    bearerToken = credential,
                    requestBody = OpenAiCompatibleProtocol.encodeStreamingRequest(request)
                )
            }
            val currentExchange = exchange
            try {
                status = currentExchange.statusCode
                if (status !in 200..299) {
                    terminalError = OpenAiCompatibleProtocol.decodeHttpError(status)
                    emit(ProviderStreamEvent.Failed(terminalError))
                    return@flow
                }
                emit(ProviderStreamEvent.Started(request.attemptId))
                val reader = OpenAiCompatibleSseReader(
                    BufferedReader(InputStreamReader(currentExchange.responseStream, Charsets.UTF_8))
                )
                var completed = false
                while (currentCoroutineContext().isActive) {
                    val raw = runInterruptible(Dispatchers.IO) { reader.next() } ?: break
                    when (val decoded = OpenAiCompatibleProtocol.decodeStreamEvent(raw.data, request.attemptId)) {
                        is OpenAiDecodedEvent.TextDelta -> emit(ProviderStreamEvent.Delta(decoded.text))
                        is OpenAiDecodedEvent.Completed -> {
                            decoded.usage?.let { emit(ProviderStreamEvent.Usage(it)) }
                            emit(ProviderStreamEvent.Completed(decoded.messageId))
                            completed = true
                            break
                        }
                        is OpenAiDecodedEvent.Failed -> {
                            terminalError = decoded.error
                            emit(ProviderStreamEvent.Failed(decoded.error))
                            return@flow
                        }
                        OpenAiDecodedEvent.Done -> {
                            if (!completed) {
                                emit(ProviderStreamEvent.Completed(request.attemptId))
                                completed = true
                            }
                            break
                        }
                        OpenAiDecodedEvent.Malformed -> {
                            terminalError = NormalizedProviderError(
                                kind = ProviderErrorKind.MALFORMED_RESPONSE,
                                safeMessage = "Provider returned an unreadable stream event."
                            )
                            emit(ProviderStreamEvent.Failed(terminalError))
                            return@flow
                        }
                    }
                }
                if (!completed && currentCoroutineContext().isActive) {
                    terminalError = NormalizedProviderError(
                        kind = ProviderErrorKind.MALFORMED_RESPONSE,
                        safeMessage = "Provider stream ended before completion."
                    )
                    emit(ProviderStreamEvent.Failed(terminalError))
                }
            } finally {
                currentExchange.close()
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: IOException) {
            terminalError = NormalizedProviderError(
                kind = ProviderErrorKind.NETWORK_UNAVAILABLE,
                safeMessage = "Provider could not be reached. Check the network connection.",
                retryable = true
            )
            emit(ProviderStreamEvent.Failed(terminalError))
        } catch (_: Throwable) {
            terminalError = NormalizedProviderError(
                kind = ProviderErrorKind.UNKNOWN,
                safeMessage = "Provider request could not be completed."
            )
            emit(ProviderStreamEvent.Failed(terminalError))
        } finally {
            exchange?.close()
            diagnostics.record(SafeNetworkDiagnostic(
                providerId = providerId,
                host = URI(baseUrl).host,
                attemptId = request.attemptId,
                startedAtEpochMs = startedAt,
                finishedAtEpochMs = nowEpochMs(),
                httpStatus = status,
                errorKind = terminalError?.kind
            ))
        }
    }
}

class OpenRouterChatProvider(
    vault: EncryptedSecretVault,
    gate: OpenAiCompatibleNetworkGate = OpenAiCompatibleNetworkGate(ID, BASE_URL, vault)
) : ChatProvider {
    override val providerId: ProviderId = ID
    private val networkGate = gate
    override suspend fun validateConnection(credentialReference: CredentialReference) = networkGate.validateStoredCredential(credentialReference)
    override suspend fun discoverModels(credentialReference: CredentialReference): List<ProviderModelDescriptor> = emptyList()
    override fun streamChat(request: ProviderChatRequest) = networkGate.stream(request)

    companion object {
        val ID = ProviderId("openrouter")
        const val BASE_URL = "https://openrouter.ai/api/v1"
    }
}

class CustomOpenAiChatProvider(
    baseUrl: String,
    vault: EncryptedSecretVault,
    gate: OpenAiCompatibleNetworkGate = OpenAiCompatibleNetworkGate(ID, baseUrl, vault)
) : ChatProvider {
    override val providerId: ProviderId = ID
    private val networkGate = gate
    override suspend fun validateConnection(credentialReference: CredentialReference) = networkGate.validateStoredCredential(credentialReference)
    override suspend fun discoverModels(credentialReference: CredentialReference): List<ProviderModelDescriptor> = emptyList()
    override fun streamChat(request: ProviderChatRequest) = networkGate.stream(request)

    companion object { val ID = ProviderId("custom-openai-compatible") }
}

internal data class OpenAiCompatibleSseEvent(val data: String)

internal class OpenAiCompatibleSseReader(private val reader: BufferedReader) {
    private val data = StringBuilder()
    fun next(): OpenAiCompatibleSseEvent? {
        while (true) {
            val line = reader.readLine() ?: return dispatchOrNull()
            when {
                line.isEmpty() -> dispatchOrNull()?.let { return it }
                line.startsWith("data:") -> {
                    if (data.isNotEmpty()) data.append('\n')
                    data.append(line.substringAfter(':').trimStart())
                }
                line.startsWith(':') -> Unit
            }
        }
    }
    private fun dispatchOrNull(): OpenAiCompatibleSseEvent? =
        data.takeIf { it.isNotEmpty() }?.toString()?.let { OpenAiCompatibleSseEvent(it).also { data.clear() } }
}

interface OpenAiCompatibleHttpTransport {
    @Throws(IOException::class)
    fun open(url: String, bearerToken: String, requestBody: String): OpenAiCompatibleHttpExchange
}

interface OpenAiCompatibleHttpExchange : Closeable {
    val statusCode: Int
    val responseStream: InputStream
}

class HttpUrlConnectionOpenAiCompatibleTransport : OpenAiCompatibleHttpTransport {
    override fun open(url: String, bearerToken: String, requestBody: String): OpenAiCompatibleHttpExchange {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 60_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "text/event-stream")
            setRequestProperty("Authorization", "Bearer $bearerToken")
        }
        connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(requestBody) }
        return HttpUrlConnectionOpenAiCompatibleExchange(connection)
    }
}

private class HttpUrlConnectionOpenAiCompatibleExchange(
    private val connection: HttpURLConnection
) : OpenAiCompatibleHttpExchange {
    override val statusCode: Int by lazy { connection.responseCode }
    override val responseStream: InputStream by lazy {
        if (statusCode in 200..299) connection.inputStream else connection.errorStream ?: ByteArrayInputStream(ByteArray(0))
    }
    override fun close() {
        runCatching { responseStream.close() }
        connection.disconnect()
    }
}
