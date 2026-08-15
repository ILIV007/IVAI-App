package dev.iliv007.ivai

import dev.iliv007.ivai.ui.model.MockDataRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RtlBidiCorpusTest {

    @Test
    fun `verify all 8 mandatory RTL and BiDi corpus messages are present`() {
        val corpus = MockDataRepository.rtlCorpusMessages
        assertEquals("Corpus must contain 8 test messages", 8, corpus.size)

        val expectedSnippets = listOf(
            "سلام، پروژه IVAI را با Gemini 3 بررسی کن.",
            "مرحبا، افتح الملف README.md ثم اكتب summary.",
            "نسخه 2.1 در مسیر /docs/RTL_BIDI.md قرار دارد.",
            "قیمت ۱۲۳٬۴۵۶ تومان و latency برابر 250ms بود.",
            "مدل openai/gpt-4.1-mini خطای HTTP 429 داد.",
            "لینک https://example.com/a?x=1 را باز نکن.",
            "کد: val title = \"سلام IVAI\"",
            "(نسخه Alpha) برای Android 10+ آماده می‌شود."
        )

        expectedSnippets.forEachIndexed { index, snippet ->
            assertEquals(snippet, corpus[index].text)
            assertTrue("Message ID should not be empty", corpus[index].id.isNotEmpty())
        }
    }
}
