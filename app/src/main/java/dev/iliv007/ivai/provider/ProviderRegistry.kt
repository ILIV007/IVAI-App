package dev.iliv007.ivai.provider

import java.net.URI

enum class ProviderKind(val adapterId: ProviderId) {
    CUSTOM_OPENAI_COMPATIBLE(ProviderId("custom-openai-compatible")),
    OPENROUTER(ProviderId("openrouter")),
    GEMINI(ProviderId("gemini"))
}

/**
 * Local, non-secret setup metadata. A preset never creates a connection, chooses a model,
 * performs discovery, or sends a request; the user must still review and save every value.
 */
data class ProviderPreset(
    val id: String,
    val displayName: String,
    val kind: ProviderKind,
    val suggestedBaseUrl: String?,
    val documentationUrl: String,
    val protocolLabel: String,
    val credentialRequired: Boolean = true
) {
    init {
        require(id.matches(Regex("[a-z][a-z0-9-]{0,63}"))) { "Invalid provider preset ID" }
        require(displayName.isNotBlank()) { "Provider preset name must not be blank" }
        require(documentationUrl.startsWith("https://")) { "Provider preset documentation URL must use HTTPS" }
        if (suggestedBaseUrl != null) ProviderEndpointPolicy.requireAllowedRemoteEndpoint(suggestedBaseUrl)
        require(kind == ProviderKind.CUSTOM_OPENAI_COMPATIBLE || suggestedBaseUrl == null) {
            "Managed provider presets must not override their adapter base URL"
        }
    }
}

/**
 * A deliberately small, reviewable catalog for the installed provider protocols. Cloud presets map
 * to the existing OpenAI-compatible adapter; adding a catalog row does not add an adapter, a key,
 * an implied network call, or a default provider. Local server presets are intentionally excluded
 * until explicit local-endpoint trust and Android cleartext policy are implemented.
 */
object ProviderPresetCatalog {
    val all: List<ProviderPreset> = listOf(
        ProviderPreset("gemini", "Google Gemini", ProviderKind.GEMINI, null, "https://ai.google.dev/gemini-api/docs", "Gemini API"),
        ProviderPreset("openrouter", "OpenRouter", ProviderKind.OPENROUTER, null, "https://openrouter.ai/docs", "OpenRouter API"),
        cloudOpenAiCompatible("openai", "OpenAI", "https://api.openai.com/v1", "https://platform.openai.com/docs/api-reference"),
        cloudOpenAiCompatible("groq", "Groq", "https://api.groq.com/openai/v1", "https://console.groq.com/docs/openai"),
        cloudOpenAiCompatible("mistral", "Mistral AI", "https://api.mistral.ai/v1", "https://docs.mistral.ai/resources/migration-guides"),
        cloudOpenAiCompatible("together", "Together AI", "https://api.together.ai/v1", "https://docs.together.ai/docs/inference/openai-compatibility"),
        cloudOpenAiCompatible("deepseek", "DeepSeek", "https://api.deepseek.com", "https://api-docs.deepseek.com/"),
        cloudOpenAiCompatible("fireworks", "Fireworks AI", "https://api.fireworks.ai/inference/v1", "https://docs.fireworks.ai/tools-sdks/openai-compatibility"),
        cloudOpenAiCompatible("xai", "xAI", "https://api.x.ai/v1", "https://docs.x.ai/developers/quickstart")
    )

    fun find(id: String): ProviderPreset? = all.firstOrNull { it.id == id }

    private fun cloudOpenAiCompatible(
        id: String,
        displayName: String,
        baseUrl: String,
        documentationUrl: String
    ) = ProviderPreset(
        id = id,
        displayName = displayName,
        kind = ProviderKind.CUSTOM_OPENAI_COMPATIBLE,
        suggestedBaseUrl = baseUrl,
        documentationUrl = documentationUrl,
        protocolLabel = "OpenAI-compatible"
    )
}

/** Non-secret, persisted description of a user-controlled provider connection. */
data class ProviderConnectionDescriptor(
    val id: String,
    val kind: ProviderKind,
    val displayName: String,
    val baseUrl: String?,
    val enabled: Boolean
) {
    init {
        require(id.matches(Regex("[a-z][a-z0-9._-]{0,63}"))) { "Invalid connection ID" }
        require(displayName.isNotBlank()) { "Connection display name must not be blank" }
        if (baseUrl != null) ProviderEndpointPolicy.requireAllowedRemoteEndpoint(baseUrl)
        require(kind == ProviderKind.CUSTOM_OPENAI_COMPATIBLE || baseUrl == null) {
            "Only custom OpenAI-compatible connections may override a base URL"
        }
    }
}

/** Account metadata stores only an opaque [CredentialReference], never ciphertext or plaintext. */
data class ProviderAccountDescriptor(
    val id: String,
    val connectionId: String,
    val displayName: String,
    val credentialReference: CredentialReference,
    val enabled: Boolean
)

data class ManualProviderModel(
    val id: String,
    val connectionId: String,
    val providerModelId: String,
    val displayName: String,
    val capabilities: Set<ProviderCapability>,
    val selectable: Boolean
) {
    init {
        require(id.matches(Regex("[a-z][a-z0-9._-]{0,63}"))) { "Invalid model record ID" }
        require(providerModelId.isNotBlank()) { "Provider model ID must not be blank" }
        require(displayName.isNotBlank()) { "Model display name must not be blank" }
        require(capabilities.isNotEmpty()) { "A model must declare at least one capability" }
    }
}

/**
 * Custom remote endpoints are HTTPS-only for this foundation. Local HTTP endpoints need a future,
 * explicit threat model and user-warning UX rather than an implicit bypass.
 */
object ProviderEndpointPolicy {
    fun requireAllowedRemoteEndpoint(rawUrl: String) {
        val uri = runCatching { URI(rawUrl) }.getOrElse { throw IllegalArgumentException("Invalid provider endpoint") }
        require(uri.scheme == "https") { "Custom provider endpoints must use HTTPS" }
        require(!uri.host.isNullOrBlank()) { "Custom provider endpoint host is required" }
        require(uri.userInfo == null) { "Custom provider endpoint must not include user info" }
        require(uri.fragment == null) { "Custom provider endpoint must not include a fragment" }
        val normalizedHost = uri.host.lowercase()
        require(normalizedHost != "localhost" && normalizedHost != "127.0.0.1" && normalizedHost != "::1") {
            "Local provider endpoints require an explicit future opt-in"
        }
    }
}

/** Resolves an adapter by kind; it never resolves, stores, or exposes secret plaintext. */
class ProviderAdapterRegistry(
    adapters: Set<ChatProvider>
) {
    private val byKind = adapters.associateBy { provider ->
        ProviderKind.entries.firstOrNull { it.adapterId == provider.providerId }
            ?: error("Adapter ID does not map to a supported provider kind")
    }

    fun requireAdapter(kind: ProviderKind): ChatProvider =
        requireNotNull(byKind[kind]) { "No adapter registered for $kind" }
}
