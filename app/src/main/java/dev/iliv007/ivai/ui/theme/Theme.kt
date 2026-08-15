package dev.iliv007.ivai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

val LocalDarkTheme = compositionLocalOf { false }
val LocalToggleTheme = compositionLocalOf<() -> Unit> { {} }

private val IvaiLightColorScheme = lightColorScheme(
    primary = JadePrimaryLight,
    onPrimary = LightSurface,
    primaryContainer = JadeContainerLight,
    onPrimaryContainer = JadeOnContainerLight,
    secondary = IndigoSecondaryLight,
    onSecondary = LightSurface,
    secondaryContainer = IndigoContainerLight,
    onSecondaryContainer = IndigoOnContainerLight,
    tertiary = CyanPrimaryLight,
    onTertiary = LightSurface,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainer = LightSurfaceElevated,
    surfaceContainerHigh = LightSurfaceVariant,
    outline = LightBorder,
    outlineVariant = LightBorderSubtle,
    error = IvaiErrorLight,
    onError = LightSurface
)

private val IvaiDarkColorScheme = darkColorScheme(
    primary = JadePrimary,
    onPrimary = IvaiBackground,
    primaryContainer = JadeDark,
    onPrimaryContainer = JadeBright,
    secondary = NeonViolet,
    onSecondary = TextPrimary,
    secondaryContainer = PurpleDark,
    onSecondaryContainer = NeonPurple,
    tertiary = CyanPrimary,
    onTertiary = IvaiBackground,
    background = IvaiBackground,
    onBackground = TextPrimary,
    surface = IvaiSurface,
    onSurface = TextPrimary,
    surfaceVariant = IvaiSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = IvaiElevated,
    surfaceContainerHigh = IvaiSurfaceVariant,
    outline = IvaiBorder,
    outlineVariant = IvaiBorderSubtle,
    error = IvaiError,
    onError = TextPrimary
)

@Composable
fun IvaiTheme(
    darkTheme: Boolean = false, // Default is LIGHT theme as requested
    onToggleTheme: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) IvaiDarkColorScheme else IvaiLightColorScheme

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr,
        LocalDarkTheme provides darkTheme,
        LocalToggleTheme provides onToggleTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = IvaiTypography,
            content = content
        )
    }
}
