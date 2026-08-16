package dev.iliv007.ivai.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import dev.iliv007.ivai.ui.navigation.NavDestination
import dev.iliv007.ivai.ui.theme.IvaiLayoutTokens
import dev.iliv007.ivai.ui.theme.IvaiSpacing

/** The shell arrangement is visual only; routes and destinations remain unchanged. */
enum class IvaiNavigationMode {
    COMPACT_BOTTOM_BAR,
    MEDIUM_RAIL,
    EXPANDED_RAIL
}

fun ivaiNavigationModeFor(availableWidth: Dp): IvaiNavigationMode = when {
    availableWidth < IvaiLayoutTokens.MediumBreakpoint -> IvaiNavigationMode.COMPACT_BOTTOM_BAR
    availableWidth < IvaiLayoutTokens.ExpandedBreakpoint -> IvaiNavigationMode.MEDIUM_RAIL
    else -> IvaiNavigationMode.EXPANDED_RAIL
}

/**
 * An adaptive five-destination app shell. Compact layouts use the bottom bar; medium and expanded
 * layouts use a rail so the conversation has more vertical space. No screen business logic lives
 * here.
 */
@Composable
fun IvaiAdaptiveDestinationScaffold(
    currentDestination: NavDestination,
    onDestinationSelected: (NavDestination) -> Unit,
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier, IvaiNavigationMode) -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val mode = ivaiNavigationModeFor(maxWidth)
        when (mode) {
            IvaiNavigationMode.COMPACT_BOTTOM_BAR -> {
                Scaffold(
                    topBar = topBar,
                    bottomBar = {
                        IvaiNavBar(
                            currentDestination = currentDestination,
                            onDestinationSelected = onDestinationSelected,
                            modifier = Modifier.testTag("ivai_compact_navigation")
                        )
                    }
                ) { innerPadding ->
                    content(Modifier.padding(innerPadding), mode)
                }
            }
            IvaiNavigationMode.MEDIUM_RAIL,
            IvaiNavigationMode.EXPANDED_RAIL -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    IvaiNavigationRail(
                        currentDestination = currentDestination,
                        onDestinationSelected = onDestinationSelected,
                        expanded = mode == IvaiNavigationMode.EXPANDED_RAIL,
                        modifier = Modifier.testTag("ivai_${mode.name.lowercase()}")
                    )
                    Scaffold(
                        modifier = Modifier.weight(1f),
                        topBar = topBar
                    ) { innerPadding ->
                        content(Modifier.padding(innerPadding), mode)
                    }
                }
            }
        }
    }
}

@Composable
fun IvaiNavigationRail(
    currentDestination: NavDestination,
    onDestinationSelected: (NavDestination) -> Unit,
    expanded: Boolean,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.width(
            if (expanded) IvaiLayoutTokens.ExpandedRailWidth else IvaiLayoutTokens.MediumRailWidth
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        NavDestination.entries.forEach { destination ->
            val selected = destination == currentDestination
            NavigationRailItem(
                selected = selected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = destination.title
                    )
                },
                label = {
                    Text(
                        text = destination.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                alwaysShowLabel = expanded,
                modifier = Modifier.testTag(destination.testTag)
            )
        }
    }
}
