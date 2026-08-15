package dev.iliv007.ivai.router

import dev.iliv007.ivai.data.local.ProviderAccountEntity
import dev.iliv007.ivai.data.local.ProviderConnectionEntity
import dev.iliv007.ivai.data.local.ProviderModelEntity
import dev.iliv007.ivai.data.local.RouterComboEntryEntity
import dev.iliv007.ivai.provider.ProviderCapability

/** Read-only local catalog used by the foreground router; it never contains a secret value. */
data class RouterCatalog(
    val connections: List<ProviderConnectionEntity>,
    val accounts: List<ProviderAccountEntity>,
    val models: List<ProviderModelEntity>,
    val credentialPresent: Set<String>
)

/**
 * Produces an ordered, deterministic candidate list. Network calls, retries and trace persistence
 * belong to the caller so this resolver remains pure and directly unit-testable.
 */
class SequentialRouter {
    fun resolve(
        target: ExecutionTarget,
        comboEntries: List<RouterComboEntryEntity>,
        catalog: RouterCatalog,
        requiredCapabilities: Set<ProviderCapability>
    ): RouterResolution {
        val orderedEntries = when (target) {
            is ExecutionTarget.DirectModel -> listOf(
                RouterComboEntryEntity(
                    id = "direct-${target.connectionId}-${target.accountId}-${target.modelId}",
                    comboId = "direct",
                    position = 0,
                    connectionId = target.connectionId,
                    accountId = target.accountId,
                    modelId = target.modelId,
                    isEnabled = true
                )
            )
            is ExecutionTarget.Combo -> comboEntries
                .asSequence()
                .filter { it.isEnabled }
                .sortedWith(compareBy<RouterComboEntryEntity> { it.position }.thenBy { it.id })
                .toList()
        }

        val candidates = orderedEntries.mapNotNull { entry ->
            candidateFor(entry, catalog, requiredCapabilities)
        }
        return RouterResolution(target, candidates)
    }

    private fun candidateFor(
        entry: RouterComboEntryEntity,
        catalog: RouterCatalog,
        requiredCapabilities: Set<ProviderCapability>
    ): RouterCandidate? {
        val connection = catalog.connections.firstOrNull { it.id == entry.connectionId && it.isEnabled }
            ?: return null
        val account = catalog.accounts.firstOrNull {
            it.id == entry.accountId && it.connectionId == connection.id && it.isEnabled &&
                it.credentialReference in catalog.credentialPresent
        } ?: return null
        val model = catalog.models.firstOrNull {
            it.id == entry.modelId && it.connectionId == connection.id && it.isSelectable
        } ?: return null
        val capabilities = model.capabilitiesCsv.split(',')
            .filter(String::isNotBlank)
            .map(ProviderCapability::valueOf)
            .toSet()
        if (!capabilities.containsAll(requiredCapabilities)) return null
        return RouterCandidate(
            connectionId = connection.id,
            accountId = account.id,
            modelId = model.id,
            providerModelId = model.providerModelId,
            position = entry.position,
            capabilities = capabilities
        )
    }
}
