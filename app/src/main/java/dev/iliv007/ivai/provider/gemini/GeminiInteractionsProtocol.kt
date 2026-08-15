package dev.iliv007.ivai.provider.gemini

import dev.iliv007.ivai.provider.NormalizedProviderError
import dev.iliv007.ivai.provider.ProviderChatRequest
import dev.iliv007.ivai.provider.ProviderErrorKind
import dev.iliv007.ivai.provider.ProviderMessageRole
import dev.iliv007.ivai.provider.ProviderUsage
import org.json.JSONArray
import org.json.JSONObject

internal object GeminiInteractionsProtocol {
    const val INTERACTIONS_URL =
        "https://generativelanguage.googleapis.com/v1beta/interactions?alt=sse"

    fun encodeStreamingRequest(request: ProviderChatRequest): String {
        val systemInstruction = request.messages
            .filter { it.role == ProviderMessageRole.SYSTEM }
            .joinToString(separator = "\n\n") { it.content }
        val input = JSONArray()
        request.messages
            .filterNot { it.role == ProviderMessageRole.SYSTEM }
            .forEach { message ->
                input.put(
                    JSONObject()
                        .put(
                            "type",
                            if (message.role == ProviderMessageRole.ASSISTANT) {
                                "model_output"
                            } else {
                                "user_input"
                            }
                        )
                        .put(
                            "content",
                            JSONArray().put(
                                JSONObject()
                                    .put("type", "text")
                                    .put("text", message.content)
                            )
                        )
                )
            }
        require(input.length() > 0) { "Gemini requires a non-system input message" }

        return JSONObject()
            .put("model", request.modelId)
            .put("input", input)
            .put("store", false)
            .put("stream", true)
            .put("generation_config", JSONObject().put("thinking_summaries", "none"))
            .apply {
                if (systemInstruction.isNotBlank()) {
                    put("system_instruction", systemInstruction)
                }
            }
            .toString()
    }

    fun decodeStreamEvent(data: String, fallbackMessageId: String): GeminiDecodedEvent? {
        if (data.trim() == "[DONE]") return GeminiDecodedEvent.Done
        val root = runCatching { JSONObject(data) }.getOrNull() ?: return GeminiDecodedEvent.Malformed
        return when (root.optString("event_type")) {
            "step.delta" -> {
                val delta = root.optJSONObject("delta") ?: return GeminiDecodedEvent.Malformed
                if (delta.optString("type") == "text") {
                    delta.optString("text").takeIf(String::isNotEmpty)
                        ?.let(GeminiDecodedEvent::TextDelta)
                } else {
                    null
                }
            }
            "interaction.completed" -> {
                val interaction = root.optJSONObject("interaction") ?: return GeminiDecodedEvent.Malformed
                val usage = interaction.optJSONObject("usage")?.let(::decodeUsage)
                GeminiDecodedEvent.Completed(
                    messageId = interaction.optString("id").ifBlank { fallbackMessageId },
                    usage = usage
                )
            }
            "error" -> GeminiDecodedEvent.Failed(
                decodeProviderError(
                    code = root.optJSONObject("error")?.optString("code").orEmpty(),
                    httpStatus = root.optJSONObject("error")?.optIntOrNull("http_status")
                )
            )
            else -> null
        }
    }

    fun decodeHttpError(httpStatus: Int): NormalizedProviderError = when (httpStatus) {
        401, 403 -> NormalizedProviderError(
            kind = ProviderErrorKind.AUTHENTICATION,
            safeMessage = "Gemini rejected the configured credential.",
            httpStatus = httpStatus
        )
        408, 504 -> NormalizedProviderError(
            kind = ProviderErrorKind.TIMEOUT,
            safeMessage = "The Gemini request timed out.",
            httpStatus = httpStatus,
            retryable = true
        )
        429 -> NormalizedProviderError(
            kind = ProviderErrorKind.RATE_LIMIT,
            safeMessage = "Gemini rate limit was reached.",
            httpStatus = httpStatus,
            retryable = true
        )
        in 400..499 -> NormalizedProviderError(
            kind = ProviderErrorKind.INVALID_REQUEST,
            safeMessage = "Gemini rejected this request.",
            httpStatus = httpStatus
        )
        in 500..599 -> NormalizedProviderError(
            kind = ProviderErrorKind.UNKNOWN,
            safeMessage = "Gemini is temporarily unavailable.",
            httpStatus = httpStatus,
            retryable = true
        )
        else -> NormalizedProviderError(
            kind = ProviderErrorKind.UNKNOWN,
            safeMessage = "Gemini returned an unexpected response.",
            httpStatus = httpStatus.takeIf { it in 100..599 }
        )
    }

    private fun decodeUsage(usage: JSONObject): ProviderUsage = ProviderUsage(
        inputTokens = usage.optIntOrNull("total_input_tokens"),
        outputTokens = usage.optIntOrNull("total_output_tokens")
    )

    private fun decodeProviderError(code: String, httpStatus: Int?): NormalizedProviderError = when {
        code.contains("auth", ignoreCase = true) || code.contains("permission", ignoreCase = true) ->
            NormalizedProviderError(ProviderErrorKind.AUTHENTICATION, "Gemini rejected the configured credential.", httpStatus)
        code.contains("rate", ignoreCase = true) || code.contains("resource_exhausted", ignoreCase = true) ->
            NormalizedProviderError(ProviderErrorKind.RATE_LIMIT, "Gemini rate limit was reached.", httpStatus, retryable = true)
        code.contains("timeout", ignoreCase = true) || code.contains("deadline", ignoreCase = true) ->
            NormalizedProviderError(ProviderErrorKind.TIMEOUT, "The Gemini request timed out.", httpStatus, retryable = true)
        else -> NormalizedProviderError(ProviderErrorKind.UNKNOWN, "Gemini reported a provider error.", httpStatus, retryable = true)
    }

    private fun JSONObject.optIntOrNull(name: String): Int? =
        if (has(name) && !isNull(name)) optInt(name).takeIf { it >= 0 } else null
}

internal sealed interface GeminiDecodedEvent {
    data class TextDelta(val text: String) : GeminiDecodedEvent
    data class Completed(val messageId: String, val usage: ProviderUsage?) : GeminiDecodedEvent
    data class Failed(val error: NormalizedProviderError) : GeminiDecodedEvent
    data object Done : GeminiDecodedEvent
    data object Malformed : GeminiDecodedEvent
}
