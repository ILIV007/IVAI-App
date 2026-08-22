package dev.iliv007.ivai.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import dev.iliv007.ivai.ui.components.IvaiNavigationMode
import dev.iliv007.ivai.ui.components.ivaiNavigationModeFor
import dev.iliv007.ivai.ui.model.ChatMessage
import dev.iliv007.ivai.ui.model.ChatThread
import dev.iliv007.ivai.ui.model.MessageSender
import dev.iliv007.ivai.ui.model.UiPreviewState
import dev.iliv007.ivai.ui.screens.ChatsScreen
import dev.iliv007.ivai.ui.viewmodel.RouterComboCard
import dev.iliv007.ivai.ui.viewmodel.RouterManagementState
import dev.iliv007.ivai.ui.theme.IvaiTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import androidx.compose.ui.unit.dp

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ChatFoundationTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun navigation_mode_breakpoints_keep_modal_and_persistent_sidebar_rules() {
        assertEquals(IvaiNavigationMode.COMPACT_MODAL, ivaiNavigationModeFor(599.dp))
        assertEquals(IvaiNavigationMode.MEDIUM_PERSISTENT, ivaiNavigationModeFor(600.dp))
        assertEquals(IvaiNavigationMode.MEDIUM_PERSISTENT, ivaiNavigationModeFor(839.dp))
        assertEquals(IvaiNavigationMode.EXPANDED_PERSISTENT, ivaiNavigationModeFor(840.dp))
    }

    @Test
    fun chat_without_thread_exposes_single_safe_next_action() {
        var createInvoked = false
        composeTestRule.setContent {
            IvaiTheme {
                ChatsScreen(
                    previewState = UiPreviewState.NORMAL,
                    onResetState = {},
                    onNewChatInProject = { createInvoked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("chat_onboarding_no_thread").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_onboarding_create").performClick()
        assert(createInvoked)
    }

    @Test
    fun chat_without_available_target_routes_user_to_connections_explicitly() {
        var connectionsInvoked = false
        composeTestRule.setContent {
            IvaiTheme {
                ChatsScreen(
                    previewState = UiPreviewState.NORMAL,
                    onResetState = {},
                    threads = listOf(chatThread(modelOrCombo = "No execution target selected")),
                    selectedThreadId = "thread-1",
                    onOpenConnections = { connectionsInvoked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("chat_onboarding_no_connection").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_onboarding_open_connections").performClick()
        assert(connectionsInvoked)
    }

    @Test
    fun target_ready_streaming_state_is_visible_and_recordable() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = true) {
                ChatsScreen(
                    previewState = UiPreviewState.NORMAL,
                    onResetState = {},
                    threads = listOf(
                        chatThread(
                            modelOrCombo = "Research Combo",
                            messages = listOf(
                                ChatMessage(
                                    id = "m-1",
                                    sender = MessageSender.USER,
                                    text = "سلام — summarize this Arabic note: مرحبا",
                                    timestamp = "Now"
                                )
                            )
                        )
                    ),
                    selectedThreadId = "thread-1",
                    isStreaming = true
                )
            }
        }

        composeTestRule.onNodeWithTag("chat_execution_status").assertIsDisplayed()
        composeTestRule.onNodeWithTag("input_message_text").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stop").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Stop streaming").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/phase71_chat_streaming_dark.png")
    }

    @Test
    fun compact_no_thread_state_is_recordable() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = true) {
                ChatsScreen(previewState = UiPreviewState.NORMAL, onResetState = {})
            }
        }

        composeTestRule.onNodeWithTag("chat_onboarding_no_thread").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/phase71_chat_compact_no_thread_dark.png")
    }

    @Test
    fun compact_no_connection_state_is_recordable() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = true) {
                ChatsScreen(
                    previewState = UiPreviewState.NORMAL,
                    onResetState = {},
                    threads = listOf(chatThread(modelOrCombo = "No execution target selected")),
                    selectedThreadId = "thread-1"
                )
            }
        }

        composeTestRule.onNodeWithTag("chat_onboarding_no_connection").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/phase71_chat_compact_no_connection_dark.png")
    }

    @Test
    fun compact_target_selection_state_is_recordable() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = true) {
                ChatsScreen(
                    previewState = UiPreviewState.NORMAL,
                    onResetState = {},
                    threads = listOf(chatThread(modelOrCombo = "No execution target selected")),
                    selectedThreadId = "thread-1",
                    routerManagementState = configuredResearchComboState()
                )
            }
        }

        composeTestRule.onNodeWithTag("chat_onboarding_no_target").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/phase71_chat_compact_choose_target_dark.png")
    }

    @Test
    fun target_ready_composer_uses_product_prompt_and_visible_send_affordance() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = false) {
                ChatsScreen(
                    previewState = UiPreviewState.NORMAL,
                    onResetState = {},
                    threads = listOf(chatThread(modelOrCombo = "Research Combo")),
                    selectedThreadId = "thread-1",
                    routerManagementState = configuredResearchComboState()
                )
            }
        }

        composeTestRule.onNodeWithText("Do anything…").assertIsDisplayed()
        composeTestRule.onNodeWithTag("input_message_text").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Send message").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/r3_chat_composer_light.png")
    }

    @Test
    fun draft_without_target_routes_to_explicit_target_selection() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = false) {
                ChatsScreen(
                    previewState = UiPreviewState.NORMAL,
                    onResetState = {},
                    threads = listOf(chatThread(modelOrCombo = "No execution target selected")),
                    selectedThreadId = "thread-1",
                    routerManagementState = configuredResearchComboState()
                )
            }
        }

        composeTestRule.onNodeWithTag("input_message_text").performTextInput("Draft before choosing a target")
        composeTestRule.onNodeWithContentDescription("Choose target before sending").performClick()
        composeTestRule.onNodeWithTag("target_selection_sheet").assertIsDisplayed()
    }

    @Test
    fun compact_target_ready_stopped_state_is_recordable() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = true) {
                ChatsScreen(
                    previewState = UiPreviewState.NORMAL,
                    onResetState = {},
                    threads = listOf(chatThread(modelOrCombo = "Research Combo")),
                    selectedThreadId = "thread-1",
                    isStreaming = false,
                    routerManagementState = configuredResearchComboState()
                )
            }
        }

        composeTestRule.onNodeWithTag("chat_onboarding_ready").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Send message").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/phase71_chat_compact_stopped_dark.png")
    }

    @Test
    fun persisted_incomplete_assistant_response_is_visibly_marked() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = true) {
                ChatsScreen(
                    previewState = UiPreviewState.NORMAL,
                    onResetState = {},
                    threads = listOf(
                        chatThread(
                            modelOrCombo = "User model",
                            messages = listOf(
                                ChatMessage(
                                    id = "partial-assistant",
                                    sender = MessageSender.ASSISTANT,
                                    text = "Visible partial response",
                                    timestamp = "Local",
                                    isIncomplete = true
                                )
                            )
                        )
                    ),
                    selectedThreadId = "thread-1"
                )
            }
        }

        composeTestRule.onNodeWithTag("incomplete_message_partial-assistant").assertIsDisplayed()
        composeTestRule.onNodeWithText("Incomplete response").assertIsDisplayed()
    }

    @Test
    fun empty_preview_state_is_recordable() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = true) {
                ChatsScreen(previewState = UiPreviewState.EMPTY, onResetState = {})
            }
        }

        composeTestRule.onNodeWithTag("chat_preview_state").assertIsDisplayed()
        composeTestRule.onNodeWithText("No messages in this conversation").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/phase71_chat_empty_dark.png")
    }

    @Test
    fun error_preview_state_is_recordable() {
        composeTestRule.setContent {
            IvaiTheme(darkTheme = true) {
                ChatsScreen(previewState = UiPreviewState.ERROR, onResetState = {})
            }
        }

        composeTestRule.onNodeWithTag("chat_preview_state").assertIsDisplayed()
        composeTestRule.onNodeWithText("Provider stream interrupted").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage(filePath = "build/roborazzi/phase71_chat_error_dark.png")
    }

    private fun configuredResearchComboState() = RouterManagementState(
        combos = listOf(
            RouterComboCard(
                comboId = "research-combo",
                displayName = "Research Combo",
                description = "Explicit local fallback",
                enabled = true,
                entries = emptyList()
            )
        )
    )

    private fun chatThread(
        modelOrCombo: String,
        messages: List<ChatMessage> = emptyList()
    ) = ChatThread(
        id = "thread-1",
        title = "Research chat",
        snippet = messages.lastOrNull()?.text ?: "No messages yet",
        timestamp = "Now",
        modelOrCombo = modelOrCombo,
        messages = messages,
        projectId = null,
        projectName = null
    )
}
