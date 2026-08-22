package dev.iliv007.ivai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

val LocalDarkTheme = compositionLocalOf { false }
val LocalToggleTheme = compositionLocalOf<() -> Unit> { {} }

private val IvaiLightColorScheme = lightColorScheme(
    primary = LightActionPrimary,
    onPrimary = LightOnActionPrimary,
    primaryContainer = LightActionPrimaryContainer,
    onPrimaryContainer = LightOnActionPrimaryContainer,
    secondary = LightActionSecondary,
    onSecondary = LightOnActionPrimary,
    secondaryContainer = LightActionSecondaryContainer,
    onSecondaryContainer = LightOnActionSecondaryContainer,
    tertiary = LightActionTertiary,
    onTertiary = LightOnActionPrimary,
    tertiaryContainer = LightActionTertiaryContainer,
    onTertiaryContainer = LightOnActionTertiaryContainer,
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
    onError = LightOnActionPrimary
)

private val IvaiDarkColorScheme = darkColorScheme(
    primary = DarkActionPrimary,
    onPrimary = DarkOnActionPrimary,
    primaryContainer = DarkActionPrimaryContainer,
    onPrimaryContainer = DarkOnActionPrimaryContainer,
    secondary = DarkActionSecondary,
    onSecondary = DarkOnActionPrimary,
    secondaryContainer = DarkActionSecondaryContainer,
    onSecondaryContainer = DarkOnActionSecondaryContainer,
    tertiary = DarkActionTertiary,
    onTertiary = DarkOnActionPrimary,
    tertiaryContainer = DarkActionTertiaryContainer,
    onTertiaryContainer = DarkOnActionTertiaryContainer,
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
    onError = DarkOnActionPrimary
)

@Composable
fun IvaiTheme(
    darkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) IvaiDarkColorScheme else IvaiLightColorScheme

    CompositionLocalProvider(
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
