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
import dev.iliv007.ivai.provider.ProviderAccountAuthMode
import dev.iliv007.ivai.provider.ProviderCapability
import dev.iliv007.ivai.provider.ProviderEndpointTrustMode
import dev.iliv007.ivai.provider.ProviderKind
import dev.iliv007.ivai.provider.ProviderPresetCatalog
import dev.iliv007.ivai.ui.viewmodel.ProviderConnectionCard
import dev.iliv007.ivai.ui.viewmodel.ProviderManagementState

@Composable
fun ProviderManagementSection(
    state: ProviderManagementState,
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
            onSave = { kind, name, endpoint, account, model, capabilities, trustMode, trustConfirmed, authMode, secret ->
                onAddProvider(
                    kind,
                    name,
                    endpoint,
                    account,
                    model,
                    capabilities,
                    trustMode,
                    trustConfirmed,
                    authMode,
                    secret
                )
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
                    Text(
                        text = when (connection.endpointTrustMode) {
                            ProviderEndpointTrustMode.REMOTE_HTTPS -> "Remote HTTPS"
                            ProviderEndpointTrustMode.LOCAL_LOOPBACK_HTTPS -> "Trusted local device HTTPS"
                            ProviderEndpointTrustMode.LOCAL_LAN_HTTPS -> "Trusted private-LAN HTTPS"
                        } + if (connection.localTrustConfirmed) " · confirmed" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (connection.localTrustConfirmed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
                    text = "${account.displayName}: " + when (account.authMode) {
                        ProviderAccountAuthMode.NONE -> "No credential required"
                        ProviderAccountAuthMode.API_KEY -> if (account.credentialStored) "Credential stored" else "Credential missing"
                    },
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
private const val LOCAL_LOOPBACK_HTTPS_PRESET_ID = "local-loopback-https"
private const val LOCAL_LAN_HTTPS_PRESET_ID = "local-lan-https"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProviderDialog(
    onDismiss: () -> Unit,
    onSave: (
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
    ) -> Unit
) {
    var selectedPresetId by remember { mutableStateOf<String?>(null) }
    var presetMenuOpen by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf("") }
    var endpoint by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("") }
    var modelId by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var authMode by remember { mutableStateOf(ProviderAccountAuthMode.API_KEY) }
    var localTrustConfirmed by remember { mutableStateOf(false) }
    var capabilities by remember { mutableStateOf(setOf(ProviderCapability.TEXT, ProviderCapability.STREAMING)) }
    var validationError by remember { mutableStateOf<String?>(null) }
    val selectedPreset = selectedPresetId?.let(ProviderPresetCatalog::find)
    val isAdvancedCustom = selectedPresetId == ADVANCED_CUSTOM_PRESET_ID
    val trustMode = when (selectedPresetId) {
        LOCAL_LOOPBACK_HTTPS_PRESET_ID -> ProviderEndpointTrustMode.LOCAL_LOOPBACK_HTTPS
        LOCAL_LAN_HTTPS_PRESET_ID -> ProviderEndpointTrustMode.LOCAL_LAN_HTTPS
        else -> ProviderEndpointTrustMode.REMOTE_HTTPS
    }
    val isLocalEndpoint = trustMode != ProviderEndpointTrustMode.REMOTE_HTTPS
    val kind = selectedPreset?.kind ?: if (isAdvancedCustom || isLocalEndpoint) ProviderKind.CUSTOM_OPENAI_COMPATIBLE else null
    val requiresEndpoint = kind == ProviderKind.CUSTOM_OPENAI_COMPATIBLE

    fun selectPreset(id: String, name: String, baseUrl: String?) {
        selectedPresetId = id
        displayName = name
        endpoint = baseUrl.orEmpty()
        accountName = ""
        modelId = ""
        apiKey = ""
        authMode = ProviderAccountAuthMode.API_KEY
        localTrustConfirmed = false
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
                        value = selectedPreset?.displayName ?: when (selectedPresetId) {
                            ADVANCED_CUSTOM_PRESET_ID -> "Advanced custom endpoint"
                            LOCAL_LOOPBACK_HTTPS_PRESET_ID -> "Local device server · HTTPS"
                            LOCAL_LAN_HTTPS_PRESET_ID -> "Private-LAN server · HTTPS"
                            else -> "Choose provider preset"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Connection family") },
                        supportingText = { Text("Cloud uses HTTPS. Local endpoints are HTTPS-only, user-confirmed and never discovered automatically.") },
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
                            text = { Text("Local device server · HTTPS") },
                            onClick = {
                                selectPreset(LOCAL_LOOPBACK_HTTPS_PRESET_ID, "Local device server", null)
                                presetMenuOpen = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Private-LAN server · HTTPS") },
                            onClick = {
                                selectPreset(LOCAL_LAN_HTTPS_PRESET_ID, "Private-LAN server", null)
                                presetMenuOpen = false
                            }
                        )
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
                        when {
                            isLocalEndpoint -> "Trusted local HTTPS endpoint · foreground requests only · no discovery or network scan"
                            else -> "${selectedPreset?.protocolLabel ?: "OpenAI-compatible"} · foreground requests only · no provider is made default"
                        },
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
                            supportingText = {
                                Text(
                                    when (trustMode) {
                                        ProviderEndpointTrustMode.LOCAL_LOOPBACK_HTTPS -> "Exact local host only: localhost, 127.0.0.1 or ::1. HTTP is blocked."
                                        ProviderEndpointTrustMode.LOCAL_LAN_HTTPS -> "RFC1918 IPv4 only (10/8, 172.16/12, 192.168/16). HTTP is blocked."
                                        ProviderEndpointTrustMode.REMOTE_HTTPS -> "Review this remote HTTPS endpoint before saving."
                                    }
                                )
                            },
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
                    if (isLocalEndpoint) {
                        FilterChip(
                            selected = authMode == ProviderAccountAuthMode.NONE,
                            onClick = {
                                authMode = if (authMode == ProviderAccountAuthMode.NONE) {
                                    ProviderAccountAuthMode.API_KEY
                                } else {
                                    apiKey = ""
                                    ProviderAccountAuthMode.NONE
                                }
                            },
                            label = { Text("This local server requires no API key") },
                            modifier = Modifier.fillMaxWidth().testTag("local_no_auth_selector")
                        )
                        Text(
                            "IVAI will not create, store or send a placeholder token when no API key is selected.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        FilterChip(
                            selected = localTrustConfirmed,
                            onClick = { localTrustConfirmed = !localTrustConfirmed },
                            label = { Text("I trust this exact HTTPS local endpoint and understand messages go directly to it") },
                            modifier = Modifier.fillMaxWidth().testTag("local_endpoint_trust_confirmation")
                        )
                    }
                    if (authMode == ProviderAccountAuthMode.API_KEY) {
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
                    isLocalEndpoint && !localTrustConfirmed -> validationError = "Confirm that you trust this exact local HTTPS endpoint."
                    authMode == ProviderAccountAuthMode.API_KEY && apiKey.isBlank() -> validationError = "An API key is required."
                    else -> {
                        onSave(
                            kind,
                            displayName,
                            endpoint.takeIf { requiresEndpoint },
                            accountName,
                            modelId,
                            capabilities,
                            trustMode,
                            localTrustConfirmed,
                            authMode,
                            apiKey.takeIf { authMode == ProviderAccountAuthMode.API_KEY }
                        )
                        apiKey = ""
                    }
                }
            }) { Text(if (isLocalEndpoint) "Save trusted local endpoint" else "Save encrypted credential") }
        },
        dismissButton = { TextButton(onClick = { apiKey = ""; localTrustConfirmed = false; onDismiss() }) { Text("Cancel") } }
    )
}
