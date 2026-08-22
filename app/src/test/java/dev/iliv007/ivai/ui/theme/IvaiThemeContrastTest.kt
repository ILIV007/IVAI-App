package dev.iliv007.ivai.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDirection
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class IvaiThemeContrastTest {

    @Test
    fun light_semantic_foregrounds_meet_normal_text_aa() {
        assertNormalTextAa("onPrimary on primary", LightOnActionPrimary, LightActionPrimary)
        assertNormalTextAa("onPrimaryContainer on primaryContainer", LightOnActionPrimaryContainer, LightActionPrimaryContainer)
        assertNormalTextAa("onSecondary on secondary", LightOnActionPrimary, LightActionSecondary)
        assertNormalTextAa("onSecondaryContainer on secondaryContainer", LightOnActionSecondaryContainer, LightActionSecondaryContainer)
        assertNormalTextAa("onTertiary on tertiary", LightOnActionPrimary, LightActionTertiary)
        assertNormalTextAa("onTertiaryContainer on tertiaryContainer", LightOnActionTertiaryContainer, LightActionTertiaryContainer)
        assertNormalTextAa("primary text on canvas", LightTextPrimary, LightBackground)
        assertNormalTextAa("secondary text on surface", LightTextSecondary, LightSurface)
        assertNormalTextAa("muted text on surface", LightTextMuted, LightSurface)
        assertNormalTextAa("error text on canvas", IvaiErrorLight, LightBackground)
        assertNormalTextAa("warning text on canvas", IvaiWarningLight, LightBackground)
        assertNormalTextAa("success text on canvas", IvaiSuccessLight, LightBackground)
    }

    @Test
    fun dark_semantic_foregrounds_meet_normal_text_aa() {
        assertNormalTextAa("onPrimary on primary", DarkOnActionPrimary, DarkActionPrimary)
        assertNormalTextAa("onPrimaryContainer on primaryContainer", DarkOnActionPrimaryContainer, DarkActionPrimaryContainer)
        assertNormalTextAa("onSecondary on secondary", DarkOnActionPrimary, DarkActionSecondary)
        assertNormalTextAa("onSecondaryContainer on secondaryContainer", DarkOnActionSecondaryContainer, DarkActionSecondaryContainer)
        assertNormalTextAa("onTertiary on tertiary", DarkOnActionPrimary, DarkActionTertiary)
        assertNormalTextAa("onTertiaryContainer on tertiaryContainer", DarkOnActionTertiaryContainer, DarkActionTertiaryContainer)
        assertNormalTextAa("primary text on canvas", TextPrimary, IvaiBackground)
        assertNormalTextAa("secondary text on surface", TextSecondary, IvaiSurface)
        assertNormalTextAa("muted text on surface", TextMuted, IvaiSurface)
        assertNormalTextAa("error text on canvas", IvaiError, IvaiBackground)
        assertNormalTextAa("warning text on canvas", IvaiWarning, IvaiBackground)
        assertNormalTextAa("success text on canvas", IvaiSuccess, IvaiBackground)
    }

    @Test
    fun ordinary_body_typography_is_not_forced_ltr() {
        assertNotEquals(TextDirection.Ltr, IvaiTypography.bodyLarge.textDirection)
        assertNotEquals(TextDirection.Ltr, IvaiTypography.bodyMedium.textDirection)
        assertNotEquals(TextDirection.Ltr, IvaiTypography.bodySmall.textDirection)
        assertTrue(IvaiTypography.labelSmall.textDirection == TextDirection.Ltr)
    }

    private fun assertNormalTextAa(label: String, foreground: Color, background: Color) {
        val contrast = contrastRatio(foreground, background)
        assertTrue("$label must be at least 4.5:1 but was %.2f:1".format(contrast), contrast >= 4.5f)
    }

    private fun contrastRatio(first: Color, second: Color): Float {
        val firstLuminance = first.relativeLuminance()
        val secondLuminance = second.relativeLuminance()
        val lighter = maxOf(firstLuminance, secondLuminance)
        val darker = minOf(firstLuminance, secondLuminance)
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    private fun Color.relativeLuminance(): Float =
        0.2126f * red.toLinearLight() + 0.7152f * green.toLinearLight() + 0.0722f * blue.toLinearLight()

    private fun Float.toLinearLight(): Float =
        if (this <= 0.04045f) this / 12.92f else ((this + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
}
