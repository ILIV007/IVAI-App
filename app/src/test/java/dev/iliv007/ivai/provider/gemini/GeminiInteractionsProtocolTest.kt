package dev.iliv007.ivai.provider.gemini

import dev.iliv007.ivai.provider.CredentialReference
import dev.iliv007.ivai.provider.ProviderChatRequest
import dev.iliv007.ivai.provider.ProviderErrorKind
import dev.iliv007.ivai.provider.ProviderMessage
import dev.iliv007.ivai.provider.ProviderMessageRole
import java.io.BufferedReader
import java.io.StringReader
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GeminiInteractionsProtocolTest {

    @Test
    fun `request keeps chat history local and encodes stateless streaming steps`() {
        val body = JSONObject(
            GeminiInteractionsProtocol.encodeStreamingRequest(
                ProviderChatRequest(
                    credentialReference = CredentialReference("gemini"),
                    modelId = "manual-model-id",
                    messages = listOf(
                        ProviderMessage(ProviderMessageRole.SYSTEM, "Keep it concise."),
                        ProviderMessage(ProviderMessageRole.USER, "سلام"),
                        ProviderMessage(ProviderMessageRole.ASSISTANT, "سلام، چطور کمک کنم؟"),
                        ProviderMessage(ProviderMessageRole.USER, "Explain SSE.")
                    ),
                    attemptId = "attempt-1"
                )
            )
        )

        assertEquals("manual-model-id", body.getString("model"))
        assertFalse(body.getBoolean("store"))
        assertTrue(body.getBoolean("stream"))
        assertEquals("Keep it concise.", body.getString("system_instruction"))
        val input = body.getJSONArray("input")
        assertEquals(3, input.length())
        assertEquals("user_input", input.getJSONObject(0).getString("type"))
        assertEquals("model_output", input.getJSONObject(1).getString("type"))
        assertEquals("Explain SSE.", textAt(input, 2))
    }

    @Test
    fun `decoder emits text usage and terminal completion without provider reasoning`() {
        val deltaPayload = """{"event_type":"step.delta","delta":{"type":"text","text":"بخش پاسخ"}}"""
        val rawDelta = JSONObject(deltaPayload)
        assertEquals("step.delta", rawDelta.optString("event_type"))
        assertEquals("text", rawDelta.getJSONObject("delta").optString("type"))
        assertEquals("بخش پاسخ", rawDelta.getJSONObject("delta").optString("text"))
        val delta = GeminiInteractionsProtocol.decodeStreamEvent(
            deltaPayload,
            fallbackMessageId = "attempt-1"
        )
        val completion = GeminiInteractionsProtocol.decodeStreamEvent(
            """{"event_type":"interaction.completed","interaction":{"id":"provider-message-id","usage":{"total_input_tokens":5,"total_output_tokens":8}}}""",
            fallbackMessageId = "attempt-1"
        )
        val thought = GeminiInteractionsProtocol.decodeStreamEvent(
            """{"event_type":"step.delta","delta":{"type":"thought_summary","content":{"text":"do not surface"}}}""",
            fallbackMessageId = "attempt-1"
        )

        assertTrue(delta is GeminiDecodedEvent.TextDelta)
        assertEquals("بخش پاسخ", (delta as GeminiDecodedEvent.TextDelta).text)
        assertTrue(completion is GeminiDecodedEvent.Completed)
        val decodedCompletion = completion as GeminiDecodedEvent.Completed
        assertEquals("provider-message-id", decodedCompletion.messageId)
        assertEquals(5, decodedCompletion.usage?.inputTokens)
        assertEquals(8, decodedCompletion.usage?.outputTokens)
        assertNull(thought)
    }

    @Test
    fun `sse reader groups multi-line data and ignores comments`() {
        val reader = GeminiSseReader(
            BufferedReader(
                StringReader(
                    ": keepalive\n" +
                        "event: step.delta\n" +
                        "data: {\"first\": true}\n" +
                        "data: {\"second\": true}\n\n" +
                        "data: [DONE]\n\n"
                )
            )
        )

        val first = reader.next()
        val second = reader.next()

        assertEquals("step.delta", first?.name)
        assertEquals("{\"first\": true}\n{\"second\": true}", first?.data)
        assertEquals("[DONE]", second?.data)
        assertNull(reader.next())
    }

    @Test
    fun `sse reader removes only the optional single space after data colon`() {
        val reader = GeminiSseReader(
            BufferedReader(StringReader("data:  leading-space\n\ndata:\tleading-tab\n\n"))
        )

        assertEquals(" leading-space", reader.next()?.data)
        assertEquals("\tleading-tab", reader.next()?.data)
    }

    @Test
    fun `http status maps to safe normalized categories`() {
        assertEquals(ProviderErrorKind.AUTHENTICATION, GeminiInteractionsProtocol.decodeHttpError(401).kind)
        assertEquals(ProviderErrorKind.RATE_LIMIT, GeminiInteractionsProtocol.decodeHttpError(429).kind)
        assertTrue(GeminiInteractionsProtocol.decodeHttpError(503).retryable)
        assertFalse(GeminiInteractionsProtocol.decodeHttpError(400).retryable)
    }

    private fun textAt(input: JSONArray, index: Int): String =
        input.getJSONObject(index).getJSONArray("content").getJSONObject(0).getString("text")
}
