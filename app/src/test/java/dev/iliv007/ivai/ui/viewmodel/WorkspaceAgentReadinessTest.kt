package dev.iliv007.ivai.ui.viewmodel

import android.os.Looper
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.iliv007.ivai.data.local.IvaiDatabase
import dev.iliv007.ivai.data.local.LocalWorkspaceRepository
import dev.iliv007.ivai.data.local.ProviderAccountEntity
import dev.iliv007.ivai.data.local.ProviderConnectionEntity
import dev.iliv007.ivai.data.local.ProviderModelEntity
import dev.iliv007.ivai.provider.ProviderAccountAuthMode
import dev.iliv007.ivai.security.EncryptedSecretPayload
import dev.iliv007.ivai.security.EncryptedSecretVault
import dev.iliv007.ivai.security.SecretCipher
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.robolectric.Shadows.shadowOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WorkspaceAgentReadinessTest {
    private lateinit var database: IvaiDatabase
    private lateinit var repository: LocalWorkspaceRepository
    private lateinit var vault: EncryptedSecretVault
    private lateinit var preferencesFile: File

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            IvaiDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = LocalWorkspaceRepository(database)
        preferencesFile = File(
            ApplicationProvider.getApplicationContext<android.content.Context>().cacheDir,
            "agent-readiness-${System.nanoTime()}.preferences_pb"
        )
        vault = EncryptedSecretVault(
            PreferenceDataStoreFactory.create { preferencesFile },
            cipherForReference = { TestCipher() }
        )
    }

    @After
    fun tearDown() {
        database.close()
        preferencesFile.delete()
    }

    @Test
    fun `agent direct target follows non-sensitive vault readiness`() = runBlocking {
        val connection = ProviderConnectionEntity(
            id = "test-provider",
            providerKind = "GEMINI",
            displayName = "Test provider",
            baseUrl = null,
            isEnabled = true,
            createdAtEpochMs = 1L,
            updatedAtEpochMs = 1L
        )
        val account = ProviderAccountEntity(
            id = "test-account",
            connectionId = connection.id,
            displayName = "Test account",
            credentialReference = "test-credential",
            isEnabled = true,
            createdAtEpochMs = 1L,
            updatedAtEpochMs = 1L,
            authMode = ProviderAccountAuthMode.API_KEY.name
        )
        val model = ProviderModelEntity(
            id = "test-model",
            connectionId = connection.id,
            providerModelId = "user-selected-model",
            displayName = "User selected model",
            capabilitiesCsv = "TEXT,STREAMING",
            isManual = true,
            isSelectable = true,
            updatedAtEpochMs = 1L
        )
        repository.saveProviderConnection(connection)
        repository.saveProviderAccount(account)
        repository.saveProviderModel(model)

        val viewModel = WorkspaceViewModel(workspaceRepository = repository, secretVault = vault)
        waitForUi { viewModel.providerManagementState.value.connections.singleOrNull()?.connectionId == connection.id }

        vault.store(account.credentialReference, "test-only-value")
        waitForUi {
            viewModel.agentManagementState.value.availableTargets.map { it.targetId } == listOf(model.id)
        }

        vault.clear(account.credentialReference)
        waitForUi { viewModel.agentManagementState.value.availableTargets.isEmpty() }
        assertEquals(emptyList<String>(), viewModel.agentManagementState.value.availableTargets.map { it.targetId })
    }

    private suspend fun waitForUi(predicate: () -> Boolean) {
        withTimeout(5_000) {
            while (!predicate()) {
                shadowOf(Looper.getMainLooper()).idle()
                delay(20)
            }
        }
    }

    private class TestCipher : SecretCipher {
        override fun encrypt(plaintext: ByteArray): EncryptedSecretPayload = EncryptedSecretPayload(
            version = EncryptedSecretPayload.CURRENT_VERSION,
            iv = "test-iv".encodeToByteArray(),
            ciphertext = plaintext.reversedArray()
        )

        override fun decrypt(payload: EncryptedSecretPayload): ByteArray = payload.ciphertext.reversedArray()

        override fun deleteKey() = Unit
    }
}
