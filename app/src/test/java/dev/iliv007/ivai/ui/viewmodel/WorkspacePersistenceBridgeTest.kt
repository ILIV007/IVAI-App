package dev.iliv007.ivai.ui.viewmodel

import android.os.Looper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.iliv007.ivai.data.local.IvaiDatabase
import dev.iliv007.ivai.data.local.LocalWorkspaceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WorkspacePersistenceBridgeTest {
    private lateinit var database: IvaiDatabase
    private lateinit var repository: LocalWorkspaceRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            IvaiDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = LocalWorkspaceRepository(database)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `workspace UI mutations persist locally and restore through Room observers`() = runBlocking {
        val viewModel = WorkspaceViewModel(workspaceRepository = repository)

        val project = viewModel.createNewProject("Persisted project", "Local-only")
        withTimeout(5_000) {
            repository.observeWorkspace().first { snapshot -> snapshot.projects.any { it.id == project.id } }
        }

        viewModel.createNewChat(project.id)
        val thread = withTimeout(5_000) {
            repository.observeWorkspace()
                .first { snapshot -> snapshot.threads.any { it.projectId == project.id } }
                .threads
                .first { it.projectId == project.id }
        }

        viewModel.appendUserMessage(thread.id, "سلام persistence")
        withTimeout(5_000) {
            repository.observeMessages(thread.id).first { messages -> messages.any { it.text == "سلام persistence" } }
        }
        withTimeout(5_000) {
            viewModel.uiState.first { state ->
                state.projects.any { it.id == project.id } &&
                    state.threads.any { uiThread ->
                        uiThread.id == thread.id && uiThread.messages.any { it.text == "سلام persistence" }
                    }
            }
        }

        val persistedThread = repository.observeWorkspace().first().threads.single { it.id == thread.id }
        assertEquals("سلام persistence", persistedThread.snippet)
        assertTrue(viewModel.uiState.value.projects.any { it.id == project.id })

        viewModel.deleteThread(thread.id)
        assertFalse(viewModel.uiState.value.threads.any { it.id == thread.id })
        withTimeout(5_000) {
            while (database.threadDao().findById(thread.id) != null) {
                shadowOf(Looper.getMainLooper()).idle()
                delay(20)
            }
        }
        Unit
    }
}
