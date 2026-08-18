package dev.iliv007.ivai.ui.model

enum class MessageSender {
    USER,
    ASSISTANT,
    SYSTEM
}

enum class MessageContentType {
    TEXT,
    CODE,
    MIXED_BIDI
}

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: String,
    val type: MessageContentType = MessageContentType.TEXT,
    val codeSnippet: String? = null,
    val modelBadge: String? = null,
    val latencyMs: Long? = null,
    /** True only when a visible provider stream ended before its completion event. */
    val isIncomplete: Boolean = false
)

data class ChatThread(
    val id: String,
    val title: String,
    val snippet: String,
    val timestamp: String,
    val modelOrCombo: String,
    val messages: List<ChatMessage>,
    val projectId: String? = null,
    val projectName: String? = null
)

data class BasicAgentProfile(
    val id: String,
    val name: String,
    val description: String,
    val role: String,
    val stepLimit: Int,
    val allowedTools: List<String>,
    val requiresWriteConfirmation: Boolean,
    val boundModelOrCombo: String
)

data class AgentRunStep(
    val stepNumber: Int,
    val actionName: String,
    val targetOrInput: String,
    val status: StepStatus,
    val durationMs: Long,
    val details: String? = null
)

enum class StepStatus {
    COMPLETED,
    IN_PROGRESS,
    PENDING,
    CONFIRMATION_REQUIRED
}

data class WorkspaceProject(
    val id: String,
    val name: String,
    val description: String,
    val fileCount: Int,
    val lastModified: String
)

data class RouterComboMember(
    val priority: Int,
    val provider: String,
    val modelId: String,
    val timeoutSec: Int,
    val maxRetries: Int
)

data class RouterCombo(
    val id: String,
    val name: String,
    val description: String,
    val members: List<RouterComboMember>,
    val fallbackStrategy: String
)

enum class UiPreviewState {
    NORMAL,
    LOADING,
    EMPTY,
    ERROR,
    OFFLINE
}
