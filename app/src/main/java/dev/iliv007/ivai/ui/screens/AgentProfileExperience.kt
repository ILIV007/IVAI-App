package dev.iliv007.ivai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import dev.iliv007.ivai.agent.AgentToolKind
import dev.iliv007.ivai.ui.components.IvaiPageHeader
import dev.iliv007.ivai.ui.components.IvaiStateCard
import dev.iliv007.ivai.ui.components.IvaiStateTone
import dev.iliv007.ivai.ui.theme.IvaiLayoutTokens
import dev.iliv007.ivai.ui.theme.IvaiShapeTokens
import dev.iliv007.ivai.ui.theme.IvaiSpacing
import dev.iliv007.ivai.ui.viewmodel.AgentProfileCard
import dev.iliv007.ivai.ui.viewmodel.AgentTargetOption

private const val DefaultMaxSteps = 8
private const val DefaultMaxToolCalls = 6
private const val DefaultMaxRuntimeMs = 60_000L

@Composable
internal fun AgentProfileLibrary(
    profiles: List<AgentProfileCard>,
    availableTargets: List<AgentTargetOption>,
    onAddProfile: () -> Unit,
    onStartProfile: (AgentProfileCard) -> Unit,
    onOpenConnections: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(IvaiSpacing.Small)
    ) {
        IvaiPageHeader(
            title = "Agent profiles",
            subtitle = "Choose a local target, bounded tools and a project boundary before starting a run.",
            testTag = "agent_profiles_header"
        ) {
            Button(
                onClick = onAddProfile,
                modifier = Modifier
                    .heightIn(min = IvaiLayoutTokens.MinimumTouchTarget)
                    .testTag("button_add_agent_profile")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(IvaiSpacing.XxxSmall))
                Text("Profile")
            }
        }
        when {
            availableTargets.isEmpty() -> IvaiStateCard(
                title = "Choose an execution target first",
                message = "No enabled local Direct Model or Combo is available. IVAI will not choose a provider or model for this profile.",
                tone = IvaiStateTone.WARNING,
                icon = Icons.Default.Tune,
                action = {
                    Button(
                        onClick = onOpenConnections,
                        modifier = Modifier.testTag("button_agent_open_connections")
                    ) { Text("Open Connections") }
                },
                testTag = "agent_profiles_no_target"
            )
            profiles.isEmpty() -> IvaiStateCard(
                title = "Create your first local Agent profile",
                message = "Profiles keep an explicit target, bounded tools and project scope together. Nothing runs until you start a local run.",
                tone = IvaiStateTone.INFO,
                icon = Icons.Default.Security,
                action = {
                    Button(onClick = onAddProfile, modifier = Modifier.testTag("button_agent_create_first_profile")) {
                        Text("Create profile")
                    }
                },
                testTag = "agent_profiles_empty"
            )
            else -> profiles.forEach { profile ->
                AgentProfileCardView(profile = profile, onStart = { onStartProfile(profile) })
            }
        }
    }
}

@Composable
private fun AgentProfileCardView(profile: AgentProfileCard, onStart: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("agent_profile_${profile.profileId}")
            .semantics {
                contentDescription = "Agent profile ${profile.name}. Target ${profile.targetLabel}. ${profile.enabledTools.size} bounded tools."
            },
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(IvaiSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(profile.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(profile.targetLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Text(
                    "${profile.maxSteps} steps",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(profile.instructions, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            AgentProfileDetail("Tools", profile.enabledTools.joinToString { it.displayName() })
            AgentProfileDetail("Project", profile.projectId ?: "No workspace tools")
            AgentProfileDetail("Bounds", "${profile.maxToolCalls} calls · ${profile.maxRuntimeMs / 1_000}s")
            Text(
                "Writes require review and Allow once every time.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Button(
                onClick = onStart,
                enabled = profile.enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("button_start_agent_${profile.profileId}")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(IvaiSpacing.XxxSmall))
                Text("Start local run")
            }
        }
    }
}

@Composable
private fun AgentProfileDetail(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AgentProfileEditorSheet(
    availableTargets: List<AgentTargetOption>,
    onDismiss: () -> Unit,
    onCreate: (String, String, AgentTargetOption, String?, Set<AgentToolKind>) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var name by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var selectedTarget by remember { mutableStateOf<AgentTargetOption?>(null) }
    var projectId by remember { mutableStateOf("") }
    var enabledTools by remember { mutableStateOf(setOf(AgentToolKind.CALCULATE, AgentToolKind.CURRENT_TIME)) }
    val workspaceToolSelected = enabledTools.any { it.requiresProjectScope() }
    val canAdvance = when (step) {
        1 -> name.isNotBlank() && instructions.isNotBlank()
        2 -> selectedTarget != null
        3 -> !workspaceToolSelected || projectId.isNotBlank()
        else -> true
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("agent_profile_editor_sheet")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = IvaiSpacing.Medium, vertical = IvaiSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.Small)
        ) {
            Text("Create local Agent profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Step $step of 4", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            when (step) {
                1 -> AgentIdentityStep(
                    name = name,
                    instructions = instructions,
                    onNameChange = { name = it },
                    onInstructionsChange = { instructions = it }
                )
                2 -> AgentTargetStep(
                    availableTargets = availableTargets,
                    selectedTarget = selectedTarget,
                    onSelectTarget = { selectedTarget = it }
                )
                3 -> AgentToolsProjectStep(
                    enabledTools = enabledTools,
                    projectId = projectId,
                    onToggleTool = { tool ->
                        enabledTools = if (tool in enabledTools) enabledTools - tool else enabledTools + tool
                    },
                    onProjectChange = { projectId = it }
                )
                else -> AgentProfileReviewStep(
                    name = name,
                    instructions = instructions,
                    selectedTarget = requireNotNull(selectedTarget),
                    enabledTools = enabledTools,
                    projectId = projectId.ifBlank { null }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
            ) {
                if (step == 1) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                } else {
                    OutlinedButton(
                        onClick = { step -= 1 },
                        modifier = Modifier.weight(1f).testTag("button_agent_editor_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(Modifier.width(IvaiSpacing.XxxSmall))
                        Text("Back")
                    }
                }
                Button(
                    onClick = {
                        if (step < 4) {
                            step += 1
                        } else {
                            onCreate(name, instructions, requireNotNull(selectedTarget), projectId.ifBlank { null }, enabledTools)
                        }
                    },
                    enabled = canAdvance,
                    modifier = Modifier.weight(1f).testTag(if (step == 4) "button_agent_create_final" else "button_agent_editor_continue")
                ) {
                    Text(if (step == 4) "Create profile" else "Continue")
                    if (step < 4) {
                        Spacer(Modifier.width(IvaiSpacing.XxxSmall))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentIdentityStep(
    name: String,
    instructions: String,
    onNameChange: (String) -> Unit,
    onInstructionsChange: (String) -> Unit
) {
    Text("Identity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Text("The name and instructions remain local to this Agent profile.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Profile name") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("input_agent_profile_name")
    )
    OutlinedTextField(
        value = instructions,
        onValueChange = onInstructionsChange,
        label = { Text("Local instructions") },
        minLines = 3,
        modifier = Modifier.fillMaxWidth().testTag("input_agent_profile_instructions")
    )
}

@Composable
private fun AgentTargetStep(
    availableTargets: List<AgentTargetOption>,
    selectedTarget: AgentTargetOption?,
    onSelectTarget: (AgentTargetOption) -> Unit
) {
    Text("Execution target", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Text("Choose a Direct Model or Combo you already configured. IVAI never selects a provider, model or Combo automatically.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    availableTargets.forEach { target ->
        OutlinedButton(
            onClick = { onSelectTarget(target) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("agent_target_${target.targetKind}_${target.targetId}"),
            border = androidx.compose.foundation.BorderStroke(
                width = androidx.compose.ui.unit.Dp.Hairline,
                color = if (target == selectedTarget) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(if (target == selectedTarget) "Selected: ${target.label}" else target.label, style = MaterialTheme.typography.labelLarge)
                Text(target.targetKind, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AgentToolsProjectStep(
    enabledTools: Set<AgentToolKind>,
    projectId: String,
    onToggleTool: (AgentToolKind) -> Unit,
    onProjectChange: (String) -> Unit
) {
    Text("Tools and project boundary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Text("Read results stay bounded and local. Any file write requires a visible preview and Allow once.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    AgentToolKind.entries.forEach { tool ->
        FilterChip(
            selected = tool in enabledTools,
            onClick = { onToggleTool(tool) },
            label = { Text(tool.displayName()) },
            modifier = Modifier.fillMaxWidth().testTag("agent_tool_${tool.name.lowercase()}")
        )
    }
    val requiresProject = enabledTools.any { it.requiresProjectScope() }
    OutlinedTextField(
        value = projectId,
        onValueChange = onProjectChange,
        enabled = requiresProject,
        label = { Text("Project ID for workspace tools") },
        supportingText = {
            Text(if (requiresProject) "Required: all selected workspace tools stay inside this one project." else "Choose a workspace tool to set a project boundary.")
        },
        leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("input_agent_project_id")
    )
}

@Composable
private fun AgentProfileReviewStep(
    name: String,
    instructions: String,
    selectedTarget: AgentTargetOption,
    enabledTools: Set<AgentToolKind>,
    projectId: String?
) {
    Text("Final review", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Text("Only these explicit choices will be saved. No hidden connection, permission or run is created.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
    AgentProfileDetail("Name", name)
    AgentProfileDetail("Target", selectedTarget.label)
    AgentProfileDetail("Tools", enabledTools.joinToString { it.displayName() })
    AgentProfileDetail("Project", projectId ?: "No workspace tools")
    AgentProfileDetail("Bounds", "$DefaultMaxSteps steps · $DefaultMaxToolCalls calls · ${DefaultMaxRuntimeMs / 1_000}s")
    Text(instructions, style = MaterialTheme.typography.bodySmall, maxLines = 4, overflow = TextOverflow.Ellipsis)
}

private fun AgentToolKind.requiresProjectScope(): Boolean = this in setOf(
    AgentToolKind.READ_PROJECT_FILE,
    AgentToolKind.LIST_WORKSPACE,
    AgentToolKind.SEARCH_PROJECT_FILES,
    AgentToolKind.WRITE_PROJECT_FILE
)

private fun AgentToolKind.displayName(): String = name.lowercase().split('_').joinToString(" ") { word -> word.replaceFirstChar(Char::uppercase) }
