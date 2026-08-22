package dev.iliv007.ivai.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.iliv007.ivai.ui.theme.IvaiIconSizeTokens
import dev.iliv007.ivai.ui.theme.IvaiLayoutTokens

/**
 * Product chrome: compact layouts receive a menu opener; all layouts show the current route title
 * and a small overflow route to Appearance. Theme choice remains owned by Settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IvaiTopBar(
    title: String,
    onOpenSidebar: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null
) {
    var overflowExpanded by remember { mutableStateOf(false) }
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        navigationIcon = {
            onOpenSidebar?.let { openSidebar ->
                IconButton(
                    onClick = openSidebar,
                    modifier = Modifier
                        .size(IvaiLayoutTokens.MinimumTouchTarget)
                        .testTag("button_open_product_sidebar")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open navigation",
                        modifier = Modifier.size(IvaiIconSizeTokens.Navigation)
                    )
                }
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .semantics { contentDescription = "Current destination: $title" }
                        .testTag("ivai_route_title")
                )
            }
        },
        actions = {
            onOpenSettings?.let { openSettings ->
                IconButton(
                    onClick = { overflowExpanded = true },
                    modifier = Modifier
                        .size(IvaiLayoutTokens.MinimumTouchTarget)
                        .testTag("button_open_global_overflow")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        modifier = Modifier.size(IvaiIconSizeTokens.Navigation)
                    )
                }
                DropdownMenu(
                    expanded = overflowExpanded,
                    onDismissRequest = { overflowExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Appearance") },
                        onClick = {
                            overflowExpanded = false
                            openSettings()
                        },
                        modifier = Modifier.testTag("menu_open_appearance")
                    )
                }
            }
        }
    )
}
