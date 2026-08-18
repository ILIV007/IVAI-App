package dev.iliv007.ivai.agent

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.iliv007.ivai.data.local.AgentProfileEntity
import dev.iliv007.ivai.data.local.IvaiDatabase
import dev.iliv007.ivai.data.local.LocalWorkspaceRepository
import dev.iliv007.ivai.data.local.ProjectWorkspace
import dev.iliv007.ivai.data.local.ProviderAccountEntity
import dev.iliv007.ivai.data.local.ProviderConnectionEntity
import dev.iliv007.ivai.data.local.ProviderModelEntity
import dev.iliv007.ivai.data.local.RouterComboEntity
import dev.iliv007.ivai.data.local.RouterComboEntryEntity
import dev.iliv007.ivai.data.local.WorkspaceProjectEntity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
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
            saveTargetRegistry()
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
    fun `invalid agent targets are rejected before profile persistence`() = runBlocking {
        val invalid = newProfile(targetId = "missing-combo")

        val result = runCatching { repository.saveAgentProfile(invalid) }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Unknown Agent Combo target") == true)
        assertEquals(null, repository.findAgentProfile(invalid.id))
    }

    @Test
    fun `runtime rejects legacy profile with an invalid target before starting a run`() = runBlocking {
        val legacyInvalid = newProfile(targetId = "removed-combo")
        database.agentProfileDao().upsert(legacyInvalid)

        val result = runCatching { runtime.start(legacyInvalid, "Do not start") }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Unknown Agent Combo target") == true)
        assertEquals(null, repository.findAgentRun("run-${clock.get()}"))
    }

    @Test
    fun `runs started in the same millisecond keep distinct persisted identities`() = runBlocking {
        runtime = BasicAgentRuntime(
            repository = repository,
            projectWorkspace = workspace,
            tools = AgentToolRegistry { clock.get() },
            nowEpochMs = { 7_000L }
        )
        val profile = saveProfile()

        val first = runtime.start(profile, "First bounded run")
        val second = runtime.start(profile, "Second bounded run")

        assertNotEquals(first.id, second.id)
        assertEquals(2, repository.observeAgentRuns(profile.id).first().size)
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
    fun `process death recovery expires pending write approval without writing a file`() = runBlocking {
        val profile = saveProfile()
        val run = runtime.start(profile, "Wait for a write approval")
        runtime.requestTool(
            run,
            projectId = "project-a",
            position = 1,
            request = AgentToolRequest.WriteProjectFile("notes/restart.md", "must not be replayed")
        )
        val approval = repository.observeAgentApprovals(run.id).first().single()

        val restartedRuntime = BasicAgentRuntime(
            repository = repository,
            projectWorkspace = workspace,
            tools = AgentToolRegistry { clock.get() },
            nowEpochMs = { clock.getAndIncrement() }
        )
        assertEquals(1, restartedRuntime.recoverAfterProcessDeath())

        assertEquals(ApprovalStatus.DENIED.name, repository.findAgentApproval(approval.id)?.status)
        assertEquals(AgentRunStatus.FAILED.name, repository.findAgentRun(run.id)?.status)
        assertTrue(
            repository.observeAgentRunSteps(run.id).first().any { step ->
                step.safeSummary.contains("no write was performed")
            }
        )
        assertFalse(runCatching { workspace.readText("project-a", "notes/restart.md") }.isSuccess)
        assertTrue(restartedRuntime.resolveWriteApproval(approval.id, allowOnce = true) is AgentToolResult.Rejected)
    }

    @Test
    fun `read-only workspace tools are project-bound bounded and keep content out of trace`() = runBlocking {
        val profile = saveProfile(
            enabledTools = setOf(
                AgentToolKind.READ_PROJECT_FILE,
                AgentToolKind.LIST_WORKSPACE,
                AgentToolKind.SEARCH_PROJECT_FILES
            )
        )
        workspace.writeText("project-a", "docs/readme.md", "trace-must-not-store-this secret phrase")
        workspace.writeText("project-a", "notes/todo.txt", "Find the secret phrase in this project")
        val run = runtime.start(profile, "Inspect only local project files")

        val read = runtime.requestTool(run, "project-a", 1, AgentToolRequest.ReadProjectFile("docs/readme.md"))
        val listed = runtime.requestTool(run, "project-a", 2, AgentToolRequest.ListWorkspace)
        val searched = runtime.requestTool(run, "project-a", 3, AgentToolRequest.SearchProjectFiles("secret phrase"))

        assertEquals("trace-must-not-store-this secret phrase", (read as AgentToolResult.Completed).observation)
        assertTrue((listed as AgentToolResult.Completed).observation?.contains("docs/readme.md") == true)
        assertTrue((searched as AgentToolResult.Completed).observation?.contains("notes/todo.txt") == true)
        val trace = repository.observeAgentRunSteps(run.id).first()
        assertTrue(trace.any { it.stepKind == AgentToolKind.READ_PROJECT_FILE.name && it.status == "COMPLETED" })
        assertTrue(trace.any { it.stepKind == AgentToolKind.LIST_WORKSPACE.name && it.status == "COMPLETED" })
        assertTrue(trace.any { it.stepKind == AgentToolKind.SEARCH_PROJECT_FILES.name && it.status == "COMPLETED" })
        assertFalse(trace.any { it.safeSummary.contains("trace-must-not-store-this") || it.safeSummary.contains("secret phrase") })
    }

    @Test
    fun `workspace tool is rejected when disabled or aimed at another project`() = runBlocking {
        val profile = saveProfile(enabledTools = setOf(AgentToolKind.READ_PROJECT_FILE))
        workspace.writeText("project-a", "docs/readme.md", "private local content")
        val run = runtime.start(profile, "Respect profile boundaries")

        val wrongProject = runtime.requestTool(
            run,
            "project-b",
            1,
            AgentToolRequest.ReadProjectFile("docs/readme.md")
        )
        val disabled = runtime.requestTool(run, "project-a", 2, AgentToolRequest.ListWorkspace)

        assertTrue(wrongProject is AgentToolResult.Rejected)
        assertTrue(disabled is AgentToolResult.Rejected)
        val trace = repository.observeAgentRunSteps(run.id).first()
        assertTrue(trace.any { it.position == 1 && it.safeSummary.contains("limited to the Agent profile project") })
        assertTrue(trace.any { it.position == 2 && it.safeSummary.contains("disabled in the Agent profile") })
    }

    @Test
    fun `read-only workspace tools consume the same tool-call budget`() = runBlocking {
        val profile = saveProfile(
            maxSteps = 3,
            maxToolCalls = 1,
            enabledTools = setOf(AgentToolKind.READ_PROJECT_FILE, AgentToolKind.LIST_WORKSPACE)
        )
        workspace.writeText("project-a", "docs/readme.md", "bounded")
        val run = runtime.start(profile, "Stay within read-only budget")

        val first = runtime.requestTool(run, "project-a", 1, AgentToolRequest.ReadProjectFile("docs/readme.md"))
        val overflow = runtime.requestTool(run, "project-a", 2, AgentToolRequest.ListWorkspace)

        assertTrue(first is AgentToolResult.Completed)
        assertTrue(overflow is AgentToolResult.Rejected)
        assertEquals(AgentRunStatus.FAILED.name, repository.findAgentRun(run.id)?.status)
    }

    @Test
    fun `cancellation wins over a stale concurrent write approval resolution`() = runBlocking {
        val resolutionReachedCommit = CompletableDeferred<Unit>()
        val allowResolutionCommit = CompletableDeferred<Unit>()
        runtime = BasicAgentRuntime(
            repository = repository,
            projectWorkspace = workspace,
            tools = AgentToolRegistry { clock.get() },
            nowEpochMs = { clock.getAndIncrement() },
            beforeApprovalResolutionLock = {
                resolutionReachedCommit.complete(Unit)
                allowResolutionCommit.await()
            }
        )
        val profile = saveProfile()
        val run = runtime.start(profile, "Resolve or cancel a local write")
        runtime.requestTool(
            run,
            projectId = "project-a",
            position = 1,
            request = AgentToolRequest.WriteProjectFile("notes/race.md", "must never be written after cancellation")
        )
        val approval = repository.observeAgentApprovals(run.id).first().single()

        val resolution = async { runtime.resolveWriteApproval(approval.id, allowOnce = true) }
        resolutionReachedCommit.await()
        runtime.cancel(run)
        allowResolutionCommit.complete(Unit)

        assertTrue(resolution.await() is AgentToolResult.Rejected)
        assertEquals(AgentRunStatus.CANCELLED.name, repository.findAgentRun(run.id)?.status)
        assertEquals(ApprovalStatus.DENIED.name, repository.findAgentApproval(approval.id)?.status)
        assertFalse(runCatching { workspace.readText("project-a", "notes/race.md") }.isSuccess)
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

    private suspend fun saveProfile(
        maxSteps: Int = 6,
        maxToolCalls: Int = 6,
        enabledTools: Set<AgentToolKind> = setOf(
            AgentToolKind.CALCULATE,
            AgentToolKind.CURRENT_TIME,
            AgentToolKind.WRITE_PROJECT_FILE
        )
    ): AgentProfileEntity {
        val profile = newProfile(maxSteps = maxSteps, maxToolCalls = maxToolCalls, enabledTools = enabledTools)
        repository.saveAgentProfile(profile)
        return profile
    }

    private fun newProfile(
        targetId: String = "combo-a",
        maxSteps: Int = 6,
        maxToolCalls: Int = 6,
        enabledTools: Set<AgentToolKind> = setOf(
            AgentToolKind.CALCULATE,
            AgentToolKind.CURRENT_TIME,
            AgentToolKind.WRITE_PROJECT_FILE
        )
    ): AgentProfileEntity {
        val now = clock.getAndIncrement()
        return AgentProfileEntity(
            id = "agent-$now",
            name = "Local analyst",
            instructions = "Use only bounded local tools.",
            targetKind = "COMBO",
            targetId = targetId,
            accountId = null,
            projectId = "project-a",
            enabledToolsCsv = enabledTools.joinToString(",") { it.name },
            maxSteps = maxSteps,
            maxToolCalls = maxToolCalls,
            maxRuntimeMs = 60_000L,
            isEnabled = true,
            createdAtEpochMs = now,
            updatedAtEpochMs = now
        )
    }

    private suspend fun saveTargetRegistry() {
        val now = clock.get()
        repository.saveProviderConnection(
            ProviderConnectionEntity("connection-a", "CUSTOM_OPENAI_COMPATIBLE", "Local target", "https://api.example.test/v1", true, now, now)
        )
        repository.saveProviderAccount(
            ProviderAccountEntity("account-a", "connection-a", "BYOK account", "provider.connection-a.account-a", true, now, now)
        )
        repository.saveProviderModel(
            ProviderModelEntity("model-a", "connection-a", "model-a", "Selectable model", "TEXT,STREAMING", true, true, now)
        )
        repository.saveRouterCombo(
            RouterComboEntity("combo-a", "Configured combo", "Valid local target", true, now, now),
            listOf(RouterComboEntryEntity("entry-a", "combo-a", 0, "connection-a", "account-a", "model-a", true))
        )
    }
}
