package dev.iliv007.ivai.ui.viewmodel

import dev.iliv007.ivai.ui.model.ChatThread
import dev.iliv007.ivai.ui.model.UiPreviewState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceViewModelTest {

    @Test
    fun `fallback send appends Persian message to canonical selected thread`() {
        val thread = ChatThread(
            id = "chat-test",
            title = "Test",
            snippet = "No messages yet",
            timestamp = "Now",
            modelOrCombo = "Gemini",
            messages = emptyList()
        )
        val viewModel = WorkspaceViewModel(
            initialState = WorkspaceUiState(
                previewState = UiPreviewState.NORMAL,
                threads = listOf(thread),
                projects = emptyList(),
                selectedThreadId = thread.id
            )
        )

        viewModel.sendMessage(thread.id, "  سلام Gemini  ")

        val message = viewModel.uiState.value.threads.single().messages.single()
        assertEquals("سلام Gemini", message.text)
        assertEquals("USER", message.sender.name)
        assertFalse(viewModel.uiState.value.isStreaming)
    }

    @Test
    fun `stop clears canonical streaming flag`() {
        val viewModel = WorkspaceViewModel(
            initialState = WorkspaceUiState(
                previewState = UiPreviewState.NORMAL,
                threads = emptyList(),
                projects = emptyList(),
                isStreaming = true
            )
        )

        viewModel.stopStreaming()

        assertFalse(viewModel.uiState.value.isStreaming)
    }

    @Test
    fun `send is ignored while a stream is active`() {
        val thread = ChatThread("chat-test", "Test", "", "Now", "Gemini", emptyList())
        val viewModel = WorkspaceViewModel(
            initialState = WorkspaceUiState(
                threads = listOf(thread),
                projects = emptyList(),
                selectedThreadId = thread.id,
                isStreaming = true
            )
        )

        viewModel.sendMessage(thread.id, "نباید ثبت شود")

        assertTrue(viewModel.uiState.value.threads.single().messages.isEmpty())
    }
}
