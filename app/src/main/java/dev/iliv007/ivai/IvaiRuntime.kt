package dev.iliv007.ivai

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import dev.iliv007.ivai.chat.LocalGeminiChatSession
import dev.iliv007.ivai.chat.LocalProviderChatSession
import dev.iliv007.ivai.data.local.IvaiDatabase
import dev.iliv007.ivai.data.local.LocalWorkspaceRepository
import dev.iliv007.ivai.provider.ProviderAdapterRegistry
import dev.iliv007.ivai.provider.gemini.GeminiChatProvider
import dev.iliv007.ivai.provider.openai.OpenRouterChatProvider
import dev.iliv007.ivai.provider.gemini.GeminiNetworkGate
import dev.iliv007.ivai.security.AndroidKeystoreSecretCipher
import dev.iliv007.ivai.security.EncryptedSecretVault
import java.io.File

/**
 * Local composition root. It owns no network service and creates no background work; Gemini is
 * invoked only by a foreground ViewModel action through [GeminiNetworkGate].
 */
class IvaiRuntime(context: Context) {
    private val applicationContext = context.applicationContext

    val workspaceRepository = LocalWorkspaceRepository(IvaiDatabase.create(applicationContext))

    val secretVault = EncryptedSecretVault(
        dataStore = PreferenceDataStoreFactory.create(
            produceFile = {
                File(applicationContext.filesDir, "ivai/secure-vault.preferences_pb").apply {
                    parentFile?.mkdirs()
                }
            }
        ),
        cipherForReference = { keyAlias ->
            AndroidKeystoreSecretCipher(alias = keyAlias)
        }
    )

    private val geminiProvider = GeminiChatProvider(GeminiNetworkGate(secretVault))
    private val openRouterProvider = OpenRouterChatProvider(secretVault)

    /** Registry contains installed adapters only; user-managed records decide whether any is used. */
    val providerAdapters = ProviderAdapterRegistry(setOf(geminiProvider, openRouterProvider))
    val providerChatSession = LocalProviderChatSession(workspaceRepository)

    /**
     * Compatibility bridge for the pre-registry chat UI. It is removed when Provider Management
     * replaces the fixed Gemini selection; new code must use [providerAdapters] and
     * [providerChatSession].
     */
    @Deprecated("Use providerAdapters with providerChatSession")
    val geminiChatSession = LocalGeminiChatSession(geminiProvider, workspaceRepository)
}
