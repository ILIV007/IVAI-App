package dev.iliv007.ivai.ui.viewmodel

import dev.iliv007.ivai.ui.model.ChatMessage
import dev.iliv007.ivai.ui.model.ChatThread
import dev.iliv007.ivai.ui.model.MessageSender
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * RouterChatSession persists `msg-$attemptId-user` before the provider emits Started. The
 * ViewModel's Started handler must therefore not add a second transient user row when the local
 * message is already present in the selected thread state.
 */
class RouterUserMessageInvariantTest {

    @Test
    fun `router started confirmation keeps one already persisted user message`() {
        val prompt = "one prompt"
        val thread = ChatThread(
            id = "thread",
            title = "Thread",
            snippet = prompt,
            timestamp = "Now",
            modelOrCombo = "User-selected target",
            messages = listOf(
                ChatMessage(
                    id = "msg-router-100-user",
                    sender = MessageSender.USER,
                    text = prompt,
                    timestamp = "Now"
                )
            )
        )
        val viewModel = WorkspaceViewModel(
            initialState = WorkspaceUiState(
                threads = listOf(thread),
                projects = emptyList(),
                selectedThreadId = thread.id
            )
        )

        // This is the exact helper invoked by the Router `ProviderStreamEvent.Started` branch.
        viewModel.appendUserMessage(
            threadId = thread.id,
            rawText = prompt,
            persistLocally = false,
            messageId = "msg-router-100-user"
        )

        val matchingUserMessages = viewModel.uiState.value.threads.single().messages
            .filter { it.sender == MessageSender.USER && it.text == prompt }
        assertEquals(
            "A Router start must not add a transient duplicate of the user message already persisted by RouterChatSession.",
            1,
            matchingUserMessages.size
        )
    }
}
