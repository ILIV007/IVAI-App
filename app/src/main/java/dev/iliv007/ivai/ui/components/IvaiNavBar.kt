package dev.iliv007.ivai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iliv007.ivai.ui.navigation.NavDestination
import dev.iliv007.ivai.ui.theme.IvaiBorder
import dev.iliv007.ivai.ui.theme.IvaiBorderSubtle
import dev.iliv007.ivai.ui.theme.IvaiElevated
import dev.iliv007.ivai.ui.theme.IvaiSurface
import dev.iliv007.ivai.ui.theme.IvaiSurfaceVariant
import dev.iliv007.ivai.ui.theme.JadeBright
import dev.iliv007.ivai.ui.theme.JadeDark
import dev.iliv007.ivai.ui.theme.JadePrimary
import dev.iliv007.ivai.ui.theme.NeonViolet
import dev.iliv007.ivai.ui.theme.TextMuted
import dev.iliv007.ivai.ui.theme.TextPrimary
import dev.iliv007.ivai.ui.theme.TextSecondary

@Composable
fun IvaiNavBar(
    currentDestination: NavDestination,
    onDestinationSelected: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
            )
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            modifier = Modifier.navigationBarsPadding()
        ) {
            NavDestination.values().forEach { destination ->
                val isSelected = currentDestination == destination

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onDestinationSelected(destination) },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = destination.title
                        )
                    },
                    label = {
                        Text(
                            text = destination.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.sp
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag(destination.testTag)
                )
            }
        }
    }
}

