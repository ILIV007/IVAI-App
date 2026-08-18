package dev.iliv007.ivai.agent

import dev.iliv007.ivai.data.local.AgentApprovalEntity
import dev.iliv007.ivai.data.local.AgentProfileEntity
import dev.iliv007.ivai.data.local.AgentRunEntity
import dev.iliv007.ivai.data.local.AgentRunStepEntity
import dev.iliv007.ivai.data.local.LocalWorkspaceRepository
import dev.iliv007.ivai.data.local.ProjectWorkspace
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * Bounded local runtime for the Basic Agent Alpha.
 *
 * This runtime never selects a provider, performs a network operation, stores raw model reasoning,
 * or executes a file write until the matching persisted approval is explicitly allowed once.
 */
class BasicAgentRuntime(
    private val repository: LocalWorkspaceRepository,
    private val projectWorkspace: ProjectWorkspace,
    private val tools: AgentToolRegistry,
    private val nowEpochMs: () -> Long = System::currentTimeMillis,
    /** Default no-op seam used only by deterministic concurrency tests. */
    private val beforeApprovalResolutionLock: suspend () -> Unit = {}
) {
    private val pendingWrites = mutableMapOf<String, PendingWrite>()
    private val approvalMutex = Mutex()

    suspend fun start(profile: AgentProfileEntity, goal: String): AgentRunEntity {
        require(profile.isEnabled) { "Agent profile is disabled" }
        require(goal.isNotBlank()) { "Agent goal must not be blank" }
        AgentExecutionLimits(profile.maxSteps, profile.maxToolCalls, profile.maxRuntimeMs)
        repository.validateAgentProfileTarget(profile)

        val now = nowEpochMs()
        val run = AgentRunEntity(
            id = "run-$now-${UUID.randomUUID()}",
            agentId = profile.id,
            goal = goal.trim(),
            status = AgentRunStatus.RUNNING.name,
            startedAtEpochMs = now,
            completedAtEpochMs = null,
            safeErrorMessage = null
        )
        repository.saveAgentRun(run)
        repository.saveAgentRunStep(
            AgentRunStepEntity(
                id = "${run.id}-step-0",
                runId = run.id,
                position = 0,
                stepKind = "GOAL",
                status = "COMPLETED",
                safeSummary = "Run started with bounded local policy.",
                createdAtEpochMs = now,
                completedAtEpochMs = now
            )
        )
        return run
    }

    suspend fun requestTool(
        run: AgentRunEntity,
        projectId: String?,
        position: Int,
        request: AgentToolRequest
    ): AgentToolResult {
        val currentRun = repository.findAgentRun(run.id)
            ?: return AgentToolResult.Rejected("Agent run was not found.")
        val profile = repository.findAgentProfile(currentRun.agentId)
            ?: return fail(currentRun, "Agent profile was not found.")

        val limitError = validateRequestBudget(currentRun, profile, position)
        if (limitError != null) return limitError
        val policyError = validateToolPolicy(profile, projectId, request)
        if (policyError != null) {
            return rejectAndTrace(currentRun, position, request.kind, policyError)
        }

        val result = runCatching {
            when (request) {
                is AgentToolRequest.ReadProjectFile -> executeReadProjectFile(projectId.orEmpty(), request)
                AgentToolRequest.ListWorkspace -> executeListWorkspace(projectId.orEmpty())
                is AgentToolRequest.SearchProjectFiles -> executeSearchProjectFiles(projectId.orEmpty(), request)
                else -> tools.evaluate(request)
            }
        }.getOrElse {
            AgentToolResult.Rejected("Workspace tool could not be completed safely.")
        }
        val now = nowEpochMs()
        when (result) {
            is AgentToolResult.Completed -> repository.saveAgentRunStep(
                AgentRunStepEntity(
                    id = "${currentRun.id}-step-$position",
                    runId = currentRun.id,
                    position = position,
                    stepKind = request.kind.name,
                    status = "COMPLETED",
                    safeSummary = result.safeSummary,
                    createdAtEpochMs = now,
                    completedAtEpochMs = now
                )
            )

            is AgentToolResult.Rejected -> repository.saveAgentRunStep(
                AgentRunStepEntity(
                    id = "${currentRun.id}-step-$position",
                    runId = currentRun.id,
                    position = position,
                    stepKind = request.kind.name,
                    status = "REJECTED",
                    safeSummary = result.safeReason,
                    createdAtEpochMs = now,
                    completedAtEpochMs = now
                )
            )

            is AgentToolResult.RequiresApproval -> {
                if (projectId == null) {
                    return fail(currentRun, "A write tool requires a selected project workspace.")
                }
                repository.saveAgentRun(
                    currentRun.copy(status = AgentRunStatus.AWAITING_APPROVAL.name)
                )
                repository.saveAgentRunStep(
                    AgentRunStepEntity(
                        id = "${currentRun.id}-step-$position",
                        runId = currentRun.id,
                        position = position,
                        stepKind = request.kind.name,
                        status = "AWAITING_APPROVAL",
                        safeSummary = "Write requires explicit user approval.",
                        createdAtEpochMs = now,
                        completedAtEpochMs = null
                    )
                )
                val approvalId = "${currentRun.id}-approval-$position"
                val write = request as AgentToolRequest.WriteProjectFile
                pendingWrites[approvalId] = PendingWrite(projectId, write.relativePath, write.content)
                repository.saveAgentApproval(
                    AgentApprovalEntity(
                        id = approvalId,
                        runId = currentRun.id,
                        toolKind = request.kind.name,
                        targetPath = result.targetPath,
                        preview = result.preview,
                        status = ApprovalStatus.PENDING.name,
                        createdAtEpochMs = now,
                        resolvedAtEpochMs = null
                    )
                )
            }
        }
        return result
    }

    suspend fun resolveWriteApproval(approvalId: String, allowOnce: Boolean): AgentToolResult {
        beforeApprovalResolutionLock()
        return approvalMutex.withLock {
            val approval = repository.findAgentApproval(approvalId)
                ?: return@withLock AgentToolResult.Rejected("Approval request was not found.")
            if (approval.status != ApprovalStatus.PENDING.name) {
                return@withLock AgentToolResult.Rejected("Approval request is no longer pending.")
            }
            val run = repository.findAgentRun(approval.runId)
                ?: return@withLock AgentToolResult.Rejected("Agent run was not found.")
            if (run.status != AgentRunStatus.AWAITING_APPROVAL.name) {
                return@withLock AgentToolResult.Rejected("Agent run is not awaiting approval.")
            }

            val now = nowEpochMs()
            val position = approval.id.substringAfterLast('-').toIntOrNull() ?: 0
            val pending = pendingWrites.remove(approvalId)
            if (!allowOnce) {
                repository.saveAgentApproval(approval.copy(status = ApprovalStatus.DENIED.name, resolvedAtEpochMs = now))
                repository.saveAgentRunStep(
                    AgentRunStepEntity(
                        id = "${approval.runId}-step-$position",
                        runId = approval.runId,
                        position = position,
                        stepKind = approval.toolKind,
                        status = "DENIED",
                        safeSummary = "Write was denied by the user.",
                        createdAtEpochMs = now,
                        completedAtEpochMs = now
                    )
                )
                repository.saveAgentRun(run.copy(status = AgentRunStatus.RUNNING.name))
                return@withLock AgentToolResult.Rejected("Write was denied by the user.")
            }
            if (pending == null) {
                repository.saveAgentApproval(approval.copy(status = ApprovalStatus.DENIED.name, resolvedAtEpochMs = now))
                return@withLock fail(run, "Write approval expired before execution.")
            }

            repository.saveAgentApproval(approval.copy(status = ApprovalStatus.ALLOWED_ONCE.name, resolvedAtEpochMs = now))
            try {
                projectWorkspace.writeText(pending.projectId, pending.relativePath, pending.content)
                repository.saveAgentApproval(approval.copy(status = ApprovalStatus.EXECUTED.name, resolvedAtEpochMs = now))
                repository.saveAgentRunStep(
                    AgentRunStepEntity(
                        id = "${approval.runId}-step-$position",
                        runId = approval.runId,
                        position = position,
                        stepKind = approval.toolKind,
                        status = "COMPLETED",
                        safeSummary = "Approved write completed: ${approval.targetPath}",
                        createdAtEpochMs = now,
                        completedAtEpochMs = now
                    )
                )
                repository.saveAgentRun(run.copy(status = AgentRunStatus.RUNNING.name))
                AgentToolResult.Completed("Approved write completed: ${approval.targetPath}")
            } catch (_: Exception) {
                fail(run, "Approved write could not be completed.")
            }
        }
    }

    /**
     * A process restart intentionally loses pending write content. Every persisted pending approval
     * is therefore denied and traced by the repository; no write can be replayed automatically.
     */
    suspend fun recoverAfterProcessDeath(): Int = approvalMutex.withLock {
        pendingWrites.clear()
        repository.expirePendingAgentApprovalsAfterProcessDeath(nowEpochMs())
    }

    suspend fun cancel(run: AgentRunEntity): AgentRunEntity = approvalMutex.withLock {
        val currentRun = repository.findAgentRun(run.id) ?: return@withLock run
        if (currentRun.status in TERMINAL_STATUSES) return@withLock currentRun

        val now = nowEpochMs()
        pendingWrites.entries.removeIf { (approvalId, _) -> approvalId.startsWith("${currentRun.id}-approval-") }
        repository.findPendingAgentApprovals(currentRun.id).forEach { approval ->
            repository.saveAgentApproval(approval.copy(status = ApprovalStatus.DENIED.name, resolvedAtEpochMs = now))
        }
        val cancelled = currentRun.copy(
            status = AgentRunStatus.CANCELLED.name,
            completedAtEpochMs = now,
            safeErrorMessage = null
        )
        repository.saveAgentRun(cancelled)
        repository.saveAgentRunStep(
            AgentRunStepEntity(
                id = "${currentRun.id}-terminal-cancelled",
                runId = currentRun.id,
                position = Int.MAX_VALUE,
                stepKind = "RUN",
                status = AgentRunStatus.CANCELLED.name,
                safeSummary = "Run cancelled by the user.",
                createdAtEpochMs = now,
                completedAtEpochMs = now
            )
        )
        cancelled
    }

    suspend fun complete(run: AgentRunEntity): AgentRunEntity {
        val currentRun = repository.findAgentRun(run.id) ?: return run
        if (currentRun.status in TERMINAL_STATUSES) return currentRun
        return finish(currentRun, AgentRunStatus.COMPLETED, "Run completed within configured limits.", null)
    }

    suspend fun fail(run: AgentRunEntity, safeError: String): AgentToolResult.Rejected {
        val currentRun = repository.findAgentRun(run.id) ?: run
        if (currentRun.status !in TERMINAL_STATUSES) {
            finish(currentRun, AgentRunStatus.FAILED, "Run stopped safely: $safeError", safeError)
        }
        return AgentToolResult.Rejected(safeError)
    }

    private fun validateToolPolicy(
        profile: AgentProfileEntity,
        projectId: String?,
        request: AgentToolRequest
    ): String? {
        val enabledTools = profile.enabledToolsCsv.split(',')
            .mapNotNull { serialized -> runCatching { AgentToolKind.valueOf(serialized.trim()) }.getOrNull() }
            .toSet()
        if (request.kind !in enabledTools) return "This tool is disabled in the Agent profile."

        if (request.kind in WORKSPACE_TOOL_KINDS) {
            val assignedProjectId = profile.projectId
                ?: return "This workspace tool requires a project assigned to the Agent profile."
            if (projectId != assignedProjectId) {
                return "This workspace tool is limited to the Agent profile project."
            }
        }
        return null
    }

    private fun executeReadProjectFile(
        projectId: String,
        request: AgentToolRequest.ReadProjectFile
    ): AgentToolResult.Completed {
        val read = projectWorkspace.readTextBounded(projectId, request.relativePath)
        return AgentToolResult.Completed(
            safeSummary = "Read ${read.relativePath}: ${read.byteCount} bytes${if (read.isTruncated) "; preview truncated" else ""}.",
            observation = read.content
        )
    }

    private fun executeListWorkspace(projectId: String): AgentToolResult.Completed {
        val listing = projectWorkspace.listFilesBounded(projectId)
        val observation = listing.files.joinToString("\\n") { file -> "${file.relativePath} (${file.byteCount} bytes)" }
        return AgentToolResult.Completed(
            safeSummary = "Listed ${listing.files.size} project files${if (listing.isTruncated) "; listing truncated" else ""}.",
            observation = observation
        )
    }

    private fun executeSearchProjectFiles(
        projectId: String,
        request: AgentToolRequest.SearchProjectFiles
    ): AgentToolResult.Completed {
        val search = projectWorkspace.searchTextBounded(projectId, request.query)
        val observation = search.hits.joinToString("\\n\\n") { hit -> "${hit.relativePath}: ${hit.preview}" }
        return AgentToolResult.Completed(
            safeSummary = "Search found ${search.hits.size} matches across ${search.scannedFileCount} bounded files" +
                "${if (search.skippedLargeFileCount > 0) "; skipped ${search.skippedLargeFileCount} oversized files" else ""}" +
                "${if (search.isTruncated) "; results truncated" else ""}.",
            observation = observation
        )
    }

    private suspend fun rejectAndTrace(
        run: AgentRunEntity,
        position: Int,
        kind: AgentToolKind,
        safeReason: String
    ): AgentToolResult.Rejected {
        val now = nowEpochMs()
        repository.saveAgentRunStep(
            AgentRunStepEntity(
                id = "${run.id}-step-$position",
                runId = run.id,
                position = position,
                stepKind = kind.name,
                status = "REJECTED",
                safeSummary = safeReason,
                createdAtEpochMs = now,
                completedAtEpochMs = now
            )
        )
        return AgentToolResult.Rejected(safeReason)
    }

    private suspend fun validateRequestBudget(
        run: AgentRunEntity,
        profile: AgentProfileEntity,
        position: Int
    ): AgentToolResult.Rejected? {
        if (!profile.isEnabled) return fail(run, "Agent profile is disabled.")
        if (run.status != AgentRunStatus.RUNNING.name) {
            return AgentToolResult.Rejected("Agent run is not accepting tool requests.")
        }
        val limits = AgentExecutionLimits(profile.maxSteps, profile.maxToolCalls, profile.maxRuntimeMs)
        if (position !in 1..limits.maxSteps) {
            return fail(run, "Maximum step limit reached.")
        }
        if (nowEpochMs() - run.startedAtEpochMs > limits.maxRuntimeMs) {
            return fail(run, "Maximum runtime reached.")
        }
        if (repository.countAgentToolCalls(run.id) >= limits.maxToolCalls) {
            return fail(run, "Maximum tool-call limit reached.")
        }
        return null
    }

    private suspend fun finish(
        run: AgentRunEntity,
        status: AgentRunStatus,
        safeSummary: String,
        safeError: String?
    ): AgentRunEntity {
        val now = nowEpochMs()
        val terminal = run.copy(
            status = status.name,
            completedAtEpochMs = now,
            safeErrorMessage = safeError
        )
        repository.saveAgentRun(terminal)
        repository.saveAgentRunStep(
            AgentRunStepEntity(
                id = "${run.id}-terminal-${status.name.lowercase()}",
                runId = run.id,
                position = Int.MAX_VALUE,
                stepKind = "RUN",
                status = status.name,
                safeSummary = safeSummary,
                createdAtEpochMs = now,
                completedAtEpochMs = now
            )
        )
        return terminal
    }

    private data class PendingWrite(val projectId: String, val relativePath: String, val content: String)

    private companion object {
        val WORKSPACE_TOOL_KINDS = setOf(
            AgentToolKind.READ_PROJECT_FILE,
            AgentToolKind.LIST_WORKSPACE,
            AgentToolKind.SEARCH_PROJECT_FILES,
            AgentToolKind.WRITE_PROJECT_FILE
        )
        val TERMINAL_STATUSES = setOf(
            AgentRunStatus.COMPLETED.name,
            AgentRunStatus.CANCELLED.name,
            AgentRunStatus.FAILED.name
        )
    }
}
