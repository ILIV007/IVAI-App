package dev.iliv007.ivai

import dev.iliv007.ivai.ui.model.ChatThread
import dev.iliv007.ivai.ui.model.MessageSender
import dev.iliv007.ivai.ui.model.UiPreviewState
import dev.iliv007.ivai.ui.navigation.NavDestination
import dev.iliv007.ivai.ui.viewmodel.WorkspaceUiState
import dev.iliv007.ivai.ui.viewmodel.WorkspaceViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceViewModelTest {
    @Test
    fun `workspace state owns navigation preview and selected thread`() {
        val thread = localThread()
        val viewModel = WorkspaceViewModel(
            initialState = WorkspaceUiState(threads = listOf(thread), selectedThreadId = thread.id)
        )

        viewModel.selectDestination(NavDestination.ROUTER)
        viewModel.selectPreviewState(UiPreviewState.ERROR)
        viewModel.selectThread(thread.id)

        assertEquals(NavDestination.CHATS, viewModel.uiState.value.destination)
        assertEquals(thread.id, viewModel.uiState.value.selectedThreadId)
        assertEquals(UiPreviewState.ERROR, viewModel.uiState.value.previewState)

        viewModel.resetPreviewState()
        assertEquals(UiPreviewState.NORMAL, viewModel.uiState.value.previewState)
    }

    @Test
    fun `workspace creates project chat assigns and appends message`() {
        val viewModel = WorkspaceViewModel()
        val project = viewModel.createNewProject("RTL Workspace", "Mixed-direction validation")

        viewModel.selectProject(project.id)
        viewModel.createNewChat()
        val threadId = viewModel.uiState.value.selectedThreadId

        val thread = viewModel.uiState.value.threads.first { it.id == threadId }
        assertEquals(project.id, thread.projectId)
        assertEquals("RTL Workspace", thread.projectName)

        viewModel.appendUserMessage(threadId, "سلام IVAI — inspect README.md")
        val updatedThread = viewModel.uiState.value.threads.first { it.id == threadId }

        assertEquals(1, updatedThread.messages.size)
        assertEquals(MessageSender.USER, updatedThread.messages.single().sender)
        assertEquals("سلام IVAI — inspect README.md", updatedThread.messages.single().text)
        assertEquals("سلام IVAI — inspect README.md", updatedThread.snippet)
    }

    @Test
    fun `workspace ignores invalid ids and normalizes selection after deletion`() {
        val thread = localThread()
        val viewModel = WorkspaceViewModel(
            initialState = WorkspaceUiState(threads = listOf(thread), selectedThreadId = thread.id)
        )

        viewModel.selectThread("missing-thread")
        viewModel.selectProject("missing-project")

        assertEquals(thread.id, viewModel.uiState.value.selectedThreadId)
        assertNull(viewModel.uiState.value.selectedProjectId)

        viewModel.deleteThread(thread.id)
        assertTrue(viewModel.uiState.value.threads.isEmpty())
        assertTrue(viewModel.uiState.value.selectedThreadId.isEmpty())
    }

    @Test
    fun `workspace normalizes an invalid selection to empty workspace`() {
        val viewModel = WorkspaceViewModel(initialState = WorkspaceUiState(selectedThreadId = "invalid", selectedProjectId = "invalid"))

        assertTrue(viewModel.uiState.value.threads.isEmpty())
        assertTrue(viewModel.uiState.value.selectedThreadId.isEmpty())
        assertNull(viewModel.uiState.value.selectedProjectId)
    }

    private fun localThread() = ChatThread(
        id = "thread-local",
        title = "Local chat",
        snippet = "",
        timestamp = "Now",
        modelOrCombo = "No execution target selected",
        messages = emptyList()
    )
}
