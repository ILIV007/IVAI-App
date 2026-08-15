package dev.iliv007.ivai.provider

import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeChatProviderContractTest {

    @Test
    fun `fake provider exposes normalized streaming lifecycle`() = runBlocking {
        val provider = FakeChatProvider(deltas = listOf("سلام ", "IVAI"))
        val request = requestFor(provider)

        val events = buildList {
            provider.streamChat(request).collect(::add)
        }

        assertEquals(ProviderStreamEvent.Started(request.attemptId), events.first())
        assertEquals(
            listOf("سلام ", "IVAI"),
            events.filterIsInstance<ProviderStreamEvent.Delta>().map { it.text }
        )
        assertEquals(ProviderUsage(inputTokens = 4, outputTokens = 2), events.filterIsInstance<ProviderStreamEvent.Usage>().single().usage)
        assertEquals(ProviderStreamEvent.Completed("fake-message-1"), events.last())
    }

    @Test
    fun `fake provider reports normalized failure without provider payload`() = runBlocking {
        val error = NormalizedProviderError(
            kind = ProviderErrorKind.RATE_LIMIT,
            safeMessage = "The provider is temporarily rate limited.",
            httpStatus = 429,
            retryable = true
        )
        val provider = FakeChatProvider(failure = error)

        val events = buildList { provider.streamChat(requestFor(provider)).collect(::add) }

        assertEquals(ProviderStreamEvent.Failed(error), events.last())
        assertFalse(events.any { it.toString().contains("Authorization", ignoreCase = true) })
    }

    @Test
    fun `collector cancellation is observed by provider flow`() = runBlocking {
        val provider = FakeChatProvider(deltas = listOf("first", "second"), suspendAfterFirstDelta = true)
        val job = launch {
            provider.streamChat(requestFor(provider)).collect { event ->
                if (event is ProviderStreamEvent.Delta) cancel()
            }
        }

        job.join()
        assertTrue(provider.cancellationObserved)
    }

    @Test
    fun `connection and model discovery remain credential-reference based`() = runBlocking {
        val provider = FakeChatProvider()
        val reference = CredentialReference("gemini")

        val validation = provider.validateConnection(reference)
        val models = provider.discoverModels(reference)

        assertTrue(validation.isUsable)
        assertEquals(provider.providerId, validation.providerId)
        assertTrue(models.single().capabilities.contains(ProviderCapability.STREAMING))
    }

    private fun requestFor(provider: ChatProvider) = ProviderChatRequest(
        credentialReference = CredentialReference("gemini"),
        modelId = "manual-model-id",
        messages = listOf(ProviderMessage(ProviderMessageRole.USER, "سلام")),
        requiredCapabilities = setOf(ProviderCapability.TEXT, ProviderCapability.STREAMING),
        attemptId = "attempt-test-1"
    )

    private class FakeChatProvider(
        private val deltas: List<String> = emptyList(),
        private val failure: NormalizedProviderError? = null,
        private val suspendAfterFirstDelta: Boolean = false
    ) : ChatProvider {
        override val providerId = ProviderId("fake")
        var cancellationObserved = false
            private set

        override suspend fun validateConnection(credentialReference: CredentialReference) =
            ProviderConnectionValidation(providerId = providerId, isUsable = true)

        override suspend fun discoverModels(credentialReference: CredentialReference) = listOf(
            ProviderModelDescriptor(
                id = "fake-model",
                displayName = "Fake model",
                capabilities = setOf(ProviderCapability.TEXT, ProviderCapability.STREAMING)
            )
        )

        override fun streamChat(request: ProviderChatRequest): Flow<ProviderStreamEvent> = flow {
            emit(ProviderStreamEvent.Started(request.attemptId))
            try {
                if (failure != null) {
                    emit(ProviderStreamEvent.Failed(failure))
                    return@flow
                }
                deltas.forEachIndexed { index, delta ->
                    emit(ProviderStreamEvent.Delta(delta))
                    if (suspendAfterFirstDelta && index == 0) {
                        kotlinx.coroutines.awaitCancellation()
                    }
                }
                emit(ProviderStreamEvent.Usage(ProviderUsage(inputTokens = 4, outputTokens = deltas.size)))
                emit(ProviderStreamEvent.Completed("fake-message-1"))
            } finally {
                cancellationObserved = true
            }
        }
    }
}
