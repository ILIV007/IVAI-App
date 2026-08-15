package dev.iliv007.ivai.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkspaceProjectDao {
    @Query("SELECT * FROM workspace_projects ORDER BY updated_at_epoch_ms DESC, id ASC")
    fun observeAll(): Flow<List<WorkspaceProjectEntity>>

    @Query("SELECT * FROM workspace_projects ORDER BY updated_at_epoch_ms DESC, id ASC")
    suspend fun listAll(): List<WorkspaceProjectEntity>

    @Query("SELECT * FROM workspace_projects WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): WorkspaceProjectEntity?

    @Upsert
    suspend fun upsert(project: WorkspaceProjectEntity)

    @Delete
    suspend fun delete(project: WorkspaceProjectEntity)

    @Query("DELETE FROM workspace_projects")
    suspend fun deleteAll()
}

@Dao
interface ChatThreadDao {
    @Query("SELECT * FROM chat_threads ORDER BY updated_at_epoch_ms DESC, id ASC")
    fun observeAll(): Flow<List<ChatThreadEntity>>

    @Query("SELECT * FROM chat_threads ORDER BY updated_at_epoch_ms DESC, id ASC")
    suspend fun listAll(): List<ChatThreadEntity>

    @Query("SELECT * FROM chat_threads WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): ChatThreadEntity?

    @Upsert
    suspend fun upsert(thread: ChatThreadEntity)

    @Delete
    suspend fun delete(thread: ChatThreadEntity)

    @Query("DELETE FROM chat_threads")
    suspend fun deleteAll()
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE thread_id = :threadId ORDER BY created_at_epoch_ms ASC, id ASC")
    fun observeForThread(threadId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE thread_id = :threadId ORDER BY created_at_epoch_ms ASC, id ASC")
    suspend fun listForThread(threadId: String): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_messages ORDER BY created_at_epoch_ms ASC, id ASC")
    suspend fun listAll(): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(message: ChatMessageEntity)

    @Delete
    suspend fun delete(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAll()
}
