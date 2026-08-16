package dev.iliv007.ivai.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import dev.iliv007.ivai.ui.components.IvaiAdaptiveDestinationScaffold
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
