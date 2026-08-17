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
 * The Android launcher artwork remains launcher-only. These tokens define an independent UI
 * system that can harmonize with the app icon's atmosphere without reusing its mark or artwork.
 */
object IvaiSpacing {
    val XxxSmall: Dp = 4.dp
    val XxSmall: Dp = 8.dp
    val XSmall: Dp = 12.dp
    val Small: Dp = 16.dp
    val Medium: Dp = 24.dp
    val Large: Dp = 32.dp
    val XLarge: Dp = 48.dp
}

object IvaiShapeTokens {
    val Small: Dp = 8.dp
    val Control: Dp = 12.dp
    val Card: Dp = 16.dp
    val Sheet: Dp = 20.dp
}

object IvaiElevationTokens {
    val Flat: Dp = 0.dp
    val Raised: Dp = 1.dp
    val Active: Dp = 3.dp
    val Overlay: Dp = 6.dp
}

object IvaiStrokeTokens {
    val Default: Dp = 1.dp
    val Subtle: Dp = 0.5.dp
}

object IvaiIconSizeTokens {
    val Meta: Dp = 16.dp
    val Inline: Dp = 18.dp
    val Navigation: Dp = 24.dp
    val Feature: Dp = 32.dp
}

object IvaiMotionTokens {
    const val StateChangeMillis: Int = 100
    const val SelectionMillis: Int = 180
    const val SheetMillis: Int = 240
}

object IvaiLayoutTokens {
    val MinimumTouchTarget: Dp = 48.dp
    val MediumBreakpoint: Dp = 600.dp
    val ExpandedBreakpoint: Dp = 840.dp
    val MediumRailWidth: Dp = 80.dp
    val ExpandedRailWidth: Dp = 152.dp
    val ChatDrawerWidth: Dp = 336.dp
}

@Immutable
data class IvaiSemanticColors(
    val canvas: Color,
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

/**
 * Maps the approved light/dark palette to roles used by future IVAI UI primitives.
 * Product screens must consume these roles or [MaterialTheme.colorScheme], not raw hex values.
 */
@Composable
fun rememberIvaiSemanticColors(): IvaiSemanticColors {
    val colors = MaterialTheme.colorScheme
    val darkTheme = LocalDarkTheme.current
    return IvaiSemanticColors(
        canvas = colors.background,
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
        stateInfo = colors.tertiary
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

/** Decorative-only UI gradients. Never place body text or state meaning on them. */
object IvaiDecorativeGradients {
    @Composable
    fun headerGlow(): Brush {
        val colors = MaterialTheme.colorScheme
        return Brush.linearGradient(
            listOf(
                colors.primary.copy(alpha = 0.16f),
                colors.secondary.copy(alpha = 0.14f)
            )
        )
    }

    @Composable
    fun accent(): Brush {
        val colors = MaterialTheme.colorScheme
        return Brush.linearGradient(listOf(colors.primary, colors.secondary))
    }
}
