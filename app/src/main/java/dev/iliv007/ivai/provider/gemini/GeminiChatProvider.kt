package dev.iliv007.ivai.provider.gemini

import dev.iliv007.ivai.provider.CredentialReference
import dev.iliv007.ivai.provider.NormalizedProviderError
import dev.iliv007.ivai.provider.ProviderCapability
import dev.iliv007.ivai.provider.ProviderChatRequest
import dev.iliv007.ivai.provider.ProviderConnectionValidation
import dev.iliv007.ivai.provider.ProviderErrorKind
import dev.iliv007.ivai.provider.ProviderId
import dev.iliv007.ivai.provider.ProviderModelDescriptor
import dev.iliv007.ivai.provider.ProviderStreamEvent
import dev.iliv007.ivai.provider.ChatProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GeminiChatProvider(
    private val networkGate: GeminiNetworkGate
) : ChatProvider {
    override val providerId: ProviderId = ID

    override suspend fun validateConnection(credentialReference: CredentialReference): ProviderConnectionValidation =
        networkGate.validateStoredCredential(credentialReference)

    /**
     * The Interactions API vertical slice deliberately supports manual model IDs. Dynamic model
     * discovery is deferred because the API contract and availability can change independently of
     * a user's selected model.
     */
    override suspend fun discoverModels(credentialReference: CredentialReference): List<ProviderModelDescriptor> =
        emptyList()

    override fun streamChat(request: ProviderChatRequest): Flow<ProviderStreamEvent> = flow {
        val unsupported = request.requiredCapabilities - SUPPORTED_CAPABILITIES
        if (unsupported.isNotEmpty()) {
            emit(
                ProviderStreamEvent.Failed(
                    NormalizedProviderError(
                        kind = ProviderErrorKind.UNSUPPORTED_CAPABILITY,
                        safeMessage = "Gemini vertical slice does not support: ${unsupported.joinToString()}"
                    )
                )
            )
            return@flow
        }
        networkGate.stream(request).collect(::emit)
    }

    companion object {
        val ID = ProviderId("gemini")
        private val SUPPORTED_CAPABILITIES = setOf(
            ProviderCapability.TEXT,
            ProviderCapability.STREAMING
        )
    }
}
