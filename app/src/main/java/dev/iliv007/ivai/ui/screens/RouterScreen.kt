package dev.iliv007.ivai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import dev.iliv007.ivai.ui.components.IvaiPageHeader
import dev.iliv007.ivai.ui.components.IvaiStateCard
import dev.iliv007.ivai.ui.components.IvaiStateTone
import dev.iliv007.ivai.ui.theme.IvaiIconSizeTokens
import dev.iliv007.ivai.ui.theme.IvaiLayoutTokens
import dev.iliv007.ivai.ui.theme.IvaiShapeTokens
import dev.iliv007.ivai.ui.theme.IvaiSpacing
import dev.iliv007.ivai.ui.theme.IvaiStrokeTokens
import dev.iliv007.ivai.ui.viewmodel.ProviderManagementState
import dev.iliv007.ivai.ui.viewmodel.RouterCandidateSelection
import dev.iliv007.ivai.ui.viewmodel.RouterComboCard
import dev.iliv007.ivai.ui.viewmodel.RouterManagementState
import dev.iliv007.ivai.provider.ProviderAccountAuthMode
import dev.iliv007.ivai.provider.ProviderCapability
import dev.iliv007.ivai.provider.ProviderEndpointTrustMode
import dev.iliv007.ivai.provider.ProviderKind

/**
 * Phase 7.2 Connections hub. It composes existing provider and router state without changing
 * endpoint policy, credential storage, candidate eligibility, fallback execution or persistence.
 */
@Composable
fun RouterScreen(
    state: RouterManagementState,
    providers: ProviderManagementState,
    onAddProvider: (
        ProviderKind,
        String,
        String?,
        String,
        String,
        Set<ProviderCapability>,
        ProviderEndpointTrustMode,
        Boolean,
        ProviderAccountAuthMode,
        String?
    ) -> Unit,
    onDeleteProvider: (String) -> Unit,
    onAddAccountToConnection: (String, String, ProviderAccountAuthMode, String?) -> Unit,
    onAddModelToConnection: (String, String, Set<ProviderCapability>) -> Unit,
    onSetProviderEnabled: (String, Boolean) -> Unit,
    onDismissProviderError: () -> Unit,
    onCreateCombo: (String, String, List<RouterCandidateSelection>) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateComboSheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(IvaiSpacing.Small)
            .testTag("connections_hub"),
        verticalArrangement = Arrangement.spacedBy(IvaiSpacing.Small)
    ) {
        item {
            IvaiPageHeader(
                title = "Connections",
                subtitle = "Set up providers and decide the exact ordered fallback candidates for each Combo.",
                testTag = "connections_hub_header"
            )
        }
        item {
            ProviderManagementSection(
                state = providers,
                onAddProvider = onAddProvider,
                onDeleteProvider = onDeleteProvider,
                onAddAccountToConnection = onAddAccountToConnection,
                onAddModelToConnection = onAddModelToConnection,
                onSetProviderEnabled = onSetProviderEnabled,
                onDismissError = onDismissProviderError,
                modifier = Modifier.testTag("connections_provider_management")
            )
        }
        item {
            ComboSectionHeader(
                comboCount = state.combos.size,
                onCreateCombo = { showCreateComboSheet = true }
            )
        }
        if (state.combos.isEmpty()) {
            item {
                IvaiStateCard(
                    title = "Build an ordered Combo when ready",
                    message = "First save a user-managed connection with an account, then add one or more declared models under it. Finally choose the candidates and their exact fallback order. IVAI never adds an implicit provider.",
                    tone = IvaiStateTone.NEUTRAL,
                    icon = Icons.Default.Layers,
                    action = {
                        OutlinedButton(
                            onClick = { showCreateComboSheet = true },
                            modifier = Modifier.testTag("button_create_combo_empty")
                        ) { Text("Create Combo") }
                    },
                    testTag = "connections_combo_empty_state"
                )
            }
        } else {
            items(state.combos, key = { it.comboId }) { combo ->
                ComboSummaryCard(combo = combo)
            }
        }
        if (state.latestAttempts.isNotEmpty()) {
            item {
                Text(
                    text = "Recent local attempt trace",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = IvaiSpacing.XSmall)
                )
            }
            items(state.latestAttempts, key = { it.attemptId }) { attempt ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(IvaiShapeTokens.Control),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("router_attempt_${attempt.attemptId}")
                ) {
                    Column(
                        modifier = Modifier.padding(IvaiSpacing.XSmall),
                        verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XxxSmall)
                    ) {
                        Text(attempt.targetLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(attempt.outcome.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        attempt.safeErrorMessage?.let { error ->
                            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    state.operationError?.let { error ->
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text("Combo was not saved") },
            text = { Text(error) },
            confirmButton = { TextButton(onClick = onDismissError) { Text("Dismiss") } }
        )
    }

    if (showCreateComboSheet) {
        CreateComboSheet(
            providers = providers,
            onDismiss = { showCreateComboSheet = false },
            onSave = { name, description, candidates ->
                onCreateCombo(name, description, candidates)
                showCreateComboSheet = false
            }
        )
    }
}

@Composable
private fun ComboSectionHeader(comboCount: Int, onCreateCombo: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        border = androidx.compose.foundation.BorderStroke(IvaiStrokeTokens.Default, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("connections_combo_header")
    ) {
        Row(
            modifier = Modifier.padding(IvaiSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
        ) {
            Icon(
                imageVector = Icons.Default.Layers,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(IvaiIconSizeTokens.Feature)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Ordered Combos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "$comboCount saved · fallback follows only the candidate order you choose",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onCreateCombo,
                modifier = Modifier
                    .heightIn(min = IvaiLayoutTokens.MinimumTouchTarget)
                    .testTag("button_create_combo")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(IvaiIconSizeTokens.Inline))
                Spacer(Modifier.width(IvaiSpacing.XxxSmall))
                Text("Combo")
            }
        }
    }
}

@Composable
private fun ComboSummaryCard(combo: RouterComboCard) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        border = androidx.compose.foundation.BorderStroke(IvaiStrokeTokens.Default, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("combo_card_${combo.comboId}")
    ) {
        Column(
            modifier = Modifier.padding(IvaiSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(combo.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (combo.description.isNotBlank()) {
                        Text(combo.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    text = if (combo.enabled) "Enabled" else "Disabled",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (combo.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("Fallback order", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            combo.entries.sortedBy { it.position }.forEach { entry ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(IvaiShapeTokens.Control),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(IvaiSpacing.XSmall)) {
                        Text("${entry.position + 1}. ${entry.modelLabel}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${entry.providerLabel} · ${entry.accountLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (entry.capabilities.isNotEmpty()) {
                            Text(entry.capabilities.joinToString(" · "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateComboSheet(
    providers: ProviderManagementState,
    onDismiss: () -> Unit,
    onSave: (String, String, List<RouterCandidateSelection>) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCandidates by remember { mutableStateOf(emptyList<RouterUiCandidate>()) }
    var validationError by remember { mutableStateOf<String?>(null) }
    val candidates = remember(providers.connections) { providers.toEligibleCandidates() }

    fun toggleCandidate(candidate: RouterUiCandidate) {
        selectedCandidates = if (selectedCandidates.any { it.id == candidate.id }) {
            selectedCandidates.filterNot { it.id == candidate.id }
        } else {
            selectedCandidates + candidate
        }
    }

    fun moveCandidate(candidateId: String, delta: Int) {
        val currentIndex = selectedCandidates.indexOfFirst { it.id == candidateId }
        val targetIndex = currentIndex + delta
        if (currentIndex < 0 || targetIndex !in selectedCandidates.indices) return
        selectedCandidates = selectedCandidates.toMutableList().also { ordered ->
            val item = ordered.removeAt(currentIndex)
            ordered.add(targetIndex, item)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("combo_builder_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = IvaiSpacing.Small)
                .padding(bottom = IvaiSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.Small)
        ) {
            Text("Create an ordered Combo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                text = if (step == 1) "Step 1 of 2 · select the user-managed candidates and arrange their fallback order." else "Step 2 of 2 · review the exact order before saving.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("combo_builder_step_$step")
            )

            if (step == 1) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Combo name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_combo_name")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().testTag("input_combo_description")
                )
                if (candidates.isEmpty()) {
                    IvaiStateCard(
                        title = "No eligible candidate yet",
                        message = "Complete the progression in Connections: enabled provider → ready account/credential → selectable model. IVAI will not add one automatically.",
                        tone = IvaiStateTone.WARNING,
                        icon = Icons.Default.PriorityHigh,
                        testTag = "combo_builder_no_candidates"
                    )
                } else {
                    Text("Eligible candidates", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    candidates.forEach { candidate ->
                        FilterChip(
                            selected = selectedCandidates.any { it.id == candidate.id },
                            onClick = { toggleCandidate(candidate) },
                            label = { Text(candidate.label) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("combo_candidate_${candidate.id.sanitizedTestTag()}")
                        )
                    }
                    if (selectedCandidates.isNotEmpty()) {
                        Text("Fallback order", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        selectedCandidates.forEachIndexed { index, candidate ->
                            OrderedCandidateRow(
                                candidate = candidate,
                                position = index,
                                canMoveUp = index > 0,
                                canMoveDown = index < selectedCandidates.lastIndex,
                                onMoveUp = { moveCandidate(candidate.id, -1) },
                                onMoveDown = { moveCandidate(candidate.id, 1) }
                            )
                        }
                    }
                }
            } else {
                ComboReviewCard(name = name, description = description, candidates = selectedCandidates)
            }

            validationError?.let { error ->
                Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("combo_builder_validation_error"))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
            ) {
                if (step == 2) {
                    OutlinedButton(
                        onClick = { validationError = null; step = 1 },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = IvaiLayoutTokens.MinimumTouchTarget)
                            .testTag("button_combo_builder_back")
                    ) { Text("Back") }
                }
                Button(
                    onClick = {
                        when {
                            name.isBlank() -> validationError = "A Combo name is required."
                            selectedCandidates.isEmpty() -> validationError = "Select at least one user-managed candidate."
                            step == 1 -> {
                                validationError = null
                                step = 2
                            }
                            else -> onSave(name, description, selectedCandidates.map { it.selection })
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = IvaiLayoutTokens.MinimumTouchTarget)
                        .testTag(if (step == 2) "button_combo_builder_final_save" else "button_combo_builder_review")
                ) {
                    Icon(
                        imageVector = if (step == 2) Icons.Default.Check else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        modifier = Modifier.size(IvaiIconSizeTokens.Inline)
                    )
                    Spacer(Modifier.width(IvaiSpacing.XxxSmall))
                    Text(if (step == 2) "Save Combo" else "Review order")
                }
            }
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterHorizontally).testTag("button_combo_builder_cancel")
            ) { Text("Cancel") }
        }
    }
}

@Composable
private fun OrderedCandidateRow(
    candidate: RouterUiCandidate,
    position: Int,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(IvaiShapeTokens.Control),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Fallback position ${position + 1}: ${candidate.label}" }
            .testTag("combo_ordered_candidate_${candidate.id.sanitizedTestTag()}")
    ) {
        Row(
            modifier = Modifier.padding(IvaiSpacing.XSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XxSmall)
        ) {
            Text(
                text = "${position + 1}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(candidate.label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            IconButton(
                enabled = canMoveUp,
                onClick = onMoveUp,
                modifier = Modifier
                    .size(IvaiLayoutTokens.MinimumTouchTarget)
                    .semantics { contentDescription = "Move ${candidate.label} earlier" }
                    .testTag("button_combo_move_up_${candidate.id.sanitizedTestTag()}")
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = null)
            }
            IconButton(
                enabled = canMoveDown,
                onClick = onMoveDown,
                modifier = Modifier
                    .size(IvaiLayoutTokens.MinimumTouchTarget)
                    .semantics { contentDescription = "Move ${candidate.label} later" }
                    .testTag("button_combo_move_down_${candidate.id.sanitizedTestTag()}")
            ) {
                Icon(Icons.Default.ArrowDownward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun ComboReviewCard(name: String, description: String, candidates: List<RouterUiCandidate>) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        modifier = Modifier.fillMaxWidth().testTag("combo_builder_final_review")
    ) {
        Column(
            modifier = Modifier.padding(IvaiSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XxSmall)
        ) {
            Text("Final review", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(name.ifBlank { "Unnamed Combo" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            if (description.isNotBlank()) {
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = "Only this explicit order will be saved. No provider is inserted automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            candidates.forEachIndexed { index, candidate ->
                Text("${index + 1}. ${candidate.label}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun ProviderManagementState.toEligibleCandidates(): List<RouterUiCandidate> = connections
    .filter { connection -> connection.enabled }
    .flatMap { connection ->
        connection.accounts
            .filter { account ->
                account.enabled && (account.authMode == ProviderAccountAuthMode.NONE || account.credentialStored)
            }
            .flatMap { account ->
                connection.manualModels
                    .filter { model -> model.selectable }
                    .map { model ->
                        RouterUiCandidate(
                            id = "${connection.connectionId}/${account.accountId}/${model.registryModelId}",
                            label = "${connection.displayName} · ${account.displayName} · ${model.displayName}",
                            capabilities = model.capabilities,
                            selection = RouterCandidateSelection(connection.connectionId, account.accountId, model.registryModelId)
                        )
                    }
            }
    }

private data class RouterUiCandidate(
    val id: String,
    val label: String,
    val capabilities: List<String>,
    val selection: RouterCandidateSelection
)

private fun String.sanitizedTestTag(): String = replace(Regex("[^A-Za-z0-9_-]"), "_")
