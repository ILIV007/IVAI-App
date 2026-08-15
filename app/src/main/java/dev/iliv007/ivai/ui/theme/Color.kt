package dev.iliv007.ivai.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// IVAI Light Palette (Clean, High-Contrast & Premium)
// ==========================================
val LightBackground = Color(0xFFF6F8FB)        // Refined soft slate-gray canvas
val LightSurface = Color(0xFFFFFFFF)           // Pure crisp white card surface
val LightSurfaceElevated = Color(0xFFFFFFFF)   // Elevated cards
val LightSurfaceVariant = Color(0xFFEDF2F7)    // Soft interactive chip/pill surface
val LightBorder = Color(0xFFD3DCE6)            // Subtle crisp border
val LightBorderSubtle = Color(0xFFE5ECF3)      // Ultra soft divider border

val LightTextPrimary = Color(0xFF0D1829)       // Deep slate navy for headings & high readability
val LightTextSecondary = Color(0xFF4A5568)     // Readable body text
val LightTextMuted = Color(0xFF718096)         // Subtle caption text

val JadePrimaryLight = Color(0xFF059669)       // Rich Emerald Green
val JadeBrightLight = Color(0xFF10B981)        // Vibrant Emerald
val JadeContainerLight = Color(0xFFECFDF5)     // Soft mint container
val JadeOnContainerLight = Color(0xFF064E3B)   // Deep emerald text

val IndigoSecondaryLight = Color(0xFF4F46E5)   // High-energy Indigo
val IndigoContainerLight = Color(0xFFEEF2FF)   // Soft Indigo container
val IndigoOnContainerLight = Color(0xFF312E81)  // Deep Indigo text

val CyanPrimaryLight = Color(0xFF0284C7)       // Deep sky cyan
val CyanContainerLight = Color(0xFFE0F2FE)     // Soft sky container
val CyanOnContainerLight = Color(0xFF0369A1)   // Deep sky cyan text

val IvaiErrorLight = Color(0xFFE11D48)         // Vibrant Rose/Crimson
val IvaiWarningLight = Color(0xFFD97706)       // Deep Amber Gold
val IvaiSuccessLight = Color(0xFF059669)       // Mint emerald

// ==========================================
// IVAI Dark Palette (Cyber Obsidian & Neon Aurora)
// ==========================================
val IvaiBackground = Color(0xFF080D14)         // Deep Obsidian Black
val IvaiSurface = Color(0xFF0F1724)            // Rich Dark Slate
val IvaiElevated = Color(0xFF162132)           // Elevated Dark Container
val IvaiSurfaceVariant = Color(0xFF1D2A40)     // Interactive Chip Surface
val IvaiBorder = Color(0xFF24334C)             // Slate Border
val IvaiBorderSubtle = Color(0x33475569)       // Frosted translucent border

val TextPrimary = Color(0xFFF8FAFC)            // Crisp Clean White
val TextSecondary = Color(0xFF94A3B8)          // Slate Gray
val TextMuted = Color(0xFF64748B)              // Muted Steel

val JadePrimary = Color(0xFF00E599)            // Vibrant Cyber Emerald
val JadeBright = Color(0xFF38F9B6)             // Luminous Mint
val JadeDark = Color(0xFF0A2B1F)               // Deep Emerald Container
val JadeMuted = Color(0xFF0E3D2C)              // Muted Emerald Border

val NeonPurple = Color(0xFFA855F7)             // Aurora Violet
val NeonViolet = Color(0xFF818CF8)             // Electric Indigo
val PurpleDark = Color(0xFF221642)             // Deep Indigo Container

val CyanPrimary = Color(0xFF00D2FF)            // Electric Cyan
val CyanDark = Color(0xFF09293B)               // Deep Cyan Container

val IvaiError = Color(0xFFFF4D6D)              // Crisp Crimson
val IvaiWarning = Color(0xFFFFB020)            // Amber Gold
val IvaiSuccess = Color(0xFF00E599)            // Cyber Emerald

// ==========================================
// Modern Dynamic Gradients
// ==========================================
val IvaiPrimaryGradient = Brush.linearGradient(
    listOf(Color(0xFF00E599), Color(0xFF00D2FF))
)
val IvaiSecondaryGradient = Brush.linearGradient(
    listOf(Color(0xFF818CF8), Color(0xFFA855F7))
)
val IvaiHeaderGlowGradient = Brush.linearGradient(
    listOf(Color(0xFF00E599).copy(alpha = 0.15f), Color(0xFF818CF8).copy(alpha = 0.15f))
)
val IvaiUserBubbleGradient = Brush.linearGradient(
    listOf(Color(0xFF00875A), Color(0xFF0284C7))
)
val IvaiAssistantBubbleGradient = Brush.linearGradient(
    listOf(Color(0xFF131D2E), Color(0xFF0F1724))
)
val IvaiBrandGradient = Brush.linearGradient(
    listOf(Color(0xFF00E599), Color(0xFF818CF8))
)


