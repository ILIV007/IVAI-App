package dev.iliv007.ivai.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Local-only workspace boundary for Phase 2. UI wiring is intentionally deferred until the
 * Phase 1 branch has merged and the persistence migration can be reviewed independently.
 */
data class PersistedWorkspaceSnapshot(
    val projects: List<WorkspaceProjectEntity>,
    val threads: List<ChatThreadEntity>
)

class LocalWorkspaceRepository(
    private val projectDao: WorkspaceProjectDao,
    private val threadDao: ChatThreadDao,
    private val messageDao: ChatMessageDao
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
}
