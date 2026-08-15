package dev.iliv007.ivai.ui.viewmodel

import dev.iliv007.ivai.agent.AgentRunStatus
import dev.iliv007.ivai.agent.AgentToolKind
import dev.iliv007.ivai.agent.ApprovalStatus

data class AgentManagementState(
    val profiles: List<AgentProfileCard> = emptyList(),
    val activeRuns: List<AgentRunCard> = emptyList(),
    val pendingApprovals: List<AgentApprovalCard> = emptyList(),
    val selectedRunId: String? = null,
    val selectedRunTrace: List<AgentRunTraceStepCard> = emptyList(),
    val operationError: String? = null
)

data class AgentProfileCard(
    val profileId: String,
    val name: String,
    val instructions: String,
    val targetLabel: String,
    val projectId: String?,
    val enabledTools: List<AgentToolKind>,
    val maxSteps: Int,
    val maxToolCalls: Int,
    val maxRuntimeMs: Long,
    val enabled: Boolean
)

data class AgentRunCard(
    val runId: String,
    val agentId: String,
    val agentName: String,
    val goal: String,
    val status: AgentRunStatus,
    val startedAtEpochMs: Long,
    val safeErrorMessage: String?
)

data class AgentApprovalCard(
    val approvalId: String,
    val runId: String,
    val toolKind: AgentToolKind,
    val targetPath: String,
    val preview: String,
    val status: ApprovalStatus,
    val createdAtEpochMs: Long
)

data class AgentRunTraceStepCard(
    val stepId: String,
    val runId: String,
    val position: Int,
    val stepKind: String,
    val status: String,
    val safeSummary: String,
    val createdAtEpochMs: Long
)
