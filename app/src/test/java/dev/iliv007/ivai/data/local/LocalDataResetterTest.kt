package dev.iliv007.ivai.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.iliv007.ivai.security.EncryptedSecretPayload
import dev.iliv007.ivai.security.EncryptedSecretVault
import dev.iliv007.ivai.security.SecretCipher
import dev.iliv007.ivai.ui.model.MessageContentType
import dev.iliv007.ivai.ui.model.MessageSender
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
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
class LocalDataResetterTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var database: IvaiDatabase
    private lateinit var preferencesFile: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var cipherFactory: RecordingCipherFactory
    private lateinit var vault: EncryptedSecretVault
    private lateinit var repository: LocalWorkspaceRepository
    private lateinit var workspace: ProjectWorkspace

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            IvaiDatabase::class.java
        ).allowMainThreadQueries().build()
        preferencesFile = File(temporaryFolder.root, "vault.preferences_pb")
        dataStore = PreferenceDataStoreFactory.create { preferencesFile }
        cipherFactory = RecordingCipherFactory()
        vault = EncryptedSecretVault(dataStore, cipherFactory::forAlias)
        repository = LocalWorkspaceRepository(database)
        workspace = ProjectWorkspace(temporaryFolder.newFolder("workspace"))
    }

    @After
    fun tearDown() {
        database.close()
        preferencesFile.delete()
    }

    @Test
    fun `delete all data clears local database project files and encrypted vault`() = runBlocking {
        repository.saveProject(WorkspaceProjectEntity("project-1", "Project", "", 1, 10L))
        repository.saveThread(ChatThreadEntity("thread-1", "Thread", "", 20L, "mock", "project-1"))
        repository.appendMessage(
            ChatMessageEntity(
                id = "message-1",
                threadId = "thread-1",
                sender = MessageSender.USER.name,
                text = "local-only",
                createdAtEpochMs = 30L,
                contentType = MessageContentType.TEXT.name,
                codeSnippet = null,
                modelBadge = null,
                latencyMs = null
            )
        )
        workspace.writeText("project-1", "notes.txt", "private file")
        vault.store("gemini", "credential-never-exported")

        LocalDataResetter(repository, workspace, vault).deleteAllData()

        val snapshot = repository.snapshotForArchive()
        assertTrue(snapshot.projects.isEmpty())
        assertTrue(snapshot.threads.isEmpty())
        assertTrue(snapshot.messages.isEmpty())
        assertFalse(File(temporaryFolder.root, "workspace/project-1/notes.txt").exists())
        assertTrue(dataStore.data.first().asMap().isEmpty())
        assertTrue(cipherFactory.forAlias(EncryptedSecretVault.keyAlias("gemini")).wasDeleted)
        assertFalse(preferencesFile.readBytes().decodeToString().contains("credential-never-exported"))
    }

    @Test
    fun `delete all data clears persisted Agent profiles runs steps and approvals`() = runBlocking {
        database.agentProfileDao().upsert(
            AgentProfileEntity(
                id = "agent-reset",
                name = "Local agent",
                instructions = "Do not retain this profile.",
                targetKind = "COMBO",
                targetId = "removed-target",
                accountId = null,
                projectId = null,
                enabledToolsCsv = "CALCULATE",
                maxSteps = 1,
                maxToolCalls = 1,
                maxRuntimeMs = 1_000L,
                isEnabled = true,
                createdAtEpochMs = 10L,
                updatedAtEpochMs = 10L
            )
        )
        database.agentRunDao().upsert(
            AgentRunEntity(
                id = "run-reset",
                agentId = "agent-reset",
                goal = "Do not retain this run.",
                status = "AWAITING_APPROVAL",
                startedAtEpochMs = 11L,
                completedAtEpochMs = null,
                safeErrorMessage = null
            )
        )
        database.agentRunStepDao().upsert(
            AgentRunStepEntity(
                id = "run-reset-step-1",
                runId = "run-reset",
                position = 1,
                stepKind = "WRITE_PROJECT_FILE",
                status = "AWAITING_APPROVAL",
                safeSummary = "Do not retain this trace.",
                createdAtEpochMs = 12L,
                completedAtEpochMs = null
            )
        )
        database.agentApprovalDao().upsert(
            AgentApprovalEntity(
                id = "run-reset-approval-1",
                runId = "run-reset",
                toolKind = "WRITE_PROJECT_FILE",
                targetPath = "notes/private.md",
                preview = "Do not retain this preview.",
                status = "PENDING",
                createdAtEpochMs = 13L,
                resolvedAtEpochMs = null
            )
        )

        LocalDataResetter(repository, workspace, vault).deleteAllData()

        assertTrue(database.agentProfileDao().observeAll().first().isEmpty())
        assertTrue(database.agentRunDao().observeAll().first().isEmpty())
        assertTrue(database.agentRunStepDao().observeForRun("run-reset").first().isEmpty())
        assertTrue(database.agentApprovalDao().observeForRun("run-reset").first().isEmpty())
    }

    private class RecordingCipherFactory {
        private val ciphers = mutableMapOf<String, RecordingCipher>()

        fun forAlias(alias: String): RecordingCipher = ciphers.getOrPut(alias) { RecordingCipher() }
    }

    private class RecordingCipher : SecretCipher {
        var wasDeleted: Boolean = false
            private set

        override fun encrypt(plaintext: ByteArray): EncryptedSecretPayload =
            EncryptedSecretPayload(
                version = EncryptedSecretPayload.CURRENT_VERSION,
                iv = "reset-test-iv".encodeToByteArray(),
                ciphertext = plaintext.reversedArray()
            )

        override fun decrypt(payload: EncryptedSecretPayload): ByteArray = payload.ciphertext.reversedArray()

        override fun deleteKey() {
            wasDeleted = true
        }
    }
}
