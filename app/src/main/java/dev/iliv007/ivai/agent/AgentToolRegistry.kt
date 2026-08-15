package dev.iliv007.ivai.agent

import java.time.Instant
import java.time.ZoneOffset

sealed interface AgentToolRequest {
    val kind: AgentToolKind

    data class Calculate(val expression: String) : AgentToolRequest {
        override val kind = AgentToolKind.CALCULATE
    }

    data object CurrentTime : AgentToolRequest {
        override val kind = AgentToolKind.CURRENT_TIME
    }

    data class WriteProjectFile(val relativePath: String, val content: String) : AgentToolRequest {
        override val kind = AgentToolKind.WRITE_PROJECT_FILE
    }
}

sealed interface AgentToolResult {
    data class Completed(val safeSummary: String) : AgentToolResult
    data class RequiresApproval(val targetPath: String, val preview: String) : AgentToolResult
    data class Rejected(val safeReason: String) : AgentToolResult
}

/** Safe subset only. Destructive writes are represented as approval requests and never executed here. */
class AgentToolRegistry(
    private val nowEpochMs: () -> Long = System::currentTimeMillis
) {
    fun evaluate(request: AgentToolRequest): AgentToolResult = when (request) {
        is AgentToolRequest.Calculate -> calculate(request.expression)
        AgentToolRequest.CurrentTime -> AgentToolResult.Completed(
            "UTC time: ${Instant.ofEpochMilli(nowEpochMs()).atOffset(ZoneOffset.UTC)}"
        )
        is AgentToolRequest.WriteProjectFile -> {
            val path = request.relativePath.trim()
            if (!isSafeRelativePath(path)) AgentToolResult.Rejected("Project file path is invalid.")
            else AgentToolResult.RequiresApproval(
                targetPath = path,
                preview = request.content.take(MAX_PREVIEW_CHARS)
            )
        }
    }

    private fun calculate(expression: String): AgentToolResult {
        val normalized = expression.trim()
        if (!normalized.matches(Regex("[0-9+\\-*/().\\s]+"))) {
            return AgentToolResult.Rejected("Calculator accepts only numeric arithmetic expressions.")
        }
        return AgentToolResult.Completed("Calculator request accepted: $normalized")
    }

    private fun isSafeRelativePath(path: String): Boolean =
        path.isNotBlank() && !path.startsWith('/') && !path.contains('\\') &&
            path.split('/').none { it.isBlank() || it == "." || it == ".." }

    private companion object {
        const val MAX_PREVIEW_CHARS = 4_000
    }
}
