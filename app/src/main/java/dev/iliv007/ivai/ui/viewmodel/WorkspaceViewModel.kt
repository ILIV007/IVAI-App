package dev.iliv007.ivai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.iliv007.ivai.agent.AgentRunStatus
import dev.iliv007.ivai.agent.AgentToolKind
import dev.iliv007.ivai.agent.ApprovalStatus
import dev.iliv007.ivai.agent.BasicAgentRuntime
import dev.iliv007.ivai.chat.LocalProviderChatSession
import dev.iliv007.ivai.data.local.LocalDataResetter
import dev.iliv007.ivai.data.local.LocalWorkspaceRepository
import dev.iliv007.ivai.data.local.AgentProfileEntity
import dev.iliv007.ivai.data.local.ChatThreadEntity
import dev.iliv007.ivai.data.local.ChatMessageEntity
import dev.iliv007.ivai.data.local.WorkspaceProjectEntity
import dev.iliv007.ivai.data.local.toDomainMessage
import dev.iliv007.ivai.data.local.toEntity
import dev.iliv007.ivai.data.local.ProviderAccountEntity
import dev.iliv007.ivai.data.local.ProviderConnectionEntity
import dev.iliv007.ivai.data.local.ProviderModelEntity
import dev.iliv007.ivai.data.local.RouterComboEntity
import dev.iliv007.ivai.data.local.RouterComboEntryEntity
import dev.iliv007.ivai.provider.ChatProvider
import dev.iliv007.ivai.provider.ProviderAccountAuthMode
import dev.iliv007.ivai.provider.ProviderCapability
import dev.iliv007.ivai.provider.ProviderEndpointTrustMode
import dev.iliv007.ivai.provider.ProviderKind
import dev.iliv007.ivai.provider.noAuthCredentialMarker
import dev.iliv007.ivai.router.RouterAttemptOutcome
import dev.iliv007.ivai.router.RouterChatSession
import dev.iliv007.ivai.router.RouterCatalog
import dev.iliv007.ivai.router.ExecutionTarget
import dev.iliv007.ivai.security.EncryptedSecretVault
import dev.iliv007.ivai.provider.CredentialReference
import dev.iliv007.ivai.provider.ProviderStreamEvent
import dev.iliv007.ivai.ui.model.ChatMessage
import dev.iliv007.ivai.ui.model.ChatThread
import dev.iliv007.ivai.ui.model.MessageSender
import dev.iliv007.ivai.ui.model.UiPreviewState
import dev.iliv007.ivai.ui.model.WorkspaceProject
import dev.iliv007.ivai.ui.navigation.NavDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Canonical UI state backed by local Room data when a workspace repository is available. */
data class WorkspaceUiState(
    val destination: NavDestination = NavDestination.CHATS,
    val previewState: UiPreviewState = UiPreviewState.NORMAL,
    val threads: List<ChatThread> = emptyList(),
    val projects: List<WorkspaceProject> = emptyList(),
    val selectedThreadId: String = "",
    val selectedProjectId: String? = null,
    val isStreaming: Boolean = false,
    val streamError: String? = null
)

class WorkspaceViewModel(
    initialState: WorkspaceUiState = WorkspaceUiState(),
    private val providerChatSession: LocalProviderChatSession? = null,
    private val routerChatSession: RouterChatSession? = null,
    private val providerResolver: ((ProviderKind, String?) -> ChatProvider)? = null,
    private val workspaceRepository: LocalWorkspaceRepository? = null,
    private val secretVault: EncryptedSecretVault? = null,
    private val localDataResetter: LocalDataResetter? = null,
    private val agentRuntime: BasicAgentRuntime? = null
) : ViewModel() {

    private var streamingJob: Job? = null

    private val _uiState = MutableStateFlow(initialState.normalized())
    val uiState: StateFlow<WorkspaceUiState> = _uiState.asStateFlow()

    private val _providerManagementState = MutableStateFlow(ProviderManagementState())
    val providerManagementState: StateFlow<ProviderManagementState> = _providerManagementState.asStateFlow()

    private val _routerManagementState = MutableStateFlow(RouterManagementState())
    val routerManagementState: StateFlow<RouterManagementState> = _routerManagementState.asStateFlow()

    private val _agentManagementState = MutableStateFlow(AgentManagementState())
    val agentManagementState: StateFlow<AgentManagementState> = _agentManagementState.asStateFlow()
    private val selectedAgentRunId = MutableStateFlow<String?>(null)
    private val selectedWorkspaceThreadId = MutableStateFlow(_uiState.value.selectedThreadId.ifBlank { null })

    init {
        recoverInterruptedAgentApprovals()
        observeWorkspace()
        observeSelectedWorkspaceMessages()
        observeProviderRegistry()
        observeRouterManagement()
        observeAgentManagement()
    }

    fun selectDestination(destination: NavDestination) {
        _uiState.update { it.copy(destination = destination) }
    }

    fun selectPreviewState(previewState: UiPreviewState) {
        _uiState.update { it.copy(previewState = previewState) }
    }

    fun resetPreviewState() {
        selectPreviewState(UiPreviewState.NORMAL)
    }

    fun selectThread(threadId: String) {
        _uiState.update { state ->
            if (state.threads.any { it.id == threadId }) {
                selectedWorkspaceThreadId.value = threadId
                state.copy(
                    destination = NavDestination.CHATS,
                    selectedThreadId = threadId
                )
            } else {
                state
            }
        }
    }

    fun selectProject(projectId: String?) {
        _uiState.update { state ->
            if (projectId == null || state.projects.any { it.id == projectId }) {
                state.copy(selectedProjectId = projectId)
            } else {
                state
            }
        }
    }

    fun createNewChat(targetProjectId: String? = _uiState.value.selectedProjectId) {
        val now = System.currentTimeMillis()
        val assignedProject = _uiState.value.projects.find { it.id == targetProjectId }
        val thread = ChatThread(
            id = "chat-$now",
            title = assignedProject?.let { "New ${it.name} Chat" } ?: "New Conversation",
            snippet = "No messages yet",
            timestamp = "Just now",
            modelOrCombo = "No execution target selected",
            messages = emptyList(),
            projectId = assignedProject?.id,
            projectName = assignedProject?.name
        )
        selectedWorkspaceThreadId.value = thread.id
        _uiState.update { state ->
            state.copy(
                destination = NavDestination.CHATS,
                threads = listOf(thread) + state.threads,
                selectedThreadId = thread.id
            )
        }
        persistThread(thread, now, "Chat could not be created locally.")
    }

    fun deleteThread(threadId: String) {
        val previous = _uiState.value
        val remaining = previous.threads.filterNot { it.id == threadId }
        val selectedThreadId = if (previous.selectedThreadId == threadId) {
            remaining.firstOrNull()?.id.orEmpty()
        } else {
            previous.selectedThreadId
        }
        selectedWorkspaceThreadId.value = selectedThreadId.ifBlank { null }
        _uiState.update { it.copy(threads = remaining, selectedThreadId = selectedThreadId).normalized() }
        val repository = workspaceRepository ?: return
        viewModelScope.launch {
            runCatching { repository.deleteThread(threadId) }
                .onFailure { failure -> _uiState.update { it.copy(streamError = failure.message ?: "Chat could not be deleted locally.") } }
        }
    }

    fun assignThreadToProject(threadId: String, projectId: String?) {
        val state = _uiState.value
        val project = state.projects.find { it.id == projectId }
        if (projectId != null && project == null) return
        val updatedThread = state.threads.firstOrNull { it.id == threadId }?.copy(
            projectId = project?.id,
            projectName = project?.name
        ) ?: return
        _uiState.update { current ->
            current.copy(threads = current.threads.map { if (it.id == threadId) updatedThread else it })
        }
        persistThread(updatedThread, System.currentTimeMillis(), "Chat project assignment could not be saved.")
    }

    fun createNewProject(name: String, description: String): WorkspaceProject {
        val now = System.currentTimeMillis()
        val project = WorkspaceProject(
            id = "proj-$now",
            name = name.ifBlank { "Untitled Project" },
            description = description.ifBlank { "Local workspace project" },
            fileCount = 0,
            lastModified = "Just now"
        )
        _uiState.update { state -> state.copy(projects = state.projects + project) }
        val repository = workspaceRepository
        if (repository != null) {
            viewModelScope.launch {
                runCatching {
                    repository.saveProject(
                        WorkspaceProjectEntity(
                            id = project.id,
                            name = project.name,
                            description = project.description,
                            fileCount = project.fileCount,
                            updatedAtEpochMs = now
                        )
                    )
                }.onFailure { failure ->
                    _uiState.update { it.copy(streamError = failure.message ?: "Project could not be created locally.") }
                }
            }
        }
        return project
    }

    fun updateThreadMessages(threadId: String, messages: List<ChatMessage>) {
        _uiState.update { state ->
            state.copy(
                threads = state.threads.map { thread ->
                    if (thread.id == threadId) {
                        thread.copy(
                            messages = messages,
                            snippet = messages.lastOrNull()?.text ?: "No messages yet"
                        )
                    } else {
                        thread
                    }
                }
            )
        }
    }

    fun selectComboTarget(threadId: String, comboId: String, displayLabel: String) {
        val repository = workspaceRepository ?: return
        val thread = _uiState.value.threads.firstOrNull { it.id == threadId } ?: return
        val updatedThread = thread.copy(modelOrCombo = displayLabel)
        viewModelScope.launch {
            runCatching {
                persistThreadForRouter(repository, updatedThread)
                repository.selectThreadExecutionTarget(threadId, ExecutionTarget.Combo(comboId))
                _uiState.update { state -> state.copy(threads = state.threads.map { if (it.id == threadId) updatedThread else it }) }
            }.onFailure { failure -> _uiState.update { it.copy(streamError = failure.message ?: "Combo could not be selected.") } }
        }
    }

    fun selectDirectTarget(threadId: String, connectionId: String, accountId: String, modelId: String, displayLabel: String) {
        val repository = workspaceRepository ?: return
        val thread = _uiState.value.threads.firstOrNull { it.id == threadId } ?: return
        val updatedThread = thread.copy(modelOrCombo = displayLabel)
        viewModelScope.launch {
            runCatching {
                persistThreadForRouter(repository, updatedThread)
                repository.selectThreadExecutionTarget(threadId, ExecutionTarget.DirectModel(connectionId, accountId, modelId))
                _uiState.update { state -> state.copy(threads = state.threads.map { if (it.id == threadId) updatedThread else it }) }
            }.onFailure { failure -> _uiState.update { it.copy(streamError = failure.message ?: "Model could not be selected.") } }
        }
    }

    private fun persistThread(thread: ChatThread, updatedAtEpochMs: Long, failureMessage: String) {
        val repository = workspaceRepository ?: return
        viewModelScope.launch {
            runCatching {
                repository.saveThread(
                    ChatThreadEntity(
                        id = thread.id,
                        title = thread.title,
                        snippet = thread.snippet,
                        updatedAtEpochMs = updatedAtEpochMs,
                        modelOrCombo = thread.modelOrCombo,
                        projectId = thread.projectId
                    )
                )
            }.onFailure { failure ->
                _uiState.update { it.copy(streamError = failure.message ?: failureMessage) }
            }
        }
    }

    private suspend fun persistThreadForRouter(repository: LocalWorkspaceRepository, thread: ChatThread) {
        repository.saveThread(
            ChatThreadEntity(
                id = thread.id,
                title = thread.title,
                snippet = thread.snippet,
                updatedAtEpochMs = System.currentTimeMillis(),
                modelOrCombo = thread.modelOrCombo,
                projectId = thread.projectId
            )
        )
    }

    fun sendMessage(threadId: String, rawText: String) {
        val text = rawText.trim()
        if (text.isBlank() || _uiState.value.isStreaming) return
        val thread = _uiState.value.threads.find { it.id == threadId } ?: return
        val session = routerChatSession
        val repository = workspaceRepository
        val vault = secretVault
        if (session == null || repository == null || vault == null) {
            appendUserMessage(threadId, text)
            _uiState.update { it.copy(streamError = "Router is unavailable in this local workspace.") }
            return
        }
        val attemptId = "router-${System.currentTimeMillis()}"
        streamingJob = viewModelScope.launch {
            val target = repository.resolveThreadExecutionTarget(threadId)
            if (target == null) {
                appendUserMessage(threadId, text)
                _uiState.update { it.copy(streamError = "Select a user-managed model or Combo for this chat before sending.") }
                return@launch
            }
            val registry = repository.currentProviderRegistry()
            val credentialPresent = registry.accounts.filter { account ->
                vault.observeStatus(account.credentialReference).first().exists
            }.map { it.credentialReference }.toSet()
            val entries = when (target) {
                is dev.iliv007.ivai.router.ExecutionTarget.Combo -> repository.listRouterComboEntries(target.comboId)
                is dev.iliv007.ivai.router.ExecutionTarget.DirectModel -> emptyList()
            }
            var assistantText = ""
            _uiState.update { it.copy(isStreaming = true, streamError = null) }
            session.send(
                threadId = threadId,
                target = target,
                comboEntries = entries,
                catalog = RouterCatalog(registry.connections, registry.accounts, registry.models, credentialPresent),
                history = thread.messages,
                prompt = text,
                attemptId = attemptId
            ).collect { event ->
                when (event) {
                    is ProviderStreamEvent.Started -> appendUserMessage(
                        threadId = threadId,
                        rawText = text,
                        persistLocally = false,
                        messageId = "msg-${event.attemptId}-user"
                    )
                    is ProviderStreamEvent.Delta -> {
                        assistantText += event.text
                        val current = _uiState.value.threads.find { it.id == threadId } ?: return@collect
                        val partial = ChatMessage("msg-$attemptId-assistant", MessageSender.ASSISTANT, assistantText, "Now", modelBadge = target.stableId)
                        updateThreadMessages(threadId, current.messages.filterNot { it.id == partial.id } + partial)
                    }
                    is ProviderStreamEvent.Completed -> _uiState.update { it.copy(isStreaming = false) }
                    is ProviderStreamEvent.Failed -> _uiState.update { it.copy(isStreaming = false, streamError = event.error.safeMessage) }
                    ProviderStreamEvent.Cancelled -> _uiState.update { it.copy(isStreaming = false) }
                    is ProviderStreamEvent.Usage -> Unit
                }
            }
        }
    }

    fun stopStreaming() {
        streamingJob?.cancel()
        streamingJob = null
        _uiState.update { it.copy(isStreaming = false) }
    }

    fun appendUserMessage(
        threadId: String,
        rawText: String,
        persistLocally: Boolean = true,
        messageId: String? = null
    ) {
        val text = rawText.trim()
        if (text.isBlank()) return
        val createdAt = System.currentTimeMillis()
        val message = ChatMessage(
            id = messageId ?: "msg-$createdAt",
            sender = MessageSender.USER,
            text = text,
            timestamp = "Now"
        )
        val currentThread = _uiState.value.threads.find { it.id == threadId } ?: return
        updateThreadMessages(threadId, currentThread.messages.filterNot { it.id == message.id } + message)
        if (persistLocally) {
            val repository = workspaceRepository ?: return
            viewModelScope.launch {
                runCatching { repository.appendMessage(message.toEntity(threadId, createdAt)) }
                    .onFailure { failure ->
                        _uiState.update { it.copy(streamError = failure.message ?: "Message could not be saved locally.") }
                    }
            }
        }
    }

    /** The raw secret is accepted only at the save boundary and is never copied into state. */
    fun addProviderConnection(
        kind: ProviderKind,
        displayName: String,
        customBaseUrl: String?,
        accountDisplayName: String,
        manualModelId: String,
        modelCapabilities: Set<ProviderCapability>,
        endpointTrustMode: ProviderEndpointTrustMode = ProviderEndpointTrustMode.REMOTE_HTTPS,
        localTrustConfirmed: Boolean = false,
        authMode: ProviderAccountAuthMode = ProviderAccountAuthMode.API_KEY,
        rawSecret: String? = null
    ) {
        val repository = workspaceRepository ?: return
        val vault = secretVault ?: return
        val now = System.currentTimeMillis()
        val safeId = "provider-${kind.name.lowercase().replace('_', '-')}-${now}"
        val accountId = "$safeId-account"
        val apiKeyReference = "provider.${kind.name.lowercase().replace('_', '.')}.${now}"
        val credentialReference = when (authMode) {
            ProviderAccountAuthMode.API_KEY -> apiKeyReference
            ProviderAccountAuthMode.NONE -> noAuthCredentialMarker(accountId)
        }
        viewModelScope.launch {
            var storedReference: String? = null
            runCatching {
                require(modelCapabilities.isNotEmpty()) { "Choose at least one model capability." }
                require((endpointTrustMode != ProviderEndpointTrustMode.REMOTE_HTTPS) == localTrustConfirmed) {
                    "Local endpoint trust confirmation must match the selected endpoint mode."
                }
                when (authMode) {
                    ProviderAccountAuthMode.API_KEY -> {
                        val secret = requireNotNull(rawSecret).trim()
                        require(secret.isNotBlank()) { "An API key is required for this account." }
                        vault.store(apiKeyReference, secret)
                        storedReference = apiKeyReference
                    }
                    ProviderAccountAuthMode.NONE -> require(rawSecret.isNullOrBlank()) {
                        "No-auth accounts must not retain an API key."
                    }
                }
                repository.saveProviderConnection(
                    ProviderConnectionEntity(
                        id = safeId,
                        providerKind = kind.name,
                        displayName = displayName.trim(),
                        baseUrl = customBaseUrl?.trim()?.takeIf(String::isNotBlank),
                        isEnabled = true,
                        createdAtEpochMs = now,
                        updatedAtEpochMs = now,
                        endpointTrustMode = endpointTrustMode.name,
                        localTrustConfirmedAtEpochMs = now.takeIf { localTrustConfirmed }
                    )
                )
                repository.saveProviderAccount(
                    ProviderAccountEntity(
                        id = accountId,
                        connectionId = safeId,
                        displayName = accountDisplayName.trim(),
                        credentialReference = credentialReference,
                        isEnabled = true,
                        createdAtEpochMs = now,
                        updatedAtEpochMs = now,
                        authMode = authMode.name
                    )
                )
                repository.saveProviderModel(
                    ProviderModelEntity(
                        id = "$safeId-model",
                        connectionId = safeId,
                        providerModelId = manualModelId.trim(),
                        displayName = manualModelId.trim(),
                        capabilitiesCsv = modelCapabilities.joinToString(",") { it.name },
                        isManual = true,
                        isSelectable = true,
                        updatedAtEpochMs = now
                    )
                )
            }.onFailure { failure ->
                storedReference?.let { reference -> runCatching { vault.clear(reference) } }
                _providerManagementState.update { it.copy(operationError = failure.message ?: "Provider could not be saved.") }
            }
        }
    }

    fun setProviderConnectionEnabled(connectionId: String, enabled: Boolean) {
        val repository = workspaceRepository ?: return
        viewModelScope.launch {
            runCatching { repository.setProviderConnectionEnabled(connectionId, enabled) }
                .onFailure { failure ->
                    _providerManagementState.update {
                        it.copy(operationError = failure.message ?: "Provider status could not be updated.")
                    }
                }
        }
    }

    fun deleteProviderConnection(connectionId: String) {
        val repository = workspaceRepository ?: return
        val vault = secretVault ?: return
        val accounts = _providerManagementState.value.connections
            .firstOrNull { it.connectionId == connectionId }?.accounts.orEmpty()
        viewModelScope.launch {
            runCatching {
                accounts.filter { it.authMode == ProviderAccountAuthMode.API_KEY }
                    .forEach { vault.clear(it.credentialReference) }
                repository.deleteProviderConnection(connectionId)
            }.onFailure { failure ->
                _providerManagementState.update { it.copy(operationError = failure.message ?: "Provider could not be removed.") }
            }
        }
    }

    fun createRouterCombo(
        displayName: String,
        description: String,
        candidates: List<RouterCandidateSelection>
    ) {
        val repository = workspaceRepository ?: return
        val now = System.currentTimeMillis()
        val comboId = "combo-$now"
        viewModelScope.launch {
            runCatching {
                repository.saveRouterCombo(
                    RouterComboEntity(
                        id = comboId,
                        displayName = displayName.trim(),
                        description = description.trim(),
                        isEnabled = true,
                        createdAtEpochMs = now,
                        updatedAtEpochMs = now
                    ),
                    candidates.mapIndexed { index, candidate ->
                        RouterComboEntryEntity(
                            id = "$comboId-entry-$index",
                            comboId = comboId,
                            position = index,
                            connectionId = candidate.connectionId,
                            accountId = candidate.accountId,
                            modelId = candidate.modelId,
                            isEnabled = true
                        )
                    }
                )
            }.onFailure { failure ->
                _routerManagementState.update { it.copy(operationError = failure.message ?: "Combo could not be saved.") }
            }
        }
    }

    fun createAgent(
        name: String,
        instructions: String,
        targetKind: String,
        targetId: String,
        accountId: String?,
        projectId: String?,
        enabledTools: Set<AgentToolKind>,
        maxSteps: Int,
        maxToolCalls: Int,
        maxRuntimeMs: Long
    ) {
        val repository = workspaceRepository ?: return
        val now = System.currentTimeMillis()
        val profile = AgentProfileEntity(
            id = "agent-$now",
            name = name.trim(),
            instructions = instructions.trim(),
            targetKind = targetKind.trim(),
            targetId = targetId.trim(),
            accountId = accountId?.trim()?.takeIf(String::isNotBlank),
            projectId = projectId,
            enabledToolsCsv = enabledTools.joinToString(",") { it.name },
            maxSteps = maxSteps,
            maxToolCalls = maxToolCalls,
            maxRuntimeMs = maxRuntimeMs,
            isEnabled = true,
            createdAtEpochMs = now,
            updatedAtEpochMs = now
        )
        viewModelScope.launch {
            runCatching {
                require(profile.targetKind.isNotBlank() && profile.targetId.isNotBlank()) {
                    "An agent must use a user-selected provider or combo target."
                }
                repository.saveAgentProfile(profile)
            }.onFailure { failure ->
                _agentManagementState.update {
                    it.copy(operationError = failure.message ?: "Agent profile could not be saved.")
                }
            }
        }
    }

    fun startAgentRun(profileId: String, goal: String) {
        val repository = workspaceRepository ?: return
        val runtime = agentRuntime ?: return
        viewModelScope.launch {
            runCatching {
                val profile = requireNotNull(repository.findAgentProfile(profileId)) { "Agent profile was not found." }
                runtime.start(profile, goal)
            }.onSuccess { run ->
                selectedAgentRunId.value = run.id
            }.onFailure { failure ->
                _agentManagementState.update {
                    it.copy(operationError = failure.message ?: "Agent run could not be started.")
                }
            }
        }
    }

    fun selectAgentRun(runId: String?) {
        selectedAgentRunId.value = runId
    }

    fun resolveAgentApproval(approvalId: String, allowOnce: Boolean) {
        val repository = workspaceRepository ?: return
        val runtime = agentRuntime ?: return
        viewModelScope.launch {
            val approval = repository.findAgentApproval(approvalId)
            if (approval == null) {
                _agentManagementState.update { it.copy(operationError = "Approval request was not found.") }
                return@launch
            }
            runCatching { runtime.resolveWriteApproval(approvalId, allowOnce) }
                .onSuccess { selectedAgentRunId.value = approval.runId }
                .onFailure { failure ->
                    _agentManagementState.update {
                        it.copy(operationError = failure.message ?: "Approval could not be resolved.")
                    }
                }
        }
    }

    fun cancelAgentRun(runId: String) {
        val repository = workspaceRepository ?: return
        val runtime = agentRuntime ?: return
        viewModelScope.launch {
            runCatching {
                val run = requireNotNull(repository.findAgentRun(runId)) { "Agent run was not found." }
                runtime.cancel(run)
            }.onSuccess { selectedAgentRunId.value = it.id }
                .onFailure { failure ->
                    _agentManagementState.update {
                        it.copy(operationError = failure.message ?: "Agent run could not be cancelled.")
                    }
                }
        }
    }

    fun clearAgentOperationError() {
        _agentManagementState.update { it.copy(operationError = null) }
    }

    fun clearRouterOperationError() {
        _routerManagementState.update { it.copy(operationError = null) }
    }

    fun clearProviderOperationError() {
        _providerManagementState.update { it.copy(operationError = null) }
    }

    fun deleteAllLocalData() {
        val resetter = localDataResetter ?: return
        viewModelScope.launch {
            runCatching { resetter.deleteAllData() }
                .onFailure { failure ->
                    _providerManagementState.update {
                        it.copy(operationError = failure.message ?: "Local data could not be fully deleted.")
                    }
                }
        }
    }

    private fun observeWorkspace() {
        val repository = workspaceRepository ?: return
        viewModelScope.launch {
            repository.observeWorkspace().collect { snapshot ->
                val projectById = snapshot.projects.associateBy { it.id }
                val existingThreads = _uiState.value.threads.associateBy { it.id }
                val threads = snapshot.threads.map { thread ->
                    ChatThread(
                        id = thread.id,
                        title = thread.title,
                        snippet = thread.snippet,
                        timestamp = "Local",
                        modelOrCombo = thread.modelOrCombo,
                        messages = existingThreads[thread.id]?.messages.orEmpty(),
                        projectId = thread.projectId,
                        projectName = thread.projectId?.let { projectById[it]?.name }
                    )
                }
                val selectedThreadId = _uiState.value.selectedThreadId
                    .takeIf { selected -> threads.any { it.id == selected } }
                    ?: threads.firstOrNull()?.id.orEmpty()
                if (selectedWorkspaceThreadId.value != selectedThreadId.ifBlank { null }) {
                    selectedWorkspaceThreadId.value = selectedThreadId.ifBlank { null }
                }
                _uiState.update { state ->
                    state.copy(
                        projects = snapshot.projects.map { project ->
                            WorkspaceProject(
                                id = project.id,
                                name = project.name,
                                description = project.description,
                                fileCount = project.fileCount,
                                lastModified = "Local"
                            )
                        },
                        threads = threads,
                        selectedThreadId = selectedThreadId,
                        selectedProjectId = state.selectedProjectId?.takeIf { it in projectById }
                    ).normalized()
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeSelectedWorkspaceMessages() {
        val repository = workspaceRepository ?: return
        viewModelScope.launch {
            selectedWorkspaceThreadId.flatMapLatest { threadId ->
                threadId?.let(repository::observeMessages) ?: flowOf(emptyList())
            }.collect { entities ->
                val threadId = selectedWorkspaceThreadId.value ?: return@collect
                val messages = entities.map { entity -> entity.toDomainMessage(timestamp = "Local") }
                _uiState.update { state ->
                    state.copy(
                        threads = state.threads.map { thread ->
                            if (thread.id == threadId) {
                                thread.copy(
                                    messages = messages,
                                    snippet = messages.lastOrNull()?.text ?: thread.snippet
                                )
                            } else {
                                thread
                            }
                        }
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeProviderRegistry() {
        val repository = workspaceRepository ?: return
        val vault = secretVault ?: return
        viewModelScope.launch {
            repository.observeProviderRegistry().flatMapLatest { snapshot ->
                val statusFlows = snapshot.accounts
                    .filter { ProviderAccountAuthMode.valueOf(it.authMode) == ProviderAccountAuthMode.API_KEY }
                    .map { vault.observeStatus(it.credentialReference) }
                if (statusFlows.isEmpty()) flowOf(snapshot to emptyMap())
                else combine(statusFlows) { statuses ->
                    snapshot to statuses.associate { it.reference to it.exists }
                }
            }.collect { (snapshot, statusByReference) ->
                _providerManagementState.value = ProviderManagementState(
                    connections = snapshot.connections.map { connection ->
                        ProviderConnectionCard(
                            connectionId = connection.id,
                            kind = ProviderKind.valueOf(connection.providerKind),
                            displayName = connection.displayName,
                            baseUrlLabel = connection.baseUrl,
                            endpointTrustMode = ProviderEndpointTrustMode.valueOf(connection.endpointTrustMode),
                            localTrustConfirmed = connection.localTrustConfirmedAtEpochMs != null,
                            enabled = connection.isEnabled,
                            accounts = snapshot.accounts.filter { it.connectionId == connection.id }.map { account ->
                                ProviderAccountCard(
                                    accountId = account.id,
                                    displayName = account.displayName,
                                    credentialReference = account.credentialReference,
                                    authMode = ProviderAccountAuthMode.valueOf(account.authMode),
                                    enabled = account.isEnabled,
                                    credentialStored = ProviderAccountAuthMode.valueOf(account.authMode) == ProviderAccountAuthMode.NONE ||
                                        statusByReference[account.credentialReference] == true
                                )
                            },
                            manualModels = snapshot.models.filter { it.connectionId == connection.id }.map { model ->
                                ProviderModelCard(
                                    registryModelId = model.id,
                                    modelId = model.providerModelId,
                                    displayName = model.displayName,
                                    capabilities = model.capabilitiesCsv.split(',').filter(String::isNotBlank),
                                    selectable = model.isSelectable
                                )
                            }
                        )
                    }
                )
            }
        }
    }

    private fun observeRouterManagement() {
        val repository = workspaceRepository ?: return
        viewModelScope.launch {
            combine(repository.observeRouter(), repository.observeProviderRegistry(), repository.observeAllRouterAttempts()) { router, providers, attempts ->
                val connectionById = providers.connections.associateBy { it.id }
                val accountById = providers.accounts.associateBy { it.id }
                val modelById = providers.models.associateBy { it.id }
                RouterManagementState(
                    combos = router.combos.map { combo ->
                        RouterComboCard(
                            comboId = combo.id,
                            displayName = combo.displayName,
                            description = combo.description,
                            enabled = combo.isEnabled,
                            entries = router.entries.filter { it.comboId == combo.id }.map { entry ->
                                val connection = connectionById[entry.connectionId]
                                val account = accountById[entry.accountId]
                                val model = modelById[entry.modelId]
                                RouterComboEntryCard(
                                    entryId = entry.id,
                                    position = entry.position,
                                    providerLabel = connection?.displayName ?: "Removed provider",
                                    accountLabel = account?.displayName ?: "Removed account",
                                    modelLabel = model?.displayName ?: "Removed model",
                                    capabilities = model?.capabilitiesCsv?.split(',')?.filter(String::isNotBlank).orEmpty(),
                                    enabled = entry.isEnabled
                                )
                            }
                        )
                    },
                    latestAttempts = attempts.take(12).map { attempt ->
                        RouterAttemptCard(
                            attemptId = attempt.id,
                            targetLabel = "${attempt.targetKind}: ${attempt.targetId}",
                            outcome = RouterAttemptOutcome.valueOf(attempt.outcome),
                            safeErrorMessage = attempt.safeErrorMessage
                        )
                    }
                )
            }.collect { state -> _routerManagementState.value = state }
        }
    }

    private fun recoverInterruptedAgentApprovals() {
        val runtime = agentRuntime ?: return
        viewModelScope.launch {
            runCatching { runtime.recoverAfterProcessDeath() }
                .onFailure { failure ->
                    _agentManagementState.update {
                        it.copy(operationError = failure.message ?: "Interrupted Agent approvals could not be recovered safely.")
                    }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeAgentManagement() {
        val repository = workspaceRepository ?: return
        val traceFlow = selectedAgentRunId.flatMapLatest { runId ->
            runId?.let(repository::observeAgentRunSteps) ?: flowOf(emptyList())
        }
        val targetOptionsFlow = combine(repository.observeProviderRegistry(), repository.observeRouter()) { registry, router ->
            val connections = registry.connections.associateBy { it.id }
            val accounts = registry.accounts.associateBy { it.id }
            val models = registry.models.associateBy { it.id }
            val directTargets = registry.models.flatMap { model ->
                val connection = connections[model.connectionId]
                if (connection == null || !connection.isEnabled || !model.isSelectable) {
                    emptyList()
                } else {
                    registry.accounts.filter { account ->
                        account.isEnabled && account.connectionId == connection.id
                    }.map { account ->
                        AgentTargetOption(
                            targetKind = "DIRECT_MODEL",
                            targetId = model.id,
                            accountId = account.id,
                            label = "Direct model: ${connection.displayName} / ${account.displayName} / ${model.displayName}"
                        )
                    }
                }
            }
            val comboTargets = router.combos.filter { combo ->
                combo.isEnabled && router.entries.filter { it.comboId == combo.id && it.isEnabled }.any { entry ->
                    val connection = connections[entry.connectionId]
                    val account = accounts[entry.accountId]
                    val model = models[entry.modelId]
                    connection?.isEnabled == true && account?.isEnabled == true && model?.isSelectable == true &&
                        account.connectionId == connection.id && model.connectionId == connection.id
                }
            }.map { combo ->
                AgentTargetOption(
                    targetKind = "COMBO",
                    targetId = combo.id,
                    accountId = null,
                    label = "Combo: ${combo.displayName}"
                )
            }
            (directTargets + comboTargets).sortedBy { it.label }
        }
        viewModelScope.launch {
            combine(
                repository.observeAgentProfiles(),
                repository.observeAllAgentRuns(),
                repository.observePendingAgentApprovals(),
                traceFlow,
                targetOptionsFlow
            ) { profiles, runs, approvals, trace, availableTargets ->
                val desiredRunId = selectedAgentRunId.value
                    ?.takeIf { selectedId -> runs.any { it.id == selectedId } }
                    ?: runs.firstOrNull()?.id
                if (desiredRunId != selectedAgentRunId.value) selectedAgentRunId.value = desiredRunId
                val profileById = profiles.associateBy { it.id }
                AgentManagementState(
                    availableTargets = availableTargets,
                    profiles = profiles.map { profile ->
                        AgentProfileCard(
                            profileId = profile.id,
                            name = profile.name,
                            instructions = profile.instructions,
                            targetLabel = "${profile.targetKind}: ${profile.targetId}",
                            projectId = profile.projectId,
                            enabledTools = profile.enabledToolsCsv.split(',').mapNotNull { name ->
                                runCatching { AgentToolKind.valueOf(name) }.getOrNull()
                            },
                            maxSteps = profile.maxSteps,
                            maxToolCalls = profile.maxToolCalls,
                            maxRuntimeMs = profile.maxRuntimeMs,
                            enabled = profile.isEnabled
                        )
                    },
                    activeRuns = runs.take(12).map { run ->
                        AgentRunCard(
                            runId = run.id,
                            agentId = run.agentId,
                            agentName = profileById[run.agentId]?.name ?: "Removed agent",
                            goal = run.goal,
                            status = runCatching { AgentRunStatus.valueOf(run.status) }.getOrDefault(AgentRunStatus.FAILED),
                            startedAtEpochMs = run.startedAtEpochMs,
                            safeErrorMessage = run.safeErrorMessage
                        )
                    },
                    pendingApprovals = approvals.mapNotNull { approval ->
                        runCatching {
                            AgentApprovalCard(
                                approvalId = approval.id,
                                runId = approval.runId,
                                toolKind = AgentToolKind.valueOf(approval.toolKind),
                                targetPath = approval.targetPath,
                                preview = approval.preview.take(4_000),
                                status = ApprovalStatus.valueOf(approval.status),
                                createdAtEpochMs = approval.createdAtEpochMs
                            )
                        }.getOrNull()
                    },
                    selectedRunId = desiredRunId,
                    selectedRunTrace = trace.map { step ->
                        AgentRunTraceStepCard(
                            stepId = step.id,
                            runId = step.runId,
                            position = step.position,
                            stepKind = step.stepKind,
                            status = step.status,
                            safeSummary = step.safeSummary,
                            createdAtEpochMs = step.createdAtEpochMs
                        )
                    }
                )
            }.collect { state -> _agentManagementState.value = state }
        }
    }

    private fun WorkspaceUiState.normalized(): WorkspaceUiState {
        val selectedThreadId = selectedThreadId.takeIf { id -> threads.any { it.id == id } }
            ?: threads.firstOrNull()?.id.orEmpty()
        val selectedProjectId = selectedProjectId?.takeIf { id -> projects.any { it.id == id } }
        return copy(selectedThreadId = selectedThreadId, selectedProjectId = selectedProjectId)
    }
}
