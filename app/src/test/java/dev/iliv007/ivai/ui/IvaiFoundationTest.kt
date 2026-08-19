package dev.iliv007.ivai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import dev.iliv007.ivai.ui.components.IvaiExecutionState
import dev.iliv007.ivai.ui.components.IvaiExecutionStatusBanner
import dev.iliv007.ivai.ui.components.IvaiPageHeader
import dev.iliv007.ivai.ui.components.IvaiScreenScaffold
import dev.iliv007.ivai.ui.components.IvaiStateCard
import dev.iliv007.ivai.ui.components.IvaiStateTone
import dev.iliv007.ivai.ui.components.IvaiTargetChip
import dev.iliv007.ivai.ui.theme.IvaiSpacing
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
class IvaiFoundationTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun foundation_semantics_expose_heading_target_and_execution_state() {
        composeTestRule.setContent { FoundationPreview(darkTheme = false) }

        composeTestRule.onNodeWithText("Foundation preview")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        composeTestRule.onNodeWithTag("phase7_target")
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Available"))
        composeTestRule.onNodeWithTag("phase7_execution_status")
            .assertIsDisplayed()
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.LiveRegion,
                    LiveRegionMode.Polite
                )
            )
        composeTestRule.onNodeWithTag("phase7_target").performClick()
        composeTestRule.onNodeWithText("Target changed").assertIsDisplayed()
    }

    @Test
    fun foundation_light_theme_screenshot() {
        composeTestRule.setContent { FoundationPreview(darkTheme = false) }
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/phase7_foundation_light.png")
    }

    @Test
    fun foundation_dark_theme_screenshot() {
        composeTestRule.setContent { FoundationPreview(darkTheme = true) }
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/phase7_foundation_dark.png")
    }
}

@Composable
private fun FoundationPreview(darkTheme: Boolean) {
    IvaiTheme(darkTheme = darkTheme) {
        IvaiScreenScaffold(testTag = "phase7_foundation_screen") {
            var targetChanged by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(IvaiSpacing.Small),
                verticalArrangement = Arrangement.spacedBy(IvaiSpacing.Small)
            ) {
                IvaiPageHeader(
                    title = "Foundation preview",
                    subtitle = "Independent IVAI UI tokens and accessibility contracts",
                    testTag = "phase7_page_header",
                    actions = {
                        TextButton(onClick = {}) { Text("Action") }
                    }
                )
                IvaiTargetChip(
                    label = "Local Combo · Research",
                    availabilityLabel = "Available",
                    onClick = { targetChanged = true },
                    leadingIcon = Icons.Default.AutoAwesome,
                    testTag = "phase7_target"
                )
                if (targetChanged) {
                    Text("Target changed")
                }
                IvaiExecutionStatusBanner(
                    state = IvaiExecutionState.AWAITING_APPROVAL,
                    targetLabel = "Local Combo · Research",
                    detail = "Review the one-time file write before continuing.",
                    announceChange = true,
                    testTag = "phase7_execution_status"
                )
                IvaiStateCard(
                    title = "No provider connection yet",
                    message = "Add a user-managed provider before starting a conversation.",
                    tone = IvaiStateTone.INFO,
                    icon = Icons.Default.AutoAwesome,
                    testTag = "phase7_state_card"
                )
            }
        }
    }
}
