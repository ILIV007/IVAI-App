package dev.iliv007.ivai.provider.openai

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import dev.iliv007.ivai.provider.ProviderAccountAuthMode
import dev.iliv007.ivai.provider.ProviderChatRequest
import dev.iliv007.ivai.provider.ProviderEndpointTrustMode
import dev.iliv007.ivai.provider.ProviderId
import dev.iliv007.ivai.provider.ProviderMessage
import dev.iliv007.ivai.provider.ProviderMessageRole
import dev.iliv007.ivai.provider.ProviderStreamEvent
import dev.iliv007.ivai.security.EncryptedSecretPayload
import dev.iliv007.ivai.security.EncryptedSecretVault
import dev.iliv007.ivai.security.SecretCipher
import java.io.ByteArrayInputStream
import java.io.File
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OpenAiCompatibleNoAuthTest {
    private lateinit var preferencesFile: File
    private lateinit var vault: EncryptedSecretVault

    @Before
    fun setUp() {
        preferencesFile = File(
            ApplicationProvider.getApplicationContext<android.content.Context>().cacheDir,
            "no-auth-vault-${System.nanoTime()}.preferences_pb"
        )
        vault = EncryptedSecretVault(
            PreferenceDataStoreFactory.create { preferencesFile },
            cipherForReference = { RecordingCipher() }
        )
    }

    @After
    fun tearDown() {
        preferencesFile.delete()
    }

    @Test
    fun `fatal transport error propagates instead of being normalized`() = runBlocking {
        val transport = object : OpenAiCompatibleHttpTransport {
            override fun open(url: String, bearerToken: String?, requestBody: String): OpenAiCompatibleHttpExchange =
                throw AssertionError("fatal transport error")
        }
        val gate = OpenAiCompatibleNetworkGate(
            providerId = ProviderId("custom-openai-compatible"),
            baseUrl = "https://localhost:1234/v1",
            trustMode = ProviderEndpointTrustMode.LOCAL_LOOPBACK_HTTPS,
            vault = vault,
            transport = transport
        )
        var propagated: AssertionError? = null

        try {
            gate.stream(
                ProviderChatRequest(
                    credentialReference = null,
                    authMode = ProviderAccountAuthMode.NONE,
                    modelId = "user-local-model",
                    messages = listOf(ProviderMessage(ProviderMessageRole.USER, "local test")),
                    attemptId = "fatal-no-auth-1"
                )
            ).toList()
        } catch (error: AssertionError) {
            propagated = error
        }

        assertEquals("fatal transport error", propagated?.message)
    }

    @Test
    fun `trusted local no-auth request omits Authorization and does not need a vault entry`() = runBlocking {
        val transport = RecordingTransport()
        val gate = OpenAiCompatibleNetworkGate(
            providerId = ProviderId("custom-openai-compatible"),
            baseUrl = "https://localhost:1234/v1",
            trustMode = ProviderEndpointTrustMode.LOCAL_LOOPBACK_HTTPS,
            vault = vault,
            transport = transport
        )

        val events = gate.stream(
            ProviderChatRequest(
                credentialReference = null,
                authMode = ProviderAccountAuthMode.NONE,
                modelId = "user-local-model",
                messages = listOf(ProviderMessage(ProviderMessageRole.USER, "local test")),
                attemptId = "local-no-auth-1"
            )
        ).toList()

        assertEquals("https://localhost:1234/v1/chat/completions", transport.url)
        assertNull(transport.bearerToken)
        assertTrue(transport.requestBody.contains("user-local-model"))
        assertTrue(events.any { it is ProviderStreamEvent.Started })
        assertTrue(events.last() is ProviderStreamEvent.Completed)
        assertEquals(1, transport.closeCount)
    }

    private class RecordingTransport : OpenAiCompatibleHttpTransport {
        var url: String? = null
        var bearerToken: String? = null
        var requestBody: String = ""
        var closeCount: Int = 0

        override fun open(url: String, bearerToken: String?, requestBody: String): OpenAiCompatibleHttpExchange {
            this.url = url
            this.bearerToken = bearerToken
            this.requestBody = requestBody
            return object : OpenAiCompatibleHttpExchange {
                override val statusCode: Int = 200
                override val responseStream = ByteArrayInputStream(
                    "data: {\"id\":\"local-no-auth-1\",\"choices\":[{\"delta\":{},\"finish_reason\":\"stop\"}]}\\n\\n".encodeToByteArray()
                )
                override fun close() {
                    closeCount += 1
                }
            }
        }
    }

    private class RecordingCipher : SecretCipher {
        override fun encrypt(plaintext: ByteArray) = EncryptedSecretPayload(
            version = EncryptedSecretPayload.CURRENT_VERSION,
            iv = byteArrayOf(1),
            ciphertext = plaintext
        )

        override fun decrypt(payload: EncryptedSecretPayload): ByteArray = payload.ciphertext

        override fun deleteKey() = Unit
    }
}
