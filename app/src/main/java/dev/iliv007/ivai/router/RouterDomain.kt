package dev.iliv007.ivai.router

import dev.iliv007.ivai.provider.ProviderCapability

/** A deliberate user choice for a thread; no provider is selected implicitly. */
sealed interface ExecutionTarget {
    val stableId: String

    data class DirectModel(
        val connectionId: String,
        val accountId: String,
        val modelId: String
    ) : ExecutionTarget {
        override val stableId: String = modelId
    }

    data class Combo(
        val comboId: String
    ) : ExecutionTarget {
        override val stableId: String = comboId
    }
}

enum class ExecutionTargetKind {
    DIRECT_MODEL,
    COMBO
}

enum class RouterAttemptOutcome {
    RUNNING,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    SKIPPED_UNAVAILABLE,
    SKIPPED_CAPABILITY_MISMATCH
}

data class RouterCandidate(
    val connectionId: String,
    val accountId: String,
    val modelId: String,
    val providerModelId: String,
    val position: Int,
    val capabilities: Set<ProviderCapability>
)

data class RouterResolution(
    val target: ExecutionTarget,
    val candidates: List<RouterCandidate>
)

fun ExecutionTarget.kind(): ExecutionTargetKind = when (this) {
    is ExecutionTarget.DirectModel -> ExecutionTargetKind.DIRECT_MODEL
    is ExecutionTarget.Combo -> ExecutionTargetKind.COMBO
}
