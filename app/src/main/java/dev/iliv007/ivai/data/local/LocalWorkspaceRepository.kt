package dev.iliv007.ivai.data.local

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Local-only workspace boundary for Phase 2. UI wiring is intentionally deferred until the
 * persistence migration and security boundaries can be reviewed independently.
 */
data class PersistedWorkspaceSnapshot(
    val projects: List<WorkspaceProjectEntity>,
    val threads: List<ChatThreadEntity>
)

data class LocalWorkspaceArchiveSnapshot(
    val projects: List<WorkspaceProjectEntity>,
    val threads: List<ChatThreadEntity>,
    val messages: List<ChatMessageEntity>
)

class LocalWorkspaceRepository(
    private val database: IvaiDatabase,
    private val projectDao: WorkspaceProjectDao = database.projectDao(),
    private val threadDao: ChatThreadDao = database.threadDao(),
    private val messageDao: ChatMessageDao = database.messageDao()
) {
    fun observeWorkspace(): Flow<PersistedWorkspaceSnapshot> = combine(
        projectDao.observeAll(),
        threadDao.observeAll()
    ) { projects, threads ->
        PersistedWorkspaceSnapshot(projects = projects, threads = threads)
    }

    fun observeMessages(threadId: String): Flow<List<ChatMessageEntity>> =
        messageDao.observeForThread(threadId)

    suspend fun saveProject(project: WorkspaceProjectEntity) {
        projectDao.upsert(project)
    }

    suspend fun saveThread(thread: ChatThreadEntity) {
        threadDao.upsert(thread)
    }

    suspend fun appendMessage(message: ChatMessageEntity) {
        messageDao.insert(message)
    }

    suspend fun deleteThread(threadId: String) {
        val thread = threadDao.findById(threadId) ?: return
        threadDao.delete(thread)
    }

    suspend fun snapshotForArchive(): LocalWorkspaceArchiveSnapshot = database.withTransaction {
        LocalWorkspaceArchiveSnapshot(
            projects = projectDao.listAll(),
            threads = threadDao.listAll(),
            messages = messageDao.listAll()
        )
    }

    /**
     * Commits a previously validated local-only snapshot atomically. Secret material is not part
     * of [LocalWorkspaceArchiveSnapshot] and cannot be restored through this boundary.
     */
    suspend fun replaceFromArchive(snapshot: LocalWorkspaceArchiveSnapshot) {
        database.withTransaction {
            messageDao.deleteAll()
            threadDao.deleteAll()
            projectDao.deleteAll()

            for (project in snapshot.projects) {
                projectDao.upsert(project)
            }
            for (thread in snapshot.threads) {
                threadDao.upsert(thread)
            }
            for (message in snapshot.messages) {
                messageDao.insert(message)
            }
        }
    }

    suspend fun deleteAllWorkspaceData() {
        database.withTransaction {
            messageDao.deleteAll()
            threadDao.deleteAll()
            projectDao.deleteAll()
        }
    }
}
