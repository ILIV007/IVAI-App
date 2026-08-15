package dev.iliv007.ivai.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.iliv007.ivai.ui.model.MessageContentType
import dev.iliv007.ivai.ui.model.MessageSender
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LocalWorkspaceArchiveTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var database: IvaiDatabase
    private lateinit var repository: LocalWorkspaceRepository
    private lateinit var workspace: ProjectWorkspace
    private lateinit var archive: LocalWorkspaceArchive

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            IvaiDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = LocalWorkspaceRepository(database)
        workspace = ProjectWorkspace(temporaryFolder.newFolder("workspace"))
        archive = LocalWorkspaceArchive(repository, workspace) { 123L }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `archive round trip restores local records and app private files`() = runBlocking {
        seedWorkspace("original")
        val export = File(temporaryFolder.root, "workspace.ivai")

        archive.exportTo(export)
        repository.saveProject(WorkspaceProjectEntity("later", "Later", "", 0, 1000L))
        workspace.writeText("project-1", "notes/readme.txt", "mutated")

        archive.importFrom(export)

        val restored = repository.snapshotForArchive()
        assertEquals(listOf("project-1"), restored.projects.map { it.id })
        assertEquals(listOf("thread-1"), restored.threads.map { it.id })
        assertEquals(listOf("message-1"), restored.messages.map { it.id })
        assertEquals("original", workspace.readText("project-1", "notes/readme.txt"))
        assertFalse(restored.projects.any { it.id == "later" })
        assertTrue(export.isFile)
    }

    @Test
    fun `checksum failure leaves current local workspace untouched`() = runBlocking {
        seedWorkspace("before-import")
        val export = File(temporaryFolder.root, "workspace.ivai")
        archive.exportTo(export)
        val bytes = export.readBytes()
        bytes[bytes.lastIndex] = (bytes.last().toInt() xor 0x01).toByte()
        export.writeBytes(bytes)

        val failure = runCatching { archive.importFrom(export) }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
        assertEquals("before-import", workspace.readText("project-1", "notes/readme.txt"))
        assertNotNull(database.projectDao().findById("project-1"))
        assertEquals(1, database.messageDao().listForThread("thread-1").size)
    }

    @Test
    fun `workspace rejects traversal and absolute paths`() {
        val traversalFailure = runCatching {
            workspace.writeText("project-1", "../outside.txt", "blocked")
        }.exceptionOrNull()
        val absoluteFailure = runCatching {
            workspace.writeText("project-1", "/tmp/outside.txt", "blocked")
        }.exceptionOrNull()

        assertTrue(traversalFailure is IllegalArgumentException)
        assertTrue(absoluteFailure is IllegalArgumentException)
        assertFalse(File(temporaryFolder.root, "outside.txt").exists())
    }

    private suspend fun seedWorkspace(fileContent: String) {
        repository.saveProject(
            WorkspaceProjectEntity(
                id = "project-1",
                name = "Local project",
                description = "No secret belongs in archive metadata",
                fileCount = 1,
                updatedAtEpochMs = 10L
            )
        )
        repository.saveThread(
            ChatThreadEntity(
                id = "thread-1",
                title = "Conversation",
                snippet = "Saved locally",
                updatedAtEpochMs = 20L,
                modelOrCombo = "local-mock",
                projectId = "project-1"
            )
        )
        repository.appendMessage(
            ChatMessageEntity(
                id = "message-1",
                threadId = "thread-1",
                sender = MessageSender.USER.name,
                text = "سلام README.md",
                createdAtEpochMs = 30L,
                contentType = MessageContentType.MIXED_BIDI.name,
                codeSnippet = null,
                modelBadge = null,
                latencyMs = null
            )
        )
        workspace.writeText("project-1", "notes/readme.txt", fileContent)
    }
}
