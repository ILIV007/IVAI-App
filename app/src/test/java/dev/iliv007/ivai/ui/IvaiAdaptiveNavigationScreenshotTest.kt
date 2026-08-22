package dev.iliv007.ivai.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import dev.iliv007.ivai.ui.components.IvaiAdaptiveDestinationScaffold
import dev.iliv007.ivai.ui.components.IvaiNavigationMode
import dev.iliv007.ivai.ui.components.IvaiPersistentProductSidebar
import dev.iliv007.ivai.ui.components.IvaiProductSidebar
import dev.iliv007.ivai.ui.components.IvaiTopBar
import dev.iliv007.ivai.ui.model.ChatThread
import dev.iliv007.ivai.ui.model.WorkspaceProject
import dev.iliv007.ivai.ui.navigation.NavDestination
import dev.iliv007.ivai.ui.theme.IvaiTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Covers the responsive single-sidebar contract independently from application runtime state. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w1000dp-h800dp-xxhdpi", sdk = [36])
class IvaiAdaptiveNavigationScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    @Config(qualifiers = "w360dp-h800dp-xxhdpi", sdk = [36])
    fun compact_shell_is_modal_only_and_has_no_bottom_or_persistent_navigation() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = false) {
                IvaiAdaptiveDestinationScaffold(
                    topBar = { onOpen -> IvaiTopBar(title = "Chat", onOpenSidebar = onOpen) },
                    compactSidebar = { Text("Compact sidebar") },
                    persistentSidebar = { Text("Persistent sidebar") }
                ) { contentModifier, _ ->
                    Box(modifier = contentModifier.fillMaxSize().testTag("compact_navigation_content"))
                }
            }
        }

        composeTestRule.onAllNodesWithTag("ivai_medium_persistent_sidebar").assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("ivai_expanded_persistent_sidebar").assertCountEquals(0)
        composeTestRule.onNodeWithTag("compact_navigation_content").assertIsDisplayed()
        composeTestRule.onNodeWithTag("button_open_product_sidebar").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/ux3_navigation_compact_light.png")
    }

    @Test
    fun product_sidebar_contains_exactly_five_destinations_and_chat_context() {
        val thread = ChatThread(
            id = "sidebar-thread",
            title = "Local history",
            snippet = "A local conversation",
            timestamp = "Now",
            modelOrCombo = "No execution target",
            messages = emptyList()
        )
        composeTestRule.setContent {
            IvaiTheme(darkTheme = true) {
                IvaiProductSidebar(
                    currentDestination = NavDestination.CHATS,
                    onDestinationSelected = {},
                    threads = listOf(thread),
                    selectedThreadId = thread.id,
                    projects = listOf(WorkspaceProject("project", "Local", "", 0, "Now")),
                    selectedProjectId = null,
                    onSelectThread = {},
                    onSelectProject = {},
                    onNewChat = {},
                    onDeleteThread = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("ivai_product_sidebar").assertIsDisplayed()
        NavDestination.entries.forEach { destination ->
            composeTestRule.onNodeWithTag(destination.testTag).assertIsDisplayed()
        }
        composeTestRule.onNodeWithTag("chat_session_drawer").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_session_item_sidebar-thread").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/ux3_product_sidebar_dark.png")
    }

    @Test
    fun top_bar_exposes_route_title_and_appearance_overflow_without_toggle_or_preview_controls() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = false) {
                IvaiTopBar(
                    title = "Chat",
                    onOpenSidebar = {},
                    onOpenSettings = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("button_open_product_sidebar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ivai_route_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("button_open_global_overflow").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("button_toggle_theme").assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("button_state_switcher").assertCountEquals(0)
    }

    @Test
    fun expanded_layout_uses_persistent_sidebar_and_keeps_chat_context_out_of_primary_navigation() {
        val thread = ChatThread(
            id = "persistent-thread",
            title = "Persistent local history",
            snippet = "A local conversation",
            timestamp = "Now",
            modelOrCombo = "No execution target",
            messages = emptyList()
        )
        composeTestRule.setContent {
            IvaiTheme(darkTheme = true) {
                IvaiPersistentProductSidebar(
                    mode = IvaiNavigationMode.EXPANDED_PERSISTENT,
                    currentDestination = NavDestination.CHATS,
                    onDestinationSelected = {},
                    threads = listOf(thread),
                    selectedThreadId = thread.id,
                    projects = emptyList(),
                    selectedProjectId = null,
                    onSelectThread = {},
                    onSelectProject = {},
                    onNewChat = {},
                    onDeleteThread = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("ivai_expanded_persistent_sidebar").assertIsDisplayed()
        NavDestination.entries.forEach { destination ->
            composeTestRule.onNodeWithTag(destination.testTag).assertIsDisplayed()
        }
        composeTestRule.onNodeWithTag("chat_session_drawer").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/ux3_navigation_expanded_dark.png")
    }
}
