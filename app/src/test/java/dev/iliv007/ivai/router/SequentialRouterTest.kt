package dev.iliv007.ivai.router

import dev.iliv007.ivai.data.local.ProviderAccountEntity
import dev.iliv007.ivai.data.local.ProviderConnectionEntity
import dev.iliv007.ivai.data.local.ProviderModelEntity
import dev.iliv007.ivai.data.local.RouterComboEntryEntity
import dev.iliv007.ivai.provider.ProviderCapability
import org.junit.Assert.assertEquals
import org.junit.Test

class SequentialRouterTest {
    private val router = SequentialRouter()

    @Test
    fun `combo resolution preserves user order and skips unavailable provider credentials`() {
        val catalog = catalog(
            credentialPresent = setOf("credential.second"),
            firstCapabilities = "TEXT,STREAMING",
            secondCapabilities = "TEXT,STREAMING"
        )
        val resolution = router.resolve(
            target = ExecutionTarget.Combo("user-combo"),
            comboEntries = listOf(
                entry("first-entry", position = 0, connectionId = "first", accountId = "first-account", modelId = "first-model"),
                entry("second-entry", position = 1, connectionId = "second", accountId = "second-account", modelId = "second-model")
            ),
            catalog = catalog,
            requiredCapabilities = setOf(ProviderCapability.TEXT, ProviderCapability.STREAMING)
        )

        assertEquals(listOf("second"), resolution.candidates.map { it.connectionId })
        assertEquals(listOf("second-model"), resolution.candidates.map { it.modelId })
    }

    @Test
    fun `router skips text only entry when streaming is required without selecting a default provider`() {
        val catalog = catalog(
            credentialPresent = setOf("credential.first", "credential.second"),
            firstCapabilities = "TEXT",
            secondCapabilities = "TEXT,STREAMING"
        )

        val resolution = router.resolve(
            target = ExecutionTarget.Combo("user-combo"),
            comboEntries = listOf(
                entry("first-entry", position = 0, connectionId = "first", accountId = "first-account", modelId = "first-model"),
                entry("second-entry", position = 1, connectionId = "second", accountId = "second-account", modelId = "second-model")
            ),
            catalog = catalog,
            requiredCapabilities = setOf(ProviderCapability.TEXT, ProviderCapability.STREAMING)
        )

        assertEquals(1, resolution.candidates.size)
        assertEquals("second", resolution.candidates.single().connectionId)
    }

    @Test
    fun `direct model target resolves only the exact connection account and model chosen by the user`() {
        val catalog = catalog(
            credentialPresent = setOf("credential.second"),
            firstCapabilities = "TEXT,STREAMING",
            secondCapabilities = "TEXT,STREAMING"
        )

        val resolution = router.resolve(
            target = ExecutionTarget.DirectModel("second", "second-account", "second-model"),
            comboEntries = emptyList(),
            catalog = catalog,
            requiredCapabilities = setOf(ProviderCapability.TEXT, ProviderCapability.STREAMING)
        )

        assertEquals(listOf("second"), resolution.candidates.map { it.connectionId })
    }

    private fun catalog(
        credentialPresent: Set<String>,
        firstCapabilities: String,
        secondCapabilities: String
    ) = RouterCatalog(
        connections = listOf(
            ProviderConnectionEntity("first", "CUSTOM_OPENAI_COMPATIBLE", "User endpoint", "https://example.test/v1", true, 1L, 1L),
            ProviderConnectionEntity("second", "OPENROUTER", "User OpenRouter", null, true, 1L, 1L)
        ),
        accounts = listOf(
            ProviderAccountEntity("first-account", "first", "First BYOK", "credential.first", true, 1L, 1L),
            ProviderAccountEntity("second-account", "second", "Second BYOK", "credential.second", true, 1L, 1L)
        ),
        models = listOf(
            ProviderModelEntity("first-model", "first", "first-model-id", "First", firstCapabilities, true, true, 1L),
            ProviderModelEntity("second-model", "second", "second-model-id", "Second", secondCapabilities, true, true, 1L)
        ),
        credentialPresent = credentialPresent
    )

    private fun entry(id: String, position: Int, connectionId: String, accountId: String, modelId: String) =
        RouterComboEntryEntity(id, "user-combo", position, connectionId, accountId, modelId, true)
}
