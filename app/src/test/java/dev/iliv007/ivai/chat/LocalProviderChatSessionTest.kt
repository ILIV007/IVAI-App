package dev.iliv007.ivai.chat

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.iliv007.ivai.data.local.ChatThreadEntity
import dev.iliv007.ivai.data.local.IvaiDatabase
import dev.iliv007.ivai.data.local.LocalWorkspaceRepository
import dev.iliv007.ivai.provider.ChatProvider
import dev.iliv007.ivai.provider.CredentialReference
import dev.iliv007.ivai.provider.NormalizedProviderError
import dev.iliv007.ivai.provider.ProviderChatRequest
import dev.iliv007.ivai.provider.ProviderConnectionValidation
import dev.iliv007.ivai.provider.ProviderErrorKind
import dev.iliv007.ivai.provider.ProviderId
import dev.iliv007.ivai.provider.ProviderModelDescriptor
import dev.iliv007.ivai.provider.ProviderStreamEvent
import dev.iliv007.ivai.ui.model.MessageSender
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LocalProviderChatSessionTest {
    private lateinit var database: IvaiDatabase
    private lateinit var repository: LocalWorkspaceRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            IvaiDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = LocalWorkspaceRepository(database)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `provider failed event remains visible without a generic stream exception`() = runBlocking {
        repository.saveThread(
            ChatThreadEntity(
                id = "thread-1",
                title = "Local chat",
                snippet = "",
                updatedAtEpochMs = 1L,
                modelOrCombo = "User-selected provider",
                projectId = null
            )
        )
        val error = NormalizedProviderError(
            kind = ProviderErrorKind.AUTHENTICATION,
            safeMessage = "The selected provider credential is unavailable."
        )
        val events = LocalProviderChatSession(repository, nowEpochMs = { 10L }).send(
            provider = FailureProvider(error),
            threadId = "thread-1",
            credentialReference = CredentialReference("provider-account"),
            modelId = "user-model",
            history = emptyList(),
            prompt = "Explain the local workspace.",
            attemptId = "attempt-1"
        ).toList()

        assertEquals(
            listOf(ProviderStreamEvent.Started("attempt-1"), ProviderStreamEvent.Failed(error)),
            events
        )
        val persisted = database.messageDao().listForThread("thread-1")
        assertEquals(listOf(MessageSender.USER.name), persisted.map { it.sender })
        assertFalse(persisted.any { it.sender == MessageSender.ASSISTANT.name })
    }

    private class FailureProvider(
        private val error: NormalizedProviderError
    ) : ChatProvider {
        override val providerId = ProviderId("test-provider")

        override suspend fun validateConnection(credentialReference: CredentialReference) =
            ProviderConnectionValidation(providerId, isUsable = true)

        override suspend fun discoverModels(credentialReference: CredentialReference): List<ProviderModelDescriptor> = emptyList()

        override fun streamChat(request: ProviderChatRequest): Flow<ProviderStreamEvent> = flowOf(
            ProviderStreamEvent.Started(request.attemptId),
            ProviderStreamEvent.Failed(error)
        )
    }
}
