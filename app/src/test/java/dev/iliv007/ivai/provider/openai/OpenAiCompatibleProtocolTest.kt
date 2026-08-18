package dev.iliv007.ivai.provider.openai

import dev.iliv007.ivai.provider.CredentialReference
import dev.iliv007.ivai.provider.ProviderChatRequest
import dev.iliv007.ivai.provider.ProviderErrorKind
import dev.iliv007.ivai.provider.ProviderMessage
import dev.iliv007.ivai.provider.ProviderMessageRole
import org.json.JSONObject
import org.junit.Assert.assertEquals
import java.io.BufferedReader
import java.io.StringReader
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OpenAiCompatibleProtocolTest {

    @Test
    fun `stream request uses chat completions shape without credentials`() {
        val encoded = OpenAiCompatibleProtocol.encodeStreamingRequest(
            ProviderChatRequest(
                credentialReference = CredentialReference("openrouter.main"),
                modelId = "openai/gpt-example",
                messages = listOf(ProviderMessage(ProviderMessageRole.USER, "سلام")),
                attemptId = "attempt-1"
            )
        )
        val body = JSONObject(encoded)
        assertEquals("openai/gpt-example", body.getString("model"))
        assertTrue(body.getBoolean("stream"))
        assertEquals("user", body.getJSONArray("messages").getJSONObject(0).getString("role"))
        assertTrue(!encoded.contains("openrouter.main"))
    }

    @Test
    fun `decoder normalizes delta completion usage and midstream error`() {
        val delta = OpenAiCompatibleProtocol.decodeStreamEvent(
            "{\"id\":\"x\",\"choices\":[{\"delta\":{\"content\":\"درود\"},\"finish_reason\":null}]}",
            "fallback"
        )
        assertEquals("درود", (delta as OpenAiDecodedEvent.TextDelta).text)

        val complete = OpenAiCompatibleProtocol.decodeStreamEvent(
            "{\"id\":\"x\",\"usage\":{\"prompt_tokens\":3,\"completion_tokens\":4},\"choices\":[{\"delta\":{},\"finish_reason\":\"stop\"}]}",
            "fallback"
        ) as OpenAiDecodedEvent.Completed
        assertEquals("x", complete.messageId)
        assertEquals(3, complete.usage?.inputTokens)
        assertEquals(4, complete.usage?.outputTokens)

        val failed = OpenAiCompatibleProtocol.decodeStreamEvent(
            "{\"error\":{\"code\":\"rate_limit\",\"message\":\"try later\"},\"choices\":[{\"finish_reason\":\"error\"}]}",
            "fallback"
        ) as OpenAiDecodedEvent.Failed
        assertEquals(ProviderErrorKind.RATE_LIMIT, failed.error.kind)
    }

    @Test
    fun `SSE reader ignores comments and keeps complete data events`() {
        val reader = OpenAiCompatibleSseReader(
            BufferedReader(StringReader(": OPENROUTER PROCESSING\n\ndata: {\\\"choices\\\":[{\\\"delta\\\":{\\\"content\\\":\\\"hi\\\"}}]}\n\n"))
        )
        assertEquals("{\\\"choices\\\":[{\\\"delta\\\":{\\\"content\\\":\\\"hi\\\"}}]}", reader.next()?.data)
    }

    @Test
    fun `SSE reader removes only the optional single space after data colon`() {
        val reader = OpenAiCompatibleSseReader(
            BufferedReader(StringReader("data:  leading-space\n\ndata:\tleading-tab\n\n"))
        )

        assertEquals(" leading-space", reader.next()?.data)
        assertEquals("\tleading-tab", reader.next()?.data)
    }

    @Test
    fun `http status normalizes auth rate and timeout`() {
        assertEquals(ProviderErrorKind.AUTHENTICATION, OpenAiCompatibleProtocol.decodeHttpError(401).kind)
        assertEquals(ProviderErrorKind.RATE_LIMIT, OpenAiCompatibleProtocol.decodeHttpError(429).kind)
        assertEquals(ProviderErrorKind.TIMEOUT, OpenAiCompatibleProtocol.decodeHttpError(524).kind)
    }
}
