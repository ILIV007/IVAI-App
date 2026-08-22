package dev.iliv007.ivai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared visual decisions for the in-app IVAI experience.
 *
 * The launcher artwork remains launcher-only. Tokens express hierarchy, not decoration: screens
 * must consume these semantic roles or MaterialTheme roles rather than raw colors, dimensions or
 * gradients.
 */
object IvaiSpacing {
    val XxxSmall: Dp = 4.dp
    val XxSmall: Dp = 8.dp
    val XSmall: Dp = 12.dp
    val Small: Dp = 16.dp
    val Medium: Dp = 24.dp
    val Large: Dp = 32.dp
    val XLarge: Dp = 48.dp

    val ScreenCompact: Dp = 20.dp
    val ScreenMedium: Dp = 24.dp
    val ScreenExpanded: Dp = 32.dp
    val Section: Dp = 24.dp
}

object IvaiShapeTokens {
    val Small: Dp = 8.dp
    val Control: Dp = 8.dp
    val Group: Dp = 10.dp
    val Card: Dp = 12.dp
    val Sheet: Dp = 16.dp
}

object IvaiElevationTokens {
    val Flat: Dp = 0.dp
    val Raised: Dp = 0.dp
    val Active: Dp = 1.dp
    val Overlay: Dp = 3.dp
}

object IvaiStrokeTokens {
    val Default: Dp = 1.dp
    val Subtle: Dp = 0.5.dp
}

object IvaiIconSizeTokens {
    val Meta: Dp = 16.dp
    val Inline: Dp = 20.dp
    val Navigation: Dp = 24.dp
    val Feature: Dp = 32.dp
}

object IvaiMotionTokens {
    const val StateChangeMillis: Int = 150
    const val SelectionMillis: Int = 180
    const val SheetMillis: Int = 220
}

object IvaiLayoutTokens {
    val MinimumTouchTarget: Dp = 48.dp
    val ListRowMinimumHeight: Dp = 56.dp
    val MediumBreakpoint: Dp = 600.dp
    val ExpandedBreakpoint: Dp = 840.dp
    val MediumRailWidth: Dp = 80.dp
    val ExpandedRailWidth: Dp = 152.dp
    val ChatDrawerWidth: Dp = 336.dp
}

@Immutable
data class IvaiSemanticColors(
    val canvas: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceInteractive: Color,
    val border: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val actionPrimary: Color,
    val actionOnPrimary: Color,
    val actionSecondary: Color,
    val actionTertiary: Color,
    val stateSuccess: Color,
    val stateWarning: Color,
    val stateError: Color,
    val stateInfo: Color
)

/** Decorative terminal controls mapped to semantic roles in both themes. */
@Immutable
data class IvaiTerminalControlColors(
    val close: Color,
    val minimize: Color,
    val maximize: Color
)

/** Product screens consume these roles or MaterialTheme.colorScheme, never raw palette values. */
@Composable
fun rememberIvaiSemanticColors(): IvaiSemanticColors {
    val colors = MaterialTheme.colorScheme
    val darkTheme = LocalDarkTheme.current
    return IvaiSemanticColors(
        canvas = colors.background,
        surface = colors.surface,
        surfaceRaised = colors.surfaceContainer,
        surfaceInteractive = colors.surfaceVariant,
        border = colors.outline,
        borderSubtle = colors.outlineVariant,
        textPrimary = colors.onSurface,
        textSecondary = colors.onSurfaceVariant,
        textMuted = if (darkTheme) TextMuted else LightTextMuted,
        actionPrimary = colors.primary,
        actionOnPrimary = colors.onPrimary,
        actionSecondary = colors.secondary,
        actionTertiary = colors.tertiary,
        stateSuccess = if (darkTheme) IvaiSuccess else IvaiSuccessLight,
        stateWarning = if (darkTheme) IvaiWarning else IvaiWarningLight,
        stateError = colors.error,
        stateInfo = colors.primary
    )
}

@Composable
fun rememberIvaiTerminalControlColors(): IvaiTerminalControlColors {
    val colors = MaterialTheme.colorScheme
    val semantic = rememberIvaiSemanticColors()
    return IvaiTerminalControlColors(
        close = colors.error,
        minimize = semantic.stateWarning,
        maximize = semantic.stateSuccess
    )
}

/** Decorative-only compatibility helpers. Never place body text or state meaning on gradients. */
object IvaiDecorativeGradients {
    @Composable
    fun headerGlow(): Brush {
        val colors = MaterialTheme.colorScheme
        return Brush.linearGradient(
            listOf(colors.primary.copy(alpha = 0.10f), colors.secondary.copy(alpha = 0.08f))
        )
    }

    @Composable
    fun accent(): Brush {
        val colors = MaterialTheme.colorScheme
        return Brush.linearGradient(listOf(colors.primary, colors.secondary))
    }
}
