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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    workspaceViewModel: WorkspaceViewModel = viewModel()
) {
    val uiState by workspaceViewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun closeDrawer() {
        scope.launch { drawerState.close() }
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
                        workspaceViewModel.selectDestination(destination)
                        closeDrawer()
                    },
                    threads = uiState.threads,
                    selectedThreadId = uiState.selectedThreadId,
                    onSelectThread = { threadId ->
                        workspaceViewModel.selectThread(threadId)
                        closeDrawer()
                    },
                    projects = uiState.projects,
                    selectedProjectId = uiState.selectedProjectId,
                    onSelectProject = workspaceViewModel::selectProject,
                    onNewChatClick = {
                        workspaceViewModel.createNewChat()
                        closeDrawer()
                    },
                    onDeleteThread = workspaceViewModel::deleteThread,
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
                        onStateSelected = workspaceViewModel::selectPreviewState,
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
                            onResetState = workspaceViewModel::resetPreviewState,
                            threads = uiState.threads,
                            selectedThreadId = uiState.selectedThreadId,
                            onSelectThread = workspaceViewModel::selectThread,
                            projects = uiState.projects,
                            selectedProjectId = uiState.selectedProjectId,
                            onSelectProject = workspaceViewModel::selectProject,
                            onNewChatInProject = workspaceViewModel::createNewChat,
                            onAssignThreadToProject = workspaceViewModel::assignThreadToProject,
                            onCreateNewProject = workspaceViewModel::createNewProject,
                            onUpdateThreadMessages = workspaceViewModel::updateThreadMessages
                        )
                        dev.iliv007.ivai.ui.navigation.NavDestination.AGENTS -> AgentsScreen()
                        dev.iliv007.ivai.ui.navigation.NavDestination.PROJECTS -> ProjectsScreen()
                        dev.iliv007.ivai.ui.navigation.NavDestination.ROUTER -> RouterScreen()
                        dev.iliv007.ivai.ui.navigation.NavDestination.SETTINGS -> SettingsScreen(
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = onToggleTheme
                        )
                    }
                }
            }
        }
    }
}
