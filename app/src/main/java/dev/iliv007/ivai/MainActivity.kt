package dev.iliv007.ivai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.iliv007.ivai.ui.components.IvaiProductSidebar
import dev.iliv007.ivai.ui.components.IvaiAdaptiveDestinationScaffold
import dev.iliv007.ivai.ui.components.IvaiTopBar
import dev.iliv007.ivai.ui.navigation.NavDestination
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
 * Adaptive Phase 7 shell. The destination model, local workspace state and all runtime callbacks
 * remain owned by [WorkspaceViewModel]. The drawer is deliberately Chat-only: history, search and
 * project filtering are no longer global navigation concerns.
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
    val sidebarState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun closeSidebar() {
        scope.launch { sidebarState.close() }
    }

    @Composable
    fun DestinationContent(contentModifier: Modifier) {
        Box(
            modifier = contentModifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (uiState.destination) {
                NavDestination.CHATS -> ChatsScreen(
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
                    onSelectDirectTarget = resolvedViewModel::selectDirectTarget,
                    onOpenConnections = { resolvedViewModel.selectDestination(NavDestination.ROUTER) },
                    modifier = Modifier.fillMaxSize()
                )
                NavDestination.AGENTS -> AgentsScreen(
                    state = agentManagementState,
                    onCreateAgent = resolvedViewModel::createAgent,
                    onStartRun = resolvedViewModel::startAgentRun,
                    onSelectRun = resolvedViewModel::selectAgentRun,
                    onCancelRun = resolvedViewModel::cancelAgentRun,
                    onResolveApproval = resolvedViewModel::resolveAgentApproval,
                    onDismissError = resolvedViewModel::clearAgentOperationError,
                    onOpenConnections = { resolvedViewModel.selectDestination(NavDestination.ROUTER) }
                )
                NavDestination.PROJECTS -> ProjectsScreen(
                    projects = uiState.projects,
                    selectedProjectId = uiState.selectedProjectId,
                    previewState = uiState.previewState,
                    onSelectProject = resolvedViewModel::selectProject,
                    onStartProjectChat = resolvedViewModel::createNewChat,
                    onOpenChats = { resolvedViewModel.selectDestination(NavDestination.CHATS) },
                    onOpenAgents = { resolvedViewModel.selectDestination(NavDestination.AGENTS) }
                )
                NavDestination.ROUTER -> RouterScreen(
                    state = routerManagementState,
                    providers = providerManagementState,
                    onAddProvider = resolvedViewModel::addProviderConnection,
                    onDeleteProvider = resolvedViewModel::deleteProviderConnection,
                    onAddAccountToConnection = resolvedViewModel::addAccountToConnection,
                    onAddModelToConnection = resolvedViewModel::addModelToConnection,
                    onSetProviderEnabled = resolvedViewModel::setProviderConnectionEnabled,
                    onDismissProviderError = resolvedViewModel::clearProviderOperationError,
                    onCreateCombo = resolvedViewModel::createRouterCombo,
                    onDismissError = resolvedViewModel::clearRouterOperationError
                )
                NavDestination.SETTINGS -> SettingsScreen(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    onDeleteAllLocalData = resolvedViewModel::deleteAllLocalData,
                    onOpenConnections = { resolvedViewModel.selectDestination(NavDestination.ROUTER) }
                )
            }
        }
    }

    @Composable
    fun AppShell() {
        IvaiAdaptiveDestinationScaffold(
            currentDestination = uiState.destination,
            onDestinationSelected = resolvedViewModel::selectDestination,
            topBar = {
                IvaiTopBar(
                    title = "IVAI",
                    subtitle = uiState.destination.title,
                    onOpenSidebar = {
                        scope.launch {
                            if (sidebarState.isClosed) sidebarState.open() else sidebarState.close()
                        }
                    }
                )
            }
        ) { contentModifier, _ ->
            DestinationContent(contentModifier)
        }
    }

    ModalNavigationDrawer(
        drawerState = sidebarState,
        gesturesEnabled = true,
        drawerContent = {
            IvaiProductSidebar(
                currentDestination = uiState.destination,
                onDestinationSelected = { destination ->
                    resolvedViewModel.selectDestination(destination)
                    closeSidebar()
                },
                threads = uiState.threads,
                selectedThreadId = uiState.selectedThreadId,
                projects = uiState.projects,
                selectedProjectId = uiState.selectedProjectId,
                onSelectThread = { threadId ->
                    resolvedViewModel.selectThread(threadId)
                    closeSidebar()
                },
                onSelectProject = resolvedViewModel::selectProject,
                onNewChat = {
                    resolvedViewModel.createNewChat()
                    closeSidebar()
                },
                onDeleteThread = resolvedViewModel::deleteThread
            )
        }
    ) {
        AppShell()
    }
}
