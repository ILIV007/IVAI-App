package dev.iliv007.ivai.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import dev.iliv007.ivai.ui.theme.IvaiLayoutTokens
import kotlinx.coroutines.launch

/** One sidebar model rendered as a modal surface or a persistent surface by available width. */
enum class IvaiNavigationMode {
    COMPACT_MODAL,
    MEDIUM_PERSISTENT,
    EXPANDED_PERSISTENT
}

fun ivaiNavigationModeFor(availableWidth: Dp): IvaiNavigationMode = when {
    availableWidth < IvaiLayoutTokens.MediumBreakpoint -> IvaiNavigationMode.COMPACT_MODAL
    availableWidth < IvaiLayoutTokens.ExpandedBreakpoint -> IvaiNavigationMode.MEDIUM_PERSISTENT
    else -> IvaiNavigationMode.EXPANDED_PERSISTENT
}

/**
 * Visual shell only. Route, thread and project selection remain ViewModel state and are deliberately
 * not rewritten when width changes. The drawer and persistent sidebar are never mounted together.
 */
@Composable
fun IvaiAdaptiveDestinationScaffold(
    topBar: @Composable (onOpenCompactSidebar: (() -> Unit)?) -> Unit,
    compactSidebar: @Composable (onDismiss: () -> Unit) -> Unit,
    persistentSidebar: @Composable (IvaiNavigationMode) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier, IvaiNavigationMode) -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val mode = ivaiNavigationModeFor(maxWidth)
        when (mode) {
            IvaiNavigationMode.COMPACT_MODAL -> {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val dismissSidebar: () -> Unit = { scope.launch { drawerState.close() } }
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = true,
                    drawerContent = { compactSidebar(dismissSidebar) }
                ) {
                    Scaffold(
                        topBar = {
                            topBar { scope.launch { drawerState.open() } }
                        }
                    ) { innerPadding ->
                        content(Modifier.padding(innerPadding), mode)
                    }
                }
            }
            IvaiNavigationMode.MEDIUM_PERSISTENT,
            IvaiNavigationMode.EXPANDED_PERSISTENT -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    persistentSidebar(mode)
                    Scaffold(
                        modifier = Modifier.weight(1f),
                        topBar = { topBar(null) }
                    ) { innerPadding ->
                        content(Modifier.padding(innerPadding), mode)
                    }
                }
            }
        }
    }
}
