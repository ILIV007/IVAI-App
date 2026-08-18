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
        ProviderPreset("gemini", "Gemini", ProviderKind.GEMINI, null, "https://ai.google.dev/gemini-api/docs", "Gemini API"),
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

enum class ProviderEndpointTrustMode {
    /** Existing cloud behavior: a remote, public HTTPS endpoint with API-key authentication. */
    REMOTE_HTTPS,

    /** A user-confirmed HTTPS endpoint on the Android device itself. */
    LOCAL_LOOPBACK_HTTPS,

    /** A user-confirmed private-LAN endpoint over HTTPS only. */
    LOCAL_LAN_HTTPS
}

enum class ProviderAccountAuthMode {
    API_KEY,
    NONE
}

fun noAuthCredentialMarker(accountId: String): String {
    require(accountId.matches(Regex("[a-z][a-z0-9._-]{0,63}"))) { "Invalid provider account ID" }
    return "no-auth.$accountId"
}

/** Non-secret, persisted description of a user-controlled provider connection. */
data class ProviderConnectionDescriptor(
    val id: String,
    val kind: ProviderKind,
    val displayName: String,
    val baseUrl: String?,
    val endpointTrustMode: ProviderEndpointTrustMode = ProviderEndpointTrustMode.REMOTE_HTTPS,
    val localTrustConfirmedAtEpochMs: Long? = null,
    val enabled: Boolean
) {
    init {
        require(id.matches(Regex("[a-z][a-z0-9._-]{0,63}"))) { "Invalid connection ID" }
        require(displayName.isNotBlank()) { "Connection display name must not be blank" }
        if (kind == ProviderKind.CUSTOM_OPENAI_COMPATIBLE) {
            require(!baseUrl.isNullOrBlank()) { "Custom OpenAI-compatible connections require a base URL" }
            ProviderEndpointPolicy.requireAllowedEndpoint(baseUrl, endpointTrustMode)
        } else {
            require(baseUrl == null) { "Only custom OpenAI-compatible connections may override a base URL" }
        }
        require(kind == ProviderKind.CUSTOM_OPENAI_COMPATIBLE || endpointTrustMode == ProviderEndpointTrustMode.REMOTE_HTTPS) {
            "Only custom OpenAI-compatible connections may use a local endpoint trust mode"
        }
        if (endpointTrustMode == ProviderEndpointTrustMode.REMOTE_HTTPS) {
            require(localTrustConfirmedAtEpochMs == null) { "Remote HTTPS connections must not carry local trust confirmation" }
        } else {
            require(localTrustConfirmedAtEpochMs != null && localTrustConfirmedAtEpochMs > 0L) {
                "Local endpoint connections require explicit trust confirmation"
            }
        }
    }
}

/** Account metadata stores an opaque credential reference only when [authMode] requires one. */
data class ProviderAccountDescriptor(
    val id: String,
    val connectionId: String,
    val displayName: String,
    val credentialReference: CredentialReference?,
    val authMode: ProviderAccountAuthMode = ProviderAccountAuthMode.API_KEY,
    val enabled: Boolean
) {
    init {
        require(id.matches(Regex("[a-z][a-z0-9._-]{0,63}"))) { "Invalid provider account ID" }
        require(connectionId.matches(Regex("[a-z][a-z0-9._-]{0,63}"))) { "Invalid provider connection ID" }
        require(displayName.isNotBlank()) { "Provider account name must not be blank" }
        require((authMode == ProviderAccountAuthMode.API_KEY) == (credentialReference != null)) {
            "API-key accounts require exactly one credential reference; no-auth accounts require none"
        }
    }
}

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
 * Endpoint classification is intentionally narrow. Remote cloud and user-managed local endpoints
 * are HTTPS-only. There is no HTTP opt-in, host scanning, dynamic DNS allowlist, userinfo, query
 * or fragment bypass. Cleartext local-server transport requires a separately reviewed Android
 * policy and device evidence; it is intentionally not part of this trust mode.
 */
object ProviderEndpointPolicy {
    private val loopbackHosts = setOf("localhost", "127.0.0.1", "::1", "[::1]")

    fun requireAllowedRemoteEndpoint(rawUrl: String) =
        requireAllowedEndpoint(rawUrl, ProviderEndpointTrustMode.REMOTE_HTTPS)

    fun requireAllowedEndpoint(rawUrl: String, trustMode: ProviderEndpointTrustMode) {
        val uri = runCatching { URI(rawUrl) }.getOrElse { throw IllegalArgumentException("Invalid provider endpoint") }
        val host = requireNotNull(uri.host) { "Custom provider endpoint host is required" }.lowercase()
        require(uri.userInfo == null) { "Custom provider endpoint must not include user info" }
        require(uri.fragment == null) { "Custom provider endpoint must not include a fragment" }
        require(uri.query == null) { "Custom provider endpoint must not include a query" }
        require(uri.port in -1..65535) { "Custom provider endpoint port is invalid" }
        require(uri.path.isEmpty() || uri.path.startsWith('/')) { "Custom provider endpoint path is invalid" }

        when (trustMode) {
            ProviderEndpointTrustMode.REMOTE_HTTPS -> {
                require(uri.scheme == "https") { "Remote provider endpoints must use HTTPS" }
                require(!isLoopback(host) && !isPrivateLanIpv4(host)) {
                    "Remote provider endpoint must not use a local or private-LAN host"
                }
            }
            ProviderEndpointTrustMode.LOCAL_LOOPBACK_HTTPS -> {
                require(uri.scheme == "https") { "Local loopback endpoint must use HTTPS" }
                require(isLoopback(host)) { "Local loopback endpoint must use localhost, 127.0.0.1, or ::1" }
            }
            ProviderEndpointTrustMode.LOCAL_LAN_HTTPS -> {
                require(uri.scheme == "https") { "Private-LAN endpoints must use HTTPS" }
                require(isPrivateLanIpv4(host)) { "Private-LAN endpoint must use an RFC1918 IPv4 address" }
            }
        }
    }

    private fun isLoopback(host: String): Boolean = host in loopbackHosts

    private fun isPrivateLanIpv4(host: String): Boolean {
        val parts = host.split('.')
        if (parts.size != 4) return false
        val octets = parts.map { it.toIntOrNull() ?: return false }
        if (octets.any { it !in 0..255 }) return false
        return octets[0] == 10 ||
            (octets[0] == 172 && octets[1] in 16..31) ||
            (octets[0] == 192 && octets[1] == 168)
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
