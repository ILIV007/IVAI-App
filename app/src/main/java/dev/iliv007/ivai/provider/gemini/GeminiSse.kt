package dev.iliv007.ivai.provider.gemini

import java.io.BufferedReader

internal data class GeminiSseEvent(
    val name: String?,
    val data: String
)

/** Minimal SSE framing parser. JSON interpretation remains in the Gemini protocol mapper. */
internal class GeminiSseReader(
    private val reader: BufferedReader
) {
    private var eventName: String? = null
    private val data = StringBuilder()

    /** Returns one complete SSE event, or null after the stream has ended. */
    fun next(): GeminiSseEvent? {
        while (true) {
            val line = reader.readLine()
            if (line == null) {
                return dispatchOrNull()
            }
            when {
                line.isEmpty() -> {
                    val event = dispatchOrNull()
                    if (event != null) return event
                }
                line.startsWith("event:") -> eventName = line.substringAfter(':').trim()
                line.startsWith("data:") -> {
                    if (data.isNotEmpty()) data.append('\n')
                    data.append(line.substringAfter(':').removePrefix(" "))
                }
                line.startsWith(':') -> Unit // SSE comment / keepalive
            }
        }
    }

    private fun dispatchOrNull(): GeminiSseEvent? {
        if (eventName == null && data.isEmpty()) return null
        return GeminiSseEvent(eventName, data.toString()).also {
            eventName = null
            data.clear()
        }
    }
}
