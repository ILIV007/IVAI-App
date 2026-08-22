package dev.iliv007.ivai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import dev.iliv007.ivai.ui.components.IvaiPageHeader
import dev.iliv007.ivai.ui.components.IvaiScreenScaffold
import dev.iliv007.ivai.ui.components.IvaiStateCard
import dev.iliv007.ivai.ui.components.IvaiStateTone
import dev.iliv007.ivai.ui.model.UiPreviewState
import dev.iliv007.ivai.ui.model.WorkspaceProject
import dev.iliv007.ivai.ui.theme.IvaiElevationTokens
import dev.iliv007.ivai.ui.theme.IvaiIconSizeTokens
import dev.iliv007.ivai.ui.theme.IvaiLayoutTokens
import dev.iliv007.ivai.ui.theme.IvaiShapeTokens
import dev.iliv007.ivai.ui.theme.IvaiSpacing
import dev.iliv007.ivai.ui.theme.IvaiStrokeTokens
import dev.iliv007.ivai.ui.theme.rememberIvaiSemanticColors

/**
 * Local-only Project Hub. It deliberately presents only fields already available in [WorkspaceProject]
 * and routes user-initiated work to the existing Chat and Agents destinations.
 */
@Composable
fun ProjectsScreen(
    projects: List<WorkspaceProject>,
    modifier: Modifier = Modifier,
    selectedProjectId: String? = null,
    previewState: UiPreviewState = UiPreviewState.NORMAL,
    onSelectProject: (String?) -> Unit = {},
    onStartProjectChat: (String) -> Unit = {},
    onOpenChats: () -> Unit = {},
    onOpenAgents: () -> Unit = {}
) {
    val semanticColors = rememberIvaiSemanticColors()
    val visibleProjects = if (previewState == UiPreviewState.EMPTY) emptyList() else projects
    val selectedProject = visibleProjects.firstOrNull { it.id == selectedProjectId }

    IvaiScreenScaffold(modifier = modifier, testTag = "projects_screen") {
        LazyColumn(
            contentPadding = PaddingValues(IvaiSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.Small)
        ) {
            item {
                IvaiPageHeader(
                    title = "Workspace",
                    subtitle = "Local project context, files, and intentional routes to Chat and Agents.",
                    testTag = "projects_header"
                )
            }

            workspacePreviewPresentation(previewState)?.let { presentation ->
                item {
                    IvaiStateCard(
                        title = presentation.title,
                        message = presentation.message,
                        tone = presentation.tone,
                        icon = Icons.Default.Info,
                        action = presentation.actionLabel?.let { actionLabel ->
                            {
                                OutlinedButton(
                                    onClick = onOpenChats,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = IvaiLayoutTokens.MinimumTouchTarget)
                                        .testTag("projects_state_open_chat")
                                ) {
                                    Text(actionLabel)
                                }
                            }
                        },
                        testTag = "projects_preview_state"
                    )
                }
            }

            if (visibleProjects.isEmpty()) {
                item {
                    IvaiStateCard(
                        title = "No local projects yet",
                        message = "You can start a chat without selecting a project. A project only adds context when you choose one.",
                        tone = IvaiStateTone.NEUTRAL,
                        icon = Icons.Default.FolderOpen,
                        action = {
                            OutlinedButton(
                                onClick = onOpenChats,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = IvaiLayoutTokens.MinimumTouchTarget)
                                    .testTag("projects_empty_open_chat")
                            ) {
                                Text("Open Chat")
                            }
                        },
                        testTag = "projects_empty_state"
                    )
                }
            } else {
                selectedProject?.let { project ->
                    item {
                        SelectedProjectDetail(
                            project = project,
                            onClear = { onSelectProject(null) }
                        )
                    }
                }

                item {
                    Text(
                        text = "Local projects",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = semanticColors.textPrimary,
                        modifier = Modifier
                            .testTag("workspace_project_library_header")
                            .semantics { this[SemanticsProperties.Heading] = Unit }
                    )
                }

                items(visibleProjects, key = { it.id }) { project ->
                    ProjectSummaryCard(
                        project = project,
                        isSelected = project.id == selectedProjectId,
                        onSelectProject = onSelectProject,
                        onStartProjectChat = onStartProjectChat
                    )
                }

                item {
                    WorkspaceContinuationRoutes(
                        selectedProject = selectedProject,
                        onOpenChats = onOpenChats,
                        onOpenAgents = onOpenAgents
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedProjectDetail(
    project: WorkspaceProject,
    onClear: () -> Unit
) {
    val semanticColors = rememberIvaiSemanticColors()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("workspace_selected_project_detail")
            .semantics { stateDescription = "Selected local project: ${project.name}" },
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        color = semanticColors.surfaceInteractive,
        border = BorderStroke(IvaiStrokeTokens.Default, semanticColors.actionPrimary.copy(alpha = 0.6f)),
        tonalElevation = IvaiElevationTokens.Active
    ) {
        Column(
            modifier = Modifier.padding(IvaiSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = semanticColors.actionPrimary,
                    modifier = Modifier.size(IvaiIconSizeTokens.Inline)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Selected local project",
                        style = MaterialTheme.typography.labelLarge,
                        color = semanticColors.textPrimary
                    )
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = semanticColors.textPrimary,
                        modifier = Modifier.testTag("project_detail_name_${project.id}")
                    )
                }
                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier
                        .heightIn(min = IvaiLayoutTokens.MinimumTouchTarget)
                        .testTag("projects_clear_context")
                ) {
                    Text("Clear")
                }
            }
            Text(
                project.description,
                style = MaterialTheme.typography.bodyMedium,
                color = semanticColors.textSecondary,
                modifier = Modifier.testTag("project_detail_description_${project.id}")
            )
            Text(
                text = "${project.fileCount} known local files",
                style = MaterialTheme.typography.labelLarge,
                color = semanticColors.actionSecondary,
                modifier = Modifier.testTag("project_detail_file_count_${project.id}")
            )
            Text(
                text = "Last modified ${project.lastModified}",
                style = MaterialTheme.typography.labelSmall,
                color = semanticColors.textSecondary
            )
            Text(
                text = "This detail summarizes only local project fields already stored on this device.",
                style = MaterialTheme.typography.labelSmall,
                color = semanticColors.textSecondary
            )
        }
    }
}

@Composable
private fun ProjectSummaryCard(
    project: WorkspaceProject,
    isSelected: Boolean,
    onSelectProject: (String?) -> Unit,
    onStartProjectChat: (String) -> Unit
) {
    val semanticColors = rememberIvaiSemanticColors()
    val borderColor = if (isSelected) semanticColors.actionPrimary else semanticColors.border
    val selectionLabel = if (isSelected) "Selected for new chats" else "Use as chat context"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_card_${project.id}")
            .semantics { stateDescription = selectionLabel },
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        color = semanticColors.surfaceRaised,
        border = BorderStroke(IvaiStrokeTokens.Default, borderColor),
        tonalElevation = if (isSelected) IvaiElevationTokens.Active else IvaiElevationTokens.Raised
    ) {
        Column(
            modifier = Modifier.padding(IvaiSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(IvaiShapeTokens.Control),
                    color = semanticColors.surfaceInteractive
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = semanticColors.actionPrimary,
                        modifier = Modifier
                            .padding(IvaiSpacing.XSmall)
                            .size(IvaiIconSizeTokens.Inline)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XxxSmall)
                ) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = semanticColors.textPrimary
                    )
                    Text(
                        text = "Updated ${project.lastModified}",
                        style = MaterialTheme.typography.bodySmall,
                        color = semanticColors.textSecondary
                    )
                }
            }

            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                color = semanticColors.textSecondary
            )

            ProjectMetadata(fileCount = project.fileCount)

            OutlinedButton(
                onClick = { onSelectProject(if (isSelected) null else project.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = IvaiLayoutTokens.MinimumTouchTarget)
                    .testTag("project_context_${project.id}")
            ) {
                Text(if (isSelected) "Clear project context" else "Use for new chats")
            }
            Button(
                onClick = {
                    onSelectProject(project.id)
                    onStartProjectChat(project.id)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = semanticColors.actionPrimary,
                    contentColor = semanticColors.actionOnPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = IvaiLayoutTokens.MinimumTouchTarget)
                    .testTag("project_start_chat_${project.id}")
            ) {
                Text("Start chat in this project")
            }
        }
    }
}

@Composable
private fun ProjectMetadata(fileCount: Int) {
    val semanticColors = rememberIvaiSemanticColors()
    Surface(
        shape = RoundedCornerShape(IvaiShapeTokens.Control),
        color = semanticColors.surfaceInteractive,
        border = BorderStroke(IvaiStrokeTokens.Subtle, semanticColors.borderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = IvaiSpacing.XSmall, vertical = IvaiSpacing.XxSmall),
            horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = semanticColors.actionSecondary,
                modifier = Modifier.size(IvaiIconSizeTokens.Meta)
            )
            Text(
                text = "$fileCount local files",
                style = MaterialTheme.typography.labelMedium,
                color = semanticColors.textSecondary
            )
        }
    }
}

@Composable
private fun WorkspaceContinuationRoutes(
    selectedProject: WorkspaceProject?,
    onOpenChats: () -> Unit,
    onOpenAgents: () -> Unit
) {
    val semanticColors = rememberIvaiSemanticColors()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("workspace_activity_routes")
            .semantics { stateDescription = "Explicit continuation routes" },
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        color = semanticColors.surfaceRaised,
        border = BorderStroke(IvaiStrokeTokens.Default, semanticColors.borderSubtle),
        tonalElevation = IvaiElevationTokens.Raised
    ) {
        Column(
            modifier = Modifier.padding(IvaiSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
        ) {
            Text(
                text = if (selectedProject == null) "Continue in IVAI" else "Continue with this project",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = semanticColors.textPrimary
            )
            Text(
                text = if (selectedProject == null) {
                    "Open Chat or Agents intentionally. Each surface keeps its own local scope."
                } else {
                    "Starting a chat applies this selected project only through your explicit next action. Agent profiles keep their own explicit project scope."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = semanticColors.textSecondary
            )
            OutlinedButton(
                onClick = onOpenChats,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = IvaiLayoutTokens.MinimumTouchTarget)
                    .testTag("projects_open_chats")
            ) {
                Text("Open Chat")
            }
            OutlinedButton(
                onClick = onOpenAgents,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = IvaiLayoutTokens.MinimumTouchTarget)
                    .testTag("projects_open_agents")
            ) {
                Text("Open Agents")
            }
        }
    }
}

private data class WorkspacePreviewPresentation(
    val title: String,
    val message: String,
    val tone: IvaiStateTone,
    val actionLabel: String? = null
)

private fun workspacePreviewPresentation(state: UiPreviewState): WorkspacePreviewPresentation? = when (state) {
    UiPreviewState.NORMAL -> null
    UiPreviewState.LOADING -> WorkspacePreviewPresentation(
        title = "Loading local workspace",
        message = "Project summaries are being read on this device.",
        tone = IvaiStateTone.INFO
    )
    UiPreviewState.EMPTY -> null
    UiPreviewState.OFFLINE -> WorkspacePreviewPresentation(
        title = "Provider connection unavailable",
        message = "This workspace does not make a network request. Local project summaries remain available.",
        tone = IvaiStateTone.WARNING,
        actionLabel = "Open Chat"
    )
    UiPreviewState.ERROR -> WorkspacePreviewPresentation(
        title = "Workspace summary unavailable",
        message = "No project data was changed. You can continue with an existing chat or review local data in Settings.",
        tone = IvaiStateTone.ERROR,
        actionLabel = "Open Chat"
    )
}
