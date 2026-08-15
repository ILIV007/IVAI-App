package dev.iliv007.ivai.ui.viewmodel

import dev.iliv007.ivai.router.RouterAttemptOutcome

data class RouterManagementState(
    val combos: List<RouterComboCard> = emptyList(),
    val latestAttempts: List<RouterAttemptCard> = emptyList(),
    val operationError: String? = null
)

data class RouterCandidateSelection(
    val connectionId: String,
    val accountId: String,
    val modelId: String
)

data class RouterComboCard(
    val comboId: String,
    val displayName: String,
    val description: String,
    val enabled: Boolean,
    val entries: List<RouterComboEntryCard>
)

data class RouterComboEntryCard(
    val entryId: String,
    val position: Int,
    val providerLabel: String,
    val accountLabel: String,
    val modelLabel: String,
    val capabilities: List<String>,
    val enabled: Boolean
)

data class RouterAttemptCard(
    val attemptId: String,
    val targetLabel: String,
    val outcome: RouterAttemptOutcome,
    val safeErrorMessage: String?
)
