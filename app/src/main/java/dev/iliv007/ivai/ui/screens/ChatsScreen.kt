package dev.iliv007.ivai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.iliv007.ivai.ui.components.BidiMessageBubble
import dev.iliv007.ivai.ui.components.IvaiExecutionState
import dev.iliv007.ivai.ui.components.IvaiExecutionStatusBanner
import dev.iliv007.ivai.ui.components.IvaiScreenScaffold
import dev.iliv007.ivai.ui.components.IvaiStateCard
import dev.iliv007.ivai.ui.components.IvaiStateTone
import dev.iliv007.ivai.ui.components.IvaiTargetChip
import dev.iliv007.ivai.ui.model.ChatMessage
import dev.iliv007.ivai.ui.model.ChatThread
import dev.iliv007.ivai.ui.model.MessageSender
import dev.iliv007.ivai.ui.model.UiPreviewState
import dev.iliv007.ivai.ui.model.WorkspaceProject
import dev.iliv007.ivai.ui.theme.IvaiElevationTokens
import dev.iliv007.ivai.ui.theme.IvaiIconSizeTokens
import dev.iliv007.ivai.ui.theme.IvaiLayoutTokens
import dev.iliv007.ivai.ui.theme.IvaiShapeTokens
import dev.iliv007.ivai.ui.theme.IvaiSpacing
import dev.iliv007.ivai.ui.theme.IvaiStrokeTokens
import dev.iliv007.ivai.ui.viewmodel.ProviderManagementState
import dev.iliv007.ivai.ui.viewmodel.RouterComboCard
import dev.iliv007.ivai.ui.viewmodel.RouterManagementState
import kotlinx.coroutines.launch

private const val NoExecutionTarget = "No execution target selected"

/**
 * Phase 7.1 Chat Foundation. It reorders existing state and callbacks into a target-first flow;
 * it does not alter persistence, target resolution, streaming, provider routing or model rules.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    previewState: UiPreviewState,
    onResetState: () -> Unit,
    modifier: Modifier = Modifier,
    threads: List<ChatThread> = emptyList(),
    selectedThreadId: String = threads.firstOrNull()?.id ?: "",
    onSelectThread: (String) -> Unit = {},
    projects: List<WorkspaceProject> = emptyList(),
    selectedProjectId: String? = null,
    onSelectProject: (String?) -> Unit = {},
    onNewChatInProject: (String?) -> Unit = {},
    onAssignThreadToProject: (String, String?) -> Unit = { _, _ -> },
    onCreateNewProject: (String, String) -> WorkspaceProject = { name, description ->
        WorkspaceProject("preview-project", name, description, 0, "Local")
    },
    onUpdateThreadMessages: (String, List<ChatMessage>) -> Unit = { _, _ -> },
    onSendMessage: (String, String) -> Unit = { _, _ -> },
    isStreaming: Boolean = false,
    onStopStreaming: () -> Unit = {},
    routerManagementState: RouterManagementState = RouterManagementState(),
    providerManagementState: ProviderManagementState = ProviderManagementState(),
    onSelectComboTarget: (String, String, String) -> Unit = { _, _, _ -> },
    onSelectDirectTarget: (String, String, String, String, String) -> Unit = { _, _, _, _, _ -> },
    onOpenConnections: () -> Unit = {}
) {
    val currentThread = threads.find { it.id == selectedThreadId } ?: threads.firstOrNull() ?: ChatThread(
        id = "chat-fallback",
        title = "New conversation",
        snippet = "No messages yet",
        timestamp = "Just now",
        modelOrCombo = NoExecutionTarget,
        messages = emptyList(),
        projectId = null,
        projectName = null
    )
    val hasPersistedThread = threads.any { it.id == currentThread.id }
    val hasTarget = currentThread.modelOrCombo != NoExecutionTarget
    val hasCombo = routerManagementState.combos.any { it.enabled }
    val hasDirectModel = providerManagementState.connections.any { connection ->
        connection.enabled && connection.accounts.any { it.enabled && it.credentialStored } &&
            connection.manualModels.any { it.selectable }
    }
    val hasAvailableTarget = hasCombo || hasDirectModel

    var messages by remember(currentThread.id, currentThread.messages) { mutableStateOf(currentThread.messages) }
    var inputText by remember(currentThread.id) { mutableStateOf("") }
    var showTargetSheet by remember { mutableStateOf(false) }
    var showProjectSheet by remember { mutableStateOf(false) }
    var showCreateProjectDialog by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }
    var newProjectDescription by remember { mutableStateOf("") }
    var selectedTargetLabel by remember(currentThread.id) { mutableStateOf(currentThread.modelOrCombo) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isScrolledUp by remember {
        derivedStateOf {
            val total = listState.layoutInfo.totalItemsCount
            total > 1 && (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) < total - 1
        }
    }

    LaunchedEffect(currentThread.id, messages.size) {
        if (messages.isNotEmpty()) listState.scrollToItem(messages.lastIndex)
    }

    when (previewState) {
        UiPreviewState.LOADING -> {
            ChatPreviewState(title = "Preparing conversation", detail = "Waiting for local workspace state.", tone = IvaiStateTone.INFO, onDismiss = onResetState)
            return
        }
        UiPreviewState.EMPTY -> {
            ChatPreviewState(title = "No messages in this conversation", detail = "Choose a target and send a test prompt when ready.", tone = IvaiStateTone.NEUTRAL, onDismiss = onResetState)
            return
        }
        UiPreviewState.ERROR -> {
            ChatPreviewState(title = "Provider stream interrupted", detail = "The target remains selected. Review the route and try again when ready.", tone = IvaiStateTone.ERROR, onDismiss = onResetState)
            return
        }
        UiPreviewState.OFFLINE -> {
            ChatPreviewState(title = "Offline mode active", detail = "Your local chats and project records remain available on this device.", tone = IvaiStateTone.WARNING, onDismiss = onResetState)
            return
        }
        UiPreviewState.NORMAL -> Unit
    }

    IvaiScreenScaffold(modifier = modifier, testTag = "chat_foundation_screen") {
        Column(modifier = Modifier.fillMaxSize()) {
            ChatContextBar(
                targetLabel = selectedTargetLabel,
                targetAvailable = hasTarget,
                hasPersistedThread = hasPersistedThread,
                projectName = currentThread.projectName,
                onTargetClick = {
                    if (hasPersistedThread) showTargetSheet = true else onNewChatInProject(selectedProjectId)
                },
                onProjectClick = {
                    if (hasPersistedThread) showProjectSheet = true else onNewChatInProject(selectedProjectId)
                }
            )

            if (isStreaming || !hasTarget) {
                IvaiExecutionStatusBanner(
                    state = when {
                        isStreaming -> IvaiExecutionState.STREAMING
                        hasPersistedThread && hasAvailableTarget -> IvaiExecutionState.READY
                        else -> IvaiExecutionState.READY
                    },
                    targetLabel = if (hasTarget) selectedTargetLabel else "No execution target",
                    detail = when {
                        isStreaming -> "Streaming through the selected user-managed target."
                        !hasPersistedThread -> "Create a chat before choosing a target."
                        !hasAvailableTarget -> "Add a user-managed connection before choosing a target."
                        else -> "Choose a model or Combo for this chat before sending."
                    },
                    announceChange = isStreaming,
                    modifier = Modifier.padding(horizontal = IvaiSpacing.Small, vertical = IvaiSpacing.XSmall),
                    testTag = "chat_execution_status"
                )
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (messages.isEmpty()) {
                    ChatOnboardingState(
                        hasPersistedThread = hasPersistedThread,
                        hasAvailableTarget = hasAvailableTarget,
                        hasTarget = hasTarget,
                        projectName = currentThread.projectName,
                        onCreateChat = { onNewChatInProject(selectedProjectId) },
                        onOpenConnections = onOpenConnections,
                        onChooseTarget = { showTargetSheet = true }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(IvaiSpacing.XSmall),
                        verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XxxSmall),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("chat_messages_list")
                    ) {
                        items(messages, key = { it.id }) { message ->
                            BidiMessageBubble(
                                message = message,
                                onDeleteMessage = { deleted ->
                                    val updated = messages.filterNot { it.id == deleted.id }
                                    messages = updated
                                    onUpdateThreadMessages(currentThread.id, updated)
                                }
                            )
                        }
                    }
                }
                if (isScrolledUp) {
                    FloatingActionButton(
                        onClick = { scope.launch { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex) } },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(IvaiSpacing.Small)
                            .testTag("fab_scroll_to_bottom")
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Scroll to latest message")
                    }
                }
            }

            ChatComposer(
                value = inputText,
                onValueChange = { inputText = it },
                canSendToTarget = hasPersistedThread && hasTarget,
                isStreaming = isStreaming,
                onSend = {
                    if (inputText.isBlank() || !hasPersistedThread || !hasTarget) return@ChatComposer
                    onSendMessage(currentThread.id, inputText.trim())
                    inputText = ""
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
                onStop = onStopStreaming,
                onResolveTarget = {
                    when {
                        !hasPersistedThread -> onNewChatInProject(selectedProjectId)
                        hasAvailableTarget -> showTargetSheet = true
                        else -> onOpenConnections()
                    }
                }
            )
        }
    }

    if (showProjectSheet && hasPersistedThread) {
        ProjectAssignmentSheet(
            currentThread = currentThread,
            projects = projects,
            onDismiss = { showProjectSheet = false },
            onAssign = { projectId ->
                onAssignThreadToProject(currentThread.id, projectId)
                showProjectSheet = false
            },
            onCreateProject = {
                newProjectName = ""
                newProjectDescription = ""
                showCreateProjectDialog = true
            }
        )
    }

    if (showTargetSheet && hasPersistedThread) {
        TargetSelectionSheet(
            selectedTargetLabel = selectedTargetLabel,
            combos = routerManagementState.combos.filter { it.enabled },
            providerManagementState = providerManagementState,
            onDismiss = { showTargetSheet = false },
            onSelectCombo = { comboId, displayLabel ->
                selectedTargetLabel = displayLabel
                onSelectComboTarget(currentThread.id, comboId, displayLabel)
                showTargetSheet = false
            },
            onSelectDirect = { connectionId, accountId, modelId, displayLabel ->
                selectedTargetLabel = displayLabel
                onSelectDirectTarget(currentThread.id, connectionId, accountId, modelId, displayLabel)
                showTargetSheet = false
            },
            onOpenConnections = onOpenConnections
        )
    }

    if (showCreateProjectDialog && hasPersistedThread) {
        AlertDialog(
            onDismissRequest = { showCreateProjectDialog = false },
            title = { Text("Create workspace project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)) {
                    Text("Projects organize local chats and files without changing your selected target.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = newProjectName,
                        onValueChange = { newProjectName = it },
                        label = { Text("Project name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_create_project_name")
                    )
                    OutlinedTextField(
                        value = newProjectDescription,
                        onValueChange = { newProjectDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth().testTag("input_create_project_desc")
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = newProjectName.isNotBlank(),
                    onClick = {
                        val project = onCreateNewProject(newProjectName.trim(), newProjectDescription.trim())
                        onAssignThreadToProject(currentThread.id, project.id)
                        onSelectProject(project.id)
                        showCreateProjectDialog = false
                        showProjectSheet = false
                    },
                    modifier = Modifier.testTag("button_confirm_create_project")
                ) { Text("Create and assign") }
            },
            dismissButton = { TextButton(onClick = { showCreateProjectDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ChatContextBar(
    targetLabel: String,
    targetAvailable: Boolean,
    hasPersistedThread: Boolean,
    projectName: String?,
    onTargetClick: () -> Unit,
    onProjectClick: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = IvaiSpacing.Small, vertical = IvaiSpacing.XSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
        ) {
            IvaiTargetChip(
                label = if (targetAvailable) targetLabel else "Choose execution target",
                availabilityLabel = when {
                    !hasPersistedThread -> "Create chat first"
                    targetAvailable -> "Selected for this chat"
                    else -> "Required before sending"
                },
                onClick = onTargetClick,
                leadingIcon = Icons.AutoMirrored.Filled.AltRoute,
                modifier = Modifier.weight(1f),
                testTag = "button_select_combo"
            )
            IconButton(
                onClick = onProjectClick,
                modifier = Modifier
                    .size(IvaiLayoutTokens.MinimumTouchTarget)
                    .semantics { contentDescription = "Assign chat to project" }
                    .testTag("button_assign_project")
            ) {
                Icon(
                    imageVector = if (projectName == null) Icons.Default.FolderOpen else Icons.Default.Folder,
                    contentDescription = null,
                    tint = if (projectName == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun ChatOnboardingState(
    hasPersistedThread: Boolean,
    hasAvailableTarget: Boolean,
    hasTarget: Boolean,
    projectName: String?,
    onCreateChat: () -> Unit,
    onOpenConnections: () -> Unit,
    onChooseTarget: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().padding(IvaiSpacing.Medium), contentAlignment = Alignment.Center) {
        when {
            !hasPersistedThread -> IvaiStateCard(
                title = "Start a local conversation",
                message = "Create a chat first. Then choose the user-managed model or Combo that should handle it.",
                tone = IvaiStateTone.INFO,
                icon = Icons.Default.Add,
                action = {
                    Button(onClick = onCreateChat, modifier = Modifier.testTag("chat_onboarding_create")) { Text("Create chat") }
                },
                testTag = "chat_onboarding_no_thread"
            )
            !hasAvailableTarget -> IvaiStateCard(
                title = "Add a connection before sending",
                message = "IVAI has no user-managed model or Combo available for this chat yet. Connections stay under your control.",
                tone = IvaiStateTone.WARNING,
                icon = Icons.Default.FolderOpen,
                action = {
                    Button(onClick = onOpenConnections, modifier = Modifier.testTag("chat_onboarding_open_connections")) { Text("Open connections") }
                },
                testTag = "chat_onboarding_no_connection"
            )
            !hasTarget -> IvaiStateCard(
                title = "Choose an execution target",
                message = "Select a direct model or an ordered Combo explicitly before the first message is sent.",
                tone = IvaiStateTone.INFO,
                icon = Icons.AutoMirrored.Filled.AltRoute,
                action = {
                    Button(onClick = onChooseTarget, modifier = Modifier.testTag("chat_onboarding_choose_target")) { Text("Choose target") }
                },
                testTag = "chat_onboarding_no_target"
            )
            else -> IvaiStateCard(
                title = "Ready for this conversation",
                message = projectName?.let { "This chat belongs to $it. Your selected target is shown above." }
                    ?: "Your selected target is shown above. Add a project only when it helps organize the work.",
                tone = IvaiStateTone.SUCCESS,
                icon = Icons.AutoMirrored.Filled.Send,
                testTag = "chat_onboarding_ready"
            )
        }
    }
}

@Composable
private fun ChatComposer(
    value: String,
    onValueChange: (String) -> Unit,
    canSendToTarget: Boolean,
    isStreaming: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onResolveTarget: () -> Unit
) {
    val hasDraft = value.isNotBlank()
    val canSend = hasDraft && canSendToTarget && !isStreaming
    val canAct = isStreaming || hasDraft
    Surface(
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = androidx.compose.foundation.BorderStroke(IvaiStrokeTokens.Default, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = IvaiSpacing.XSmall, vertical = IvaiSpacing.XxSmall)
            .testTag("chat_composer")
    ) {
        Row(
            modifier = Modifier.padding(IvaiSpacing.XSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = IvaiLayoutTokens.MinimumTouchTarget, max = 144.dp)
                    .padding(horizontal = IvaiSpacing.XSmall, vertical = IvaiSpacing.XxSmall)
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = !isStreaming,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send, capitalization = KeyboardCapitalization.Sentences),
                    keyboardActions = KeyboardActions(onSend = {
                        if (canSend) onSend() else if (hasDraft) onResolveTarget()
                    }),
                    minLines = 1,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_message_text"),
                    decorationBox = { input ->
                        if (value.isBlank()) {
                            Text(
                                text = if (isStreaming) "Streaming response…" else "Do anything…",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        input()
                    }
                )
            }
            Button(
                onClick = {
                    when {
                        isStreaming -> onStop()
                        canSend -> onSend()
                        hasDraft -> onResolveTarget()
                    }
                },
                enabled = canAct,
                shape = if (isStreaming) RoundedCornerShape(IvaiShapeTokens.Control) else CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isStreaming) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    contentColor = if (isStreaming) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = (if (isStreaming) {
                    Modifier
                        .width(76.dp)
                        .heightIn(min = IvaiLayoutTokens.MinimumTouchTarget)
                } else {
                    Modifier.size(IvaiLayoutTokens.MinimumTouchTarget)
                })
                    .semantics {
                        contentDescription = when {
                            isStreaming -> "Stop streaming"
                            canSendToTarget -> "Send message"
                            else -> "Choose target before sending"
                        }
                    }
                    .testTag("button_send_message")
            ) {
                if (isStreaming) {
                    Text("Stop", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(IvaiIconSizeTokens.Inline)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatPreviewState(title: String, detail: String, tone: IvaiStateTone, onDismiss: () -> Unit) {
    IvaiScreenScaffold(testTag = "chat_preview_state") {
        Box(modifier = Modifier.fillMaxSize().padding(IvaiSpacing.Medium), contentAlignment = Alignment.Center) {
            IvaiStateCard(
                title = title,
                message = detail,
                tone = tone,
                action = { TextButton(onClick = onDismiss) { Text("Return to chat") } }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectAssignmentSheet(
    currentThread: ChatThread,
    projects: List<WorkspaceProject>,
    onDismiss: () -> Unit,
    onAssign: (String?) -> Unit,
    onCreateProject: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.padding(IvaiSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Assign chat to project", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Projects organize local work; they do not alter the selected execution target.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(onClick = onCreateProject, modifier = Modifier.testTag("button_add_new_project")) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(IvaiIconSizeTokens.Inline))
                    Spacer(Modifier.width(IvaiSpacing.XxxSmall))
                    Text("Project")
                }
            }
            ProjectOption(
                title = "No project",
                subtitle = "Keep this as a standalone conversation",
                selected = currentThread.projectId == null,
                onClick = { onAssign(null) },
                testTag = "project_option_none"
            )
            projects.forEach { project ->
                ProjectOption(
                    title = project.name,
                    subtitle = project.description,
                    selected = project.id == currentThread.projectId,
                    onClick = { onAssign(project.id) },
                    testTag = "project_option_${project.id}"
                )
            }
        }
    }
}

@Composable
private fun ProjectOption(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit, testTag: String) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
        border = androidx.compose.foundation.BorderStroke(
            IvaiStrokeTokens.Default,
            if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth().testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(IvaiSpacing.XSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
        ) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TargetSelectionSheet(
    selectedTargetLabel: String,
    combos: List<RouterComboCard>,
    providerManagementState: ProviderManagementState,
    onDismiss: () -> Unit,
    onSelectCombo: (String, String) -> Unit,
    onSelectDirect: (String, String, String, String) -> Unit,
    onOpenConnections: () -> Unit
) {
    val directOptions = providerManagementState.connections.filter { it.enabled }.flatMap { connection ->
        connection.accounts.filter { it.enabled && it.credentialStored }.flatMap { account ->
            connection.manualModels.filter { it.selectable }.map { model ->
                DirectTargetOption(
                    connectionId = connection.connectionId,
                    accountId = account.accountId,
                    modelId = model.registryModelId,
                    label = "${connection.displayName} · ${model.displayName}",
                    detail = account.displayName
                )
            }
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("target_selection_sheet")
    ) {
        Column(
            modifier = Modifier.padding(IvaiSpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
        ) {
            Text("Choose execution target", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Targets are explicit per chat. A Combo tries only its user-defined ordered candidates.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (combos.isEmpty() && directOptions.isEmpty()) {
                IvaiStateCard(
                    title = "No target is available",
                    message = "Add a user-managed provider connection, account and model before selecting a target.",
                    tone = IvaiStateTone.WARNING,
                    action = { Button(onClick = onOpenConnections) { Text("Open connections") } }
                )
            }
            if (combos.isNotEmpty()) {
                Text("Your Combos", style = MaterialTheme.typography.labelLarge)
                combos.forEach { combo ->
                    TargetOption(
                        label = combo.displayName,
                        detail = "${combo.entries.size} ordered candidates",
                        selected = combo.displayName == selectedTargetLabel,
                        onClick = { onSelectCombo(combo.comboId, combo.displayName) },
                        testTag = "combo_option_${combo.comboId}"
                    )
                }
            }
            if (directOptions.isNotEmpty()) {
                Text("Your direct provider models", style = MaterialTheme.typography.labelLarge)
                directOptions.forEach { option ->
                    TargetOption(
                        label = option.label,
                        detail = option.detail,
                        selected = option.label == selectedTargetLabel,
                        onClick = { onSelectDirect(option.connectionId, option.accountId, option.modelId, option.label) },
                        testTag = "direct_model_option_${option.modelId}"
                    )
                }
            }
        }
    }
}

private data class DirectTargetOption(
    val connectionId: String,
    val accountId: String,
    val modelId: String,
    val label: String,
    val detail: String
)

@Composable
private fun TargetOption(label: String, detail: String, selected: Boolean, onClick: () -> Unit, testTag: String) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        border = androidx.compose.foundation.BorderStroke(
            IvaiStrokeTokens.Default,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth().testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(IvaiSpacing.XSmall), verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XxxSmall)) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
