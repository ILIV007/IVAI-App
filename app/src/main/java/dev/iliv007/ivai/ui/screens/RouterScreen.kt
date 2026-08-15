package dev.iliv007.ivai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.iliv007.ivai.ui.viewmodel.ProviderManagementState
import dev.iliv007.ivai.ui.viewmodel.RouterCandidateSelection
import dev.iliv007.ivai.ui.viewmodel.RouterManagementState

@Composable
fun RouterScreen(
    state: RouterManagementState,
    providers: ProviderManagementState,
    onCreateCombo: (String, String, List<RouterCandidateSelection>) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .testTag("router_notice_banner")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Local Router & Ordered Fallback", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "IVAI never selects a default provider. A Combo contains only the provider accounts and models you add, in the order you choose.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Your Combos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(onClick = { showCreateDialog = true }, modifier = Modifier.testTag("router_add_combo_button")) {
                    Text("Add Combo")
                }
            }
        }

        if (state.combos.isEmpty()) {
            item {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No local Combo yet. Add at least one provider with a stored credential and selectable model, then create your own fallback chain.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        items(state.combos, key = { it.comboId }) { combo ->
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                    .testTag("combo_card_${combo.comboId}")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(combo.displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            if (combo.description.isNotBlank()) Text(combo.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(if (combo.enabled) "Enabled" else "Disabled", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("Ordered candidates", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    combo.entries.sortedBy { it.position }.forEach { entry ->
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("${entry.position + 1}. ${entry.modelLabel}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("${entry.providerLabel} · ${entry.accountLabel}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (entry.capabilities.isNotEmpty()) Text(entry.capabilities.joinToString(" · "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }

        if (state.latestAttempts.isNotEmpty()) {
            item {
                Text("Recent local attempt trace", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(state.latestAttempts, key = { it.attemptId }) { attempt ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("router_attempt_${attempt.attemptId}")
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(attempt.targetLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(attempt.outcome.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        attempt.safeErrorMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }

    state.operationError?.let { error ->
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text("Router action failed") },
            text = { Text(error) },
            confirmButton = { TextButton(onClick = onDismissError) { Text("Dismiss") } }
        )
    }

    if (showCreateDialog) {
        CreateComboDialog(
            providers = providers,
            onDismiss = { showCreateDialog = false },
            onSave = { name, description, candidates ->
                onCreateCombo(name, description, candidates)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun CreateComboDialog(
    providers: ProviderManagementState,
    onDismiss: () -> Unit,
    onSave: (String, String, List<RouterCandidateSelection>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf(emptySet<String>()) }
    var error by remember { mutableStateOf<String?>(null) }
    val candidates = providers.connections
        .filter { it.enabled }
        .flatMap { connection ->
            connection.accounts.filter { it.enabled && it.credentialStored }.flatMap { account ->
                connection.manualModels.filter { it.selectable }.map { model ->
                    val id = "${connection.connectionId}/${account.accountId}/${model.registryModelId}"
                    RouterUiCandidate(
                        id = id,
                        label = "${connection.displayName} · ${account.displayName} · ${model.displayName}",
                        selection = RouterCandidateSelection(connection.connectionId, account.accountId, model.registryModelId)
                    )
                }
            }
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create local Combo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select candidates in fallback order. IVAI will not add an implicit Gemini, OpenRouter, or other provider.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(name, { name = it }, label = { Text("Combo name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                if (candidates.isEmpty()) {
                    Text("No eligible candidate. Add an enabled provider, save its credential, and register a selectable model in Settings.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                } else {
                    candidates.forEach { candidate ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = candidate.id in selectedIds,
                                onCheckedChange = { checked ->
                                    selectedIds = if (checked) selectedIds + candidate.id else selectedIds - candidate.id
                                }
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(candidate.label, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(onClick = {
                val selected = candidates.filter { it.id in selectedIds }.map { it.selection }
                when {
                    name.isBlank() -> error = "A Combo name is required."
                    selected.isEmpty() -> error = "Select at least one user-managed candidate."
                    else -> onSave(name, description, selected)
                }
            }) { Text("Save Combo") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private data class RouterUiCandidate(
    val id: String,
    val label: String,
    val selection: RouterCandidateSelection
)
