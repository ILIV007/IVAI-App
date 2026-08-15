package dev.iliv007.ivai.data.local

import androidx.room.withTransaction
import dev.iliv007.ivai.provider.CredentialReference
import dev.iliv007.ivai.provider.ManualProviderModel
import dev.iliv007.ivai.provider.ProviderAccountDescriptor
import dev.iliv007.ivai.provider.ProviderCapability
import dev.iliv007.ivai.provider.ProviderConnectionDescriptor
import dev.iliv007.ivai.provider.ProviderKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Local-only workspace boundary for Phase 2. UI wiring is intentionally deferred until the
 * persistence migration and security boundaries can be reviewed independently.
 */
data class PersistedWorkspaceSnapshot(
    val projects: List<WorkspaceProjectEntity>,
    val threads: List<ChatThreadEntity>
)

data class LocalWorkspaceArchiveSnapshot(
    val projects: List<WorkspaceProjectEntity>,
    val threads: List<ChatThreadEntity>,
    val messages: List<ChatMessageEntity>
)

data class PersistedProviderRegistrySnapshot(
    val connections: List<ProviderConnectionEntity>,
    val accounts: List<ProviderAccountEntity>,
    val models: List<ProviderModelEntity>
)

class LocalWorkspaceRepository(
    private val database: IvaiDatabase,
    private val projectDao: WorkspaceProjectDao = database.projectDao(),
    private val threadDao: ChatThreadDao = database.threadDao(),
    private val messageDao: ChatMessageDao = database.messageDao(),
    private val providerConnectionDao: ProviderConnectionDao = database.providerConnectionDao(),
    private val providerAccountDao: ProviderAccountDao = database.providerAccountDao(),
    private val providerModelDao: ProviderModelDao = database.providerModelDao()
) {
    fun observeWorkspace(): Flow<PersistedWorkspaceSnapshot> = combine(
        projectDao.observeAll(),
        threadDao.observeAll()
    ) { projects, threads ->
        PersistedWorkspaceSnapshot(projects = projects, threads = threads)
    }

    fun observeMessages(threadId: String): Flow<List<ChatMessageEntity>> =
        messageDao.observeForThread(threadId)

    fun observeProviderRegistry(): Flow<PersistedProviderRegistrySnapshot> = combine(
        providerConnectionDao.observeAll(),
        providerAccountDao.observeAll(),
        providerModelDao.observeAll()
    ) { connections, accounts, models ->
        PersistedProviderRegistrySnapshot(connections, accounts, models)
    }

    suspend fun saveProject(project: WorkspaceProjectEntity) {
        projectDao.upsert(project)
    }

    suspend fun saveThread(thread: ChatThreadEntity) {
        threadDao.upsert(thread)
    }

    suspend fun appendMessage(message: ChatMessageEntity) {
        messageDao.insert(message)
    }

    suspend fun saveProviderConnection(connection: ProviderConnectionEntity) {
        ProviderConnectionDescriptor(
            id = connection.id,
            kind = ProviderKind.valueOf(connection.providerKind),
            displayName = connection.displayName,
            baseUrl = connection.baseUrl,
            enabled = connection.isEnabled
        )
        providerConnectionDao.upsert(connection)
    }

    suspend fun saveProviderAccount(account: ProviderAccountEntity) {
        require(providerConnectionDao.findById(account.connectionId) != null) { "Unknown provider connection" }
        ProviderAccountDescriptor(
            id = account.id,
            connectionId = account.connectionId,
            displayName = account.displayName,
            credentialReference = CredentialReference(account.credentialReference),
            enabled = account.isEnabled
        )
        providerAccountDao.upsert(account)
    }

    suspend fun saveProviderModel(model: ProviderModelEntity) {
        require(providerConnectionDao.findById(model.connectionId) != null) { "Unknown provider connection" }
        ManualProviderModel(
            id = model.id,
            connectionId = model.connectionId,
            providerModelId = model.providerModelId,
            displayName = model.displayName,
            capabilities = model.capabilitiesCsv.split(',').filter(String::isNotBlank)
                .map(ProviderCapability::valueOf).toSet(),
            selectable = model.isSelectable
        )
        providerModelDao.upsert(model)
    }

    suspend fun deleteProviderConnection(connectionId: String) {
        val connection = providerConnectionDao.findById(connectionId) ?: return
        providerConnectionDao.delete(connection)
    }

    suspend fun deleteThread(threadId: String) {
        val thread = threadDao.findById(threadId) ?: return
        threadDao.delete(thread)
    }

    suspend fun snapshotForArchive(): LocalWorkspaceArchiveSnapshot = database.withTransaction {
        LocalWorkspaceArchiveSnapshot(
            projects = projectDao.listAll(),
            threads = threadDao.listAll(),
            messages = messageDao.listAll()
        )
    }

    /**
     * Commits a previously validated local-only snapshot atomically. Secret material is not part
     * of [LocalWorkspaceArchiveSnapshot] and cannot be restored through this boundary.
     */
    suspend fun replaceFromArchive(snapshot: LocalWorkspaceArchiveSnapshot) {
        database.withTransaction {
            // Archive v1 deliberately excludes provider records and all secrets. Keeping the
            // existing local registry prevents an older workspace archive from deleting a user's
            // current provider configuration; provider archive support requires its own versioned
            // format change and migration.
            messageDao.deleteAll()
            threadDao.deleteAll()
            projectDao.deleteAll()

            for (project in snapshot.projects) {
                projectDao.upsert(project)
            }
            for (thread in snapshot.threads) {
                threadDao.upsert(thread)
            }
            for (message in snapshot.messages) {
                messageDao.insert(message)
            }
        }
    }

    suspend fun deleteAllWorkspaceData() {
        database.withTransaction {
            providerModelDao.deleteAll()
            providerAccountDao.deleteAll()
            providerConnectionDao.deleteAll()
            messageDao.deleteAll()
            threadDao.deleteAll()
            projectDao.deleteAll()
        }
    }
}
