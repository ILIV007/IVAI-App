package dev.iliv007.ivai.data.local

import androidx.room.withTransaction
import dev.iliv007.ivai.provider.CredentialReference
import dev.iliv007.ivai.provider.ManualProviderModel
import dev.iliv007.ivai.provider.ProviderAccountDescriptor
import dev.iliv007.ivai.provider.ProviderCapability
import dev.iliv007.ivai.provider.ProviderConnectionDescriptor
import dev.iliv007.ivai.provider.ProviderKind
import dev.iliv007.ivai.router.ExecutionTarget
import dev.iliv007.ivai.router.ExecutionTargetKind
import kotlinx.coroutines.flow.first
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

data class PersistedRouterSnapshot(
    val combos: List<RouterComboEntity>,
    val entries: List<RouterComboEntryEntity>
)

class LocalWorkspaceRepository(
    private val database: IvaiDatabase,
    private val projectDao: WorkspaceProjectDao = database.projectDao(),
    private val threadDao: ChatThreadDao = database.threadDao(),
    private val messageDao: ChatMessageDao = database.messageDao(),
    private val providerConnectionDao: ProviderConnectionDao = database.providerConnectionDao(),
    private val providerAccountDao: ProviderAccountDao = database.providerAccountDao(),
    private val providerModelDao: ProviderModelDao = database.providerModelDao(),
    private val routerComboDao: RouterComboDao = database.routerComboDao(),
    private val routerComboEntryDao: RouterComboEntryDao = database.routerComboEntryDao(),
    private val threadExecutionTargetDao: ThreadExecutionTargetDao = database.threadExecutionTargetDao(),
    private val routerAttemptDao: RouterAttemptDao = database.routerAttemptDao(),
    private val routerAttemptEntryDao: RouterAttemptEntryDao = database.routerAttemptEntryDao(),
    private val agentProfileDao: AgentProfileDao = database.agentProfileDao(),
    private val agentRunDao: AgentRunDao = database.agentRunDao(),
    private val agentRunStepDao: AgentRunStepDao = database.agentRunStepDao(),
    private val agentApprovalDao: AgentApprovalDao = database.agentApprovalDao()
) {
    fun observeAgentProfiles(): Flow<List<AgentProfileEntity>> = agentProfileDao.observeAll()

    fun observeAgentRuns(agentId: String): Flow<List<AgentRunEntity>> = agentRunDao.observeForAgent(agentId)

    fun observeAllAgentRuns(): Flow<List<AgentRunEntity>> = agentRunDao.observeAll()

    fun observeAgentRunSteps(runId: String): Flow<List<AgentRunStepEntity>> = agentRunStepDao.observeForRun(runId)

    fun observeAgentApprovals(runId: String): Flow<List<AgentApprovalEntity>> = agentApprovalDao.observeForRun(runId)

    fun observePendingAgentApprovals(): Flow<List<AgentApprovalEntity>> =
        agentApprovalDao.observeByStatus("PENDING")

    fun observeWorkspace(): Flow<PersistedWorkspaceSnapshot> = combine(
        projectDao.observeAll(),
        threadDao.observeAll()
    ) { projects, threads ->
        PersistedWorkspaceSnapshot(projects = projects, threads = threads)
    }

    fun observeMessages(threadId: String): Flow<List<ChatMessageEntity>> =
        messageDao.observeForThread(threadId)

    fun observeRouter(): Flow<PersistedRouterSnapshot> = combine(
        routerComboDao.observeAll(),
        routerComboEntryDao.observeAll()
    ) { combos, entries -> PersistedRouterSnapshot(combos, entries) }

    fun observeComboEntries(comboId: String): Flow<List<RouterComboEntryEntity>> =
        routerComboEntryDao.observeForCombo(comboId)

    suspend fun resolveThreadExecutionTarget(threadId: String): ExecutionTarget? {
        val stored = threadExecutionTargetDao.findForThread(threadId) ?: return null
        return when (ExecutionTargetKind.valueOf(stored.targetKind)) {
            ExecutionTargetKind.DIRECT_MODEL -> {
                val model = providerModelDao.findById(stored.targetId) ?: return null
                val accountId = stored.accountId ?: return null
                ExecutionTarget.DirectModel(model.connectionId, accountId, model.id)
            }
            ExecutionTargetKind.COMBO -> ExecutionTarget.Combo(stored.targetId)
        }
    }

    suspend fun listRouterComboEntries(comboId: String): List<RouterComboEntryEntity> =
        routerComboEntryDao.listForCombo(comboId)

    suspend fun currentProviderRegistry(): PersistedProviderRegistrySnapshot =
        observeProviderRegistry().first()

    fun observeThreadExecutionTarget(threadId: String): Flow<ThreadExecutionTargetEntity?> =
        threadExecutionTargetDao.observeForThread(threadId)

    fun observeRouterAttempts(threadId: String): Flow<List<RouterAttemptEntity>> =
        routerAttemptDao.observeForThread(threadId)

    fun observeAllRouterAttempts(): Flow<List<RouterAttemptEntity>> =
        routerAttemptDao.observeAll()

    fun observeRouterAttemptEntries(attemptId: String): Flow<List<RouterAttemptEntryEntity>> =
        routerAttemptEntryDao.observeForAttempt(attemptId)

    fun observeProviderRegistry(): Flow<PersistedProviderRegistrySnapshot> = combine(
        providerConnectionDao.observeAll(),
        providerAccountDao.observeAll(),
        providerModelDao.observeAll()
    ) { connections, accounts, models ->
        PersistedProviderRegistrySnapshot(connections, accounts, models)
    }

    suspend fun saveRouterCombo(combo: RouterComboEntity, entries: List<RouterComboEntryEntity>) {
        require(combo.displayName.isNotBlank()) { "Combo name must not be blank" }
        require(entries.isNotEmpty()) { "A combo needs at least one entry" }
        require(entries.map { it.position }.sorted() == entries.indices.toList()) {
            "Combo entry positions must be consecutive from zero"
        }
        database.withTransaction {
            entries.forEach { entry -> validateRouterCandidate(entry.connectionId, entry.accountId, entry.modelId) }
            routerComboDao.upsert(combo)
            routerComboEntryDao.deleteForCombo(combo.id)
            entries.forEach { entry ->
                require(entry.comboId == combo.id) { "Combo entry belongs to another combo" }
                routerComboEntryDao.upsert(entry)
            }
        }
    }

    suspend fun selectThreadExecutionTarget(threadId: String, target: ExecutionTarget) {
        require(threadDao.findById(threadId) != null) { "Unknown chat thread" }
        val entity = when (target) {
            is ExecutionTarget.DirectModel -> {
                validateRouterCandidate(target.connectionId, target.accountId, target.modelId)
                ThreadExecutionTargetEntity(threadId, ExecutionTargetKind.DIRECT_MODEL.name, target.modelId, target.accountId, System.currentTimeMillis())
            }
            is ExecutionTarget.Combo -> {
                require(routerComboDao.findById(target.comboId)?.isEnabled == true) { "Unknown or disabled combo" }
                ThreadExecutionTargetEntity(threadId, ExecutionTargetKind.COMBO.name, target.comboId, null, System.currentTimeMillis())
            }
        }
        threadExecutionTargetDao.upsert(entity)
    }

    suspend fun saveRouterAttempt(attempt: RouterAttemptEntity) {
        routerAttemptDao.upsert(attempt)
    }

    suspend fun saveRouterAttemptEntry(entry: RouterAttemptEntryEntity) {
        routerAttemptEntryDao.upsert(entry)
    }

    private suspend fun validateRouterCandidate(connectionId: String, accountId: String, modelId: String) {
        val connection = providerConnectionDao.findById(connectionId)
            ?: throw IllegalArgumentException("Unknown provider connection")
        val account = providerAccountDao.findById(accountId)
            ?: throw IllegalArgumentException("Unknown provider account")
        val model = providerModelDao.findById(modelId)
            ?: throw IllegalArgumentException("Unknown provider model")
        require(account.connectionId == connection.id && model.connectionId == connection.id) {
            "Router candidate references must belong to one provider connection"
        }
    }

    suspend fun findAgentProfile(profileId: String): AgentProfileEntity? = agentProfileDao.findById(profileId)

    suspend fun findAgentRun(runId: String): AgentRunEntity? = agentRunDao.findById(runId)

    suspend fun countAgentToolCalls(runId: String): Int = agentRunStepDao.countToolCallsForRun(runId)

    suspend fun findPendingAgentApprovals(runId: String): List<AgentApprovalEntity> =
        agentApprovalDao.findPendingForRun(runId)

    suspend fun saveAgentProfile(profile: AgentProfileEntity) {
        require(profile.name.isNotBlank()) { "Agent name must not be blank" }
        require(profile.instructions.isNotBlank()) { "Agent instructions must not be blank" }
        require(profile.maxSteps in 1..20) { "Agent max steps must be between 1 and 20" }
        require(profile.maxToolCalls in 0..20) { "Agent max tool calls must be between 0 and 20" }
        require(profile.maxRuntimeMs in 1_000L..300_000L) { "Agent max runtime must be between 1 second and 5 minutes" }
        agentProfileDao.upsert(profile)
    }

    suspend fun saveAgentRun(run: AgentRunEntity) = agentRunDao.upsert(run)

    suspend fun saveAgentRunStep(step: AgentRunStepEntity) = agentRunStepDao.upsert(step)

    suspend fun findAgentApproval(approvalId: String): AgentApprovalEntity? = agentApprovalDao.findById(approvalId)

    suspend fun saveAgentApproval(approval: AgentApprovalEntity) = agentApprovalDao.upsert(approval)

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

    suspend fun setProviderConnectionEnabled(connectionId: String, enabled: Boolean) {
        val connection = providerConnectionDao.findById(connectionId) ?: return
        saveProviderConnection(
            connection.copy(
                isEnabled = enabled,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
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
            routerAttemptEntryDao.deleteAll()
            routerAttemptDao.deleteAll()
            threadExecutionTargetDao.deleteAll()
            routerComboEntryDao.deleteAll()
            routerComboDao.deleteAll()
            providerModelDao.deleteAll()
            providerAccountDao.deleteAll()
            providerConnectionDao.deleteAll()
            messageDao.deleteAll()
            threadDao.deleteAll()
            projectDao.deleteAll()
        }
    }
}
