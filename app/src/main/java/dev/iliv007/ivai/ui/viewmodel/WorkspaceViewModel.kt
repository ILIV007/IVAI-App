package dev.iliv007.ivai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.iliv007.ivai.chat.LocalProviderChatSession
import dev.iliv007.ivai.data.local.LocalDataResetter
import dev.iliv007.ivai.data.local.LocalWorkspaceRepository
import dev.iliv007.ivai.data.local.ProviderAccountEntity
import dev.iliv007.ivai.data.local.ProviderConnectionEntity
import dev.iliv007.ivai.data.local.ProviderModelEntity
import dev.iliv007.ivai.provider.ChatProvider
import dev.iliv007.ivai.provider.ProviderKind
import dev.iliv007.ivai.security.EncryptedSecretVault
import dev.iliv007.ivai.provider.CredentialReference
import dev.iliv007.ivai.provider.ProviderStreamEvent
import dev.iliv007.ivai.ui.model.ChatMessage
import dev.iliv007.ivai.ui.model.ChatThread
import dev.iliv007.ivai.ui.model.MessageSender
import dev.iliv007.ivai.ui.model.MockDataRepository
import dev.iliv007.ivai.ui.model.UiPreviewState
import dev.iliv007.ivai.ui.model.WorkspaceProject
import dev.iliv007.ivai.ui.navigation.NavDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Canonical in-memory workspace state for the Phase 1 UI Skeleton.
 *
 * This ViewModel deliberately keeps mock data only. Persistence, secure storage, and
 * provider integration belong to later roadmap phases.
 */
data class WorkspaceUiState(
    val destination: NavDestination = NavDestination.CHATS,
    val previewState: UiPreviewState = UiPreviewState.NORMAL,
    val threads: List<ChatThread> = MockDataRepository.defaultChatThreads,
    val projects: List<WorkspaceProject> = MockDataRepository.mockProjects,
    val selectedThreadId: String = MockDataRepository.defaultChatThreads.firstOrNull()?.id.orEmpty(),
    val selectedProjectId: String? = null,
    val isStreaming: Boolean = false,
    val streamError: String? = null
)

class WorkspaceViewModel(
    initialState: WorkspaceUiState = WorkspaceUiState(),
    private val providerChatSession: LocalProviderChatSession? = null,
    private val providerResolver: ((ProviderKind, String?) -> ChatProvider)? = null,
    private val workspaceRepository: LocalWorkspaceRepository? = null,
    private val secretVault: EncryptedSecretVault? = null,
    private val localDataResetter: LocalDataResetter? = null
) : ViewModel() {

    private var streamingJob: Job? = null

    private val _uiState = MutableStateFlow(initialState.normalized())
    val uiState: StateFlow<WorkspaceUiState> = _uiState.asStateFlow()

    private val _providerManagementState = MutableStateFlow(ProviderManagementState())
    val providerManagementState: StateFlow<ProviderManagementState> = _providerManagementState.asStateFlow()

    init {
        observeProviderRegistry()
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
        _uiState.update { state ->
            val assignedProject = state.projects.find { it.id == targetProjectId }
            val threadId = "chat-${System.currentTimeMillis()}"
            val thread = ChatThread(
                id = threadId,
                title = assignedProject?.let { "New ${it.name} Chat" } ?: "New Conversation",
                snippet = "No messages yet",
                timestamp = "Just now",
                modelOrCombo = "Gemini Flash Combo",
                messages = emptyList(),
                projectId = assignedProject?.id,
                projectName = assignedProject?.name
            )
            state.copy(
                destination = NavDestination.CHATS,
                threads = listOf(thread) + state.threads,
                selectedThreadId = threadId
            )
        }
    }

    fun deleteThread(threadId: String) {
        _uiState.update { state ->
            val remaining = state.threads.filterNot { it.id == threadId }
            val selectedThreadId = if (state.selectedThreadId == threadId) {
                remaining.firstOrNull()?.id.orEmpty()
            } else {
                state.selectedThreadId
            }
            state.copy(threads = remaining, selectedThreadId = selectedThreadId).normalized()
        }
    }

    fun assignThreadToProject(threadId: String, projectId: String?) {
        _uiState.update { state ->
            val project = state.projects.find { it.id == projectId }
            if (projectId != null && project == null) {
                state
            } else {
                state.copy(
                    threads = state.threads.map { thread ->
                        if (thread.id == threadId) {
                            thread.copy(projectId = project?.id, projectName = project?.name)
                        } else {
                            thread
                        }
                    }
                )
            }
        }
    }

    fun createNewProject(name: String, description: String): WorkspaceProject {
        val project = WorkspaceProject(
            id = "proj-${System.currentTimeMillis()}",
            name = name.ifBlank { "Untitled Project" },
            description = description.ifBlank { "Local workspace project" },
            fileCount = 0,
            lastModified = "Just now"
        )
        _uiState.update { state -> state.copy(projects = state.projects + project) }
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

    fun sendMessage(threadId: String, rawText: String) {
        val text = rawText.trim()
        if (text.isBlank() || _uiState.value.isStreaming) return
        val thread = _uiState.value.threads.find { it.id == threadId } ?: return
        val session = providerChatSession
        val resolver = providerResolver
        val connection = _providerManagementState.value.connections.firstOrNull { it.enabled }
        val account = connection?.accounts?.firstOrNull { it.enabled && it.credentialStored }
        val selectedModel = connection?.manualModels?.firstOrNull { it.selectable }
        if (session == null || resolver == null || connection == null || account == null || selectedModel == null) {
            appendUserMessage(threadId, text)
            _uiState.update {
                it.copy(streamError = "Select an enabled provider, stored credential, and manual model before sending.")
            }
            return
        }
        val provider = runCatching { resolver.invoke(connection.kind, connection.baseUrlLabel) }
            .getOrElse { failure ->
                appendUserMessage(threadId, text)
                _uiState.update { it.copy(streamError = failure.message ?: "Selected provider is unavailable.") }
                return
            }
        val attemptId = "provider-${System.currentTimeMillis()}"
        streamingJob = viewModelScope.launch {
            var assistantText = ""
            _uiState.update { it.copy(isStreaming = true, streamError = null) }
            session.send(provider, threadId, CredentialReference(account.credentialReference), selectedModel.modelId, thread.messages, text, attemptId).collect { event ->
                when (event) {
                    is ProviderStreamEvent.Started -> appendUserMessage(threadId, text)
                    is ProviderStreamEvent.Delta -> {
                        assistantText += event.text
                        val current = _uiState.value.threads.find { it.id == threadId } ?: return@collect
                        val partial = ChatMessage("msg-$attemptId-assistant", MessageSender.ASSISTANT, assistantText, "Now", modelBadge = selectedModel.modelId)
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

    fun appendUserMessage(threadId: String, rawText: String) {
        val text = rawText.trim()
        if (text.isBlank()) return

        val message = ChatMessage(
            id = "msg-${System.currentTimeMillis()}",
            sender = MessageSender.USER,
            text = text,
            timestamp = "Now"
        )
        val currentThread = _uiState.value.threads.find { it.id == threadId } ?: return
        updateThreadMessages(threadId, currentThread.messages + message)
    }

    /** The raw secret is accepted only at the save boundary and is never copied into state. */
    fun addProviderConnection(
        kind: ProviderKind,
        displayName: String,
        customBaseUrl: String?,
        accountDisplayName: String,
        manualModelId: String,
        rawSecret: String
    ) {
        val repository = workspaceRepository ?: return
        val vault = secretVault ?: return
        val now = System.currentTimeMillis()
        val safeId = "provider-${kind.name.lowercase().replace('_', '-')}-${now}"
        val reference = "provider.${kind.name.lowercase().replace('_', '.')}.${now}"
        viewModelScope.launch {
            runCatching {
                repository.saveProviderConnection(
                    ProviderConnectionEntity(
                        id = safeId,
                        providerKind = kind.name,
                        displayName = displayName.trim(),
                        baseUrl = customBaseUrl?.trim()?.takeIf(String::isNotBlank),
                        isEnabled = true,
                        createdAtEpochMs = now,
                        updatedAtEpochMs = now
                    )
                )
                repository.saveProviderAccount(
                    ProviderAccountEntity(
                        id = "$safeId-account",
                        connectionId = safeId,
                        displayName = accountDisplayName.trim(),
                        credentialReference = reference,
                        isEnabled = true,
                        createdAtEpochMs = now,
                        updatedAtEpochMs = now
                    )
                )
                repository.saveProviderModel(
                    ProviderModelEntity(
                        id = "$safeId-model",
                        connectionId = safeId,
                        providerModelId = manualModelId.trim(),
                        displayName = manualModelId.trim(),
                        capabilitiesCsv = "TEXT,STREAMING",
                        isManual = true,
                        isSelectable = true,
                        updatedAtEpochMs = now
                    )
                )
                vault.store(reference, rawSecret)
            }.onFailure { failure ->
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
                accounts.forEach { vault.clear(it.credentialReference) }
                repository.deleteProviderConnection(connectionId)
            }.onFailure { failure ->
                _providerManagementState.update { it.copy(operationError = failure.message ?: "Provider could not be removed.") }
            }
        }
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeProviderRegistry() {
        val repository = workspaceRepository ?: return
        val vault = secretVault ?: return
        viewModelScope.launch {
            repository.observeProviderRegistry().flatMapLatest { snapshot ->
                val statusFlows = snapshot.accounts.map { vault.observeStatus(it.credentialReference) }
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
                            enabled = connection.isEnabled,
                            accounts = snapshot.accounts.filter { it.connectionId == connection.id }.map { account ->
                                ProviderAccountCard(
                                    accountId = account.id,
                                    displayName = account.displayName,
                                    credentialReference = account.credentialReference,
                                    enabled = account.isEnabled,
                                    credentialStored = statusByReference[account.credentialReference] == true
                                )
                            },
                            manualModels = snapshot.models.filter { it.connectionId == connection.id }.map { model ->
                                ProviderModelCard(
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

    private fun WorkspaceUiState.normalized(): WorkspaceUiState {
        val selectedThreadId = selectedThreadId.takeIf { id -> threads.any { it.id == id } }
            ?: threads.firstOrNull()?.id.orEmpty()
        val selectedProjectId = selectedProjectId?.takeIf { id -> projects.any { it.id == id } }
        return copy(selectedThreadId = selectedThreadId, selectedProjectId = selectedProjectId)
    }
}
