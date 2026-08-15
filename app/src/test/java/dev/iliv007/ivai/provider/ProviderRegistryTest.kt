package dev.iliv007.ivai.provider

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ProviderRegistryTest {

    @Test
    fun `custom endpoint accepts only remote HTTPS without credentials or fragment`() {
        ProviderEndpointPolicy.requireAllowedRemoteEndpoint("https://api.example.test/v1")

        listOf(
            "http://api.example.test/v1",
            "https://localhost:8443/v1",
            "https://127.0.0.1/v1",
            "https://user:pass@api.example.test/v1",
            "https://api.example.test/v1#fragment"
        ).forEach { invalid ->
            assertThrows(IllegalArgumentException::class.java) {
                ProviderEndpointPolicy.requireAllowedRemoteEndpoint(invalid)
            }
        }
    }

    @Test
    fun `provider connection rejects base URL override for managed provider kinds`() {
        assertThrows(IllegalArgumentException::class.java) {
            ProviderConnectionDescriptor(
                id = "gemini-main",
                kind = ProviderKind.GEMINI,
                displayName = "Gemini",
                baseUrl = "https://example.test",
                enabled = true
            )
        }
    }

    @Test
    fun `adapter registry resolves only registered kind`() {
        val adapter = object : ChatProvider {
            override val providerId = ProviderId("gemini")
            override suspend fun validateConnection(credentialReference: CredentialReference) =
                ProviderConnectionValidation(providerId, isUsable = true)
            override suspend fun discoverModels(credentialReference: CredentialReference) = emptyList<ProviderModelDescriptor>()
            override fun streamChat(request: ProviderChatRequest) = kotlinx.coroutines.flow.emptyFlow<ProviderStreamEvent>()
        }

        val registry = ProviderAdapterRegistry(setOf(adapter))
        assertEquals(adapter, registry.requireAdapter(ProviderKind.GEMINI))
        assertThrows(IllegalArgumentException::class.java) {
            registry.requireAdapter(ProviderKind.OPENROUTER)
        }
    }
}
