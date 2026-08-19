package dev.iliv007.ivai.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import dev.iliv007.ivai.ui.components.IvaiAdaptiveDestinationScaffold
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

/** Covers the wide-layout rail contract independently from application runtime state. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w1000dp-h800dp-xxhdpi", sdk = [36])
class IvaiAdaptiveNavigationScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    @Config(qualifiers = "w360dp-h800dp-xxhdpi", sdk = [36])
    fun compact_shell_has_no_bottom_navigation_and_is_recordable() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = false) {
                IvaiAdaptiveDestinationScaffold(
                    currentDestination = NavDestination.CHATS,
                    onDestinationSelected = {},
                    topBar = { Text("IVAI") }
                ) { contentModifier, _ ->
                    Box(modifier = contentModifier.fillMaxSize().testTag("compact_navigation_content"))
                }
            }
        }

        composeTestRule.onAllNodesWithTag("ivai_compact_navigation").assertCountEquals(0)
        composeTestRule.onNodeWithTag("compact_navigation_content").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/r2_navigation_compact_light.png")
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp-xxhdpi", sdk = [36])
    fun product_sidebar_contains_destinations_and_local_chat_history() {
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
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/r2_product_sidebar_dark.png")
    }

    @Test
    fun top_bar_is_navigation_only_in_production_shell() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = false) {
                IvaiTopBar(
                    title = "IVAI",
                    subtitle = "Chat",
                    onOpenSidebar = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("button_open_product_sidebar").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("button_toggle_theme").assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("button_state_switcher").assertCountEquals(0)
    }

    @Test
    fun expanded_rail_is_visible_and_recordable() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = true) {
                IvaiAdaptiveDestinationScaffold(
                    currentDestination = NavDestination.CHATS,
                    onDestinationSelected = {},
                    topBar = { Text("IVAI") }
                ) { contentModifier, _ ->
                    Box(modifier = contentModifier.fillMaxSize().testTag("expanded_navigation_content"))
                }
            }
        }

        composeTestRule.onNodeWithTag("ivai_expanded_rail").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_item_chats").assertIsDisplayed()
        composeTestRule.onNodeWithTag("expanded_navigation_content").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/phase71_navigation_expanded_dark.png")
    }
}
