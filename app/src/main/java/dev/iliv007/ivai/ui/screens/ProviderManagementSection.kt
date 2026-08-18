package dev.iliv007.ivai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import dev.iliv007.ivai.provider.ProviderAccountAuthMode
import dev.iliv007.ivai.provider.ProviderCapability
import dev.iliv007.ivai.provider.ProviderEndpointTrustMode
import dev.iliv007.ivai.provider.ProviderKind
import dev.iliv007.ivai.provider.ProviderPresetCatalog
import dev.iliv007.ivai.ui.components.IvaiPageHeader
import dev.iliv007.ivai.ui.components.IvaiStateCard
import dev.iliv007.ivai.ui.components.IvaiStateTone
import dev.iliv007.ivai.ui.theme.IvaiIconSizeTokens
import dev.iliv007.ivai.ui.theme.IvaiLayoutTokens
import dev.iliv007.ivai.ui.theme.IvaiShapeTokens
import dev.iliv007.ivai.ui.theme.IvaiSpacing
import dev.iliv007.ivai.ui.theme.IvaiStrokeTokens
import dev.iliv007.ivai.ui.viewmodel.ProviderConnectionCard
import dev.iliv007.ivai.ui.viewmodel.ProviderManagementState

private const val ADVANCED_CUSTOM_PRESET_ID = "advanced-custom"
private const val LOCAL_LOOPBACK_HTTPS_PRESET_ID = "local-loopback-https"
private const val LOCAL_LAN_HTTPS_PRESET_ID = "local-lan-https"
private const val PROVIDER_SETUP_TOTAL_STEPS = 4

/**
 * Phase 7.2 Connections presentation. It reads existing provider cards and invokes only the
 * existing ViewModel callbacks. Persistent provider creation still occurs exclusively at final save.
 */
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
    var showSetupSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(IvaiSpacing.Small)
    ) {
        IvaiPageHeader(
            title = "Connections",
            subtitle = "Your providers, accounts and declared models stay under local control.",
            testTag = "connections_provider_header"
        )
        ProviderProgressionCard(state = state)

        if (state.connections.isEmpty()) {
            IvaiStateCard(
                title = "Start with a connection",
                message = "Choose a provider family, review its HTTPS trust boundary, add an account and credential, then declare the model you want IVAI to use.",
                tone = IvaiStateTone.INFO,
                icon = Icons.Default.Add,
                action = {
                    Button(
                        onClick = { showSetupSheet = true },
                        modifier = Modifier.testTag("button_add_provider")
                    ) { Text("Add connection") }
                },
                testTag = "connections_empty_state"
            )
        } else {
            state.connections.forEach { connection ->
                ProviderConnectionCardItem(
                    connection = connection,
                    onDeleteProvider = onDeleteProvider,
                    onSetProviderEnabled = onSetProviderEnabled
                )
            }
            OutlinedButton(
                onClick = { showSetupSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = IvaiLayoutTokens.MinimumTouchTarget)
                    .testTag("button_add_provider")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(IvaiIconSizeTokens.Inline))
                Spacer(Modifier.width(IvaiSpacing.XxSmall))
                Text("Add another connection")
            }
        }

        Text(
            text = "IVAI never selects a default provider, model or Combo. Provider metadata stays local; API keys are written to the Android Keystore-backed vault only when you confirm final save.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(IvaiShapeTokens.Control))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(IvaiSpacing.XSmall)
                .testTag("connections_byok_notice")
        )
    }

    state.operationError?.let { error ->
        AlertDialog(
            onDismissRequest = onDismissError,
            confirmButton = { TextButton(onClick = onDismissError) { Text("Dismiss") } },
            title = { Text("Connection was not saved") },
            text = { Text(error) }
        )
    }

    if (showSetupSheet) {
        ProviderSetupSheet(
            onDismiss = { showSetupSheet = false },
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
                showSetupSheet = false
            }
        )
    }
}

@Composable
private fun ProviderProgressionCard(state: ProviderManagementState) {
    val accountCount = state.connections.sumOf { it.accounts.size }
    val modelCount = state.connections.sumOf { it.manualModels.count { model -> model.selectable } }
    Surface(
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(IvaiStrokeTokens.Default, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("connections_progression")
    ) {
        Column(
            modifier = Modifier.padding(IvaiSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XxSmall)
        ) {
            Text(
                text = "Connection readiness",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${state.connections.size} connection${if (state.connections.size == 1) "" else "s"} · $accountCount account${if (accountCount == 1) "" else "s"} · $modelCount selectable model${if (modelCount == 1) "" else "s"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (modelCount == 0) {
                    "Progression: connection → account/credential → declared model → Combo"
                } else {
                    "Declared models are ready to be intentionally added to an ordered Combo."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ProviderConnectionCardItem(
    connection: ProviderConnectionCard,
    onDeleteProvider: (String) -> Unit,
    onSetProviderEnabled: (String, Boolean) -> Unit
) {
    val selectableModels = connection.manualModels.filter { it.selectable }
    val credentialReady = connection.accounts.count { account ->
        account.enabled && (account.authMode == ProviderAccountAuthMode.NONE || account.credentialStored)
    }
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        border = androidx.compose.foundation.BorderStroke(IvaiStrokeTokens.Default, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("connection_card_${connection.connectionId}")
    ) {
        Column(
            modifier = Modifier.padding(IvaiSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
            ) {
                Box(
                    modifier = Modifier
                        .size(IvaiLayoutTokens.MinimumTouchTarget)
                        .clip(RoundedCornerShape(IvaiShapeTokens.Control))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = connection.endpointTrustMode.trustIcon(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(IvaiIconSizeTokens.Inline)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = connection.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${connection.kind.displayLabel()} · ${connection.endpointTrustMode.displayLabel()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    connection.baseUrlLabel?.let { baseUrl ->
                        Text(
                            text = baseUrl,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
                Switch(
                    checked = connection.enabled,
                    onCheckedChange = { enabled -> onSetProviderEnabled(connection.connectionId, enabled) },
                    modifier = Modifier
                        .semantics { contentDescription = "Enable ${connection.displayName}" }
                        .testTag("switch_provider_${connection.connectionId}")
                )
                IconButton(
                    onClick = { onDeleteProvider(connection.connectionId) },
                    modifier = Modifier
                        .size(IvaiLayoutTokens.MinimumTouchTarget)
                        .testTag("button_delete_provider_${connection.connectionId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete ${connection.displayName}",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            ConnectionReadinessRow(
                label = "Trust",
                value = connection.endpointTrustMode.displayLabel() + if (connection.endpointTrustMode != ProviderEndpointTrustMode.REMOTE_HTTPS) {
                    if (connection.localTrustConfirmed) " · confirmed" else " · confirmation missing"
                } else ""
            )
            ConnectionReadinessRow(
                label = "Credential",
                value = if (credentialReady == connection.accounts.size && connection.accounts.isNotEmpty()) {
                    "$credentialReady account${if (credentialReady == 1) "" else "s"} ready"
                } else {
                    "$credentialReady of ${connection.accounts.size} account${if (connection.accounts.size == 1) "" else "s"} ready"
                }
            )
            ConnectionReadinessRow(
                label = "Declared models",
                value = if (selectableModels.isEmpty()) "No selectable model" else selectableModels.joinToString { it.displayName }
            )
        }
    }
}

@Composable
private fun ConnectionReadinessRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XxSmall)) {
        Text("$label:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderSetupSheet(
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
    var step by remember { mutableStateOf(1) }
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

    fun validationForCurrentStep(): String? = when (step) {
        1 -> if (kind == null) "Choose a provider family or an explicit custom HTTPS option." else null
        2 -> when {
            displayName.isBlank() -> "A connection name is required."
            requiresEndpoint && endpoint.isBlank() -> "A custom HTTPS endpoint is required."
            isLocalEndpoint && !localTrustConfirmed -> "Confirm that you trust this exact local HTTPS endpoint."
            else -> null
        }
        3 -> when {
            accountName.isBlank() -> "An account label is required."
            authMode == ProviderAccountAuthMode.API_KEY && apiKey.isBlank() -> "An API key is required for this account."
            else -> null
        }
        else -> when {
            modelId.isBlank() -> "A model ID selected by you is required."
            capabilities.isEmpty() -> "Choose at least one declared model capability."
            else -> null
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            apiKey = ""
            onDismiss()
        },
        modifier = Modifier.testTag("provider_setup_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = IvaiSpacing.Small)
                .padding(bottom = IvaiSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.Small)
        ) {
            Text("Add a user-managed connection", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                text = "Step $step of $PROVIDER_SETUP_TOTAL_STEPS · IVAI will not create a provider, test a connection, discover a model, or select a target automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("provider_setup_step_$step")
            )
            LinearProgressIndicator(
                progress = { step.toFloat() / PROVIDER_SETUP_TOTAL_STEPS.toFloat() },
                modifier = Modifier.fillMaxWidth().testTag("provider_setup_progress")
            )

            when (step) {
                1 -> ProviderFamilyStep(
                    selectedPresetId = selectedPresetId,
                    selectedPresetName = selectedPreset?.displayName,
                    presetMenuOpen = presetMenuOpen,
                    onMenuExpanded = { presetMenuOpen = it },
                    onSelect = { id, name, baseUrl -> selectPreset(id, name, baseUrl) }
                )
                2 -> ProviderEndpointTrustStep(
                    displayName = displayName,
                    onDisplayNameChange = { displayName = it },
                    requiresEndpoint = requiresEndpoint,
                    endpoint = endpoint,
                    onEndpointChange = { endpoint = it },
                    trustMode = trustMode,
                    isLocalEndpoint = isLocalEndpoint,
                    localTrustConfirmed = localTrustConfirmed,
                    onTrustConfirmationChange = { localTrustConfirmed = it }
                )
                3 -> ProviderAccountStep(
                    accountName = accountName,
                    onAccountNameChange = { accountName = it },
                    isLocalEndpoint = isLocalEndpoint,
                    authMode = authMode,
                    onAuthModeChange = { mode ->
                        authMode = mode
                        if (mode == ProviderAccountAuthMode.NONE) apiKey = ""
                    },
                    apiKey = apiKey,
                    onApiKeyChange = { apiKey = it }
                )
                else -> ProviderModelReviewStep(
                    kind = kind,
                    displayName = displayName,
                    trustMode = trustMode,
                    accountName = accountName,
                    authMode = authMode,
                    modelId = modelId,
                    onModelIdChange = { modelId = it },
                    capabilities = capabilities,
                    onCapabilitiesChange = { capabilities = it }
                )
            }

            validationError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("provider_setup_validation_error")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { validationError = null; step -= 1 },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = IvaiLayoutTokens.MinimumTouchTarget)
                            .testTag("button_provider_setup_back")
                    ) { Text("Back") }
                }
                Button(
                    onClick = {
                        val error = validationForCurrentStep()
                        if (error != null) {
                            validationError = error
                        } else if (step < PROVIDER_SETUP_TOTAL_STEPS) {
                            validationError = null
                            step += 1
                        } else {
                            onSave(
                                requireNotNull(kind),
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
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = IvaiLayoutTokens.MinimumTouchTarget)
                        .testTag(if (step == PROVIDER_SETUP_TOTAL_STEPS) "button_provider_setup_final_save" else "button_provider_setup_next")
                ) {
                    Text(if (step == PROVIDER_SETUP_TOTAL_STEPS) "Save connection" else "Continue")
                }
            }
            TextButton(
                onClick = { apiKey = ""; onDismiss() },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag("button_provider_setup_cancel")
            ) { Text("Cancel setup") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderFamilyStep(
    selectedPresetId: String?,
    selectedPresetName: String?,
    presetMenuOpen: Boolean,
    onMenuExpanded: (Boolean) -> Unit,
    onSelect: (String, String, String?) -> Unit
) {
    Text("1. Choose connection family", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Text(
        text = "A preset only prepares a family and HTTPS suggestion. It does not create a connection or choose a model.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    ExposedDropdownMenuBox(expanded = presetMenuOpen, onExpandedChange = onMenuExpanded) {
        OutlinedTextField(
            value = selectedPresetName ?: when (selectedPresetId) {
                ADVANCED_CUSTOM_PRESET_ID -> "Advanced custom OpenAI-compatible"
                LOCAL_LOOPBACK_HTTPS_PRESET_ID -> "Local device server · HTTPS"
                LOCAL_LAN_HTTPS_PRESET_ID -> "Private-LAN server · HTTPS"
                else -> "Choose provider family"
            },
            onValueChange = {},
            readOnly = true,
            label = { Text("Connection family") },
            supportingText = { Text("Cloud and local endpoints are HTTPS-only. IVAI never scans or discovers endpoints.") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(presetMenuOpen) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
                .testTag("provider_preset_selector")
        )
        ExposedDropdownMenu(
            expanded = presetMenuOpen,
            onDismissRequest = { onMenuExpanded(false) }
        ) {
            ProviderPresetCatalog.all.forEach { preset ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text("${preset.displayName} · ${preset.protocolLabel}") },
                    onClick = {
                        onSelect(preset.id, preset.displayName, preset.suggestedBaseUrl)
                        onMenuExpanded(false)
                    },
                    modifier = Modifier.testTag("provider_family_${preset.id}")
                )
            }
            androidx.compose.material3.DropdownMenuItem(
                text = { Text("Local device server · HTTPS") },
                onClick = {
                    onSelect(LOCAL_LOOPBACK_HTTPS_PRESET_ID, "Local device server", null)
                    onMenuExpanded(false)
                },
                modifier = Modifier.testTag("provider_family_local_loopback")
            )
            androidx.compose.material3.DropdownMenuItem(
                text = { Text("Private-LAN server · HTTPS") },
                onClick = {
                    onSelect(LOCAL_LAN_HTTPS_PRESET_ID, "Private-LAN server", null)
                    onMenuExpanded(false)
                },
                modifier = Modifier.testTag("provider_family_local_lan")
            )
            androidx.compose.material3.DropdownMenuItem(
                text = { Text("Advanced custom OpenAI-compatible") },
                onClick = {
                    onSelect(ADVANCED_CUSTOM_PRESET_ID, "Custom OpenAI-compatible", null)
                    onMenuExpanded(false)
                },
                modifier = Modifier.testTag("provider_family_advanced_custom")
            )
        }
    }
}

@Composable
private fun ProviderEndpointTrustStep(
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    requiresEndpoint: Boolean,
    endpoint: String,
    onEndpointChange: (String) -> Unit,
    trustMode: ProviderEndpointTrustMode,
    isLocalEndpoint: Boolean,
    localTrustConfirmed: Boolean,
    onTrustConfirmationChange: (Boolean) -> Unit
) {
    Text("2. Name and review HTTPS trust", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    TrustZoneCard(trustMode = trustMode)
    OutlinedTextField(
        value = displayName,
        onValueChange = onDisplayNameChange,
        label = { Text("Connection name") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("input_provider_display_name")
    )
    if (requiresEndpoint) {
        OutlinedTextField(
            value = endpoint,
            onValueChange = onEndpointChange,
            label = { Text("HTTPS base URL") },
            supportingText = {
                Text(
                    when (trustMode) {
                        ProviderEndpointTrustMode.REMOTE_HTTPS -> "Review this public remote HTTPS endpoint before saving."
                        ProviderEndpointTrustMode.LOCAL_LOOPBACK_HTTPS -> "Exact host only: localhost, 127.0.0.1 or ::1. HTTP is blocked."
                        ProviderEndpointTrustMode.LOCAL_LAN_HTTPS -> "RFC1918 IPv4 only: 10/8, 172.16/12 or 192.168/16. HTTP is blocked."
                    }
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_provider_endpoint")
        )
    }
    if (isLocalEndpoint) {
        FilterChip(
            selected = localTrustConfirmed,
            onClick = { onTrustConfirmationChange(!localTrustConfirmed) },
            label = { Text("I trust this exact HTTPS endpoint and understand messages go directly to it") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("local_endpoint_trust_confirmation")
        )
    }
}

@Composable
private fun TrustZoneCard(trustMode: ProviderEndpointTrustMode) {
    val (title, detail) = when (trustMode) {
        ProviderEndpointTrustMode.REMOTE_HTTPS -> "Remote HTTPS" to "A public provider endpoint. IVAI uses HTTPS only and never makes this provider a default."
        ProviderEndpointTrustMode.LOCAL_LOOPBACK_HTTPS -> "Local-device HTTPS" to "An explicit endpoint on this device only. No local discovery, scan, HTTP, or certificate bypass is available."
        ProviderEndpointTrustMode.LOCAL_LAN_HTTPS -> "Private-LAN HTTPS" to "An explicit RFC1918 endpoint. It remains HTTPS-only, user-confirmed, and is never discovered automatically."
    }
    Surface(
        shape = RoundedCornerShape(IvaiShapeTokens.Control),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("provider_trust_${trustMode.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier.padding(IvaiSpacing.XSmall),
            horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall),
            verticalAlignment = Alignment.Top
        ) {
            Icon(trustMode.trustIcon(), contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Column {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
    }
}

@Composable
private fun ProviderAccountStep(
    accountName: String,
    onAccountNameChange: (String) -> Unit,
    isLocalEndpoint: Boolean,
    authMode: ProviderAccountAuthMode,
    onAuthModeChange: (ProviderAccountAuthMode) -> Unit,
    apiKey: String,
    onApiKeyChange: (String) -> Unit
) {
    Text("3. Add account and credential", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Text(
        text = "An API key is kept only in the encrypted local vault when you confirm final save. It is never displayed again.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    OutlinedTextField(
        value = accountName,
        onValueChange = onAccountNameChange,
        label = { Text("Account label") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("input_provider_account_name")
    )
    if (isLocalEndpoint) {
        FilterChip(
            selected = authMode == ProviderAccountAuthMode.NONE,
            onClick = {
                onAuthModeChange(
                    if (authMode == ProviderAccountAuthMode.NONE) ProviderAccountAuthMode.API_KEY else ProviderAccountAuthMode.NONE
                )
            },
            label = { Text("This local server requires no API key") },
            modifier = Modifier.fillMaxWidth().testTag("local_no_auth_selector")
        )
        Text(
            text = "No-auth is explicit. IVAI does not generate, store or send a placeholder token.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    if (authMode == ProviderAccountAuthMode.API_KEY) {
        OutlinedTextField(
            value = apiKey,
            onValueChange = onApiKeyChange,
            label = { Text("API key") },
            supportingText = { Text("Stored only after final save; never shown again.") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().testTag("input_provider_api_key")
        )
    }
}

@Composable
private fun ProviderModelReviewStep(
    kind: ProviderKind?,
    displayName: String,
    trustMode: ProviderEndpointTrustMode,
    accountName: String,
    authMode: ProviderAccountAuthMode,
    modelId: String,
    onModelIdChange: (String) -> Unit,
    capabilities: Set<ProviderCapability>,
    onCapabilitiesChange: (Set<ProviderCapability>) -> Unit
) {
    Text("4. Declare model and review", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Text(
        text = "Model discovery is not automatic. Declare the specific model and capabilities you intend to use.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    OutlinedTextField(
        value = modelId,
        onValueChange = onModelIdChange,
        label = { Text("Model ID selected by you") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("input_provider_model_id")
    )
    Text("Declared model capabilities", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    ProviderCapability.entries.forEach { capability ->
        FilterChip(
            selected = capability in capabilities,
            onClick = {
                onCapabilitiesChange(
                    if (capability in capabilities) capabilities - capability else capabilities + capability
                )
            },
            label = { Text(capability.name) },
            modifier = Modifier.fillMaxWidth().testTag("provider_capability_${capability.name.lowercase()}")
        )
    }
    Surface(
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth().testTag("provider_setup_final_review")
    ) {
        Column(
            modifier = Modifier.padding(IvaiSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XxSmall)
        ) {
            Text("Final review", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            ConnectionReadinessRow("Family", kind?.displayLabel() ?: "Not selected")
            ConnectionReadinessRow("Connection", displayName.ifBlank { "Not named" })
            ConnectionReadinessRow("Trust", trustMode.displayLabel())
            ConnectionReadinessRow("Account", accountName.ifBlank { "Not named" })
            ConnectionReadinessRow(
                "Credential",
                if (authMode == ProviderAccountAuthMode.NONE) "No-auth explicitly selected" else "API key will be encrypted after save"
            )
            ConnectionReadinessRow("Model", modelId.ifBlank { "Not declared" })
            ConnectionReadinessRow("Capabilities", capabilities.joinToString().ifBlank { "None selected" })
        }
    }
}

private fun ProviderKind.displayLabel(): String = when (this) {
    ProviderKind.GEMINI -> "Gemini"
    ProviderKind.OPENROUTER -> "OpenRouter"
    ProviderKind.CUSTOM_OPENAI_COMPATIBLE -> "OpenAI-compatible"
}

private fun ProviderEndpointTrustMode.displayLabel(): String = when (this) {
    ProviderEndpointTrustMode.REMOTE_HTTPS -> "Remote HTTPS"
    ProviderEndpointTrustMode.LOCAL_LOOPBACK_HTTPS -> "Trusted local-device HTTPS"
    ProviderEndpointTrustMode.LOCAL_LAN_HTTPS -> "Trusted private-LAN HTTPS"
}

private fun ProviderEndpointTrustMode.trustIcon() = when (this) {
    ProviderEndpointTrustMode.REMOTE_HTTPS -> Icons.Default.Cloud
    ProviderEndpointTrustMode.LOCAL_LOOPBACK_HTTPS -> Icons.Default.Lock
    ProviderEndpointTrustMode.LOCAL_LAN_HTTPS -> Icons.Default.Lan
}
