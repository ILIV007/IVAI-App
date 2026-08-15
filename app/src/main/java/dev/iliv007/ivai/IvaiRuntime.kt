package dev.iliv007.ivai

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import dev.iliv007.ivai.chat.LocalGeminiChatSession
import dev.iliv007.ivai.data.local.IvaiDatabase
import dev.iliv007.ivai.data.local.LocalWorkspaceRepository
import dev.iliv007.ivai.provider.gemini.GeminiChatProvider
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

    val geminiProvider = GeminiChatProvider(GeminiNetworkGate(secretVault))
    val geminiChatSession = LocalGeminiChatSession(geminiProvider, workspaceRepository)
}
