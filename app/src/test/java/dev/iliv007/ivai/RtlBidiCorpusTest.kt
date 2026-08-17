package dev.iliv007.ivai

import dev.iliv007.ivai.ui.model.ChatMessage
import dev.iliv007.ivai.ui.model.MessageContentType
import dev.iliv007.ivai.ui.model.MessageSender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RtlBidiCorpusTest {

    private val corpus = listOf(
        ChatMessage(
            id = "rtl-1",
            sender = MessageSender.USER,
            text = "سلام، پروژه IVAI را با Gemini 3 بررسی کن.",
            timestamp = "10:14 AM"
        ),
        ChatMessage(
            id = "rtl-2",
            sender = MessageSender.ASSISTANT,
            text = "مرحبا، افتح الملف README.md ثم اكتب summary.",
            timestamp = "10:15 AM",
            modelBadge = "gemini-2.5-flash",
            latencyMs = 280
        ),
        ChatMessage(
            id = "rtl-3",
            sender = MessageSender.USER,
            text = "نسخه 2.1 در مسیر /docs/RTL_BIDI.md قرار دارد.",
            timestamp = "10:16 AM"
        ),
        ChatMessage(
            id = "rtl-4",
            sender = MessageSender.ASSISTANT,
            text = "قیمت ۱۲۳٬۴۵۶ تومان و latency برابر 250ms بود.",
            timestamp = "10:17 AM",
            modelBadge = "openrouter/auto",
            latencyMs = 250
        ),
        ChatMessage(
            id = "rtl-5",
            sender = MessageSender.USER,
            text = "مدل openai/gpt-4.1-mini خطای HTTP 429 داد.",
            timestamp = "10:18 AM"
        ),
        ChatMessage(
            id = "rtl-6",
            sender = MessageSender.ASSISTANT,
            text = "لینک https://example.com/a?x=1 را باز نکن.",
            timestamp = "10:19 AM",
            modelBadge = "custom-openai",
            latencyMs = 310
        ),
        ChatMessage(
            id = "rtl-7",
            sender = MessageSender.USER,
            text = "کد: val title = \"سلام IVAI\"",
            timestamp = "10:20 AM",
            type = MessageContentType.CODE,
            codeSnippet = """
                val title = "سلام IVAI"
                println("Active locale: LTR shell with BiDi prose")
            """.trimIndent()
        ),
        ChatMessage(
            id = "rtl-8",
            sender = MessageSender.ASSISTANT,
            text = "(نسخه Alpha) برای Android 10+ آماده می‌شود.",
            timestamp = "10:21 AM",
            modelBadge = "gemini-2.5-pro",
            latencyMs = 420
        )
    )

    @Test
    fun `verify all 8 mandatory RTL and BiDi corpus messages are present`() {
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
