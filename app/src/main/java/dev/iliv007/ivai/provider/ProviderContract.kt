package dev.iliv007.ivai.provider

import kotlinx.coroutines.flow.Flow

@JvmInline
value class ProviderId(val value: String) {
    init {
        require(value.matches(Regex("[a-z][a-z0-9._-]{0,63}"))) { "Invalid provider ID" }
    }
}

@JvmInline
value class CredentialReference(val value: String) {
    init {
        require(value.matches(Regex("[a-z0-9][a-z0-9._-]{0,63}"))) { "Invalid credential reference" }
    }
}

enum class ProviderCapability {
    TEXT,
    STREAMING,
    TOOL_CALLING,
    VISION,
    AUDIO,
    JSON_MODE,
    REASONING,
    EMBEDDINGS
}

enum class ProviderMessageRole {
    SYSTEM,
    USER,
    ASSISTANT
}

data class ProviderMessage(
    val role: ProviderMessageRole,
    val content: String
) {
    init {
        require(content.isNotBlank()) { "Provider message content must not be blank" }
    }
}

data class ProviderModelDescriptor(
    val id: String,
    val displayName: String,
    val capabilities: Set<ProviderCapability>,
    val isSelectable: Boolean = true
) {
    init {
        require(id.isNotBlank()) { "Model ID must not be blank" }
        require(displayName.isNotBlank()) { "Model display name must not be blank" }
    }
}

data class ProviderChatRequest(
    val credentialReference: CredentialReference?,
    val authMode: ProviderAccountAuthMode = ProviderAccountAuthMode.API_KEY,
    val modelId: String,
    val messages: List<ProviderMessage>,
    val requiredCapabilities: Set<ProviderCapability> = setOf(ProviderCapability.TEXT),
    val attemptId: String
) {
    init {
        require((authMode == ProviderAccountAuthMode.API_KEY) == (credentialReference != null)) {
            "API-key requests require a credential reference; no-auth requests must not carry one"
        }
        require(modelId.isNotBlank()) { "Manual model ID must not be blank" }
        require(messages.isNotEmpty()) { "A provider request needs at least one message" }
        require(attemptId.matches(Regex("[A-Za-z0-9._-]{1,128}"))) { "Invalid opaque attempt ID" }
    }
}

data class ProviderUsage(
    val inputTokens: Int? = null,
    val outputTokens: Int? = null
) {
    init {
        require(inputTokens == null || inputTokens >= 0) { "Input token count must be non-negative" }
        require(outputTokens == null || outputTokens >= 0) { "Output token count must be non-negative" }
    }
}

enum class ProviderErrorKind {
    AUTHENTICATION,
    RATE_LIMIT,
    TIMEOUT,
    NETWORK_UNAVAILABLE,
    INVALID_REQUEST,
    UNSUPPORTED_CAPABILITY,
    MALFORMED_RESPONSE,
    CANCELLED,
    UNKNOWN
}

data class NormalizedProviderError(
    val kind: ProviderErrorKind,
    val safeMessage: String,
    val httpStatus: Int? = null,
    val retryable: Boolean = false
) {
    init {
        require(safeMessage.isNotBlank()) { "A safe error message is required" }
        require(httpStatus == null || httpStatus in 100..599) { "Invalid HTTP status" }
    }
}

sealed interface ProviderStreamEvent {
    data class Started(val attemptId: String) : ProviderStreamEvent
    data class Delta(val text: String) : ProviderStreamEvent {
        init {
            require(text.isNotEmpty()) { "A streaming delta must not be empty" }
        }
    }

    data class Usage(val usage: ProviderUsage) : ProviderStreamEvent
    data class Completed(val messageId: String) : ProviderStreamEvent
    data class Failed(val error: NormalizedProviderError) : ProviderStreamEvent
    data object Cancelled : ProviderStreamEvent
}

data class ProviderConnectionValidation(
    val providerId: ProviderId,
    val isUsable: Boolean,
    val error: NormalizedProviderError? = null
) {
    init {
        require(isUsable || error != null) { "An unusable provider needs a normalized error" }
        require(!isUsable || error == null) { "A usable provider cannot carry an error" }
    }
}

/**
 * Provider adapters receive no raw credential. For API-key requests, the Network Gate resolves the
 * reference from [dev.iliv007.ivai.security.EncryptedSecretVault] immediately before constructing
 * an HTTP call. Explicit no-auth local requests carry no reference and no Authorization header.
 */
interface ChatProvider {
    val providerId: ProviderId

    suspend fun validateConnection(credentialReference: CredentialReference): ProviderConnectionValidation

    suspend fun discoverModels(credentialReference: CredentialReference): List<ProviderModelDescriptor>

    fun streamChat(request: ProviderChatRequest): Flow<ProviderStreamEvent>
}
