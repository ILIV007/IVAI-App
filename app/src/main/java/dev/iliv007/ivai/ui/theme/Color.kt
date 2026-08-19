package dev.iliv007.ivai.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// IVAI Light Palette (Clean, High-Contrast & Premium)
// ==========================================
val LightBackground = Color(0xFFF7F5FF)        // Soft violet-white canvas derived from IVAI brand glow
val LightSurface = Color(0xFFFFFFFF)           // Crisp white card surface
val LightSurfaceElevated = Color(0xFFFFFFFF)   // Elevated cards
val LightSurfaceVariant = Color(0xFFEEEAFE)    // Violet-tinted interactive chip/pill surface
val LightBorder = Color(0xFFD8D2EF)            // Soft indigo border
val LightBorderSubtle = Color(0xFFE9E5F7)      // Ultra-soft violet divider

val LightTextPrimary = Color(0xFF0D1829)       // Deep slate navy for headings & high readability
val LightTextSecondary = Color(0xFF4A5568)     // Readable body text
val LightTextMuted = Color(0xFF667085)       // AA-readable subtle caption text

val JadePrimaryLight = Color(0xFF08795D)       // Brand emerald with AA contrast against white
val JadeBrightLight = Color(0xFF14C79A)        // Vibrant emerald/aqua
val JadeContainerLight = Color(0xFFDFFBF2)     // Soft aurora mint container
val JadeOnContainerLight = Color(0xFF064B3A)   // Deep emerald text

val IndigoSecondaryLight = Color(0xFF6554C7)   // Brand violet with readable contrast
val IndigoContainerLight = Color(0xFFECE8FF)   // Soft aurora violet container
val IndigoOnContainerLight = Color(0xFF31225F)  // Deep violet text

val CyanPrimaryLight = Color(0xFF087E9A)       // Deep aqua cyan
val CyanContainerLight = Color(0xFFDAF7F7)     // Soft aqua container
val CyanOnContainerLight = Color(0xFF064B5C)   // Deep aqua text

val IvaiErrorLight = Color(0xFFE11D48)         // Vibrant Rose/Crimson
val IvaiWarningLight = Color(0xFFB45309)       // AA-readable deep amber
val IvaiSuccessLight = Color(0xFF047857)       // AA-readable deep emerald

// ==========================================
// IVAI Dark Palette (Cyber Obsidian & Neon Aurora)
// ==========================================
val IvaiBackground = Color(0xFF101432)         // Deep indigo foundation for the independent IVAI UI
val IvaiSurface = Color(0xFF171C40)            // Rich indigo-slate surface
val IvaiElevated = Color(0xFF202652)           // Elevated aurora container
val IvaiSurfaceVariant = Color(0xFF2B3165)     // Interactive chip surface
val IvaiBorder = Color(0xFF464C83)             // Indigo border
val IvaiBorderSubtle = Color(0x335E65A2)       // Frosted indigo border

val TextPrimary = Color(0xFFF8F7FF)            // Crisp violet-white text
val TextSecondary = Color(0xFFC2C4E2)          // Readable lavender gray
val TextMuted = Color(0xFF8F95C3)              // Muted indigo gray

val JadePrimary = Color(0xFF4EE8B5)            // Brand aurora emerald
val JadeBright = Color(0xFF72F8CE)             // Luminous mint-aqua
val JadeDark = Color(0xFF123F42)               // Deep emerald container
val JadeMuted = Color(0xFF1D5752)              // Muted emerald border

val NeonPurple = Color(0xFFAB6AF5)             // Aurora violet
val NeonViolet = Color(0xFF9F8CFF)             // Electric indigo with AA contrast against deep indigo
val PurpleDark = Color(0xFF37215F)             // Deep violet container

val CyanPrimary = Color(0xFF56E2EF)            // Electric aqua
val CyanDark = Color(0xFF0D3C52)               // Deep aqua container

val IvaiError = Color(0xFFFF4D6D)              // Crisp Crimson
val IvaiWarning = Color(0xFFFFB020)            // Amber Gold
val IvaiSuccess = Color(0xFF00E599)            // Cyber Emerald

// ==========================================
// Decorative IVAI UI gradients — never use as a state, text, or launcher-art substitute
// ==========================================
val IvaiPrimaryGradient = Brush.linearGradient(
    listOf(Color(0xFF4EE8B5), Color(0xFF56E2EF))
)
val IvaiSecondaryGradient = Brush.linearGradient(
    listOf(Color(0xFF72F8CE), Color(0xFFAB6AF5))
)
val IvaiHeaderGlowGradient = Brush.linearGradient(
    listOf(Color(0xFF4EE8B5).copy(alpha = 0.20f), Color(0xFFAB6AF5).copy(alpha = 0.18f))
)
val IvaiUserBubbleGradient = Brush.linearGradient(
    listOf(Color(0xFF00875A), Color(0xFF0284C7))
)
val IvaiAssistantBubbleGradient = Brush.linearGradient(
    listOf(Color(0xFF131D2E), Color(0xFF0F1724))
)
val IvaiAccentGradient = Brush.linearGradient(
    listOf(Color(0xFF4EE8B5), Color(0xFFAB6AF5))
)


