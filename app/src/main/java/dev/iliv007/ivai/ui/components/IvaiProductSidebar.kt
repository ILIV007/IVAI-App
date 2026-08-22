package dev.iliv007.ivai.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.iliv007.ivai.ui.model.ChatThread
import dev.iliv007.ivai.ui.model.WorkspaceProject
import dev.iliv007.ivai.ui.navigation.NavDestination
import dev.iliv007.ivai.ui.theme.IvaiLayoutTokens
import dev.iliv007.ivai.ui.theme.IvaiShapeTokens
import dev.iliv007.ivai.ui.theme.IvaiSpacing

/**
 * Compact wrapper for the single product sidebar. The content is deliberately shared with the
 * persistent width variants so no drawer/rail navigation implementation can diverge.
 */
@Composable
fun IvaiProductSidebar(
    currentDestination: NavDestination,
    onDestinationSelected: (NavDestination) -> Unit,
    threads: List<ChatThread>,
    selectedThreadId: String,
    projects: List<WorkspaceProject>,
    selectedProjectId: String?,
    onSelectThread: (String) -> Unit,
    onSelectProject: (String?) -> Unit,
    onNewChat: () -> Unit,
    onDeleteThread: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
            .width(IvaiLayoutTokens.ChatDrawerWidth)
            .fillMaxHeight()
            .testTag("ivai_product_sidebar"),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerShape = androidx.compose.foundation.shape.RoundedCornerShape(
            topEnd = IvaiShapeTokens.Sheet,
            bottomEnd = IvaiShapeTokens.Sheet
        )
    ) {
        IvaiSidebarNavigationContent(
            currentDestination = currentDestination,
            onDestinationSelected = onDestinationSelected,
            threads = threads,
            selectedThreadId = selectedThreadId,
            projects = projects,
            selectedProjectId = selectedProjectId,
            onSelectThread = onSelectThread,
            onSelectProject = onSelectProject,
            onNewChat = onNewChat,
            onDeleteThread = onDeleteThread
        )
    }
}

/** Persistent wrapper used by medium and expanded layouts; it does not create a second nav model. */
@Composable
fun IvaiPersistentProductSidebar(
    mode: IvaiNavigationMode,
    currentDestination: NavDestination,
    onDestinationSelected: (NavDestination) -> Unit,
    threads: List<ChatThread>,
    selectedThreadId: String,
    projects: List<WorkspaceProject>,
    selectedProjectId: String?,
    onSelectThread: (String) -> Unit,
    onSelectProject: (String?) -> Unit,
    onNewChat: () -> Unit,
    onDeleteThread: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val width = when (mode) {
        IvaiNavigationMode.MEDIUM_PERSISTENT -> IvaiLayoutTokens.PersistentSidebarMediumWidth
        IvaiNavigationMode.EXPANDED_PERSISTENT -> IvaiLayoutTokens.PersistentSidebarExpandedWidth
        IvaiNavigationMode.COMPACT_MODAL -> IvaiLayoutTokens.ChatDrawerWidth
    }
    Surface(
        modifier = modifier
            .width(width)
            .fillMaxHeight()
            .testTag("ivai_${mode.name.lowercase()}_sidebar"),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        IvaiSidebarNavigationContent(
            currentDestination = currentDestination,
            onDestinationSelected = onDestinationSelected,
            threads = threads,
            selectedThreadId = selectedThreadId,
            projects = projects,
            selectedProjectId = selectedProjectId,
            onSelectThread = onSelectThread,
            onSelectProject = onSelectProject,
            onNewChat = onNewChat,
            onDeleteThread = onDeleteThread
        )
    }
}

/** Shared primary navigation plus the Chat-owned local history context section. */
@Composable
private fun IvaiSidebarNavigationContent(
    currentDestination: NavDestination,
    onDestinationSelected: (NavDestination) -> Unit,
    threads: List<ChatThread>,
    selectedThreadId: String,
    projects: List<WorkspaceProject>,
    selectedProjectId: String?,
    onSelectThread: (String) -> Unit,
    onSelectProject: (String?) -> Unit,
    onNewChat: () -> Unit,
    onDeleteThread: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = IvaiSpacing.XSmall, vertical = IvaiSpacing.Small),
        verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XxSmall)
    ) {
        Text(
            text = "IVAI",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(horizontal = IvaiSpacing.XSmall, vertical = IvaiSpacing.XxSmall)
                .testTag("sidebar_wordmark")
        )
        Text(
            text = "Local AI, under your control",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = IvaiSpacing.XSmall)
        )
        Spacer(modifier = Modifier.height(IvaiSpacing.XxSmall))
        NavDestination.entries.forEach { destination ->
            val selected = destination == currentDestination
            NavigationDrawerItem(
                label = { Text(destination.title, style = MaterialTheme.typography.labelLarge) },
                selected = selected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(destination.testTag),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    unselectedContainerColor = MaterialTheme.colorScheme.surface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(vertical = IvaiSpacing.XSmall),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        if (currentDestination == NavDestination.CHATS) {
            ChatSessionDrawerSection(
                threads = threads,
                selectedThreadId = selectedThreadId,
                projects = projects,
                selectedProjectId = selectedProjectId,
                onSelectThread = onSelectThread,
                onSelectProject = onSelectProject,
                onNewChat = onNewChat,
                onDeleteThread = onDeleteThread,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        } else {
            Spacer(modifier = Modifier.height(1.dp).weight(1f))
        }
    }
}
