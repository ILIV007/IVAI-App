package dev.iliv007.ivai.provider.gemini

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import dev.iliv007.ivai.provider.CredentialReference
import dev.iliv007.ivai.provider.ProviderChatRequest
import dev.iliv007.ivai.provider.ProviderErrorKind
import dev.iliv007.ivai.provider.ProviderMessage
import dev.iliv007.ivai.provider.ProviderMessageRole
import dev.iliv007.ivai.provider.ProviderStreamEvent
import dev.iliv007.ivai.security.EncryptedSecretPayload
import dev.iliv007.ivai.security.EncryptedSecretVault
import dev.iliv007.ivai.security.SecretCipher
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GeminiNetworkGateTest {

    private lateinit var preferencesFile: File
    private lateinit var vault: EncryptedSecretVault

    @Before
    fun setUp() {
        preferencesFile = File(
            ApplicationProvider.getApplicationContext<android.content.Context>().cacheDir,
            "gemini-gate-${System.nanoTime()}.preferences_pb"
        )
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create { preferencesFile }
        vault = EncryptedSecretVault(dataStore) { RecordingCipher() }
    }

    @After
    fun tearDown() {
        preferencesFile.delete()
    }

    @Test
    fun `network gate streams normalized events with vault credential only in transport`() = runBlocking {
        vault.store("gemini", "test-only-credential")
        val exchange = FakeExchange(
            statusCode = 200,
            responseStream = ByteArrayInputStream(
                (
                    "event: step.delta\n" +
                        "data: {\"event_type\":\"step.delta\",\"delta\":{\"type\":\"text\",\"text\":\"سلام \"}}\n\n" +
                        "event: interaction.completed\n" +
                        "data: {\"event_type\":\"interaction.completed\",\"interaction\":{\"id\":\"response-1\",\"usage\":{\"total_input_tokens\":3,\"total_output_tokens\":5}}}\n\n" +
                        "data: [DONE]\n\n"
                    ).encodeToByteArray()
            )
        )
        val transport = FakeTransport(exchange)
        val diagnostics = mutableListOf<SafeNetworkDiagnostic>()
        val events = GeminiNetworkGate(
            vault = vault,
            transport = transport,
            diagnostics = SafeNetworkDiagnostics { diagnostics += it },
            nowEpochMs = { 100L }
        ).stream(request()).toList()

        assertTrue(events.first() is ProviderStreamEvent.Started)
        assertEquals("سلام ", (events[1] as ProviderStreamEvent.Delta).text)
        assertEquals(3, (events[2] as ProviderStreamEvent.Usage).usage.inputTokens)
        assertEquals("response-1", (events[3] as ProviderStreamEvent.Completed).messageId)
        assertEquals("test-only-credential", transport.apiKey)
        assertTrue(transport.body?.contains("\"store\":false") == true)
        assertEquals(1, diagnostics.size)
        assertEquals("generativelanguage.googleapis.com", diagnostics.single().host)
        assertFalse(diagnostics.single().toString().contains("test-only-credential"))
        assertTrue(exchange.wasClosed)
    }

    @Test
    fun `missing vault credential fails without opening transport`() = runBlocking {
        val transport = FakeTransport(FakeExchange(200, ByteArrayInputStream(ByteArray(0))))

        val events = GeminiNetworkGate(vault, transport).stream(request()).toList()

        assertTrue(events.single() is ProviderStreamEvent.Failed)
        assertEquals(
            ProviderErrorKind.AUTHENTICATION,
            (events.single() as ProviderStreamEvent.Failed).error.kind
        )
        assertFalse(transport.wasOpened)
    }

    @Test
    fun `http failure is normalized and does not expose provider payload`() = runBlocking {
        vault.store("gemini", "test-only-credential")
        val exchange = FakeExchange(429, ByteArrayInputStream("raw provider detail".encodeToByteArray()))

        val event = GeminiNetworkGate(vault, FakeTransport(exchange)).stream(request()).toList().single()

        assertTrue(event is ProviderStreamEvent.Failed)
        val error = (event as ProviderStreamEvent.Failed).error
        assertEquals(ProviderErrorKind.RATE_LIMIT, error.kind)
        assertFalse(error.safeMessage.contains("raw provider detail"))
        assertTrue(exchange.wasClosed)
    }

    @Test
    fun `cancelling collection closes active foreground exchange`() = runBlocking {
        vault.store("gemini", "test-only-credential")
        val input = PipedInputStream()
        val output = PipedOutputStream(input)
        val exchange = FakeExchange(200, input)
        val started = CompletableDeferred<Unit>()
        val job = launch {
            GeminiNetworkGate(vault, FakeTransport(exchange)).stream(request()).collect { event ->
                if (event is ProviderStreamEvent.Started) started.complete(Unit)
            }
        }

        withTimeout(2_000) { started.await() }
        job.cancelAndJoin()
        output.close()

        assertTrue(exchange.wasClosed)
    }

    private fun request() = ProviderChatRequest(
        credentialReference = CredentialReference("gemini"),
        modelId = "manual-model-id",
        messages = listOf(ProviderMessage(ProviderMessageRole.USER, "hello")),
        attemptId = "attempt-1"
    )

    private class FakeTransport(
        private val exchange: GeminiHttpExchange
    ) : GeminiHttpTransport {
        var wasOpened: Boolean = false
        var apiKey: String? = null
        var body: String? = null

        override fun open(url: String, apiKey: String, requestBody: String): GeminiHttpExchange {
            wasOpened = true
            this.apiKey = apiKey
            body = requestBody
            return exchange
        }
    }

    private class FakeExchange(
        override val statusCode: Int,
        override val responseStream: InputStream
    ) : GeminiHttpExchange {
        var wasClosed: Boolean = false

        override fun close() {
            wasClosed = true
            runCatching { responseStream.close() }
        }
    }

    private class RecordingCipher : SecretCipher {
        override fun encrypt(plaintext: ByteArray): EncryptedSecretPayload =
            EncryptedSecretPayload(
                version = EncryptedSecretPayload.CURRENT_VERSION,
                iv = "gemini-test-iv".encodeToByteArray(),
                ciphertext = plaintext.reversedArray()
            )

        override fun decrypt(payload: EncryptedSecretPayload): ByteArray = payload.ciphertext.reversedArray()

        override fun deleteKey() = Unit
    }
}
