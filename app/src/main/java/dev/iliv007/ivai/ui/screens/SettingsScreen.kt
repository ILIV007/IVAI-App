package dev.iliv007.ivai.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import dev.iliv007.ivai.ui.components.IvaiPageHeader
import dev.iliv007.ivai.ui.components.IvaiScreenScaffold
import dev.iliv007.ivai.ui.components.IvaiStateCard
import dev.iliv007.ivai.ui.components.IvaiStateTone
import dev.iliv007.ivai.ui.theme.IvaiElevationTokens
import dev.iliv007.ivai.ui.theme.IvaiIconSizeTokens
import dev.iliv007.ivai.ui.theme.IvaiLayoutTokens
import dev.iliv007.ivai.ui.theme.IvaiShapeTokens
import dev.iliv007.ivai.ui.theme.IvaiSpacing
import dev.iliv007.ivai.ui.theme.IvaiStrokeTokens
import dev.iliv007.ivai.ui.theme.rememberIvaiSemanticColors

/**
 * Settings only surfaces user-controlled presentation, navigation, privacy commitments, and the
 * existing local-data deletion action. Provider management and credential operations stay in
 * Connections, where their explicit review flows already exist.
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onDeleteAllLocalData: () -> Unit = {},
    onOpenConnections: () -> Unit = {}
) {
    var deleteConfirmationOpen by remember { mutableStateOf(false) }

    IvaiScreenScaffold(modifier = modifier, testTag = "settings_screen") {
        LazyColumn(
            contentPadding = PaddingValues(IvaiSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.Medium)
        ) {
            item {
                IvaiPageHeader(
                    title = "Settings",
                    subtitle = "Appearance, user-controlled connections, privacy, and local data.",
                    testTag = "settings_header"
                )
            }

            item {
                SettingsSection(
                    title = "Appearance",
                    description = "Choose the display mode used on this device.",
                    testTag = "settings_appearance_section"
                ) {
                    AppearanceSettingsCard(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme
                    )
                }
            }

            item {
                SettingsSection(
                    title = "Connections",
                    description = "Providers, credentials, models, and Combos remain under your control.",
                    testTag = "settings_connections_section"
                ) {
                    ConnectionsSettingsCard(onOpenConnections = onOpenConnections)
                }
            }

            item {
                SettingsSection(
                    title = "Privacy",
                    description = "IVAI keeps its operating commitments visible and specific.",
                    testTag = "settings_privacy_section"
                ) {
                    PrivacySettingsContent()
                }
            }

            item {
                SettingsSection(
                    title = "Local data",
                    description = "Review the consequence before removing data from this device.",
                    testTag = "settings_local_data_section"
                ) {
                    LocalDataSettingsCard(onRequestDelete = { deleteConfirmationOpen = true })
                }
            }
        }
    }

    if (deleteConfirmationOpen) {
        LocalDataDeleteConfirmationDialog(
            onCancel = { deleteConfirmationOpen = false },
            onConfirm = {
                deleteConfirmationOpen = false
                onDeleteAllLocalData()
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    description: String,
    testTag: String,
    content: @Composable () -> Unit
) {
    val semanticColors = rememberIvaiSemanticColors()
    Column(
        modifier = Modifier.testTag(testTag),
        verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = semanticColors.textPrimary,
            modifier = Modifier.semantics { this[SemanticsProperties.Heading] = Unit }
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = semanticColors.textSecondary
        )
        content()
    }
}

@Composable
private fun AppearanceSettingsCard(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val semanticColors = rememberIvaiSemanticColors()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings_theme_card"),
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        color = semanticColors.surfaceRaised,
        border = BorderStroke(IvaiStrokeTokens.Default, semanticColors.border),
        tonalElevation = IvaiElevationTokens.Raised
    ) {
        Row(
            modifier = Modifier.padding(IvaiSpacing.Small),
            horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsFeatureIcon(
                icon = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                tone = semanticColors.actionPrimary
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XxxSmall)
            ) {
                Text(
                    text = "Dark mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = semanticColors.textPrimary
                )
                Text(
                    text = if (isDarkTheme) "Dark mode is on." else "Light mode is on.",
                    style = MaterialTheme.typography.bodySmall,
                    color = semanticColors.textSecondary
                )
            }
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { onToggleTheme() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = semanticColors.actionOnPrimary,
                    checkedTrackColor = semanticColors.actionPrimary,
                    uncheckedThumbColor = semanticColors.textSecondary,
                    uncheckedTrackColor = semanticColors.surfaceInteractive
                ),
                modifier = Modifier
                    .testTag("switch_theme_mode")
                    .semantics {
                        contentDescription = "Toggle dark mode"
                        stateDescription = if (isDarkTheme) "Dark mode on" else "Dark mode off"
                    }
            )
        }
    }
}

@Composable
private fun ConnectionsSettingsCard(onOpenConnections: () -> Unit) {
    val semanticColors = rememberIvaiSemanticColors()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings_connections_shortcut"),
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        color = semanticColors.surfaceRaised,
        border = BorderStroke(IvaiStrokeTokens.Default, semanticColors.border),
        tonalElevation = IvaiElevationTokens.Raised
    ) {
        Column(
            modifier = Modifier.padding(IvaiSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SettingsFeatureIcon(
                    icon = Icons.Default.Key,
                    tone = semanticColors.actionSecondary
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XxxSmall)
                ) {
                    Text(
                        text = "Manage Connections",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = semanticColors.textPrimary
                    )
                    Text(
                        text = "Add and review providers, HTTPS trust, credentials, declared models, and ordered Combos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = semanticColors.textSecondary
                    )
                }
            }
            OutlinedButton(
                onClick = onOpenConnections,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = IvaiLayoutTokens.MinimumTouchTarget)
                    .testTag("button_open_connections_from_settings")
            ) {
                Text("Open Connections")
            }
        }
    }
}

@Composable
private fun PrivacySettingsContent() {
    val semanticColors = rememberIvaiSemanticColors()
    Column(verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)) {
        IvaiStateCard(
            title = "Your data stays under your control",
            message = "IVAI has no central backend, mandatory account, analytics tracker, or remote error log. Credentials are managed on this device.",
            tone = IvaiStateTone.SUCCESS,
            icon = Icons.Default.Shield,
            testTag = "settings_privacy_summary"
        )
        PrivacyCommitmentCard(
            title = "Network protection",
            description = "Provider calls require HTTPS. Cleartext connections are not accepted."
        )
        PrivacyCommitmentCard(
            title = "Explicit execution target",
            description = "A provider, model, or Combo is chosen by you before a chat or Agent run uses it."
        )
    }
}

@Composable
private fun PrivacyCommitmentCard(
    title: String,
    description: String
) {
    val semanticColors = rememberIvaiSemanticColors()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(IvaiShapeTokens.Control),
        color = semanticColors.surfaceInteractive,
        border = BorderStroke(IvaiStrokeTokens.Subtle, semanticColors.borderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(IvaiSpacing.XSmall),
            horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = semanticColors.actionPrimary,
                modifier = Modifier.size(IvaiIconSizeTokens.Meta)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XxxSmall)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = semanticColors.textPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = semanticColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun LocalDataSettingsCard(onRequestDelete: () -> Unit) {
    val semanticColors = rememberIvaiSemanticColors()
    IvaiStateCard(
        title = "Delete all local data",
        message = "This permanently removes IVAI's local database, workspace files, and encrypted provider credentials from this device.",
        tone = IvaiStateTone.ERROR,
        icon = Icons.Default.DeleteOutline,
        action = {
            OutlinedButton(
                onClick = onRequestDelete,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = semanticColors.stateError),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = IvaiLayoutTokens.MinimumTouchTarget)
                    .testTag("button_delete_all_data")
            ) {
                Text("Delete all local data")
            }
        },
        testTag = "settings_local_data"
    )
}

@Composable
private fun LocalDataDeleteConfirmationDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val semanticColors = rememberIvaiSemanticColors()
    AlertDialog(
        onDismissRequest = onCancel,
        modifier = Modifier.testTag("local_data_delete_confirmation"),
        title = {
            Text(
                text = "Permanently delete local data?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)) {
                Text("This action is permanent on this device.", style = MaterialTheme.typography.bodyMedium)
                Text("It removes only IVAI data stored here:", style = MaterialTheme.typography.bodySmall)
                Text("• Local workspace project files", style = MaterialTheme.typography.bodySmall)
                Text("• Local database records, including chats, projects, Agents, Connections, Accounts, Models and Combos", style = MaterialTheme.typography.bodySmall)
                Text("• Encrypted stored provider credentials", style = MaterialTheme.typography.bodySmall)
                Text(
                    "It does not delete remote provider data, system backups, external files or data from other apps.",
                    style = MaterialTheme.typography.labelSmall,
                    color = semanticColors.textSecondary
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.testTag("button_cancel_delete_all_data")
            ) {
                Text("Cancel")
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = semanticColors.stateError,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.testTag("button_confirm_delete_all_data")
            ) {
                Text("Delete permanently")
            }
        }
    )
}

@Composable
private fun SettingsFeatureIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tone: androidx.compose.ui.graphics.Color
) {
    val semanticColors = rememberIvaiSemanticColors()
    Surface(
        shape = RoundedCornerShape(IvaiShapeTokens.Control),
        color = semanticColors.surfaceInteractive
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tone,
            modifier = Modifier
                .padding(IvaiSpacing.XSmall)
                .size(IvaiIconSizeTokens.Inline)
        )
    }
}
