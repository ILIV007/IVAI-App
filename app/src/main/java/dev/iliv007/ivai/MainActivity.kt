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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import dev.iliv007.ivai.ui.components.IvaiSidebarContent
import dev.iliv007.ivai.ui.components.IvaiTopBar
import dev.iliv007.ivai.ui.model.ChatMessage
import dev.iliv007.ivai.ui.model.ChatThread
import dev.iliv007.ivai.ui.model.MockDataRepository
import dev.iliv007.ivai.ui.model.UiPreviewState
import dev.iliv007.ivai.ui.model.WorkspaceProject
import dev.iliv007.ivai.ui.navigation.NavDestination
import dev.iliv007.ivai.ui.screens.AgentsScreen
import dev.iliv007.ivai.ui.screens.ChatsScreen
import dev.iliv007.ivai.ui.screens.ProjectsScreen
import dev.iliv007.ivai.ui.screens.RouterScreen
import dev.iliv007.ivai.ui.screens.SettingsScreen
import dev.iliv007.ivai.ui.theme.IvaiTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) } // Default Theme = LIGHT

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

@Composable
fun IvaiMainApp(
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    var currentDestination by remember { mutableStateOf(NavDestination.CHATS) }
    var previewState by remember { mutableStateOf(UiPreviewState.NORMAL) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Central state for Chat Threads and Workspace Projects
    var threads by remember { mutableStateOf(MockDataRepository.defaultChatThreads) }
    var projects by remember { mutableStateOf(MockDataRepository.mockProjects) }
    var selectedThreadId by remember { mutableStateOf(threads.firstOrNull()?.id ?: "") }
    var selectedProjectId by remember { mutableStateOf<String?>(null) }

    fun createNewChat(targetProjectId: String? = selectedProjectId) {
        val assignedProject = projects.find { it.id == targetProjectId }
        val newThreadId = "chat-${System.currentTimeMillis()}"
        val newThread = ChatThread(
            id = newThreadId,
            title = if (assignedProject != null) "New ${assignedProject.name} Chat" else "New Conversation",
            snippet = "No messages yet",
            timestamp = "Just now",
            modelOrCombo = "Gemini Flash Combo",
            messages = emptyList(),
            projectId = assignedProject?.id,
            projectName = assignedProject?.name
        )
        threads = listOf(newThread) + threads
        selectedThreadId = newThreadId
        currentDestination = NavDestination.CHATS
    }

    fun deleteThread(threadId: String) {
        val remaining = threads.filterNot { it.id == threadId }
        threads = remaining
        if (selectedThreadId == threadId) {
            selectedThreadId = remaining.firstOrNull()?.id ?: ""
        }
    }

    fun assignChatToProject(threadId: String, projectId: String?) {
        val targetProject = projects.find { it.id == projectId }
        threads = threads.map { thread ->
            if (thread.id == threadId) {
                thread.copy(
                    projectId = targetProject?.id,
                    projectName = targetProject?.name
                )
            } else thread
        }
    }

    fun createNewProject(name: String, description: String): WorkspaceProject {
        val newProject = WorkspaceProject(
            id = "proj-${System.currentTimeMillis()}",
            name = name.ifBlank { "Untitled Project" },
            description = description.ifBlank { "Local workspace project" },
            fileCount = 0,
            lastModified = "Just now"
        )
        projects = projects + newProject
        return newProject
    }

    // Explicitly lock app shell chrome to LTR as required by IVAI Alpha specification
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerContent = {
                IvaiSidebarContent(
                    currentDestination = currentDestination,
                    onDestinationSelected = { destination ->
                        currentDestination = destination
                        scope.launch { drawerState.close() }
                    },
                    threads = threads,
                    selectedThreadId = selectedThreadId,
                    onSelectThread = { threadId ->
                        selectedThreadId = threadId
                        currentDestination = NavDestination.CHATS
                        scope.launch { drawerState.close() }
                    },
                    projects = projects,
                    selectedProjectId = selectedProjectId,
                    onSelectProject = { projId ->
                        selectedProjectId = projId
                    },
                    onNewChatClick = {
                        createNewChat()
                        scope.launch { drawerState.close() }
                    },
                    onDeleteThread = { threadId ->
                        deleteThread(threadId)
                    },
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
                        subtitle = currentDestination.title,
                        currentState = previewState,
                        onStateSelected = { previewState = it },
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
                    when (currentDestination) {
                        NavDestination.CHATS -> ChatsScreen(
                            previewState = previewState,
                            onResetState = { previewState = UiPreviewState.NORMAL },
                            threads = threads,
                            selectedThreadId = selectedThreadId,
                            onSelectThread = { selectedThreadId = it },
                            projects = projects,
                            selectedProjectId = selectedProjectId,
                            onSelectProject = { selectedProjectId = it },
                            onNewChatInProject = { projId -> createNewChat(projId) },
                            onAssignThreadToProject = { tId, pId -> assignChatToProject(tId, pId) },
                            onCreateNewProject = { name, desc -> createNewProject(name, desc) },
                            onUpdateThreadMessages = { threadId, updatedMessages ->
                                threads = threads.map { t ->
                                    if (t.id == threadId) {
                                        val latestSnippet = updatedMessages.lastOrNull()?.text ?: t.snippet
                                        t.copy(messages = updatedMessages, snippet = latestSnippet)
                                    } else t
                                }
                            }
                        )
                        NavDestination.AGENTS -> AgentsScreen()
                        NavDestination.PROJECTS -> ProjectsScreen()
                        NavDestination.ROUTER -> RouterScreen()
                        NavDestination.SETTINGS -> SettingsScreen(
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = onToggleTheme
                        )
                    }
                }
            }
        }
    }
}


