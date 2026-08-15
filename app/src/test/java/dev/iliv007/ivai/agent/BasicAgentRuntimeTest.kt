package dev.iliv007.ivai.agent

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.iliv007.ivai.data.local.AgentProfileEntity
import dev.iliv007.ivai.data.local.IvaiDatabase
import dev.iliv007.ivai.data.local.LocalWorkspaceRepository
import dev.iliv007.ivai.data.local.ProjectWorkspace
import dev.iliv007.ivai.data.local.WorkspaceProjectEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicLong

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BasicAgentRuntimeTest {
    private lateinit var database: IvaiDatabase
    private lateinit var repository: LocalWorkspaceRepository
    private lateinit var workspace: ProjectWorkspace
    private lateinit var workspaceRoot: java.io.File
    private lateinit var runtime: BasicAgentRuntime
    private val clock = AtomicLong(1_000L)

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            IvaiDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = LocalWorkspaceRepository(database)
        runBlocking {
            repository.saveProject(
                WorkspaceProjectEntity(
                    id = "project-a",
                    name = "Agent test project",
                    description = "Local fixture",
                    fileCount = 0,
                    updatedAtEpochMs = clock.get()
                )
            )
        }
        workspaceRoot = Files.createTempDirectory("ivai-agent-test").toFile()
        workspace = ProjectWorkspace(workspaceRoot)
        runtime = BasicAgentRuntime(
            repository = repository,
            projectWorkspace = workspace,
            tools = AgentToolRegistry { clock.get() },
            nowEpochMs = { clock.getAndIncrement() }
        )
    }

    @After
    fun tearDown() {
        database.close()
        workspaceRoot.deleteRecursively()
    }

    @Test
    fun `allow once executes only the reviewed project write`() = runBlocking {
        val profile = saveProfile()
        val run = runtime.start(profile, "Create a local summary")

        val requested = runtime.requestTool(
            run,
            projectId = "project-a",
            position = 1,
            request = AgentToolRequest.WriteProjectFile("notes/summary.md", "reviewed content")
        )
        assertTrue(requested is AgentToolResult.RequiresApproval)
        val approval = repository.observeAgentApprovals(run.id).first().single()
        assertEquals(ApprovalStatus.PENDING.name, approval.status)

        val result = runtime.resolveWriteApproval(approval.id, allowOnce = true)

        assertTrue(result is AgentToolResult.Completed)
        assertEquals("reviewed content", workspace.readText("project-a", "notes/summary.md"))
        assertEquals(ApprovalStatus.EXECUTED.name, repository.findAgentApproval(approval.id)?.status)
        assertEquals(AgentRunStatus.RUNNING.name, repository.findAgentRun(run.id)?.status)
    }

    @Test
    fun `denied approval never writes the project file`() = runBlocking {
        val profile = saveProfile()
        val run = runtime.start(profile, "Attempt a local write")
        runtime.requestTool(
            run,
            projectId = "project-a",
            position = 1,
            request = AgentToolRequest.WriteProjectFile("notes/denied.md", "must not exist")
        )
        val approval = repository.observeAgentApprovals(run.id).first().single()

        val result = runtime.resolveWriteApproval(approval.id, allowOnce = false)

        assertTrue(result is AgentToolResult.Rejected)
        assertFalse(runCatching { workspace.readText("project-a", "notes/denied.md") }.isSuccess)
        assertEquals(ApprovalStatus.DENIED.name, repository.findAgentApproval(approval.id)?.status)
    }

    @Test
    fun `step budget overflow fails the run before tool evaluation`() = runBlocking {
        val profile = saveProfile(maxSteps = 1, maxToolCalls = 1)
        val run = runtime.start(profile, "Stay within budget")

        val result = runtime.requestTool(run, null, position = 2, request = AgentToolRequest.CurrentTime)

        assertTrue(result is AgentToolResult.Rejected)
        assertEquals(AgentRunStatus.FAILED.name, repository.findAgentRun(run.id)?.status)
        assertTrue(repository.observeAgentRunSteps(run.id).first().any { it.status == AgentRunStatus.FAILED.name })
    }

    @Test
    fun `cancellation stops a pending write and invalidates its approval`() = runBlocking {
        val profile = saveProfile()
        val run = runtime.start(profile, "Wait for write approval")
        runtime.requestTool(
            run,
            projectId = "project-a",
            position = 1,
            request = AgentToolRequest.WriteProjectFile("notes/cancelled.md", "must not exist")
        )
        val approval = repository.observeAgentApprovals(run.id).first().single()

        runtime.cancel(run)
        val laterRequest = runtime.requestTool(run, null, position = 2, request = AgentToolRequest.CurrentTime)

        assertEquals(AgentRunStatus.CANCELLED.name, repository.findAgentRun(run.id)?.status)
        assertEquals(ApprovalStatus.DENIED.name, repository.findAgentApproval(approval.id)?.status)
        assertTrue(laterRequest is AgentToolResult.Rejected)
        assertFalse(runCatching { workspace.readText("project-a", "notes/cancelled.md") }.isSuccess)
    }

    private suspend fun saveProfile(maxSteps: Int = 6, maxToolCalls: Int = 6): AgentProfileEntity {
        val now = clock.getAndIncrement()
        val profile = AgentProfileEntity(
            id = "agent-$now",
            name = "Local analyst",
            instructions = "Use only bounded local tools.",
            targetKind = "COMBO",
            targetId = "user-configured-combo",
            accountId = null,
            projectId = "project-a",
            enabledToolsCsv = "CALCULATE,CURRENT_TIME,WRITE_PROJECT_FILE",
            maxSteps = maxSteps,
            maxToolCalls = maxToolCalls,
            maxRuntimeMs = 60_000L,
            isEnabled = true,
            createdAtEpochMs = now,
            updatedAtEpochMs = now
        )
        repository.saveAgentProfile(profile)
        return profile
    }
}
