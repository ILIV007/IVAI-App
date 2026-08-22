package dev.iliv007.ivai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.iliv007.ivai.agent.AgentRunStatus
import dev.iliv007.ivai.agent.AgentToolKind
import dev.iliv007.ivai.ui.components.IvaiExecutionState
import dev.iliv007.ivai.ui.components.IvaiExecutionStatusBanner
import dev.iliv007.ivai.ui.components.IvaiPageHeader
import dev.iliv007.ivai.ui.components.IvaiScreenScaffold
import dev.iliv007.ivai.ui.components.IvaiStateCard
import dev.iliv007.ivai.ui.components.IvaiStateTone
import dev.iliv007.ivai.ui.theme.IvaiShapeTokens
import dev.iliv007.ivai.ui.theme.IvaiSpacing
import dev.iliv007.ivai.ui.viewmodel.AgentApprovalCard
import dev.iliv007.ivai.ui.viewmodel.AgentManagementState
import dev.iliv007.ivai.ui.viewmodel.AgentProfileCard
import dev.iliv007.ivai.ui.viewmodel.AgentRunCard
import dev.iliv007.ivai.ui.viewmodel.AgentRunTraceStepCard

/**
 * Phase 7.3 local Agent workspace. It only rearranges existing Room-backed UI state and callbacks;
 * Agent tools, limits, approval rules, persistence and runtime execution remain unchanged.
 */
private data class ApprovalOutcome(val targetPath: String, val allowedOnce: Boolean)

@Composable
fun AgentsScreen(
    state: AgentManagementState,
    onCreateAgent: (
        name: String,
        instructions: String,
        targetKind: String,
        targetId: String,
        accountId: String?,
        projectId: String?,
        enabledTools: Set<AgentToolKind>,
        maxSteps: Int,
        maxToolCalls: Int,
        maxRuntimeMs: Long
    ) -> Unit,
    onStartRun: (profileId: String, goal: String) -> Unit,
    onSelectRun: (runId: String) -> Unit,
    onCancelRun: (runId: String) -> Unit,
    onResolveApproval: (approvalId: String, allowOnce: Boolean) -> Unit,
    onDismissError: () -> Unit,
    onOpenConnections: () -> Unit,
    modifier: Modifier = Modifier
) {
    var profileEditorOpen by remember { mutableStateOf(false) }
    var startProfile by remember { mutableStateOf<AgentProfileCard?>(null) }
    var approvalToReview by remember { mutableStateOf<AgentApprovalCard?>(null) }
    var lastApprovalOutcome by remember { mutableStateOf<ApprovalOutcome?>(null) }
    val selectedRun = state.activeRuns.firstOrNull { it.runId == state.selectedRunId }

    IvaiScreenScaffold(modifier = modifier, testTag = "agent_workspace_screen") {
        LazyColumn(
            modifier = Modifier.padding(IvaiSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.Medium)
        ) {
            item {
                IvaiPageHeader(
                    title = "Agents",
                    subtitle = "Local profiles, bounded runs, and one-time write reviews stay under your control.",
                    testTag = "agents_page_header"
                )
            }
            item { AgentSafetyNotice() }
            if (state.pendingApprovals.isNotEmpty()) {
                item {
                    PendingApprovalPrioritySection(
                        approvals = state.pendingApprovals,
                        onReview = { approval ->
                            lastApprovalOutcome = null
                            approvalToReview = approval
                        }
                    )
                }
            }
            lastApprovalOutcome?.let { outcome ->
                item {
                    IvaiStateCard(
                        title = if (outcome.allowedOnce) "Write approved once" else "Write denied",
                        message = if (outcome.allowedOnce) {
                            "The reviewed write for ${outcome.targetPath} was approved once. No permission was remembered."
                        } else {
                            "The reviewed write for ${outcome.targetPath} was denied. The file remains unchanged."
                        },
                        tone = if (outcome.allowedOnce) IvaiStateTone.SUCCESS else IvaiStateTone.WARNING,
                        testTag = "agent_approval_resolved"
                    )
                }
            }
            selectedRun?.let { run ->
                item {
                    Column(modifier = Modifier.testTag("agent_selected_run_section")) {
                        AgentRunWorkspace(
                            run = run,
                            trace = state.selectedRunTrace,
                            onCancel = { onCancelRun(run.runId) }
                        )
                    }
                }
            }
            item {
                AgentProfileLibrary(
                    profiles = state.profiles,
                    availableTargets = state.availableTargets,
                    onAddProfile = { profileEditorOpen = true },
                    onStartProfile = { startProfile = it },
                    onOpenConnections = onOpenConnections,
                    modifier = Modifier.testTag("agent_profile_library")
                )
            }
            item {
                IvaiPageHeader(
                    title = "Local run workspace",
                    subtitle = "Runs and trace stay on this device. Select a run to inspect its safe local trace.",
                    testTag = "agent_runs_header"
                )
            }
            if (state.activeRuns.isEmpty()) {
                item {
                    IvaiStateCard(
                        title = "No local runs yet",
                        message = "Start a run from an enabled profile. IVAI will use only that profile’s explicit target and limits.",
                        tone = IvaiStateTone.NEUTRAL,
                        testTag = "agent_runs_empty"
                    )
                }
            } else {
                items(state.activeRuns, key = { it.runId }) { run ->
                    AgentRunCardView(
                        run = run,
                        selected = run.runId == state.selectedRunId,
                        onSelect = { onSelectRun(run.runId) },
                        onCancel = { onCancelRun(run.runId) }
                    )
                }
            }
            if (state.pendingApprovals.isEmpty()) {
                item {
                    IvaiPageHeader(
                        title = "Write approvals",
                        subtitle = "A project-file write never proceeds without a visible preview and one-time decision.",
                        testTag = "agent_approvals_header"
                    )
                    IvaiStateCard(
                        title = "No approval is waiting",
                        message = "There are no pending local file modifications.",
                        tone = IvaiStateTone.NEUTRAL,
                        testTag = "agent_approvals_empty"
                    )
                }
            }
        }
    }

    if (profileEditorOpen) {
        AgentProfileEditorSheet(
            availableTargets = state.availableTargets,
            onDismiss = { profileEditorOpen = false },
            onCreate = { name, instructions, target, projectId, enabledTools ->
                onCreateAgent(
                    name,
                    instructions,
                    target.targetKind,
                    target.targetId,
                    target.accountId,
                    projectId,
                    enabledTools,
                    8,
                    6,
                    60_000L
                )
                profileEditorOpen = false
            }
        )
    }
    startProfile?.let { profile ->
        StartRunDialog(
            profile = profile,
            onDismiss = { startProfile = null },
            onStart = { goal ->
                onStartRun(profile.profileId, goal)
                startProfile = null
            }
        )
    }
    approvalToReview?.let { approval ->
        WriteApprovalSheet(
            approval = approval,
            onDismiss = { approvalToReview = null },
            onResolve = { allowOnce ->
                lastApprovalOutcome = ApprovalOutcome(approval.targetPath, allowOnce)
                onResolveApproval(approval.approvalId, allowOnce)
                approvalToReview = null
            }
        )
    }
    state.operationError?.let { error ->
        AgentRecoveryDialog(error = error, onDismiss = onDismissError)
    }
}

@Composable
private fun AgentSafetyNotice() {
    IvaiStateCard(
        title = "Bounded local Agent",
        message = "Runs are step-, tool-call-, and time-limited. File writes require an explicit, one-time review. No shell, external storage, background work or automatic network action is available.",
        tone = IvaiStateTone.INFO,
        icon = Icons.Default.Security,
        testTag = "agent_notice_banner"
    )
}

@Composable
private fun PendingApprovalPrioritySection(
    approvals: List<AgentApprovalCard>,
    onReview: (AgentApprovalCard) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("agent_pending_approvals_priority"),
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(IvaiSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
        ) {
            Text(
                text = "One-time write review required",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${approvals.size} pending local file ${if (approvals.size == 1) "write" else "writes"}. Nothing changes until you review and decide once.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.testTag("agent_approvals_header")
            )
            approvals.forEach { approval ->
                ApprovalPreviewCard(approval = approval, onReview = { onReview(approval) })
            }
        }
    }
}

@Composable
private fun AgentRunCardView(run: AgentRunCard, selected: Boolean, onSelect: () -> Unit, onCancel: () -> Unit) {
    val terminal = run.status in setOf(AgentRunStatus.COMPLETED, AgentRunStatus.CANCELLED, AgentRunStatus.FAILED)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("agent_run_${run.runId}")
            .semantics { contentDescription = "Run ${run.agentName}, ${run.status.name.lowercase()}" },
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(IvaiSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(run.agentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    run.status.name.replace('_', ' '),
                    color = runStatusColor(run.status),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Text(run.goal, style = MaterialTheme.typography.bodySmall)
            run.safeErrorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)) {
                OutlinedButton(onClick = onSelect, modifier = Modifier.testTag("button_select_agent_run_${run.runId}")) {
                    Text(if (selected) "Trace selected" else "View trace")
                }
                if (!terminal && !selected) {
                    OutlinedButton(onClick = onCancel, modifier = Modifier.testTag("button_cancel_agent_run_${run.runId}")) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Spacer(Modifier.width(IvaiSpacing.XxxSmall))
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentRunWorkspace(
    run: AgentRunCard,
    trace: List<AgentRunTraceStepCard>,
    onCancel: () -> Unit
) {
    val terminal = run.status in setOf(AgentRunStatus.COMPLETED, AgentRunStatus.CANCELLED, AgentRunStatus.FAILED)
    Column(
        modifier = Modifier.testTag("agent_run_workspace_${run.runId}"),
        verticalArrangement = Arrangement.spacedBy(IvaiSpacing.Small)
    ) {
        IvaiPageHeader(
            title = "Live local run",
            subtitle = "${run.agentName} · ${run.goal}",
            testTag = "agent_run_workspace_header"
        )
        IvaiExecutionStatusBanner(
            state = run.status.toIvaiExecutionState(),
            targetLabel = run.agentName,
            detail = run.workspaceDetail(),
            announceChange = run.status in setOf(
                AgentRunStatus.AWAITING_APPROVAL,
                AgentRunStatus.COMPLETED,
                AgentRunStatus.CANCELLED,
                AgentRunStatus.FAILED
            ),
            action = if (terminal) null else {
                {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.testTag("button_workspace_cancel_${run.runId}")
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Spacer(Modifier.width(IvaiSpacing.XxxSmall))
                        Text("Cancel")
                    }
                }
            },
            testTag = "agent_run_status_${run.runId}"
        )
        run.safeErrorMessage?.let { message ->
            IvaiStateCard(
                title = "Run needs attention",
                message = message,
                tone = IvaiStateTone.ERROR,
                testTag = "agent_run_error_${run.runId}"
            )
        }
        IvaiPageHeader(
            title = "Timeline and safe trace",
            subtitle = "Each item is a persisted local summary. Hidden reasoning is not displayed.",
            testTag = "agent_trace_header"
        )
        if (trace.isEmpty()) {
            IvaiStateCard(
                title = "Trace will appear here",
                message = "This selected run has no persisted trace step yet.",
                tone = IvaiStateTone.INFO,
                testTag = "agent_trace_empty"
            )
        } else {
            trace.forEach { step -> TraceStepCard(step) }
        }
    }
}

private fun AgentRunStatus.toIvaiExecutionState(): IvaiExecutionState = when (this) {
    AgentRunStatus.DRAFT -> IvaiExecutionState.READY
    AgentRunStatus.RUNNING -> IvaiExecutionState.STREAMING
    AgentRunStatus.AWAITING_APPROVAL -> IvaiExecutionState.AWAITING_APPROVAL
    AgentRunStatus.COMPLETED -> IvaiExecutionState.COMPLETED
    AgentRunStatus.CANCELLED -> IvaiExecutionState.STOPPED
    AgentRunStatus.PAUSED_ERROR, AgentRunStatus.FAILED -> IvaiExecutionState.FAILED
}

private fun AgentRunCard.workspaceDetail(): String = when (status) {
    AgentRunStatus.DRAFT -> "Ready to start with this profile’s explicit local target."
    AgentRunStatus.RUNNING -> "Working within this profile’s fixed local bounds."
    AgentRunStatus.AWAITING_APPROVAL -> "A file write is paused until you review and decide once."
    AgentRunStatus.PAUSED_ERROR, AgentRunStatus.FAILED -> safeErrorMessage ?: "The run stopped without changing your target or permissions."
    AgentRunStatus.COMPLETED -> "The local run completed."
    AgentRunStatus.CANCELLED -> "The local run was cancelled."
}

@Composable
private fun TraceStepCard(step: AgentRunTraceStepCard) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("trace_step_${step.stepId}"),
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(IvaiSpacing.Small), verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XxxSmall)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${step.position}. ${step.stepKind}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text(step.status, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
            }
            Text(step.safeSummary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ApprovalPreviewCard(approval: AgentApprovalCard, onReview: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("agent_approval_${approval.approvalId}"),
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(IvaiSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.Small)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Write requires one-time review", fontWeight = FontWeight.Bold)
                Text(approval.targetPath, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = onReview, modifier = Modifier.testTag("button_review_approval_${approval.approvalId}")) { Text("Review") }
        }
    }
}

@Composable
private fun StartRunDialog(profile: AgentProfileCard, onDismiss: () -> Unit, onStart: (String) -> Unit) {
    var goal by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start ${profile.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)) {
                Text("Goal and all resulting run steps stay local. The selected target remains ${profile.targetLabel}.", style = MaterialTheme.typography.bodySmall)
                androidx.compose.material3.OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it },
                    label = { Text("Run goal") },
                    modifier = Modifier.fillMaxWidth().testTag("input_agent_run_goal")
                )
            }
        },
        confirmButton = {
            Button(onClick = { onStart(goal) }, enabled = goal.isNotBlank(), modifier = Modifier.testTag("button_confirm_start_agent_run")) {
                Text("Start local run")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WriteApprovalSheet(approval: AgentApprovalCard, onDismiss: () -> Unit, onResolve: (Boolean) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("agent_write_approval_sheet")
    ) {
        LazyColumn(
            modifier = Modifier.padding(horizontal = IvaiSpacing.Medium, vertical = IvaiSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.Small)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Column {
                        Text("Review local file write", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("This decision applies only once.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                IvaiStateCard(
                    title = "Target path",
                    message = approval.targetPath,
                    tone = IvaiStateTone.WARNING,
                    testTag = "agent_approval_path_${approval.approvalId}"
                )
            }
            item {
                Text("Bounded preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    approval.preview,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(IvaiSpacing.XSmall)
                        .testTag("agent_approval_preview_${approval.approvalId}"),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            item {
                Text("Allow once writes only this reviewed preview. Deny leaves the file unchanged. IVAI never remembers this decision.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)) {
                    TextButton(
                        onClick = { onResolve(false) },
                        modifier = Modifier.weight(1f).testTag("deny_${approval.approvalId}")
                    ) { Text("Deny") }
                    Button(
                        onClick = { onResolve(true) },
                        modifier = Modifier.weight(1f).testTag("allow_once_${approval.approvalId}")
                    ) { Text("Allow once") }
                }
            }
        }
    }
}

@Composable
private fun AgentRecoveryDialog(error: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Local Agent needs attention") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)) {
                Text(recoveryTitle(error), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(error, style = MaterialTheme.typography.bodySmall)
                Text("IVAI did not change your target, permissions or local files automatically.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Dismiss") } }
    )
}

private fun recoveryTitle(error: String): String = when {
    error.contains("target", ignoreCase = true) -> "Check the selected target"
    error.contains("cancel", ignoreCase = true) -> "Run was cancelled"
    error.contains("step", ignoreCase = true) || error.contains("limit", ignoreCase = true) || error.contains("runtime", ignoreCase = true) -> "A local run limit was reached"
    else -> "Local operation could not continue"
}

@Composable
private fun runStatusColor(status: AgentRunStatus): Color = when (status) {
    AgentRunStatus.FAILED, AgentRunStatus.CANCELLED -> MaterialTheme.colorScheme.error
    AgentRunStatus.AWAITING_APPROVAL -> MaterialTheme.colorScheme.secondary
    AgentRunStatus.COMPLETED -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurface
}
