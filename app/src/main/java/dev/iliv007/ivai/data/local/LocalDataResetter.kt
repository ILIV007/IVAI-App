package dev.iliv007.ivai.data.local

import dev.iliv007.ivai.security.EncryptedSecretVault

/**
 * Coordinates destructive local reset without reading any credential values. All cleanup steps
 * are attempted so a caller receives a failure only after the maximum safe cleanup occurred.
 */
class LocalDataResetter(
    private val repository: LocalWorkspaceRepository,
    private val workspace: ProjectWorkspace,
    private val vault: EncryptedSecretVault
) {
    suspend fun deleteAllData() {
        var failure: Throwable? = null

        suspend fun attempt(cleanup: suspend () -> Unit) {
            runCatching { cleanup() }.onFailure { currentFailure ->
                if (failure == null) {
                    failure = currentFailure
                } else {
                    failure?.addSuppressed(currentFailure)
                }
            }
        }

        attempt(workspace::deleteAllProjectFiles)
        attempt(repository::deleteAllWorkspaceData)
        attempt(vault::clearAll)

        failure?.let { currentFailure ->
            throw IllegalStateException("Local data reset did not complete", currentFailure)
        }
    }
}
