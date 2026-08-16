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

@Dao
interface ProviderConnectionDao {
    @Query("SELECT * FROM provider_connections ORDER BY updated_at_epoch_ms DESC, id ASC")
    fun observeAll(): Flow<List<ProviderConnectionEntity>>

    @Query("SELECT * FROM provider_connections ORDER BY updated_at_epoch_ms DESC, id ASC")
    suspend fun listAll(): List<ProviderConnectionEntity>

    @Query("SELECT * FROM provider_connections WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): ProviderConnectionEntity?

    @Upsert
    suspend fun upsert(connection: ProviderConnectionEntity)

    @Delete
    suspend fun delete(connection: ProviderConnectionEntity)

    @Query("DELETE FROM provider_connections")
    suspend fun deleteAll()
}

@Dao
interface ProviderAccountDao {
    @Query("SELECT * FROM provider_accounts WHERE connection_id = :connectionId ORDER BY updated_at_epoch_ms DESC, id ASC")
    fun observeForConnection(connectionId: String): Flow<List<ProviderAccountEntity>>

    @Query("SELECT * FROM provider_accounts ORDER BY updated_at_epoch_ms DESC, id ASC")
    fun observeAll(): Flow<List<ProviderAccountEntity>>

    @Query("SELECT * FROM provider_accounts ORDER BY updated_at_epoch_ms DESC, id ASC")
    suspend fun listAll(): List<ProviderAccountEntity>

    @Query("SELECT * FROM provider_accounts WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): ProviderAccountEntity?

    @Upsert
    suspend fun upsert(account: ProviderAccountEntity)

    @Delete
    suspend fun delete(account: ProviderAccountEntity)

    @Query("DELETE FROM provider_accounts")
    suspend fun deleteAll()
}

@Dao
interface ProviderModelDao {
    @Query("SELECT * FROM provider_models WHERE connection_id = :connectionId ORDER BY display_name COLLATE NOCASE ASC, id ASC")
    fun observeForConnection(connectionId: String): Flow<List<ProviderModelEntity>>

    @Query("SELECT * FROM provider_models ORDER BY connection_id ASC, display_name COLLATE NOCASE ASC, id ASC")
    fun observeAll(): Flow<List<ProviderModelEntity>>

    @Query("SELECT * FROM provider_models ORDER BY connection_id ASC, display_name COLLATE NOCASE ASC, id ASC")
    suspend fun listAll(): List<ProviderModelEntity>

    @Query("SELECT * FROM provider_models WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): ProviderModelEntity?

    @Upsert
    suspend fun upsert(model: ProviderModelEntity)

    @Delete
    suspend fun delete(model: ProviderModelEntity)

    @Query("DELETE FROM provider_models")
    suspend fun deleteAll()
}

@Dao
interface RouterComboDao {
    @Query("SELECT * FROM router_combos ORDER BY updated_at_epoch_ms DESC, id ASC")
    fun observeAll(): Flow<List<RouterComboEntity>>

    @Query("SELECT * FROM router_combos WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): RouterComboEntity?

    @Upsert
    suspend fun upsert(combo: RouterComboEntity)

    @Delete
    suspend fun delete(combo: RouterComboEntity)

    @Query("DELETE FROM router_combos")
    suspend fun deleteAll()
}

@Dao
interface RouterComboEntryDao {
    @Query("SELECT * FROM router_combo_entries WHERE combo_id = :comboId ORDER BY position ASC, id ASC")
    fun observeForCombo(comboId: String): Flow<List<RouterComboEntryEntity>>

    @Query("SELECT * FROM router_combo_entries ORDER BY combo_id ASC, position ASC, id ASC")
    fun observeAll(): Flow<List<RouterComboEntryEntity>>

    @Query("SELECT * FROM router_combo_entries WHERE combo_id = :comboId ORDER BY position ASC, id ASC")
    suspend fun listForCombo(comboId: String): List<RouterComboEntryEntity>

    @Upsert
    suspend fun upsert(entry: RouterComboEntryEntity)

    @Query("DELETE FROM router_combo_entries WHERE combo_id = :comboId")
    suspend fun deleteForCombo(comboId: String)

    @Query("DELETE FROM router_combo_entries")
    suspend fun deleteAll()
}

@Dao
interface ThreadExecutionTargetDao {
    @Query("SELECT * FROM thread_execution_targets WHERE thread_id = :threadId LIMIT 1")
    fun observeForThread(threadId: String): Flow<ThreadExecutionTargetEntity?>

    @Query("SELECT * FROM thread_execution_targets WHERE thread_id = :threadId LIMIT 1")
    suspend fun findForThread(threadId: String): ThreadExecutionTargetEntity?

    @Upsert
    suspend fun upsert(target: ThreadExecutionTargetEntity)

    @Query("DELETE FROM thread_execution_targets")
    suspend fun deleteAll()
}

@Dao
interface RouterAttemptDao {
    @Query("SELECT * FROM router_attempts WHERE thread_id = :threadId ORDER BY started_at_epoch_ms DESC, id DESC")
    fun observeForThread(threadId: String): Flow<List<RouterAttemptEntity>>

    @Query("SELECT * FROM router_attempts ORDER BY started_at_epoch_ms DESC, id DESC")
    fun observeAll(): Flow<List<RouterAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(attempt: RouterAttemptEntity)

    @Upsert
    suspend fun upsert(attempt: RouterAttemptEntity)

    @Query("DELETE FROM router_attempts")
    suspend fun deleteAll()
}

@Dao
interface RouterAttemptEntryDao {
    @Query("SELECT * FROM router_attempt_entries WHERE attempt_id = :attemptId ORDER BY position ASC, id ASC")
    fun observeForAttempt(attemptId: String): Flow<List<RouterAttemptEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: RouterAttemptEntryEntity)

    @Upsert
    suspend fun upsert(entry: RouterAttemptEntryEntity)

    @Query("DELETE FROM router_attempt_entries")
    suspend fun deleteAll()
}


@Dao
interface AgentProfileDao {
    @Query("SELECT * FROM agent_profiles ORDER BY updated_at_epoch_ms DESC, id ASC")
    fun observeAll(): Flow<List<AgentProfileEntity>>

    @Query("SELECT * FROM agent_profiles WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): AgentProfileEntity?

    @Upsert
    suspend fun upsert(profile: AgentProfileEntity)

    @Query("DELETE FROM agent_profiles")
    suspend fun deleteAll()
}

@Dao
interface AgentRunDao {
    @Query("SELECT * FROM agent_runs WHERE agent_id = :agentId ORDER BY started_at_epoch_ms DESC, id DESC")
    fun observeForAgent(agentId: String): Flow<List<AgentRunEntity>>

    @Query("SELECT * FROM agent_runs ORDER BY started_at_epoch_ms DESC, id DESC")
    fun observeAll(): Flow<List<AgentRunEntity>>

    @Query("SELECT * FROM agent_runs WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): AgentRunEntity?

    @Upsert
    suspend fun upsert(run: AgentRunEntity)

    @Query("DELETE FROM agent_runs")
    suspend fun deleteAll()
}

@Dao
interface AgentRunStepDao {
    @Query("SELECT * FROM agent_run_steps WHERE run_id = :runId ORDER BY position ASC, id ASC")
    fun observeForRun(runId: String): Flow<List<AgentRunStepEntity>>

    @Query("SELECT COUNT(*) FROM agent_run_steps WHERE run_id = :runId AND step_kind IN ('CALCULATE', 'CURRENT_TIME', 'READ_PROJECT_FILE', 'LIST_WORKSPACE', 'SEARCH_PROJECT_FILES', 'WRITE_PROJECT_FILE')")
    suspend fun countToolCallsForRun(runId: String): Int

    @Upsert
    suspend fun upsert(step: AgentRunStepEntity)

    @Query("DELETE FROM agent_run_steps")
    suspend fun deleteAll()
}

@Dao
interface AgentApprovalDao {
    @Query("SELECT * FROM agent_approvals WHERE run_id = :runId ORDER BY created_at_epoch_ms ASC, id ASC")
    fun observeForRun(runId: String): Flow<List<AgentApprovalEntity>>

    @Query("SELECT * FROM agent_approvals WHERE status = :status ORDER BY created_at_epoch_ms ASC, id ASC")
    fun observeByStatus(status: String): Flow<List<AgentApprovalEntity>>

    @Query("SELECT * FROM agent_approvals WHERE run_id = :runId AND status = 'PENDING' ORDER BY created_at_epoch_ms ASC, id ASC")
    suspend fun findPendingForRun(runId: String): List<AgentApprovalEntity>

    @Query("SELECT * FROM agent_approvals WHERE status = 'PENDING' ORDER BY created_at_epoch_ms ASC, id ASC")
    suspend fun listPending(): List<AgentApprovalEntity>

    @Query("SELECT * FROM agent_approvals WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): AgentApprovalEntity?

    @Upsert
    suspend fun upsert(approval: AgentApprovalEntity)

    @Query("DELETE FROM agent_approvals")
    suspend fun deleteAll()
}
