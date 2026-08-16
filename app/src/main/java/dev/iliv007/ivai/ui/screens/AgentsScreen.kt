package dev.iliv007.ivai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.iliv007.ivai.agent.AgentRunStatus
import dev.iliv007.ivai.agent.AgentToolKind
import dev.iliv007.ivai.ui.viewmodel.AgentApprovalCard
import dev.iliv007.ivai.ui.viewmodel.AgentManagementState
import dev.iliv007.ivai.ui.viewmodel.AgentProfileCard
import dev.iliv007.ivai.ui.viewmodel.AgentRunCard
import dev.iliv007.ivai.ui.viewmodel.AgentRunTraceStepCard
import dev.iliv007.ivai.ui.viewmodel.AgentTargetOption

/** Local Agent Alpha surface. All content is rendered from Room-backed state. */
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
    modifier: Modifier = Modifier
) {
    var profileEditorOpen by remember { mutableStateOf(false) }
    var startProfile by remember { mutableStateOf<AgentProfileCard?>(null) }
    var approvalToReview by remember { mutableStateOf<AgentApprovalCard?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SecurityNotice()
        }
        item {
            SectionHeader("Agent profiles") {
                Button(onClick = { profileEditorOpen = true }, modifier = Modifier.testTag("add_agent_profile")) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Add profile")
                }
            }
        }
        if (state.profiles.isEmpty()) {
            item {
                EmptyCard("No local Agent profiles exist. Add one with an explicit provider or Combo target.")
            }
        } else {
            items(state.profiles, key = { it.profileId }) { profile ->
                AgentProfileCardView(
                    profile = profile,
                    onStart = { startProfile = profile }
                )
            }
        }

        item { SectionHeader("Runs and visible trace") }
        if (state.activeRuns.isEmpty()) {
            item { EmptyCard("No Agent runs have been started on this device.") }
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
        if (state.selectedRunId != null) {
            item { Text("Run trace", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            if (state.selectedRunTrace.isEmpty()) {
                item { EmptyCard("This run has no persisted trace steps yet.") }
            } else {
                items(state.selectedRunTrace, key = { it.stepId }) { step -> TraceStepCard(step) }
            }
        }

        item { SectionHeader("Pending write approvals") }
        if (state.pendingApprovals.isEmpty()) {
            item { EmptyCard("No file modifications are awaiting approval.") }
        } else {
            items(state.pendingApprovals, key = { it.approvalId }) { approval ->
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("agent_approval_${approval.approvalId}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Write requires review", fontWeight = FontWeight.Bold)
                            Text(approval.targetPath, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                        }
                        Button(onClick = { approvalToReview = approval }) { Text("Review") }
                    }
                }
            }
        }
    }

    if (profileEditorOpen) {
        AgentProfileEditor(
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
        WriteApprovalDialog(
            approval = approval,
            onDismiss = { approvalToReview = null },
            onResolve = { allowOnce ->
                onResolveApproval(approval.approvalId, allowOnce)
                approvalToReview = null
            }
        )
    }
    state.operationError?.let { error ->
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text("Local Agent operation") },
            text = { Text(error) },
            confirmButton = { TextButton(onClick = onDismissError) { Text("Dismiss") } }
        )
    }
}

@Composable
private fun SecurityNotice() {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("agent_notice_banner"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Bounded local Agent", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Runs are step-, tool-call-, and time-limited. File writes require an explicit, one-time review. No shell, external storage, or automatic network action is available.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: @Composable (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        action?.invoke()
    }
}

@Composable
private fun EmptyCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(message, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun AgentProfileCardView(profile: AgentProfileCard, onStart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("agent_card_${profile.profileId}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(profile.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(profile.targetLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Text("${profile.maxSteps} steps", style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace)
            }
            Text(profile.instructions, style = MaterialTheme.typography.bodySmall)
            Text(
                "Tools: ${profile.enabledTools.joinToString { it.name }} • ${profile.maxToolCalls} calls • ${profile.maxRuntimeMs / 1_000}s",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace
            )
            Text(
                "Write tool: approval required every time",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Button(onClick = onStart, enabled = profile.enabled) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Start local run")
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
            .border(if (selected) 1.dp else 0.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
            .testTag("agent_run_${run.runId}")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(run.agentName, fontWeight = FontWeight.Bold)
                Text(run.status.name, color = runStatusColor(run.status), fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
            }
            Text(run.goal, style = MaterialTheme.typography.bodySmall)
            run.safeErrorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onSelect) { Text(if (selected) "Trace selected" else "View trace") }
                if (!terminal) {
                    OutlinedButton(onClick = onCancel) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun TraceStepCard(step: AgentRunTraceStepCard) {
    Card(modifier = Modifier.fillMaxWidth().testTag("trace_step_${step.stepId}")) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${step.position}. ${step.stepKind}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text(step.status, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
            }
            Text(step.safeSummary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun runStatusColor(status: AgentRunStatus): Color = when (status) {
    AgentRunStatus.FAILED, AgentRunStatus.CANCELLED -> MaterialTheme.colorScheme.error
    AgentRunStatus.AWAITING_APPROVAL -> MaterialTheme.colorScheme.secondary
    AgentRunStatus.COMPLETED -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurface
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgentProfileEditor(
    availableTargets: List<AgentTargetOption>,
    onDismiss: () -> Unit,
    onCreate: (String, String, AgentTargetOption, String?, Set<AgentToolKind>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var selectedTarget by remember(availableTargets) { mutableStateOf(availableTargets.firstOrNull()) }
    var projectId by remember { mutableStateOf("") }
    var enabledTools by remember {
        mutableStateOf(setOf(AgentToolKind.CALCULATE, AgentToolKind.CURRENT_TIME))
    }
    val workspaceToolSelected = enabledTools.any { tool ->
        tool in setOf(
            AgentToolKind.READ_PROJECT_FILE,
            AgentToolKind.LIST_WORKSPACE,
            AgentToolKind.SEARCH_PROJECT_FILES,
            AgentToolKind.WRITE_PROJECT_FILE
        )
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add local Agent profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select a target already configured in local Provider or Router management. IVAI will not add a default provider.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(name, { name = it }, label = { Text("Profile name") }, singleLine = true)
                OutlinedTextField(instructions, { instructions = it }, label = { Text("Instructions") })
                Text("Execution target", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                if (availableTargets.isEmpty()) {
                    Text(
                        "No enabled local Direct Model or Combo is available. Configure one first.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    availableTargets.forEach { target ->
                        OutlinedButton(
                            onClick = { selectedTarget = target },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = true
                        ) {
                            Text(if (target == selectedTarget) "Selected: ${target.label}" else target.label)
                        }
                    }
                }
                OutlinedTextField(
                    projectId,
                    { projectId = it },
                    label = { Text("Project ID for Workspace tools") },
                    supportingText = {
                        Text("Required when read, list, search, or write is enabled. All file access stays inside this one project.")
                    },
                    singleLine = true
                )
                Text("Enabled local tools", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                AgentToolKind.entries.forEach { tool ->
                    FilterChip(
                        selected = tool in enabledTools,
                        onClick = {
                            enabledTools = if (tool in enabledTools) enabledTools - tool else enabledTools + tool
                        },
                        label = { Text(tool.name) },
                        modifier = Modifier.fillMaxWidth().testTag("agent_tool_${tool.name.lowercase()}")
                    )
                }
                Text(
                    "Read-only results are bounded and remain local. Every write requires Allow once.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedTarget?.let { target ->
                        onCreate(name, instructions, target, projectId.ifBlank { null }, enabledTools)
                    }
                },
                enabled = name.isNotBlank() && instructions.isNotBlank() && selectedTarget != null &&
                    (!workspaceToolSelected || projectId.isNotBlank())
            ) {
                Text("Create profile")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun StartRunDialog(profile: AgentProfileCard, onDismiss: () -> Unit, onStart: (String) -> Unit) {
    var goal by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start ${profile.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Goal and all resulting run steps stay local. The selected target remains ${profile.targetLabel}.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(goal, { goal = it }, label = { Text("Run goal") })
            }
        },
        confirmButton = { Button(onClick = { onStart(goal) }, enabled = goal.isNotBlank()) { Text("Start") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun WriteApprovalDialog(approval: AgentApprovalCard, onDismiss: () -> Unit, onResolve: (Boolean) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Lock, contentDescription = null) },
        title = { Text("Review file write") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Target: ${approval.targetPath}", fontFamily = FontFamily.Monospace)
                Text("Preview (max 4,000 characters)", style = MaterialTheme.typography.labelMedium)
                Text(approval.preview, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp), fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                Text("Approval applies to this one write only.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
        },
        confirmButton = { Button(onClick = { onResolve(true) }, modifier = Modifier.testTag("allow_once_${approval.approvalId}")) { Text("Allow once") } },
        dismissButton = { TextButton(onClick = { onResolve(false) }, modifier = Modifier.testTag("deny_${approval.approvalId}")) { Text("Deny") } }
    )
}
