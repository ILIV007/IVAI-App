package dev.iliv007.ivai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import dev.iliv007.ivai.provider.ProviderKind
import dev.iliv007.ivai.ui.viewmodel.ProviderConnectionCard
import dev.iliv007.ivai.ui.viewmodel.ProviderManagementState

@Composable
fun ProviderManagementSection(
    state: ProviderManagementState,
    onAddProvider: (ProviderKind, String, String?, String, String, String) -> Unit,
    onDeleteProvider: (String) -> Unit,
    onSetProviderEnabled: (String, Boolean) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(4.dp, 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.secondary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Model Providers (BYOK)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                .testTag("settings_provider_management")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Provider metadata stays on this device. API keys are encrypted in the Android Keystore-backed vault and are never displayed again.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (state.connections.isEmpty()) {
                    Text(
                        text = "No local provider connections yet. Add Gemini, OpenRouter, or a custom OpenAI-compatible endpoint.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    state.connections.forEach { connection ->
                        ProviderConnectionCardItem(connection, onDeleteProvider, onSetProviderEnabled)
                    }
                }

                OutlinedButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("button_add_provider")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add Provider")
                }
            }
        }

        state.operationError?.let { error ->
            AlertDialog(
                onDismissRequest = onDismissError,
                confirmButton = { TextButton(onClick = onDismissError) { Text("OK") } },
                title = { Text("Provider was not saved") },
                text = { Text(error) }
            )
        }
    }

    if (showAddDialog) {
        AddProviderDialog(
            onDismiss = { showAddDialog = false },
            onSave = { kind, name, endpoint, account, model, secret ->
                onAddProvider(kind, name, endpoint, account, model, secret)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ProviderConnectionCardItem(
    connection: ProviderConnectionCard,
    onDeleteProvider: (String) -> Unit,
    onSetProviderEnabled: (String, Boolean) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(17.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.padding(end = 8.dp)) {
                    Text(connection.displayName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    Text(
                        text = connection.kind.label() + connection.baseUrlLabel?.let { " · $it" }.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = connection.enabled,
                    onCheckedChange = { enabled -> onSetProviderEnabled(connection.connectionId, enabled) },
                    modifier = Modifier.testTag("switch_provider_${connection.connectionId}")
                )
                TextButton(onClick = { onDeleteProvider(connection.connectionId) }) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete provider", tint = MaterialTheme.colorScheme.error)
                }
            }
            connection.accounts.forEach { account ->
                Text(
                    text = "${account.displayName}: ${if (account.credentialStored) "Credential stored" else "Credential missing"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (account.credentialStored) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            if (connection.manualModels.isNotEmpty()) {
                Text(
                    text = "Models: " + connection.manualModels.joinToString { it.displayName },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProviderDialog(
    onDismiss: () -> Unit,
    onSave: (ProviderKind, String, String?, String, String, String) -> Unit
) {
    var kind by remember { mutableStateOf(ProviderKind.GEMINI) }
    var kindMenuOpen by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf("") }
    var endpoint by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("Default") }
    var modelId by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add local provider") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("The API key is sent directly to the encrypted vault and cannot be viewed after saving.", style = MaterialTheme.typography.bodySmall)
                ExposedDropdownMenuBox(expanded = kindMenuOpen, onExpandedChange = { kindMenuOpen = it }) {
                    OutlinedTextField(
                        value = kind.label(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Provider type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(kindMenuOpen) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = kindMenuOpen, onDismissRequest = { kindMenuOpen = false }) {
                        ProviderKind.entries.forEach { candidate ->
                            DropdownMenuItem(text = { Text(candidate.label()) }, onClick = {
                                kind = candidate
                                kindMenuOpen = false
                            })
                        }
                    }
                }
                OutlinedTextField(value = displayName, onValueChange = { displayName = it }, label = { Text("Connection name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                if (kind == ProviderKind.CUSTOM_OPENAI_COMPATIBLE) {
                    OutlinedTextField(value = endpoint, onValueChange = { endpoint = it }, label = { Text("HTTPS base URL") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                OutlinedTextField(value = accountName, onValueChange = { accountName = it }, label = { Text("Account label") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = modelId, onValueChange = { modelId = it }, label = { Text("Manual model ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API key") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("input_provider_api_key")
                )
                validationError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(onClick = {
                when {
                    displayName.isBlank() -> validationError = "A connection name is required."
                    accountName.isBlank() -> validationError = "An account label is required."
                    modelId.isBlank() -> validationError = "A manual model ID is required."
                    apiKey.isBlank() -> validationError = "An API key is required."
                    kind == ProviderKind.CUSTOM_OPENAI_COMPATIBLE && endpoint.isBlank() -> validationError = "A custom HTTPS endpoint is required."
                    else -> {
                        onSave(kind, displayName, endpoint.takeIf { kind == ProviderKind.CUSTOM_OPENAI_COMPATIBLE }, accountName, modelId, apiKey)
                        apiKey = ""
                    }
                }
            }) { Text("Save encrypted credential") }
        },
        dismissButton = { TextButton(onClick = { apiKey = ""; onDismiss() }) { Text("Cancel") } }
    )
}

private fun ProviderKind.label(): String = when (this) {
    ProviderKind.GEMINI -> "Google Gemini"
    ProviderKind.OPENROUTER -> "OpenRouter"
    ProviderKind.CUSTOM_OPENAI_COMPATIBLE -> "Custom OpenAI-compatible"
}
