package dev.iliv007.ivai

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dev.iliv007.ivai.ui.screens.MainChatScreen
import dev.iliv007.ivai.ui.theme.IvaiTheme
import dev.iliv007.ivai.ui.viewmodel.MainChatViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MainChatViewModelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `viewModel handles input text change and send message`() {
        val viewModel = MainChatViewModel(initialMessages = emptyList())

        assertEquals("", viewModel.uiState.value.inputText)
        assertEquals(0, viewModel.uiState.value.messages.size)

        viewModel.onInputTextChanged("سلام هوش مصنوعی")
        assertEquals("سلام هوش مصنوعی", viewModel.uiState.value.inputText)

        viewModel.sendMessage()

        // Input should be cleared and user message added
        assertEquals("", viewModel.uiState.value.inputText)
        assertTrue(viewModel.uiState.value.messages.isNotEmpty())
        assertEquals("سلام هوش مصنوعی", viewModel.uiState.value.messages.first().text)
    }

    @Test
    fun `compose UI updates dynamically via ViewModel`() {
        val viewModel = MainChatViewModel(initialMessages = emptyList())

        composeTestRule.setContent {
            IvaiTheme {
                MainChatScreen(viewModel = viewModel)
            }
        }

        // Initially empty state text
        composeTestRule.onNodeWithText("Start a new conversation").assertIsDisplayed()

        // Enter text in composer
        composeTestRule.onNodeWithTag("input_message_text").performTextInput("Hello IVAI")
        composeTestRule.onNodeWithTag("button_send_message").performClick()

        // User message should appear dynamically in message list
        composeTestRule.onNodeWithText("Hello IVAI").assertIsDisplayed()
    }

    @Test
    fun `verify copy to clipboard and delete message action`() {
        val testMessage = dev.iliv007.ivai.ui.model.ChatMessage(
            id = "msg-test-1",
            sender = dev.iliv007.ivai.ui.model.MessageSender.USER,
            text = "Test copy & delete message",
            timestamp = "10:30"
        )
        val viewModel = MainChatViewModel(initialMessages = listOf(testMessage))

        composeTestRule.setContent {
            IvaiTheme {
                MainChatScreen(viewModel = viewModel)
            }
        }

        // Verify message bubble, copy button, and delete button are displayed
        composeTestRule.onNodeWithTag("message_bubble_msg-test-1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("button_copy_message_msg-test-1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("button_delete_message_msg-test-1").assertIsDisplayed()

        // Perform copy action
        composeTestRule.onNodeWithTag("button_copy_message_msg-test-1").performClick()
        composeTestRule.onNodeWithText("Copied!").assertIsDisplayed()

        // Perform delete action
        composeTestRule.onNodeWithTag("button_delete_message_msg-test-1").performClick()

        // Verify message is deleted from list
        assertEquals(0, viewModel.uiState.value.messages.size)
        composeTestRule.onNodeWithText("Start a new conversation").assertIsDisplayed()
    }
}
