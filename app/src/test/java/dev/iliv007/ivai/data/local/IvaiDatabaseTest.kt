package dev.iliv007.ivai.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.iliv007.ivai.ui.model.ChatMessage
import dev.iliv007.ivai.ui.model.MessageContentType
import dev.iliv007.ivai.router.ExecutionTarget
import dev.iliv007.ivai.ui.model.MessageSender
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class IvaiDatabaseTest {

    private lateinit var database: IvaiDatabase

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            IvaiDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun `thread deletion cascades messages and project deletion unassigns threads`() = runBlocking {
        val project = WorkspaceProjectEntity(
            id = "project-1",
            name = "Workspace",
            description = "Local-only",
            fileCount = 0,
            updatedAtEpochMs = 100L
        )
        val thread = ChatThreadEntity(
            id = "thread-1",
            title = "Conversation",
            snippet = "Initial",
            updatedAtEpochMs = 200L,
            modelOrCombo = "Local mock",
            projectId = project.id
        )
        val message = ChatMessageEntity(
            id = "message-1",
            threadId = thread.id,
            sender = MessageSender.USER.name,
            text = "سلام README.md",
            createdAtEpochMs = 300L,
            contentType = MessageContentType.MIXED_BIDI.name,
            codeSnippet = "val path = \"README.md\"",
            modelBadge = "local",
            latencyMs = 12L
        )

        database.projectDao().upsert(project)
        database.threadDao().upsert(thread)
        database.messageDao().insert(message)
        assertEquals(1, database.messageDao().listForThread(thread.id).size)

        database.projectDao().delete(project)
        assertNull(database.threadDao().findById(thread.id)?.projectId)

        database.threadDao().delete(thread.copy(projectId = null))
        assertTrue(database.messageDao().listForThread(thread.id).isEmpty())
    }

    @Test
    fun `repository emits deterministic workspace snapshot ordered by recency`() = runBlocking {
        val repository = LocalWorkspaceRepository(database)
        repository.saveProject(
            WorkspaceProjectEntity("project-old", "Old", "", 0, updatedAtEpochMs = 10L)
        )
        repository.saveProject(
            WorkspaceProjectEntity("project-new", "New", "", 0, updatedAtEpochMs = 20L)
        )
        repository.saveThread(
            ChatThreadEntity("thread-old", "Old", "", 30L, "mock", projectId = "project-old")
        )
        repository.saveThread(
            ChatThreadEntity("thread-new", "New", "", 40L, "mock", projectId = "project-new")
        )

        val snapshot = repository.observeWorkspace().first()

        assertEquals(listOf("project-new", "project-old"), snapshot.projects.map { it.id })
        assertEquals(listOf("thread-new", "thread-old"), snapshot.threads.map { it.id })
    }

    @Test
    fun `provider connection retains multiple accounts and models then cascade deletes them while retaining only credential references`() = runBlocking {
        val repository = LocalWorkspaceRepository(database)
        repository.saveProviderConnection(
            ProviderConnectionEntity(
                id = "custom-primary",
                providerKind = "CUSTOM_OPENAI_COMPATIBLE",
                displayName = "Private endpoint",
                baseUrl = "https://api.example.test/v1",
                isEnabled = true,
                createdAtEpochMs = 10L,
                updatedAtEpochMs = 20L
            )
        )
        repository.saveProviderAccount(
            ProviderAccountEntity(
                id = "custom-account",
                connectionId = "custom-primary",
                displayName = "Primary key",
                credentialReference = "provider.custom-primary.primary",
                isEnabled = true,
                createdAtEpochMs = 10L,
                updatedAtEpochMs = 20L
            )
        )
        repository.saveProviderAccount(
            ProviderAccountEntity(
                id = "custom-account-work",
                connectionId = "custom-primary",
                displayName = "Work key",
                credentialReference = "provider.custom-primary.work",
                isEnabled = true,
                createdAtEpochMs = 11L,
                updatedAtEpochMs = 21L
            )
        )
        repository.saveProviderModel(
            ProviderModelEntity(
                id = "custom-model",
                connectionId = "custom-primary",
                providerModelId = "gpt-example",
                displayName = "Example model",
                capabilitiesCsv = "TEXT,STREAMING",
                isManual = true,
                isSelectable = true,
                updatedAtEpochMs = 20L
            )
        )

        repository.saveProviderModel(
            ProviderModelEntity(
                id = "custom-model-fast",
                connectionId = "custom-primary",
                providerModelId = "gpt-example-fast",
                displayName = "Example fast model",
                capabilitiesCsv = "TEXT,STREAMING",
                isManual = true,
                isSelectable = true,
                updatedAtEpochMs = 21L
            )
        )

        val snapshot = repository.observeProviderRegistry().first()
        assertEquals(listOf("custom-primary"), snapshot.connections.map { it.id })
        assertEquals(
            listOf("provider.custom-primary.work", "provider.custom-primary.primary"),
            snapshot.accounts.map { it.credentialReference }
        )
        assertEquals(listOf("gpt-example-fast", "gpt-example"), snapshot.models.map { it.providerModelId })

        repository.deleteProviderConnection("custom-primary")
        assertTrue(database.providerAccountDao().listAll().isEmpty())
        assertTrue(database.providerModelDao().listAll().isEmpty())
    }

    @Test
    fun `router combo and explicit direct target use only user managed provider references`() = runBlocking {
        val repository = LocalWorkspaceRepository(database)
        val thread = ChatThreadEntity("router-thread", "Router", "", 1L, "Unset", null)
        repository.saveThread(thread)
        repository.saveProviderConnection(ProviderConnectionEntity("connection", "OPENROUTER", "OpenRouter", null, true, 1L, 1L))
        repository.saveProviderAccount(ProviderAccountEntity("account", "connection", "BYOK", "provider.openrouter.test", true, 1L, 1L))
        repository.saveProviderModel(ProviderModelEntity("model", "connection", "openai/gpt-test", "GPT Test", "TEXT,STREAMING", true, true, 1L))

        repository.saveRouterCombo(
            RouterComboEntity("combo", "My BYOK Combo", "First candidate", true, 2L, 2L),
            listOf(RouterComboEntryEntity("entry", "combo", 0, "connection", "account", "model", true))
        )
        repository.selectThreadExecutionTarget(
            thread.id,
            ExecutionTarget.DirectModel(connectionId = "connection", accountId = "account", modelId = "model")
        )

        assertEquals(listOf("combo"), repository.observeRouter().first().combos.map { it.id })
        assertEquals(listOf("model"), repository.observeComboEntries("combo").first().map { it.modelId })
        val target = repository.observeThreadExecutionTarget(thread.id).first()
        assertEquals("DIRECT_MODEL", target?.targetKind)
        assertEquals("account", target?.accountId)
    }

    @Test
    fun `message mapping preserves BiDi code and model metadata`() {
        val source = ChatMessage(
            id = "message-rtl",
            sender = MessageSender.ASSISTANT,
            text = "این مسیر را بررسی کن: README.md",
            timestamp = "10:16 AM",
            type = MessageContentType.MIXED_BIDI,
            codeSnippet = "fun inspect(path: String) = path",
            modelBadge = "gemini-flash",
            latencyMs = 247L
        )

        val restored = source.toEntity(threadId = "thread-rtl", createdAtEpochMs = 99L)
            .toDomainMessage(timestamp = source.timestamp)

        assertEquals(source, restored)
        assertFalse(restored.text.isBlank())
    }
}
