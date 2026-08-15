package dev.iliv007.ivai.provider.gemini

import dev.iliv007.ivai.provider.CredentialReference
import dev.iliv007.ivai.provider.NormalizedProviderError
import dev.iliv007.ivai.provider.ProviderChatRequest
import dev.iliv007.ivai.provider.ProviderConnectionValidation
import dev.iliv007.ivai.provider.ProviderErrorKind
import dev.iliv007.ivai.provider.ProviderId
import dev.iliv007.ivai.provider.ProviderStreamEvent
import dev.iliv007.ivai.security.EncryptedSecretVault
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
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
 * The only Gemini call site permitted to resolve a vault reference into transient plaintext.
 * It performs one foreground request; it does not schedule, retry, retain prompts, or use a
 * backend proxy.
 */
class GeminiNetworkGate(
    private val vault: EncryptedSecretVault,
    private val transport: GeminiHttpTransport = HttpUrlConnectionGeminiTransport(),
    private val diagnostics: SafeNetworkDiagnostics = SafeNetworkDiagnostics.None,
    private val nowEpochMs: () -> Long = System::currentTimeMillis
) {
    /** This checks only the local vault boundary; a live request is validated when chat starts. */
    suspend fun validateStoredCredential(reference: CredentialReference): ProviderConnectionValidation {
        val exists = !vault.read(reference.value).isNullOrBlank()
        return if (exists) {
            ProviderConnectionValidation(providerId = GeminiChatProvider.ID, isUsable = true)
        } else {
            ProviderConnectionValidation(
                providerId = GeminiChatProvider.ID,
                isUsable = false,
                error = NormalizedProviderError(
                    kind = ProviderErrorKind.AUTHENTICATION,
                    safeMessage = "No usable Gemini credential is stored for this provider."
                )
            )
        }
    }

    fun stream(request: ProviderChatRequest): Flow<ProviderStreamEvent> = flow {
        val credential = vault.read(request.credentialReference.value)
        if (credential.isNullOrBlank()) {
            emit(
                ProviderStreamEvent.Failed(
                    NormalizedProviderError(
                        kind = ProviderErrorKind.AUTHENTICATION,
                        safeMessage = "No usable Gemini credential is stored for this provider."
                    )
                )
            )
            return@flow
        }

        val startedAt = nowEpochMs()
        var status: Int? = null
        var terminalError: NormalizedProviderError? = null
        var exchange: GeminiHttpExchange? = null
        try {
            exchange = withContext(Dispatchers.IO) {
                transport.open(
                    url = GeminiInteractionsProtocol.INTERACTIONS_URL,
                    apiKey = credential,
                    requestBody = GeminiInteractionsProtocol.encodeStreamingRequest(request)
                )
            }
            val currentExchange = exchange
            try {
                status = currentExchange.statusCode
                if (status !in 200..299) {
                    terminalError = GeminiInteractionsProtocol.decodeHttpError(status)
                    emit(ProviderStreamEvent.Failed(terminalError))
                    return@flow
                }

                emit(ProviderStreamEvent.Started(request.attemptId))
                val sseReader = GeminiSseReader(
                    BufferedReader(InputStreamReader(currentExchange.responseStream, Charsets.UTF_8))
                )
                var completed = false
                while (currentCoroutineContext().isActive) {
                    val rawEvent = runInterruptible(Dispatchers.IO) { sseReader.next() } ?: break
                    when (val decoded = GeminiInteractionsProtocol.decodeStreamEvent(
                        data = rawEvent.data,
                        fallbackMessageId = request.attemptId
                    )) {
                        is GeminiDecodedEvent.TextDelta -> emit(ProviderStreamEvent.Delta(decoded.text))
                        is GeminiDecodedEvent.Completed -> {
                            decoded.usage?.let { emit(ProviderStreamEvent.Usage(it)) }
                            emit(ProviderStreamEvent.Completed(decoded.messageId))
                            completed = true
                            break
                        }
                        is GeminiDecodedEvent.Failed -> {
                            terminalError = decoded.error
                            emit(ProviderStreamEvent.Failed(decoded.error))
                            return@flow
                        }
                        GeminiDecodedEvent.Malformed -> {
                            terminalError = NormalizedProviderError(
                                kind = ProviderErrorKind.MALFORMED_RESPONSE,
                                safeMessage = "Gemini returned an unreadable stream event."
                            )
                            emit(ProviderStreamEvent.Failed(terminalError))
                            return@flow
                        }
                        GeminiDecodedEvent.Done, null -> Unit
                    }
                }
                if (!completed && currentCoroutineContext().isActive) {
                    terminalError = NormalizedProviderError(
                        kind = ProviderErrorKind.MALFORMED_RESPONSE,
                        safeMessage = "Gemini stream ended before completion."
                    )
                    emit(ProviderStreamEvent.Failed(terminalError))
                }
            } finally {
                currentExchange.close()
            }
        } catch (cancellation: CancellationException) {
            // The ViewModel owns the user-visible cancelled terminal state; closing the exchange
            // immediately stops foreground I/O without attempting a background cancellation call.
            throw cancellation
        } catch (_: IOException) {
            terminalError = NormalizedProviderError(
                kind = ProviderErrorKind.NETWORK_UNAVAILABLE,
                safeMessage = "Gemini could not be reached. Check the network connection.",
                retryable = true
            )
            emit(ProviderStreamEvent.Failed(terminalError))
        } catch (_: Throwable) {
            terminalError = NormalizedProviderError(
                kind = ProviderErrorKind.UNKNOWN,
                safeMessage = "Gemini request could not be completed."
            )
            emit(ProviderStreamEvent.Failed(terminalError))
        } finally {
            exchange?.close()
            diagnostics.record(
                SafeNetworkDiagnostic(
                    providerId = GeminiChatProvider.ID,
                    host = "generativelanguage.googleapis.com",
                    attemptId = request.attemptId,
                    startedAtEpochMs = startedAt,
                    finishedAtEpochMs = nowEpochMs(),
                    httpStatus = status,
                    errorKind = terminalError?.kind
                )
            )
        }
    }
}

interface GeminiHttpTransport {
    @Throws(IOException::class)
    fun open(url: String, apiKey: String, requestBody: String): GeminiHttpExchange
}

interface GeminiHttpExchange : Closeable {
    val statusCode: Int
    val responseStream: InputStream
}

class HttpUrlConnectionGeminiTransport : GeminiHttpTransport {
    override fun open(url: String, apiKey: String, requestBody: String): GeminiHttpExchange {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "text/event-stream")
            setRequestProperty("x-goog-api-key", apiKey)
        }
        connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(requestBody) }
        return HttpUrlConnectionGeminiExchange(connection)
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 20_000
        const val READ_TIMEOUT_MS = 60_000
    }
}

private class HttpUrlConnectionGeminiExchange(
    private val connection: HttpURLConnection
) : GeminiHttpExchange {
    override val statusCode: Int by lazy { connection.responseCode }
    override val responseStream: InputStream by lazy {
        if (statusCode in 200..299) connection.inputStream else connection.errorStream ?: ByteArrayInputStream(ByteArray(0))
    }

    override fun close() {
        runCatching { responseStream.close() }
        connection.disconnect()
    }
}

data class SafeNetworkDiagnostic(
    val providerId: ProviderId,
    val host: String,
    val attemptId: String,
    val startedAtEpochMs: Long,
    val finishedAtEpochMs: Long,
    val httpStatus: Int?,
    val errorKind: ProviderErrorKind?
)

fun interface SafeNetworkDiagnostics {
    fun record(diagnostic: SafeNetworkDiagnostic)

    data object None : SafeNetworkDiagnostics {
        override fun record(diagnostic: SafeNetworkDiagnostic) = Unit
    }
}
