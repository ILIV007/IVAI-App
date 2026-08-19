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
 * The single mobile navigation surface for the product. It owns destination-level navigation and
 * embeds local Chat history only when Chat is active, so the app never presents bottom navigation
 * beside a second, unrelated drawer.
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
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = IvaiSpacing.XSmall, vertical = IvaiSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XxSmall)
        ) {
            Text(
                text = "IVAI",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(horizontal = IvaiSpacing.XSmall, vertical = IvaiSpacing.XxSmall)
                    .testTag("sidebar_wordmark")
            )
            Text(
                text = "WORKSPACE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = IvaiSpacing.XSmall)
            )
            NavDestination.entries.forEach { destination ->
                NavigationDrawerItem(
                    label = { Text(destination.title) },
                    selected = destination == currentDestination,
                    onClick = { onDestinationSelected(destination) },
                    icon = {
                        Icon(
                            imageVector = if (destination == currentDestination) {
                                destination.selectedIcon
                            } else {
                                destination.unselectedIcon
                            },
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(destination.testTag),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
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
}
