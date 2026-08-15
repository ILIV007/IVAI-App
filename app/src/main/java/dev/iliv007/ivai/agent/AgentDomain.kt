package dev.iliv007.ivai.agent

enum class AgentRunStatus {
    DRAFT,
    RUNNING,
    AWAITING_APPROVAL,
    PAUSED_ERROR,
    COMPLETED,
    CANCELLED,
    FAILED
}

enum class AgentToolKind(val requiresApproval: Boolean) {
    CALCULATE(false),
    CURRENT_TIME(false),
    READ_PROJECT_FILE(false),
    LIST_WORKSPACE(false),
    WRITE_PROJECT_FILE(true)
}

enum class ApprovalStatus {
    PENDING,
    ALLOWED_ONCE,
    DENIED,
    EXECUTED
}

data class AgentExecutionLimits(
    val maxSteps: Int,
    val maxToolCalls: Int,
    val maxRuntimeMs: Long
) {
    init {
        require(maxSteps in 1..20) { "Agent max steps must be between 1 and 20" }
        require(maxToolCalls in 0..20) { "Agent max tool calls must be between 0 and 20" }
        require(maxRuntimeMs in 1_000L..300_000L) { "Agent max runtime must be between 1 second and 5 minutes" }
    }
}
