package dev.iliv007.ivai

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import dev.iliv007.ivai.ui.theme.IvaiTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class IvaiScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun ivai_main_screen_screenshot() {
        composeTestRule.setContent {
            IvaiTheme {
                IvaiMainApp()
            }
        }

        composeTestRule.onNodeWithTag("ivai_wordmark").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("IVAI wordmark").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/ivai_main.png")
    }
}
