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
    @ColumnInfo(name = "updated_at_epoch_ms") val updatedAtEpochMs: Long
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
    @ColumnInfo(name = "credential_reference") val credentialReference: String,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean,
    @ColumnInfo(name = "created_at_epoch_ms") val createdAtEpochMs: Long,
    @ColumnInfo(name = "updated_at_epoch_ms") val updatedAtEpochMs: Long
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
