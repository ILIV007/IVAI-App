package dev.iliv007.ivai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import dev.iliv007.ivai.provider.ProviderCapability
import dev.iliv007.ivai.provider.ProviderKind
import dev.iliv007.ivai.provider.ProviderPresetCatalog
import dev.iliv007.ivai.ui.viewmodel.ProviderConnectionCard
import dev.iliv007.ivai.ui.viewmodel.ProviderManagementState

@Composable
fun ProviderManagementSection(
    state: ProviderManagementState,
    onAddProvider: (ProviderKind, String, String?, String, String, Set<ProviderCapability>, String) -> Unit,
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
                        text = "No local provider connections yet. Choose a user-managed preset or configure a custom HTTPS OpenAI-compatible endpoint.",
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
            onSave = { kind, name, endpoint, account, model, capabilities, secret ->
                onAddProvider(kind, name, endpoint, account, model, capabilities, secret)
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
                        text = connection.kind.displayLabel() + connection.baseUrlLabel?.let { " · $it" }.orEmpty(),
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

private fun ProviderKind.displayLabel(): String = when (this) {
    ProviderKind.GEMINI -> "Google Gemini"
    ProviderKind.OPENROUTER -> "OpenRouter"
    ProviderKind.CUSTOM_OPENAI_COMPATIBLE -> "OpenAI-compatible"
}

private const val ADVANCED_CUSTOM_PRESET_ID = "advanced-custom"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProviderDialog(
    onDismiss: () -> Unit,
    onSave: (ProviderKind, String, String?, String, String, Set<ProviderCapability>, String) -> Unit
) {
    var selectedPresetId by remember { mutableStateOf<String?>(null) }
    var presetMenuOpen by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf("") }
    var endpoint by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("") }
    var modelId by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var capabilities by remember { mutableStateOf(setOf(ProviderCapability.TEXT, ProviderCapability.STREAMING)) }
    var validationError by remember { mutableStateOf<String?>(null) }
    val selectedPreset = selectedPresetId?.let(ProviderPresetCatalog::find)
    val isAdvancedCustom = selectedPresetId == ADVANCED_CUSTOM_PRESET_ID
    val kind = selectedPreset?.kind ?: if (isAdvancedCustom) ProviderKind.CUSTOM_OPENAI_COMPATIBLE else null
    val requiresEndpoint = kind == ProviderKind.CUSTOM_OPENAI_COMPATIBLE

    fun selectPreset(id: String, name: String, baseUrl: String?) {
        selectedPresetId = id
        displayName = name
        endpoint = baseUrl.orEmpty()
        accountName = ""
        modelId = ""
        apiKey = ""
        capabilities = setOf(ProviderCapability.TEXT, ProviderCapability.STREAMING)
        validationError = null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add user-managed provider") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Choose a preset or advanced custom endpoint. IVAI never creates a provider, tests a connection, or selects a model automatically.",
                    style = MaterialTheme.typography.bodySmall
                )
                ExposedDropdownMenuBox(expanded = presetMenuOpen, onExpandedChange = { presetMenuOpen = it }) {
                    OutlinedTextField(
                        value = selectedPreset?.displayName ?: if (isAdvancedCustom) "Advanced custom endpoint" else "Choose provider preset",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Connection family") },
                        supportingText = { Text("Cloud presets use the installed provider protocol; local servers are not enabled in this Alpha flow.") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(presetMenuOpen) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth().testTag("provider_preset_selector")
                    )
                    ExposedDropdownMenu(expanded = presetMenuOpen, onDismissRequest = { presetMenuOpen = false }) {
                        ProviderPresetCatalog.all.forEach { preset ->
                            DropdownMenuItem(
                                text = { Text("${preset.displayName} · ${preset.protocolLabel}") },
                                onClick = {
                                    selectPreset(preset.id, preset.displayName, preset.suggestedBaseUrl)
                                    presetMenuOpen = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Advanced custom OpenAI-compatible") },
                            onClick = {
                                selectPreset(ADVANCED_CUSTOM_PRESET_ID, "Custom OpenAI-compatible", null)
                                presetMenuOpen = false
                            }
                        )
                    }
                }
                if (kind != null) {
                    Text(
                        "${selectedPreset?.protocolLabel ?: "OpenAI-compatible"} · foreground requests only · no provider is made default",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Connection name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (requiresEndpoint) {
                        OutlinedTextField(
                            value = endpoint,
                            onValueChange = { endpoint = it },
                            label = { Text("HTTPS base URL") },
                            supportingText = { Text("Review this endpoint before saving. Device-local and LAN HTTP servers require a later explicit trust mode.") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_provider_endpoint")
                        )
                    }
                    OutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = { Text("Account label") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = modelId,
                        onValueChange = { modelId = it },
                        label = { Text("Model ID selected by you") },
                        supportingText = { Text("Model discovery is a separate user-initiated action; no model is assumed by the preset.") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_provider_model_id")
                    )
                    Text("Declared model capabilities", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    ProviderCapability.entries.forEach { capability ->
                        FilterChip(
                            selected = capability in capabilities,
                            onClick = {
                                capabilities = if (capability in capabilities) capabilities - capability else capabilities + capability
                            },
                            label = { Text(capability.name) },
                            modifier = Modifier.fillMaxWidth().testTag("provider_capability_${capability.name.lowercase()}")
                        )
                    }
                    Text(
                        "The API key is written directly to the encrypted local vault and is never shown again. It is not sent until you start a foreground request.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API key") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("input_provider_api_key")
                    )
                }
                validationError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(onClick = {
                when {
                    kind == null -> validationError = "Choose a provider preset or advanced custom endpoint."
                    displayName.isBlank() -> validationError = "A connection name is required."
                    requiresEndpoint && endpoint.isBlank() -> validationError = "A custom HTTPS endpoint is required."
                    accountName.isBlank() -> validationError = "An account label is required."
                    modelId.isBlank() -> validationError = "A model ID selected by you is required."
                    capabilities.isEmpty() -> validationError = "Choose at least one declared model capability."
                    apiKey.isBlank() -> validationError = "An API key is required."
                    else -> {
                        onSave(kind, displayName, endpoint.takeIf { requiresEndpoint }, accountName, modelId, capabilities, apiKey)
                        apiKey = ""
                    }
                }
            }) { Text("Save encrypted credential") }
        },
        dismissButton = { TextButton(onClick = { apiKey = ""; onDismiss() }) { Text("Cancel") } }
    )
}
