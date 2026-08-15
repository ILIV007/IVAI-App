package dev.iliv007.ivai.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PersistentWorkspaceDatabaseTest {

    @Test
    fun `persisted workspace survives database reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val databaseName = "workspace-reopen-${System.nanoTime()}.db"
        val firstOpen = Room.databaseBuilder(context, IvaiDatabase::class.java, databaseName)
            .allowMainThreadQueries()
            .build()
        try {
            val repository = LocalWorkspaceRepository(firstOpen)
            repository.saveProject(WorkspaceProjectEntity("project-1", "Persisted", "", 0, 10L))
            repository.saveThread(ChatThreadEntity("thread-1", "Persisted thread", "", 20L, "mock", "project-1"))
        } finally {
            firstOpen.close()
        }

        val reopened = Room.databaseBuilder(context, IvaiDatabase::class.java, databaseName)
            .allowMainThreadQueries()
            .build()
        try {
            assertNotNull(reopened.projectDao().findById("project-1"))
            assertEquals("project-1", reopened.threadDao().findById("thread-1")?.projectId)
            assertTrue(reopened.messageDao().listForThread("thread-1").isEmpty())
        } finally {
            reopened.close()
            context.deleteDatabase(databaseName)
        }
    }
}
