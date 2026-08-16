package dev.iliv007.ivai.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.iliv007.ivai.provider.ProviderAccountAuthMode
import dev.iliv007.ivai.provider.ProviderCapability
import dev.iliv007.ivai.provider.ProviderEndpointTrustMode
import dev.iliv007.ivai.provider.noAuthCredentialMarker
import dev.iliv007.ivai.router.ExecutionTarget
import dev.iliv007.ivai.router.RouterCatalog
import dev.iliv007.ivai.router.SequentialRouter
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LocalEndpointTrustRepositoryTest {
    private lateinit var database: IvaiDatabase
    private lateinit var repository: LocalWorkspaceRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            IvaiDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = LocalWorkspaceRepository(database)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `confirmed local no-auth account is router-usable without credential presence`() = runBlocking {
        val connection = ProviderConnectionEntity(
            id = "local",
            providerKind = "CUSTOM_OPENAI_COMPATIBLE",
            displayName = "Local HTTPS",
            baseUrl = "https://localhost:1234/v1",
            isEnabled = true,
            createdAtEpochMs = 1L,
            updatedAtEpochMs = 1L,
            endpointTrustMode = ProviderEndpointTrustMode.LOCAL_LOOPBACK_HTTPS.name,
            localTrustConfirmedAtEpochMs = 1L
        )
        val account = ProviderAccountEntity(
            id = "local-account",
            connectionId = connection.id,
            displayName = "No key",
            credentialReference = noAuthCredentialMarker("local-account"),
            isEnabled = true,
            createdAtEpochMs = 1L,
            updatedAtEpochMs = 1L,
            authMode = ProviderAccountAuthMode.NONE.name
        )
        val model = ProviderModelEntity(
            id = "local-model",
            connectionId = connection.id,
            providerModelId = "user-local-model",
            displayName = "User local model",
            capabilitiesCsv = "TEXT,STREAMING",
            isManual = true,
            isSelectable = true,
            updatedAtEpochMs = 1L
        )
        repository.saveProviderConnection(connection)
        repository.saveProviderAccount(account)
        repository.saveProviderModel(model)

        val snapshot = repository.currentProviderRegistry()
        val resolution = SequentialRouter().resolve(
            target = ExecutionTarget.DirectModel(connection.id, account.id, model.id),
            comboEntries = emptyList(),
            catalog = RouterCatalog(snapshot.connections, snapshot.accounts, snapshot.models, credentialPresent = emptySet()),
            requiredCapabilities = setOf(ProviderCapability.TEXT, ProviderCapability.STREAMING)
        )

        assertEquals(listOf(model.id), resolution.candidates.map { it.modelId })
        assertTrue(resolution.candidates.single().providerModelId == "user-local-model")
    }

    @Test
    fun `no-auth account is rejected for remote HTTPS connection`() = runBlocking {
        val connection = ProviderConnectionEntity(
            id = "remote",
            providerKind = "CUSTOM_OPENAI_COMPATIBLE",
            displayName = "Remote HTTPS",
            baseUrl = "https://api.example.com/v1",
            isEnabled = true,
            createdAtEpochMs = 1L,
            updatedAtEpochMs = 1L
        )
        repository.saveProviderConnection(connection)

        val failure = runCatching {
            repository.saveProviderAccount(
                ProviderAccountEntity(
                    id = "remote-no-auth",
                    connectionId = connection.id,
                    displayName = "Invalid",
                    credentialReference = noAuthCredentialMarker("remote-no-auth"),
                    isEnabled = true,
                    createdAtEpochMs = 1L,
                    updatedAtEpochMs = 1L,
                    authMode = ProviderAccountAuthMode.NONE.name
                )
            )
        }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
    }
}
