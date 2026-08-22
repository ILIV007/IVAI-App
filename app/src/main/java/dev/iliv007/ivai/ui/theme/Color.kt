package dev.iliv007.ivai.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * UX-2 semantic palette.
 *
 * Light and dark are sibling systems: each is designed from its own canvas, surface and ink
 * hierarchy. The launcher artwork remains launcher-only; gradients below are decorative-only
 * compatibility helpers and may not convey state, text hierarchy or target readiness.
 */

// Light — quiet neutral canvas with a restrained indigo action color.
val LightBackground = Color(0xFFF7F8FC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFEEF1F7)
val LightSurfaceVariant = Color(0xFFE6E9FF)
val LightBorder = Color(0xFFD9E0EB)
val LightBorderSubtle = Color(0xFFE8ECF3)

val LightTextPrimary = Color(0xFF17202E)
val LightTextSecondary = Color(0xFF526072)
val LightTextMuted = Color(0xFF667085)

val LightActionPrimary = Color(0xFF4656C8)
val LightOnActionPrimary = Color(0xFFFFFFFF)
val LightActionPrimaryContainer = Color(0xFFE6E9FF)
val LightOnActionPrimaryContainer = Color(0xFF25337A)
val LightActionSecondary = Color(0xFF5E508D)
val LightActionSecondaryContainer = Color(0xFFEEE9FA)
val LightOnActionSecondaryContainer = Color(0xFF35295D)
val LightActionTertiary = Color(0xFF087E9A)
val LightActionTertiaryContainer = Color(0xFFDDF5F8)
val LightOnActionTertiaryContainer = Color(0xFF064B5C)

val IvaiErrorLight = Color(0xFFB3261E)
val IvaiWarningLight = Color(0xFF9C6200)
val IvaiSuccessLight = Color(0xFF16785F)

// Compatibility aliases retained until individual screens migrate to semantic Material roles.
val JadePrimaryLight = LightActionPrimary
val JadeBrightLight = Color(0xFF6070DB)
val JadeContainerLight = LightActionPrimaryContainer
val JadeOnContainerLight = LightOnActionPrimaryContainer
val IndigoSecondaryLight = LightActionSecondary
val IndigoContainerLight = LightActionSecondaryContainer
val IndigoOnContainerLight = LightOnActionSecondaryContainer
val CyanPrimaryLight = LightActionTertiary
val CyanContainerLight = LightActionTertiaryContainer
val CyanOnContainerLight = LightOnActionTertiaryContainer

// Dark — independent low-glare sibling system, not an inverted light palette.
val IvaiBackground = Color(0xFF0D1420)
val IvaiSurface = Color(0xFF151E2D)
val IvaiElevated = Color(0xFF1B2738)
val IvaiSurfaceVariant = Color(0xFF242C62)
val IvaiBorder = Color(0xFF2B3A50)
val IvaiBorderSubtle = Color(0xFF223047)

val TextPrimary = Color(0xFFF3F6FB)
val TextSecondary = Color(0xFFB8C3D5)
val TextMuted = Color(0xFF8E9BB0)

val DarkActionPrimary = Color(0xFF7D8CFF)
val DarkOnActionPrimary = Color(0xFF111936)
val DarkActionPrimaryContainer = Color(0xFF242C62)
val DarkOnActionPrimaryContainer = Color(0xFFDCE1FF)
val DarkActionSecondary = Color(0xFFBCA6FF)
val DarkActionSecondaryContainer = Color(0xFF352A61)
val DarkOnActionSecondaryContainer = Color(0xFFF0EAFF)
val DarkActionTertiary = Color(0xFF78D9E5)
val DarkActionTertiaryContainer = Color(0xFF123E4D)
val DarkOnActionTertiaryContainer = Color(0xFFC8F5FA)

val IvaiError = Color(0xFFFFB4AB)
val IvaiWarning = Color(0xFFF0BE62)
val IvaiSuccess = Color(0xFF55C8A6)

// Compatibility aliases retained for existing visual callers.
val JadePrimary = DarkActionPrimary
val JadeBright = Color(0xFFAEB8FF)
val JadeDark = DarkActionPrimaryContainer
val JadeMuted = Color(0xFF3B4A80)
val NeonPurple = DarkActionSecondary
val NeonViolet = DarkActionPrimary
val PurpleDark = DarkActionSecondaryContainer
val CyanPrimary = DarkActionTertiary
val CyanDark = DarkActionTertiaryContainer

/** Decorative-only compatibility gradients. Do not use for state, text or launcher identity. */
val IvaiPrimaryGradient = Brush.linearGradient(listOf(DarkActionPrimary, DarkActionTertiary))
val IvaiSecondaryGradient = Brush.linearGradient(listOf(DarkActionPrimary, DarkActionSecondary))
val IvaiHeaderGlowGradient = Brush.linearGradient(
    listOf(DarkActionPrimary.copy(alpha = 0.14f), DarkActionSecondary.copy(alpha = 0.10f))
)
val IvaiUserBubbleGradient = Brush.linearGradient(listOf(DarkActionPrimary, DarkActionTertiary))
val IvaiAssistantBubbleGradient = Brush.linearGradient(listOf(IvaiSurface, IvaiElevated))
val IvaiAccentGradient = Brush.linearGradient(listOf(DarkActionPrimary, DarkActionSecondary))
