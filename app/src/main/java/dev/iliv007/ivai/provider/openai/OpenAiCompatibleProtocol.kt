package dev.iliv007.ivai.provider.openai

import dev.iliv007.ivai.provider.NormalizedProviderError
import dev.iliv007.ivai.provider.ProviderChatRequest
import dev.iliv007.ivai.provider.ProviderErrorKind
import dev.iliv007.ivai.provider.ProviderMessageRole
import dev.iliv007.ivai.provider.ProviderUsage
import org.json.JSONArray
import org.json.JSONObject

internal sealed interface OpenAiDecodedEvent {
    data class TextDelta(val text: String) : OpenAiDecodedEvent
    data class Completed(val messageId: String, val usage: ProviderUsage?) : OpenAiDecodedEvent
    data class Failed(val error: NormalizedProviderError) : OpenAiDecodedEvent
    data object Done : OpenAiDecodedEvent
    data object Malformed : OpenAiDecodedEvent
}

/** OpenAI Chat Completions wire format shared by OpenRouter and compatible endpoints. */
internal object OpenAiCompatibleProtocol {
    fun encodeStreamingRequest(request: ProviderChatRequest): String = JSONObject().apply {
        put("model", request.modelId)
        put("stream", true)
        put("messages", JSONArray().apply {
            request.messages.forEach { message ->
                put(JSONObject().apply {
                    put("role", message.role.name.lowercase())
                    put("content", message.content)
                })
            }
        })
    }.toString()

    fun decodeStreamEvent(data: String, fallbackMessageId: String): OpenAiDecodedEvent {
        if (data == "[DONE]") return OpenAiDecodedEvent.Done
        val payload = runCatching { JSONObject(data) }.getOrElse { return OpenAiDecodedEvent.Malformed }
        payload.optJSONObject("error")?.let { error ->
            return OpenAiDecodedEvent.Failed(
                NormalizedProviderError(
                    kind = errorKind(payload.optInt("status", 0), error.optString("code")),
                    safeMessage = safeErrorMessage(error.optString("message"))
                )
            )
        }
        val choice = payload.optJSONArray("choices")?.optJSONObject(0) ?: return OpenAiDecodedEvent.Malformed
        choice.optJSONObject("delta")?.optString("content")?.takeIf(String::isNotEmpty)?.let {
            return OpenAiDecodedEvent.TextDelta(it)
        }
        if (!choice.isNull("finish_reason")) {
            return OpenAiDecodedEvent.Completed(
                messageId = payload.optString("id").ifBlank { fallbackMessageId },
                usage = payload.optJSONObject("usage")?.toUsage()
            )
        }
        payload.optJSONObject("usage")?.let { usage ->
            return OpenAiDecodedEvent.Completed(
                messageId = payload.optString("id").ifBlank { fallbackMessageId },
                usage = usage.toUsage()
            )
        }
        return OpenAiDecodedEvent.Malformed
    }

    fun decodeHttpError(status: Int): NormalizedProviderError = NormalizedProviderError(
        kind = when (status) {
            401, 403 -> ProviderErrorKind.AUTHENTICATION
            408, 524 -> ProviderErrorKind.TIMEOUT
            429 -> ProviderErrorKind.RATE_LIMIT
            400, 404, 413, 422 -> ProviderErrorKind.INVALID_REQUEST
            else -> ProviderErrorKind.UNKNOWN
        },
        safeMessage = when (status) {
            401, 403 -> "Provider authentication was rejected."
            408, 524 -> "Provider request timed out."
            429 -> "Provider rate limit was reached."
            else -> "Provider request was not accepted."
        },
        httpStatus = status,
        retryable = status == 408 || status == 429 || status == 524 || status in 500..599
    )

    private fun JSONObject.toUsage(): ProviderUsage = ProviderUsage(
        inputTokens = optInt("prompt_tokens", -1).takeIf { it >= 0 },
        outputTokens = optInt("completion_tokens", -1).takeIf { it >= 0 }
    )

    private fun errorKind(status: Int, code: String): ProviderErrorKind = when {
        status == 401 || status == 403 -> ProviderErrorKind.AUTHENTICATION
        status == 429 || code.contains("rate", ignoreCase = true) -> ProviderErrorKind.RATE_LIMIT
        code.contains("timeout", ignoreCase = true) -> ProviderErrorKind.TIMEOUT
        else -> ProviderErrorKind.UNKNOWN
    }

    private fun safeErrorMessage(value: String): String =
        value.takeIf { it.isNotBlank() }?.take(160) ?: "Provider stream failed."
}
