package dev.iliv007.ivai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.iliv007.ivai.ui.components.IvaiAdaptiveDestinationScaffold
import dev.iliv007.ivai.ui.components.IvaiPersistentProductSidebar
import dev.iliv007.ivai.ui.components.IvaiProductSidebar
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
 * UX-3 product shell. Width changes only replace the sidebar presentation; destination, thread
 * and project state remain owned by [WorkspaceViewModel]. Chat history is a Chat context section,
 * not a second navigation drawer or a sixth route.
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

    @Composable
    fun DestinationContent(contentModifier: Modifier) {
        Box(
            modifier = contentModifier
                .fillMaxSize()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
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

    IvaiAdaptiveDestinationScaffold(
        topBar = { onOpenCompactSidebar ->
            IvaiTopBar(
                title = uiState.destination.title,
                onOpenSidebar = onOpenCompactSidebar,
                onOpenSettings = { resolvedViewModel.selectDestination(NavDestination.SETTINGS) }
            )
        },
        compactSidebar = { dismissSidebar ->
            IvaiProductSidebar(
                currentDestination = uiState.destination,
                onDestinationSelected = { destination ->
                    resolvedViewModel.selectDestination(destination)
                    dismissSidebar()
                },
                threads = uiState.threads,
                selectedThreadId = uiState.selectedThreadId,
                projects = uiState.projects,
                selectedProjectId = uiState.selectedProjectId,
                onSelectThread = { threadId ->
                    resolvedViewModel.selectThread(threadId)
                    dismissSidebar()
                },
                onSelectProject = resolvedViewModel::selectProject,
                onNewChat = {
                    resolvedViewModel.createNewChat()
                    dismissSidebar()
                },
                onDeleteThread = resolvedViewModel::deleteThread
            )
        },
        persistentSidebar = { mode ->
            IvaiPersistentProductSidebar(
                mode = mode,
                currentDestination = uiState.destination,
                onDestinationSelected = resolvedViewModel::selectDestination,
                threads = uiState.threads,
                selectedThreadId = uiState.selectedThreadId,
                projects = uiState.projects,
                selectedProjectId = uiState.selectedProjectId,
                onSelectThread = resolvedViewModel::selectThread,
                onSelectProject = resolvedViewModel::selectProject,
                onNewChat = resolvedViewModel::createNewChat,
                onDeleteThread = resolvedViewModel::deleteThread
            )
        }
    ) { contentModifier, _ ->
        DestinationContent(contentModifier)
    }
}
