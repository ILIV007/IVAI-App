package dev.iliv007.ivai.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.iliv007.ivai.ui.model.ChatMessage
import dev.iliv007.ivai.ui.model.MessageContentType
import dev.iliv007.ivai.ui.model.MessageSender

@Entity(
    tableName = "workspace_projects",
    indices = [Index(value = ["updated_at_epoch_ms"])]
)
data class WorkspaceProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val fileCount: Int,
    @ColumnInfo(name = "updated_at_epoch_ms") val updatedAtEpochMs: Long
)

@Entity(
    tableName = "chat_threads",
    foreignKeys = [
        ForeignKey(
            entity = WorkspaceProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["project_id"]), Index(value = ["updated_at_epoch_ms"])]
)
data class ChatThreadEntity(
    @PrimaryKey val id: String,
    val title: String,
    val snippet: String,
    @ColumnInfo(name = "updated_at_epoch_ms") val updatedAtEpochMs: Long,
    val modelOrCombo: String,
    @ColumnInfo(name = "project_id") val projectId: String?
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatThreadEntity::class,
            parentColumns = ["id"],
            childColumns = ["thread_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["thread_id", "created_at_epoch_ms"])]
)
data class ChatMessageEntity(
    @PrimaryKey     val id: String,
    @ColumnInfo(name = "thread_id") val threadId: String,
    val sender: String,
    val text: String,
    @ColumnInfo(name = "created_at_epoch_ms") val createdAtEpochMs: Long,
    @ColumnInfo(name = "content_type") val contentType: String,
    @ColumnInfo(name = "code_snippet") val codeSnippet: String?,
    @ColumnInfo(name = "model_badge") val modelBadge: String?,
    @ColumnInfo(name = "latency_ms") val latencyMs: Long?

)

fun ChatMessage.toEntity(threadId: String, createdAtEpochMs: Long): ChatMessageEntity =
    ChatMessageEntity(
        id = id,
        threadId = threadId,
        sender = sender.name,
        text = text,
        createdAtEpochMs = createdAtEpochMs,
        contentType = type.name,
        codeSnippet = codeSnippet,
        modelBadge = modelBadge,
        latencyMs = latencyMs
    )

fun ChatMessageEntity.toDomainMessage(timestamp: String): ChatMessage =
    ChatMessage(
        id = id,
        sender = MessageSender.valueOf(sender),
        text = text,
        timestamp = timestamp,
        type = MessageContentType.valueOf(contentType),
        codeSnippet = codeSnippet,
        modelBadge = modelBadge,
        latencyMs = latencyMs
    )

@Entity(
    tableName = "provider_connections",
    indices = [Index(value = ["provider_kind"]), Index(value = ["updated_at_epoch_ms"])]
)
data class ProviderConnectionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "provider_kind") val providerKind: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "base_url") val baseUrl: String?,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean,
    @ColumnInfo(name = "created_at_epoch_ms") val createdAtEpochMs: Long,
    @ColumnInfo(name = "updated_at_epoch_ms") val updatedAtEpochMs: Long,
    @ColumnInfo(name = "endpoint_trust_mode", defaultValue = "'REMOTE_HTTPS'") val endpointTrustMode: String = "REMOTE_HTTPS",
    @ColumnInfo(name = "local_trust_confirmed_at_epoch_ms") val localTrustConfirmedAtEpochMs: Long? = null
)

@Entity(
    tableName = "provider_accounts",
    foreignKeys = [
        ForeignKey(
            entity = ProviderConnectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["connection_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["connection_id"]), Index(value = ["credential_reference"], unique = true)]
)
data class ProviderAccountEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "connection_id") val connectionId: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    /** Opaque API-key reference or non-secret `no-auth.<account-id>` marker; never plaintext credential. */
    @ColumnInfo(name = "credential_reference") val credentialReference: String,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean,
    @ColumnInfo(name = "created_at_epoch_ms") val createdAtEpochMs: Long,
    @ColumnInfo(name = "updated_at_epoch_ms") val updatedAtEpochMs: Long,
    @ColumnInfo(name = "auth_mode", defaultValue = "'API_KEY'") val authMode: String = "API_KEY"
)

@Entity(
    tableName = "provider_models",
    foreignKeys = [
        ForeignKey(
            entity = ProviderConnectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["connection_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["connection_id"]), Index(value = ["connection_id", "provider_model_id"], unique = true)]
)
data class ProviderModelEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "connection_id") val connectionId: String,
    @ColumnInfo(name = "provider_model_id") val providerModelId: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "capabilities_csv") val capabilitiesCsv: String,
    @ColumnInfo(name = "is_manual") val isManual: Boolean,
    @ColumnInfo(name = "is_selectable") val isSelectable: Boolean,
    @ColumnInfo(name = "updated_at_epoch_ms") val updatedAtEpochMs: Long
)

@Entity(
    tableName = "router_combos",
    indices = [Index(value = ["updated_at_epoch_ms"])]
)
data class RouterComboEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    val description: String,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean,
    @ColumnInfo(name = "created_at_epoch_ms") val createdAtEpochMs: Long,
    @ColumnInfo(name = "updated_at_epoch_ms") val updatedAtEpochMs: Long
)

@Entity(
    tableName = "router_combo_entries",
    foreignKeys = [
        ForeignKey(entity = RouterComboEntity::class, parentColumns = ["id"], childColumns = ["combo_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ProviderConnectionEntity::class, parentColumns = ["id"], childColumns = ["connection_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ProviderAccountEntity::class, parentColumns = ["id"], childColumns = ["account_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ProviderModelEntity::class, parentColumns = ["id"], childColumns = ["model_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index(value = ["combo_id", "position"], unique = true),
        Index(value = ["connection_id"]), Index(value = ["account_id"]), Index(value = ["model_id"])
    ]
)
data class RouterComboEntryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "combo_id") val comboId: String,
    val position: Int,
    @ColumnInfo(name = "connection_id") val connectionId: String,
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "model_id") val modelId: String,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean
)

@Entity(
    tableName = "thread_execution_targets",
    foreignKeys = [ForeignKey(entity = ChatThreadEntity::class, parentColumns = ["id"], childColumns = ["thread_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["target_kind", "target_id"])]
)
data class ThreadExecutionTargetEntity(
    @PrimaryKey @ColumnInfo(name = "thread_id") val threadId: String,
    @ColumnInfo(name = "target_kind") val targetKind: String,
    @ColumnInfo(name = "target_id") val targetId: String,
    @ColumnInfo(name = "account_id") val accountId: String?,
    @ColumnInfo(name = "updated_at_epoch_ms") val updatedAtEpochMs: Long
)

@Entity(
    tableName = "router_attempts",
    foreignKeys = [ForeignKey(entity = ChatThreadEntity::class, parentColumns = ["id"], childColumns = ["thread_id"], onDelete = ForeignKey.SET_NULL)],
    indices = [Index(value = ["thread_id", "started_at_epoch_ms"]), Index(value = ["target_kind", "target_id"])]
)
data class RouterAttemptEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "thread_id") val threadId: String?,
    @ColumnInfo(name = "target_kind") val targetKind: String,
    @ColumnInfo(name = "target_id") val targetId: String,
    val outcome: String,
    @ColumnInfo(name = "started_at_epoch_ms") val startedAtEpochMs: Long,
    @ColumnInfo(name = "completed_at_epoch_ms") val completedAtEpochMs: Long?,
    @ColumnInfo(name = "safe_error_kind") val safeErrorKind: String?,
    @ColumnInfo(name = "safe_error_message") val safeErrorMessage: String?
)

@Entity(
    tableName = "router_attempt_entries",
    foreignKeys = [ForeignKey(entity = RouterAttemptEntity::class, parentColumns = ["id"], childColumns = ["attempt_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["attempt_id", "position"], unique = true)]
)
data class RouterAttemptEntryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "attempt_id") val attemptId: String,
    val position: Int,
    @ColumnInfo(name = "connection_id") val connectionId: String,
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "model_id") val modelId: String,
    val outcome: String,
    @ColumnInfo(name = "started_at_epoch_ms") val startedAtEpochMs: Long,
    @ColumnInfo(name = "completed_at_epoch_ms") val completedAtEpochMs: Long?,
    @ColumnInfo(name = "safe_error_kind") val safeErrorKind: String?,
    @ColumnInfo(name = "safe_error_message") val safeErrorMessage: String?
)

@Entity(
    tableName = "agent_profiles",
    foreignKeys = [ForeignKey(entity = WorkspaceProjectEntity::class, parentColumns = ["id"], childColumns = ["project_id"], onDelete = ForeignKey.SET_NULL)],
    indices = [Index(value = ["project_id"]), Index(value = ["updated_at_epoch_ms"])]
)
data class AgentProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val instructions: String,
    @ColumnInfo(name = "target_kind") val targetKind: String,
    @ColumnInfo(name = "target_id") val targetId: String,
    @ColumnInfo(name = "account_id") val accountId: String?,
    @ColumnInfo(name = "project_id") val projectId: String?,
    @ColumnInfo(name = "enabled_tools_csv") val enabledToolsCsv: String,
    @ColumnInfo(name = "max_steps") val maxSteps: Int,
    @ColumnInfo(name = "max_tool_calls") val maxToolCalls: Int,
    @ColumnInfo(name = "max_runtime_ms") val maxRuntimeMs: Long,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean,
    @ColumnInfo(name = "created_at_epoch_ms") val createdAtEpochMs: Long,
    @ColumnInfo(name = "updated_at_epoch_ms") val updatedAtEpochMs: Long
)

@Entity(
    tableName = "agent_runs",
    foreignKeys = [ForeignKey(entity = AgentProfileEntity::class, parentColumns = ["id"], childColumns = ["agent_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["agent_id", "started_at_epoch_ms"]), Index(value = ["status"])]
)
data class AgentRunEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "agent_id") val agentId: String,
    val goal: String,
    val status: String,
    @ColumnInfo(name = "started_at_epoch_ms") val startedAtEpochMs: Long,
    @ColumnInfo(name = "completed_at_epoch_ms") val completedAtEpochMs: Long?,
    @ColumnInfo(name = "safe_error_message") val safeErrorMessage: String?
)

@Entity(
    tableName = "agent_run_steps",
    foreignKeys = [ForeignKey(entity = AgentRunEntity::class, parentColumns = ["id"], childColumns = ["run_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["run_id", "position"], unique = true)]
)
data class AgentRunStepEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "run_id") val runId: String,
    val position: Int,
    @ColumnInfo(name = "step_kind") val stepKind: String,
    val status: String,
    @ColumnInfo(name = "safe_summary") val safeSummary: String,
    @ColumnInfo(name = "created_at_epoch_ms") val createdAtEpochMs: Long,
    @ColumnInfo(name = "completed_at_epoch_ms") val completedAtEpochMs: Long?
)

@Entity(
    tableName = "agent_approvals",
    foreignKeys = [ForeignKey(entity = AgentRunEntity::class, parentColumns = ["id"], childColumns = ["run_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["run_id", "status"])]
)
data class AgentApprovalEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "run_id") val runId: String,
    @ColumnInfo(name = "tool_kind") val toolKind: String,
    @ColumnInfo(name = "target_path") val targetPath: String,
    val preview: String,
    val status: String,
    @ColumnInfo(name = "created_at_epoch_ms") val createdAtEpochMs: Long,
    @ColumnInfo(name = "resolved_at_epoch_ms") val resolvedAtEpochMs: Long?
)
