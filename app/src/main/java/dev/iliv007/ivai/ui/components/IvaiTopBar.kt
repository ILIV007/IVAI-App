package dev.iliv007.ivai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import dev.iliv007.ivai.ui.model.UiPreviewState
import dev.iliv007.ivai.ui.theme.CyanPrimary
import dev.iliv007.ivai.ui.theme.IvaiError
import dev.iliv007.ivai.ui.theme.IvaiWarning
import dev.iliv007.ivai.ui.theme.JadeBright
import dev.iliv007.ivai.ui.theme.JadeDark
import dev.iliv007.ivai.ui.theme.JadePrimary
import dev.iliv007.ivai.ui.theme.NeonPurple
import dev.iliv007.ivai.ui.theme.NeonViolet
import dev.iliv007.ivai.ui.theme.PurpleDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IvaiTopBar(
    title: String = "IVAI",
    subtitle: String? = null,
    currentState: UiPreviewState,
    onStateSelected: (UiPreviewState) -> Unit,
    onOpenSidebar: () -> Unit = {},
    onOpenModelSelector: (() -> Unit)? = null,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {}
) {
    var stateMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp)
            )
    ) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            navigationIcon = {
                // Sidebar Menu Toggle Button
                IconButton(
                    onClick = onOpenSidebar,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 4.dp)
                        .size(44.dp)
                        .testTag("button_sidebar_toggle")
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Sidebar Menu",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "IVAI",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (subtitle != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(0.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.3.sp
                                ),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            },
            actions = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Theme Toggle Button (Light/Dark Mode Switcher)
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("button_toggle_theme")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDarkTheme) "Switch to Light Theme" else "Switch to Dark Theme",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Sleek State Switcher Pill Button for testing
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (currentState != UiPreviewState.NORMAL) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    1.dp,
                                    if (currentState != UiPreviewState.NORMAL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(10.dp)
                                )
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .clickable { stateMenuExpanded = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("button_state_switcher"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Switch UI state for testing (Normal, Loading, Empty, Error, Offline)",
                                tint = if (currentState == UiPreviewState.NORMAL) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentState.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = if (currentState == UiPreviewState.NORMAL) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = stateMenuExpanded,
                            onDismissRequest = { stateMenuExpanded = false },
                            properties = PopupProperties(
                                focusable = true,
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true,
                                clippingEnabled = true
                            ),
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        ) {
                            UiPreviewState.values().forEach { state ->
                                val isCurrent = currentState == state
                                val stateColor = when (state) {
                                    UiPreviewState.NORMAL -> MaterialTheme.colorScheme.primary
                                    UiPreviewState.LOADING -> MaterialTheme.colorScheme.tertiary
                                    UiPreviewState.EMPTY -> MaterialTheme.colorScheme.secondary
                                    UiPreviewState.ERROR -> MaterialTheme.colorScheme.error
                                    UiPreviewState.OFFLINE -> IvaiWarning
                                }

                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(stateColor)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = state.name.lowercase().replaceFirstChar { it.uppercase() },
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (isCurrent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    trailingIcon = {
                                        if (isCurrent) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    },
                                    onClick = {
                                        onStateSelected(state)
                                        stateMenuExpanded = false
                                    },
                                    modifier = Modifier.testTag("state_menu_item_${state.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

