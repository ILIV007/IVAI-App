package dev.iliv007.ivai.ui.viewmodel

import dev.iliv007.ivai.provider.ProviderKind

data class ProviderConnectionCard(
    val connectionId: String,
    val kind: ProviderKind,
    val displayName: String,
    val baseUrlLabel: String?,
    val enabled: Boolean,
    val accounts: List<ProviderAccountCard>,
    val manualModels: List<ProviderModelCard>
)

data class ProviderAccountCard(
    val accountId: String,
    val displayName: String,
    val credentialReference: String,
    val enabled: Boolean,
    val credentialStored: Boolean
)

data class ProviderModelCard(
    val modelId: String,
    val displayName: String,
    val capabilities: List<String>,
    val selectable: Boolean
)

data class ProviderManagementState(
    val connections: List<ProviderConnectionCard> = emptyList(),
    val operationError: String? = null
)
