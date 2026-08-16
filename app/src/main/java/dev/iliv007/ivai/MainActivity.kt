package dev.iliv007.ivai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.iliv007.ivai.ui.components.IvaiSidebarContent
import dev.iliv007.ivai.ui.components.IvaiTopBar
import dev.iliv007.ivai.ui.screens.AgentsScreen
import dev.iliv007.ivai.ui.screens.ChatsScreen
import dev.iliv007.ivai.ui.screens.ProjectsScreen
import dev.iliv007.ivai.ui.screens.RouterScreen
import dev.iliv007.ivai.ui.screens.SettingsScreen
import dev.iliv007.ivai.ui.theme.IvaiTheme
import dev.iliv007.ivai.ui.viewmodel.WorkspaceViewModel
import dev.iliv007.ivai.ui.viewmodel.WorkspaceViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }

            IvaiTheme(
                darkTheme = isDarkTheme,
                onToggleTheme = { isDarkTheme = !isDarkTheme }
            ) {
                IvaiMainApp(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}

/**
 * Phase 1 app shell. All mutable workspace state comes from [WorkspaceViewModel].
 */
@Composable
fun IvaiMainApp(
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    workspaceViewModel: WorkspaceViewModel? = null
) {
    val context = LocalContext.current
    val runtime = remember(context) { IvaiRuntime(context) }
    val resolvedViewModel = workspaceViewModel ?: viewModel(
        factory = remember(runtime) { WorkspaceViewModelFactory(runtime) }
    )
    val uiState by resolvedViewModel.uiState.collectAsStateWithLifecycle()
    val providerManagementState by resolvedViewModel.providerManagementState.collectAsStateWithLifecycle()
    val routerManagementState by resolvedViewModel.routerManagementState.collectAsStateWithLifecycle()
    val agentManagementState by resolvedViewModel.agentManagementState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun closeDrawer() {
        scope.launch { drawerState.close() }
    }

    val enabledComboCount = routerManagementState.combos.count { it.enabled }
    val enabledConnectionCount = providerManagementState.connections.count { it.enabled }
    val executionTargetAvailable = enabledComboCount > 0 || enabledConnectionCount > 0
    val executionStatusLabel = when {
        enabledComboCount > 0 -> "$enabledComboCount local Combo${if (enabledComboCount == 1) "" else "s"} ready"
        enabledConnectionCount > 0 -> "$enabledConnectionCount local provider connection${if (enabledConnectionCount == 1) "" else "s"} ready"
        else -> "No local execution target selected"
    }
    val executionStatusDetail = routerManagementState.latestAttempts.firstOrNull()?.let { attempt ->
        "Last router attempt: ${attempt.outcome.name}"
    } ?: if (executionTargetAvailable) {
        "Choose a model or Combo for each chat."
    } else {
        "Configure Provider or Router to begin."
    }

    // The app chrome is deliberately LTR in Alpha. Individual message components
    // resolve BiDi direction from their own content.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerContent = {
                IvaiSidebarContent(
                    currentDestination = uiState.destination,
                    onDestinationSelected = { destination ->
                        resolvedViewModel.selectDestination(destination)
                        closeDrawer()
                    },
                    threads = uiState.threads,
                    selectedThreadId = uiState.selectedThreadId,
                    onSelectThread = { threadId ->
                        resolvedViewModel.selectThread(threadId)
                        closeDrawer()
                    },
                    projects = uiState.projects,
                    selectedProjectId = uiState.selectedProjectId,
                    onSelectProject = resolvedViewModel::selectProject,
                    onNewChatClick = {
                        resolvedViewModel.createNewChat()
                        closeDrawer()
                    },
                    onDeleteThread = resolvedViewModel::deleteThread,
                    executionStatusLabel = executionStatusLabel,
                    executionStatusDetail = executionStatusDetail,
                    executionTargetAvailable = executionTargetAvailable,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme
                )
            }
        ) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                topBar = {
                    IvaiTopBar(
                        title = "IVAI",
                        subtitle = uiState.destination.title,
                        currentState = uiState.previewState,
                        onStateSelected = resolvedViewModel::selectPreviewState,
                        onOpenSidebar = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        },
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    when (uiState.destination) {
                        dev.iliv007.ivai.ui.navigation.NavDestination.CHATS -> ChatsScreen(
                            previewState = uiState.previewState,
                            onResetState = resolvedViewModel::resetPreviewState,
                            threads = uiState.threads,
                            selectedThreadId = uiState.selectedThreadId,
                            onSelectThread = resolvedViewModel::selectThread,
                            projects = uiState.projects,
                            selectedProjectId = uiState.selectedProjectId,
                            onSelectProject = resolvedViewModel::selectProject,
                            onNewChatInProject = resolvedViewModel::createNewChat,
                            onAssignThreadToProject = resolvedViewModel::assignThreadToProject,
                            onCreateNewProject = resolvedViewModel::createNewProject,
                            onUpdateThreadMessages = resolvedViewModel::updateThreadMessages,
                            onSendMessage = resolvedViewModel::sendMessage,
                            isStreaming = uiState.isStreaming,
                            onStopStreaming = resolvedViewModel::stopStreaming,
                            routerManagementState = routerManagementState,
                            providerManagementState = providerManagementState,
                            onSelectComboTarget = resolvedViewModel::selectComboTarget,
                            onSelectDirectTarget = resolvedViewModel::selectDirectTarget
                        )
                        dev.iliv007.ivai.ui.navigation.NavDestination.AGENTS -> AgentsScreen(
                            state = agentManagementState,
                            onCreateAgent = resolvedViewModel::createAgent,
                            onStartRun = resolvedViewModel::startAgentRun,
                            onSelectRun = resolvedViewModel::selectAgentRun,
                            onCancelRun = resolvedViewModel::cancelAgentRun,
                            onResolveApproval = resolvedViewModel::resolveAgentApproval,
                            onDismissError = resolvedViewModel::clearAgentOperationError
                        )
                        dev.iliv007.ivai.ui.navigation.NavDestination.PROJECTS -> ProjectsScreen(
                            projects = uiState.projects
                        )
                        dev.iliv007.ivai.ui.navigation.NavDestination.ROUTER -> RouterScreen(
                            state = routerManagementState,
                            providers = providerManagementState,
                            onCreateCombo = resolvedViewModel::createRouterCombo,
                            onDismissError = resolvedViewModel::clearRouterOperationError
                        )
                        dev.iliv007.ivai.ui.navigation.NavDestination.SETTINGS -> SettingsScreen(
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = onToggleTheme,
                            providerManagementState = providerManagementState,
                            onAddProvider = resolvedViewModel::addProviderConnection,
                            onDeleteProvider = resolvedViewModel::deleteProviderConnection,
                            onSetProviderEnabled = resolvedViewModel::setProviderConnectionEnabled,
                            onDismissProviderError = resolvedViewModel::clearProviderOperationError,
                            onDeleteAllLocalData = resolvedViewModel::deleteAllLocalData
                        )
                    }
                }
            }
        }
    }
}
