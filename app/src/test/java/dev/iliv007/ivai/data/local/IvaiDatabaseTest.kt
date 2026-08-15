package dev.iliv007.ivai.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.iliv007.ivai.ui.model.ChatMessage
import dev.iliv007.ivai.ui.model.MessageContentType
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
        val repository = LocalWorkspaceRepository(
            database.projectDao(),
            database.threadDao(),
            database.messageDao()
        )
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
