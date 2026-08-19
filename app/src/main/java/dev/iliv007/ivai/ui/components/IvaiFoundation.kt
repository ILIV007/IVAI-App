package dev.iliv007.ivai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import dev.iliv007.ivai.ui.theme.IvaiElevationTokens
import dev.iliv007.ivai.ui.theme.IvaiIconSizeTokens
import dev.iliv007.ivai.ui.theme.IvaiShapeTokens
import dev.iliv007.ivai.ui.theme.IvaiSpacing
import dev.iliv007.ivai.ui.theme.IvaiStrokeTokens
import dev.iliv007.ivai.ui.theme.rememberIvaiSemanticColors

/**
 * Shared root surface for redesigned Phase 7 screens.
 *
 * It does not supply navigation or mutate layout direction. Screens retain their existing runtime
 * behavior while adopting a consistent canvas and rhythm.
 */
@Composable
fun IvaiScreenScaffold(
    modifier: Modifier = Modifier,
    testTag: String? = null,
    content: @Composable () -> Unit
) {
    val semanticColors = rememberIvaiSemanticColors()
    val taggedModifier = if (testTag == null) modifier else modifier.testTag(testTag)
    Surface(
        modifier = taggedModifier.fillMaxSize(),
        color = semanticColors.canvas,
        contentColor = semanticColors.textPrimary
    ) {
        content()
    }
}

@Composable
fun IvaiPageHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    testTag: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val semanticColors = rememberIvaiSemanticColors()
    val taggedModifier = if (testTag == null) modifier else modifier.testTag(testTag)
    Row(
        modifier = taggedModifier
            .fillMaxWidth()
            .padding(horizontal = IvaiSpacing.Small, vertical = IvaiSpacing.XSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XxxSmall)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = semanticColors.textPrimary,
                modifier = Modifier.semantics { this[SemanticsProperties.Heading] = Unit }
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = semanticColors.textSecondary
                )
            }
        }
        actions()
    }
}

enum class IvaiStateTone {
    NEUTRAL,
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

@Composable
private fun IvaiStateTone.color() = when (this) {
    IvaiStateTone.NEUTRAL -> rememberIvaiSemanticColors().textSecondary
    IvaiStateTone.INFO -> rememberIvaiSemanticColors().stateInfo
    IvaiStateTone.SUCCESS -> rememberIvaiSemanticColors().stateSuccess
    IvaiStateTone.WARNING -> rememberIvaiSemanticColors().stateWarning
    IvaiStateTone.ERROR -> rememberIvaiSemanticColors().stateError
}

private fun IvaiStateTone.description() = when (this) {
    IvaiStateTone.NEUTRAL -> "Information"
    IvaiStateTone.INFO -> "In progress information"
    IvaiStateTone.SUCCESS -> "Completed successfully"
    IvaiStateTone.WARNING -> "Attention required"
    IvaiStateTone.ERROR -> "Action failed"
}

/**
 * Reusable card for empty, offline, error, progress and success explanations.
 * The title, message and action are the source of meaning; the icon is deliberately decorative.
 */
@Composable
fun IvaiStateCard(
    title: String,
    message: String,
    tone: IvaiStateTone,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
    testTag: String? = null
) {
    val semanticColors = rememberIvaiSemanticColors()
    val stateColor = tone.color()
    val accessibleStateDescription = tone.description()
    val taggedModifier = if (testTag == null) modifier else modifier.testTag(testTag)
    Surface(
        modifier = taggedModifier
            .fillMaxWidth()
            .semantics {
                stateDescription = accessibleStateDescription
            },
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        color = semanticColors.surfaceRaised,
        border = BorderStroke(IvaiStrokeTokens.Default, stateColor.copy(alpha = 0.38f)),
        tonalElevation = IvaiElevationTokens.Raised
    ) {
        Column(
            modifier = Modifier.padding(IvaiSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = stateColor,
                        modifier = Modifier.size(IvaiIconSizeTokens.Inline)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = semanticColors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = semanticColors.textSecondary
            )
            action?.invoke()
        }
    }
}

/** Displays a direct model or user-created Combo without suggesting a default target. */
@Composable
fun IvaiTargetChip(
    label: String,
    availabilityLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    testTag: String? = null
) {
    val semanticColors = rememberIvaiSemanticColors()
    val taggedModifier = if (testTag == null) modifier else modifier.testTag(testTag)
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = taggedModifier
            .heightIn(min = IvaiSpacing.XLarge)
            .semantics {
                role = Role.Button
                stateDescription = availabilityLabel
            },
        shape = RoundedCornerShape(IvaiShapeTokens.Control),
        color = semanticColors.surfaceInteractive,
        contentColor = semanticColors.textPrimary,
        border = BorderStroke(IvaiStrokeTokens.Default, semanticColors.border),
        tonalElevation = IvaiElevationTokens.Flat
    ) {
        Row(
            modifier = Modifier.padding(horizontal = IvaiSpacing.XSmall, vertical = IvaiSpacing.XxSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XxSmall)
        ) {
            leadingIcon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(IvaiIconSizeTokens.Inline)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XxxSmall)) {
                Text(text = label, style = MaterialTheme.typography.labelLarge)
                Text(
                    text = availabilityLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = semanticColors.textSecondary
                )
            }
        }
    }
}

enum class IvaiExecutionState(val stateLabel: String) {
    READY("Ready"),
    CONNECTING("Connecting"),
    STREAMING("Streaming"),
    AWAITING_APPROVAL("Awaiting one-time approval"),
    COMPLETED("Completed"),
    STOPPED("Stopped"),
    FAILED("Failed")
}

@Composable
private fun IvaiExecutionState.tone() = when (this) {
    IvaiExecutionState.READY -> IvaiStateTone.NEUTRAL
    IvaiExecutionState.CONNECTING, IvaiExecutionState.STREAMING -> IvaiStateTone.INFO
    IvaiExecutionState.AWAITING_APPROVAL -> IvaiStateTone.WARNING
    IvaiExecutionState.COMPLETED -> IvaiStateTone.SUCCESS
    IvaiExecutionState.STOPPED, IvaiExecutionState.FAILED -> IvaiStateTone.ERROR
}

/**
 * Announces target-bound execution state. Terminal or approval transitions may request a polite
 * live-region announcement without attaching network/runtime behavior to the component.
 */
@Composable
fun IvaiExecutionStatusBanner(
    state: IvaiExecutionState,
    targetLabel: String,
    detail: String,
    modifier: Modifier = Modifier,
    announceChange: Boolean = false,
    action: (@Composable () -> Unit)? = null,
    testTag: String? = null
) {
    val semanticColors = rememberIvaiSemanticColors()
    val stateColor = state.tone().color()
    val taggedModifier = if (testTag == null) modifier else modifier.testTag(testTag)
    Surface(
        modifier = taggedModifier
            .fillMaxWidth()
            .semantics {
                stateDescription = "${state.stateLabel}: $targetLabel. $detail"
                if (announceChange) {
                    liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite
                }
            },
        shape = RoundedCornerShape(IvaiShapeTokens.Card),
        color = semanticColors.surfaceInteractive,
        border = BorderStroke(IvaiStrokeTokens.Default, stateColor.copy(alpha = 0.45f)),
        tonalElevation = IvaiElevationTokens.Flat
    ) {
        Row(
            modifier = Modifier.padding(IvaiSpacing.XSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(IvaiSpacing.XSmall)
        ) {
            Surface(
                modifier = Modifier.size(IvaiSpacing.XSmall),
                shape = RoundedCornerShape(IvaiShapeTokens.Small),
                color = stateColor,
                content = {}
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(IvaiSpacing.XxxSmall)) {
                Text(
                    text = state.stateLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = semanticColors.textPrimary
                )
                Text(
                    text = targetLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = semanticColors.textSecondary
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = semanticColors.textSecondary
                )
            }
            action?.invoke()
        }
    }
}
