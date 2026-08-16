package dev.iliv007.ivai.ui.viewmodel

import dev.iliv007.ivai.provider.ProviderAccountAuthMode
import dev.iliv007.ivai.provider.ProviderEndpointTrustMode
import dev.iliv007.ivai.provider.ProviderKind

data class ProviderConnectionCard(
    val connectionId: String,
    val kind: ProviderKind,
    val displayName: String,
    val baseUrlLabel: String?,
    val endpointTrustMode: ProviderEndpointTrustMode,
    val localTrustConfirmed: Boolean,
    val enabled: Boolean,
    val accounts: List<ProviderAccountCard>,
    val manualModels: List<ProviderModelCard>
)

data class ProviderAccountCard(
    val accountId: String,
    val displayName: String,
    val credentialReference: String,
    val authMode: ProviderAccountAuthMode,
    val enabled: Boolean,
    val credentialStored: Boolean
)

data class ProviderModelCard(
    val registryModelId: String,
    val modelId: String,
    val displayName: String,
    val capabilities: List<String>,
    val selectable: Boolean
)

data class ProviderManagementState(
    val connections: List<ProviderConnectionCard> = emptyList(),
    val operationError: String? = null
)
