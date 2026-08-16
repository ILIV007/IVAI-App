package dev.iliv007.ivai.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.outlined.AltRoute
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    CHATS("Chat", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline, "nav_item_chats"),
    AGENTS("Agents", Icons.Filled.SmartToy, Icons.Outlined.SmartToy, "nav_item_agents"),
    PROJECTS("Workspace", Icons.Filled.Folder, Icons.Outlined.Folder, "nav_item_projects"),
    ROUTER("Connections", Icons.AutoMirrored.Filled.AltRoute, Icons.AutoMirrored.Outlined.AltRoute, "nav_item_router"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_item_settings")
}
