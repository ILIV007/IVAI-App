package dev.iliv007.ivai

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import dev.iliv007.ivai.agent.AgentToolRegistry
import dev.iliv007.ivai.agent.BasicAgentRuntime
import dev.iliv007.ivai.chat.LocalGeminiChatSession
import dev.iliv007.ivai.chat.LocalProviderChatSession
import dev.iliv007.ivai.data.local.IvaiDatabase
import dev.iliv007.ivai.data.local.LocalDataResetter
import dev.iliv007.ivai.data.local.LocalWorkspaceRepository
import dev.iliv007.ivai.data.local.ProjectWorkspace
import dev.iliv007.ivai.provider.ChatProvider
import dev.iliv007.ivai.provider.ProviderAdapterRegistry
import dev.iliv007.ivai.provider.ProviderKind
import dev.iliv007.ivai.provider.ProviderEndpointTrustMode
import dev.iliv007.ivai.provider.gemini.GeminiChatProvider
import dev.iliv007.ivai.provider.openai.CustomOpenAiChatProvider
import dev.iliv007.ivai.provider.openai.OpenRouterChatProvider
import dev.iliv007.ivai.provider.gemini.GeminiNetworkGate
import dev.iliv007.ivai.security.AndroidKeystoreSecretCipher
import dev.iliv007.ivai.router.RouterChatSession
import dev.iliv007.ivai.router.SequentialRouter
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

    val projectWorkspace = ProjectWorkspace.appPrivate(applicationContext)
    val localDataResetter = LocalDataResetter(workspaceRepository, projectWorkspace, secretVault)
    val agentRuntime = BasicAgentRuntime(workspaceRepository, projectWorkspace, AgentToolRegistry())

    private val geminiProvider = GeminiChatProvider(GeminiNetworkGate(secretVault))
    private val openRouterProvider = OpenRouterChatProvider(secretVault)

    /** Registry contains installed adapters only; user-managed records decide whether any is used. */
    val providerAdapters = ProviderAdapterRegistry(setOf(geminiProvider, openRouterProvider))
    val providerChatSession = LocalProviderChatSession(workspaceRepository)
    val routerChatSession = RouterChatSession(
        workspace = workspaceRepository,
        router = SequentialRouter(),
        providerResolver = ::resolveProvider
    )

    /** Resolves a foreground adapter from user-owned connection metadata without exposing secrets. */
    fun resolveProvider(
        kind: ProviderKind,
        baseUrl: String?,
        trustMode: ProviderEndpointTrustMode = ProviderEndpointTrustMode.REMOTE_HTTPS
    ): ChatProvider = when (kind) {
        ProviderKind.CUSTOM_OPENAI_COMPATIBLE -> CustomOpenAiChatProvider(
            baseUrl = requireNotNull(baseUrl) { "Custom provider endpoint is required" },
            trustMode = trustMode,
            vault = secretVault
        )
        else -> {
            require(trustMode == ProviderEndpointTrustMode.REMOTE_HTTPS) {
                "Managed provider adapters do not support local endpoint trust modes"
            }
            providerAdapters.requireAdapter(kind)
        }
    }

    /**
     * Compatibility bridge for the pre-registry chat UI. It is removed when Provider Management
     * replaces the fixed Gemini selection; new code must use [providerAdapters] and
     * [providerChatSession].
     */
    @Deprecated("Use providerAdapters with providerChatSession")
    val geminiChatSession = LocalGeminiChatSession(geminiProvider, workspaceRepository)
}
