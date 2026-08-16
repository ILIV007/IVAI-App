package dev.iliv007.ivai.router

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.iliv007.ivai.data.local.ChatThreadEntity
import dev.iliv007.ivai.data.local.IvaiDatabase
import dev.iliv007.ivai.data.local.LocalWorkspaceRepository
import dev.iliv007.ivai.data.local.ProviderAccountEntity
import dev.iliv007.ivai.data.local.ProviderConnectionEntity
import dev.iliv007.ivai.data.local.ProviderModelEntity
import dev.iliv007.ivai.data.local.RouterComboEntity
import dev.iliv007.ivai.data.local.RouterComboEntryEntity
import dev.iliv007.ivai.provider.ChatProvider
import dev.iliv007.ivai.provider.CredentialReference
import dev.iliv007.ivai.provider.NormalizedProviderError
import dev.iliv007.ivai.provider.ProviderChatRequest
import dev.iliv007.ivai.provider.ProviderConnectionValidation
import dev.iliv007.ivai.provider.ProviderErrorKind
import dev.iliv007.ivai.provider.ProviderId
import dev.iliv007.ivai.provider.ProviderModelDescriptor
import dev.iliv007.ivai.provider.ProviderStreamEvent
import dev.iliv007.ivai.provider.ProviderKind
import dev.iliv007.ivai.ui.model.ChatMessage
import dev.iliv007.ivai.ui.model.MessageSender
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RouterChatSessionTest {
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
    fun `retryable first candidate failure falls back once and records ordered safe trace`() = runBlocking {
        repository.saveThread(ChatThreadEntity("thread", "Local router", "", 1L, "User Combo", null))
        repository.saveProviderConnection(ProviderConnectionEntity("gemini", "GEMINI", "First user provider", null, true, 1L, 1L))
        repository.saveProviderConnection(ProviderConnectionEntity("openrouter", "OPENROUTER", "Second user provider", null, true, 1L, 1L))
        repository.saveProviderAccount(ProviderAccountEntity("gemini-account", "gemini", "BYOK first", "credential.gemini", true, 1L, 1L))
        repository.saveProviderAccount(ProviderAccountEntity("openrouter-account", "openrouter", "BYOK second", "credential.openrouter", true, 1L, 1L))
        repository.saveProviderModel(ProviderModelEntity("gemini-model", "gemini", "user-first-model", "First model", "TEXT,STREAMING", true, true, 1L))
        repository.saveProviderModel(ProviderModelEntity("openrouter-model", "openrouter", "user-second-model", "Second model", "TEXT,STREAMING", true, true, 1L))
        repository.saveRouterCombo(
            RouterComboEntity("combo", "User fallback", "", true, 1L, 1L),
            listOf(
                RouterComboEntryEntity("first", "combo", 0, "gemini", "gemini-account", "gemini-model", true),
                RouterComboEntryEntity("second", "combo", 1, "openrouter", "openrouter-account", "openrouter-model", true)
            )
        )

        val catalog = RouterCatalog(
            connections = repository.currentProviderRegistry().connections,
            accounts = repository.currentProviderRegistry().accounts,
            models = repository.currentProviderRegistry().models,
            credentialPresent = setOf("credential.gemini", "credential.openrouter")
        )
        val session = RouterChatSession(
            workspace = repository,
            router = SequentialRouter(),
            providerResolver = { kind, _, _ -> if (kind == ProviderKind.GEMINI) retryableFailureProvider else succeedingProvider },
            nowEpochMs = { 100L }
        )

        val events = session.send(
            threadId = "thread",
            target = ExecutionTarget.Combo("combo"),
            comboEntries = repository.listRouterComboEntries("combo"),
            catalog = catalog,
            history = emptyList(),
            prompt = "hello",
            attemptId = "router-1"
        ).toList()

        assertEquals(1, events.filterIsInstance<ProviderStreamEvent.Started>().size)
        assertEquals(listOf("fallback answer"), events.filterIsInstance<ProviderStreamEvent.Delta>().map { it.text })
        assertTrue(events.last() is ProviderStreamEvent.Completed)

        val parent = repository.observeRouterAttempts("thread").first().single()
        assertEquals(RouterAttemptOutcome.SUCCEEDED.name, parent.outcome)
        assertEquals("thread", parent.threadId)
        val entryTrace = repository.observeRouterAttemptEntries("router-1").first()
        assertEquals(listOf(RouterAttemptOutcome.FAILED.name, RouterAttemptOutcome.SUCCEEDED.name), entryTrace.map { it.outcome })
        assertEquals(ProviderErrorKind.RATE_LIMIT.name, entryTrace.first().safeErrorKind)
        assertTrue(entryTrace.all { it.safeErrorMessage?.contains("secret", ignoreCase = true) != true })
    }

    private val retryableFailureProvider = FakeProvider(ProviderId("fake-gemini")) { request ->
        emit(ProviderStreamEvent.Failed(NormalizedProviderError(ProviderErrorKind.RATE_LIMIT, "Rate limited", retryable = true)))
    }

    private val succeedingProvider = FakeProvider(ProviderId("fake-openrouter")) { request ->
        emit(ProviderStreamEvent.Started(request.attemptId))
        emit(ProviderStreamEvent.Delta("fallback answer"))
        emit(ProviderStreamEvent.Completed(request.attemptId))
    }
}

private class FakeProvider(
    override val providerId: ProviderId,
    private val behavior: suspend kotlinx.coroutines.flow.FlowCollector<ProviderStreamEvent>.(ProviderChatRequest) -> Unit
) : ChatProvider {
    override suspend fun validateConnection(credentialReference: CredentialReference) =
        ProviderConnectionValidation(providerId, isUsable = true)

    override suspend fun discoverModels(credentialReference: CredentialReference): List<ProviderModelDescriptor> = emptyList()

    override fun streamChat(request: ProviderChatRequest): Flow<ProviderStreamEvent> = flow {
        behavior(request)
    }
}
