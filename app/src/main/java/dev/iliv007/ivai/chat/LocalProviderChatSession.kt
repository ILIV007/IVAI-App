package dev.iliv007.ivai.chat

import dev.iliv007.ivai.data.local.LocalWorkspaceRepository
import dev.iliv007.ivai.data.local.toEntity
import dev.iliv007.ivai.provider.ChatProvider
import dev.iliv007.ivai.provider.CredentialReference
import dev.iliv007.ivai.provider.ProviderCapability
import dev.iliv007.ivai.provider.ProviderChatRequest
import dev.iliv007.ivai.provider.ProviderMessage
import dev.iliv007.ivai.provider.ProviderMessageRole
import dev.iliv007.ivai.provider.ProviderStreamEvent
import dev.iliv007.ivai.ui.model.ChatMessage
import dev.iliv007.ivai.ui.model.MessageSender
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Foreground-only, provider-neutral chat use case. The selected adapter, credential reference, and
 * model are explicit input from the local Provider Registry; streamed partial content never lands
 * in persistence until the adapter reports completion.
 */
class LocalProviderChatSession(
    private val workspace: LocalWorkspaceRepository,
    private val nowEpochMs: () -> Long = System::currentTimeMillis
) {
    fun send(
        provider: ChatProvider,
        threadId: String,
        credentialReference: CredentialReference,
        modelId: String,
        history: List<ChatMessage>,
        prompt: String,
        attemptId: String
    ): Flow<ProviderStreamEvent> = flow {
        val normalizedPrompt = prompt.trim()
        require(normalizedPrompt.isNotEmpty()) { "Prompt must not be blank" }
        val createdAt = nowEpochMs()
        val userMessage = ChatMessage(
            id = "msg-$attemptId-user",
            sender = MessageSender.USER,
            text = normalizedPrompt,
            timestamp = "Now"
        )
        workspace.appendMessage(userMessage.toEntity(threadId, createdAt))

        val providerMessages = (history + userMessage).map {
            ProviderMessage(
                role = if (it.sender == MessageSender.ASSISTANT) ProviderMessageRole.ASSISTANT else ProviderMessageRole.USER,
                content = it.text
            )
        }
        val response = StringBuilder()
        var terminalEventReceived = false
        provider.streamChat(
            ProviderChatRequest(
                credentialReference = credentialReference,
                modelId = modelId,
                messages = providerMessages,
                requiredCapabilities = setOf(ProviderCapability.TEXT, ProviderCapability.STREAMING),
                attemptId = attemptId
            )
        ).collect { event ->
            when (event) {
                is ProviderStreamEvent.Delta -> response.append(event.text)
                is ProviderStreamEvent.Completed -> {
                    if (response.isNotBlank()) {
                        workspace.appendMessage(
                            ChatMessage(
                                id = "msg-$attemptId-assistant",
                                sender = MessageSender.ASSISTANT,
                                text = response.toString(),
                                timestamp = "Now",
                                modelBadge = modelId,
                                latencyMs = nowEpochMs() - createdAt
                            ).toEntity(threadId, nowEpochMs())
                        )
                    }
                    terminalEventReceived = true
                }
                is ProviderStreamEvent.Failed,
                ProviderStreamEvent.Cancelled -> terminalEventReceived = true
                else -> Unit
            }
            emit(event)
        }
        check(terminalEventReceived) { "Provider stream ended without a terminal event" }
    }
}
