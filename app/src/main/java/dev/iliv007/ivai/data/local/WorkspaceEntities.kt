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
