package dev.iliv007.ivai.router

import dev.iliv007.ivai.data.local.LocalWorkspaceRepository
import dev.iliv007.ivai.data.local.RouterAttemptEntity
import dev.iliv007.ivai.data.local.RouterAttemptEntryEntity
import dev.iliv007.ivai.data.local.RouterComboEntryEntity
import dev.iliv007.ivai.data.local.toEntity
import dev.iliv007.ivai.provider.ChatProvider
import dev.iliv007.ivai.provider.CredentialReference
import dev.iliv007.ivai.provider.ProviderAccountAuthMode
import dev.iliv007.ivai.provider.ProviderEndpointTrustMode
import dev.iliv007.ivai.provider.ProviderCapability
import dev.iliv007.ivai.provider.ProviderChatRequest
import dev.iliv007.ivai.provider.ProviderErrorKind
import dev.iliv007.ivai.provider.ProviderMessage
import dev.iliv007.ivai.provider.ProviderMessageRole
import dev.iliv007.ivai.provider.ProviderStreamEvent
import dev.iliv007.ivai.provider.ProviderKind
import dev.iliv007.ivai.ui.model.ChatMessage
import dev.iliv007.ivai.ui.model.MessageSender
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Foreground-only sequential fallback for text streaming. It deliberately falls back only before a
 * provider emitted user-visible content; this avoids silently duplicating a partially completed
 * request. Attempt records contain stable IDs and normalized error metadata only.
 */
class RouterChatSession(
    private val workspace: LocalWorkspaceRepository,
    private val router: SequentialRouter,
    private val providerResolver: (ProviderKind, String?, ProviderEndpointTrustMode) -> ChatProvider,
    private val nowEpochMs: () -> Long = System::currentTimeMillis
) {
    fun send(
        threadId: String,
        target: ExecutionTarget,
        comboEntries: List<RouterComboEntryEntity>,
        catalog: RouterCatalog,
        history: List<ChatMessage>,
        prompt: String,
        attemptId: String
    ): Flow<ProviderStreamEvent> = flow {
        val normalizedPrompt = prompt.trim()
        require(normalizedPrompt.isNotEmpty()) { "Prompt must not be blank" }
        val resolution = router.resolve(
            target = target,
            comboEntries = comboEntries,
            catalog = catalog,
            requiredCapabilities = setOf(ProviderCapability.TEXT, ProviderCapability.STREAMING)
        )
        if (resolution.candidates.isEmpty()) {
            emit(ProviderStreamEvent.Failed(routerUnavailableError()))
            return@flow
        }

        val startedAt = nowEpochMs()
        workspace.appendMessage(
            ChatMessage("msg-$attemptId-user", MessageSender.USER, normalizedPrompt, "Now")
                .toEntity(threadId, startedAt)
        )
        workspace.saveRouterAttempt(
            RouterAttemptEntity(
                id = attemptId,
                threadId = threadId,
                targetKind = target.kind().name,
                targetId = target.stableId,
                outcome = RouterAttemptOutcome.RUNNING.name,
                startedAtEpochMs = startedAt,
                completedAtEpochMs = null,
                safeErrorKind = null,
                safeErrorMessage = null
            )
        )

        val providerMessages = (history + ChatMessage("msg-$attemptId-user", MessageSender.USER, normalizedPrompt, "Now"))
            .map { ProviderMessage(if (it.sender == MessageSender.ASSISTANT) ProviderMessageRole.ASSISTANT else ProviderMessageRole.USER, it.text) }
        var userVisibleStarted = false
        var lastError: ProviderStreamEvent.Failed? = null

        for (candidate in resolution.candidates) {
            val candidateAttemptId = "$attemptId-${candidate.position}"
            val candidateStartedAt = nowEpochMs()
            val connection = catalog.connections.firstOrNull {
                it.id == candidate.connectionId && it.isEnabled
            }
            val account = catalog.accounts.firstOrNull {
                it.id == candidate.accountId && it.connectionId == candidate.connectionId && it.isEnabled
            }
            val model = catalog.models.firstOrNull {
                it.id == candidate.modelId && it.connectionId == candidate.connectionId && it.isSelectable
            }
            if (connection == null || account == null || model == null) {
                val failure = ProviderStreamEvent.Failed(routerConfigurationChangedError())
                finishEntry(candidateAttemptId, candidate, RouterAttemptOutcome.FAILED, candidateStartedAt, failure)
                finishAttempt(attemptId, threadId, target, RouterAttemptOutcome.FAILED, startedAt, failure)
                emit(failure)
                return@flow
            }
            workspace.saveRouterAttemptEntry(
                RouterAttemptEntryEntity(
                    id = candidateAttemptId,
                    attemptId = attemptId,
                    position = candidate.position,
                    connectionId = candidate.connectionId,
                    accountId = candidate.accountId,
                    modelId = candidate.modelId,
                    outcome = RouterAttemptOutcome.RUNNING.name,
                    startedAtEpochMs = candidateStartedAt,
                    completedAtEpochMs = null,
                    safeErrorKind = null,
                    safeErrorMessage = null
                )
            )

            val response = StringBuilder()
            var completed = false
            var candidateError: ProviderStreamEvent.Failed? = null
            try {
                val authMode = ProviderAccountAuthMode.valueOf(account.authMode)
                val provider = providerResolver(
                    ProviderKind.valueOf(connection.providerKind),
                    connection.baseUrl,
                    ProviderEndpointTrustMode.valueOf(connection.endpointTrustMode)
                )
                provider.streamChat(
                    ProviderChatRequest(
                        credentialReference = account.credentialReference.takeIf { authMode == ProviderAccountAuthMode.API_KEY }
                            ?.let(::CredentialReference),
                        authMode = authMode,
                        modelId = candidate.providerModelId,
                        messages = providerMessages,
                        requiredCapabilities = setOf(ProviderCapability.TEXT, ProviderCapability.STREAMING),
                        attemptId = candidateAttemptId
                    )
                ).collect { event ->
                    when (event) {
                        is ProviderStreamEvent.Started -> if (!userVisibleStarted) {
                            userVisibleStarted = true
                            emit(ProviderStreamEvent.Started(attemptId))
                        }
                        is ProviderStreamEvent.Delta -> {
                            response.append(event.text)
                            emit(event)
                        }
                        is ProviderStreamEvent.Completed -> completed = true
                        is ProviderStreamEvent.Failed -> candidateError = event
                        ProviderStreamEvent.Cancelled -> throw CancellationException("Provider stream cancelled")
                        is ProviderStreamEvent.Usage -> emit(event)
                    }
                }
            } catch (cancelled: CancellationException) {
                withContext(NonCancellable) {
                    finishEntry(candidateAttemptId, candidate, RouterAttemptOutcome.CANCELLED, candidateStartedAt, null)
                    finishAttempt(attemptId, threadId, target, RouterAttemptOutcome.CANCELLED, startedAt, null)
                }
                emit(ProviderStreamEvent.Cancelled)
                return@flow
            } catch (_: Exception) {
                candidateError = ProviderStreamEvent.Failed(unexpectedProviderError())
            }

            if (completed) {
                if (response.isNotBlank()) {
                    workspace.appendMessage(
                        ChatMessage(
                            id = "msg-$attemptId-assistant",
                            sender = MessageSender.ASSISTANT,
                            text = response.toString(),
                            timestamp = "Now",
                            modelBadge = candidate.providerModelId,
                            latencyMs = nowEpochMs() - startedAt
                        ).toEntity(threadId, nowEpochMs())
                    )
                }
                finishEntry(candidateAttemptId, candidate, RouterAttemptOutcome.SUCCEEDED, candidateStartedAt, null)
                finishAttempt(attemptId, threadId, target, RouterAttemptOutcome.SUCCEEDED, startedAt, null)
                emit(ProviderStreamEvent.Completed(attemptId))
                return@flow
            }

            val failure = candidateError ?: ProviderStreamEvent.Failed(routerIncompleteStreamError())
            lastError = failure
            if (response.isNotEmpty()) {
                val completedAt = maxOf(nowEpochMs(), startedAt + 1)
                workspace.appendMessage(
                    ChatMessage(
                        id = "msg-$attemptId-assistant",
                        sender = MessageSender.ASSISTANT,
                        text = response.toString(),
                        timestamp = "Now",
                        modelBadge = candidate.providerModelId,
                        latencyMs = completedAt - startedAt,
                        isIncomplete = true
                    ).toEntity(threadId, completedAt)
                )
            }
            finishEntry(candidateAttemptId, candidate, RouterAttemptOutcome.FAILED, candidateStartedAt, failure)
            val mayFallback = response.isEmpty() && isSafeFallback(failure)
            if (!mayFallback) {
                finishAttempt(attemptId, threadId, target, RouterAttemptOutcome.FAILED, startedAt, failure)
                emit(failure)
                return@flow
            }
        }

        val terminal = lastError ?: ProviderStreamEvent.Failed(routerUnavailableError())
        finishAttempt(attemptId, threadId, target, RouterAttemptOutcome.FAILED, startedAt, terminal)
        emit(terminal)
    }

    private suspend fun finishEntry(
        entryId: String,
        candidate: RouterCandidate,
        outcome: RouterAttemptOutcome,
        startedAt: Long,
        failure: ProviderStreamEvent.Failed?
    ) {
        workspace.saveRouterAttemptEntry(
            RouterAttemptEntryEntity(
                id = entryId,
                attemptId = entryId.substringBeforeLast('-'),
                position = candidate.position,
                connectionId = candidate.connectionId,
                accountId = candidate.accountId,
                modelId = candidate.modelId,
                outcome = outcome.name,
                startedAtEpochMs = startedAt,
                completedAtEpochMs = nowEpochMs(),
                safeErrorKind = failure?.error?.kind?.name,
                safeErrorMessage = failure?.error?.safeMessage
            )
        )
    }

    private suspend fun finishAttempt(
        attemptId: String,
        threadId: String,
        target: ExecutionTarget,
        outcome: RouterAttemptOutcome,
        startedAt: Long,
        failure: ProviderStreamEvent.Failed?
    ) {
        workspace.saveRouterAttempt(
            RouterAttemptEntity(
                id = attemptId,
                threadId = threadId,
                targetKind = target.kind().name,
                targetId = target.stableId,
                outcome = outcome.name,
                startedAtEpochMs = startedAt,
                completedAtEpochMs = nowEpochMs(),
                safeErrorKind = failure?.error?.kind?.name,
                safeErrorMessage = failure?.error?.safeMessage
            )
        )
    }

    private fun isSafeFallback(failure: ProviderStreamEvent.Failed): Boolean =
        failure.error.retryable || failure.error.kind in setOf(
            ProviderErrorKind.AUTHENTICATION,
            ProviderErrorKind.RATE_LIMIT,
            ProviderErrorKind.NETWORK_UNAVAILABLE,
            ProviderErrorKind.TIMEOUT
        )

    private fun unexpectedProviderError() = dev.iliv007.ivai.provider.NormalizedProviderError(
        kind = ProviderErrorKind.UNKNOWN,
        safeMessage = "Provider request failed before a complete response.",
        retryable = true
    )

    private fun routerUnavailableError() = dev.iliv007.ivai.provider.NormalizedProviderError(
        kind = ProviderErrorKind.UNSUPPORTED_CAPABILITY,
        safeMessage = "No enabled provider target can satisfy this request."
    )

    private fun routerConfigurationChangedError() = dev.iliv007.ivai.provider.NormalizedProviderError(
        kind = ProviderErrorKind.INVALID_REQUEST,
        safeMessage = "Selected provider configuration changed before the request could start."
    )

    private fun routerIncompleteStreamError() = dev.iliv007.ivai.provider.NormalizedProviderError(
        kind = ProviderErrorKind.MALFORMED_RESPONSE,
        safeMessage = "Provider stream ended without a completion event."
    )
}
