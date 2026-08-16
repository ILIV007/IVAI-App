package dev.iliv007.ivai.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class IvaiThemeContrastTest {

    @Test
    fun dark_semantic_foregrounds_meet_normal_text_aa() {
        assertNormalTextAa(
            label = "onSecondaryContainer on secondaryContainer",
            foreground = TextPrimary,
            background = PurpleDark
        )
        assertNormalTextAa(
            label = "onError on error",
            foreground = IvaiBackground,
            background = IvaiError
        )
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
