package dev.iliv007.ivai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.iliv007.ivai.ui.model.ChatMessage
import dev.iliv007.ivai.ui.model.MessageSender
import dev.iliv007.ivai.ui.model.MockDataRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State representing the chat screen.
 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val activeModelName: String = "Gemini 3 Pro",
    val isGenerating: Boolean = false,
    val errorMessage: String? = null
)

/**
 * MainChatViewModel manages the state of the chat history, user input,
 * prompt suggestions, and simulated streaming responses.
 */
class MainChatViewModel(
    initialMessages: List<ChatMessage> = MockDataRepository.defaultChatThreads.firstOrNull()?.messages ?: emptyList()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ChatUiState(
            messages = initialMessages,
            activeModelName = "Gemini 3 Pro"
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun onInputTextChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText) }
    }

    fun onClearInput() {
        _uiState.update { it.copy(inputText = "") }
    }

    fun onModelSelected(modelName: String) {
        _uiState.update { it.copy(activeModelName = modelName) }
    }

    fun onPromptSuggestionSelected(prompt: String) {
        _uiState.update { it.copy(inputText = prompt) }
    }

    fun sendMessage() {
        val currentText = _uiState.value.inputText.trim()
        if (currentText.isBlank() || _uiState.value.isGenerating) return

        val userMessage = ChatMessage(
            id = "user-${System.currentTimeMillis()}",
            sender = MessageSender.USER,
            text = currentText,
            timestamp = formatCurrentTime()
        )

        _uiState.update { currentState ->
            currentState.copy(
                messages = currentState.messages + userMessage,
                inputText = "",
                isGenerating = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                // Simulate AI response generation with contextual BiDi support
                delay(1000)

                val responseText = generateAssistantResponse(currentText)
                val aiMessage = ChatMessage(
                    id = "ai-${System.currentTimeMillis()}",
                    sender = MessageSender.ASSISTANT,
                    text = responseText,
                    timestamp = formatCurrentTime(),
                    modelBadge = _uiState.value.activeModelName,
                    latencyMs = (280..450).random().toLong()
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        messages = currentState.messages + aiMessage,
                        isGenerating = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isGenerating = false,
                        errorMessage = e.localizedMessage ?: "Failed to generate response"
                    )
                }
            }
        }
    }

    fun deleteMessage(messageId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                messages = currentState.messages.filterNot { it.id == messageId }
            )
        }
    }

    fun clearChat() {
        _uiState.update { it.copy(messages = emptyList()) }
    }

    private fun generateAssistantResponse(userPrompt: String): String {
        return when {
            userPrompt.contains("معماری", ignoreCase = true) || userPrompt.contains("architecture", ignoreCase = true) -> {
                "معماری سیستم IVAI بر پایه MVVM و Jetpack Compose طراحی شده است. لایه Data و ViewModel وظیفه مدیریت StateFlow و جریان پیام‌ها را بر عهده دارند و UI به صورت واکنشی به‌روزرسانی می‌شود."
            }
            userPrompt.contains("BiDi", ignoreCase = true) || userPrompt.contains("فارسی", ignoreCase = true) -> {
                "پشتیبانی کامل از متون دوجهته (BiDi) فعال است. متون فارسی با TextDirection.ContentOrLtr و کدهای برنامه با FontFamily.Monospace ایزوله و رندر می‌شوند."
            }
            userPrompt.contains("code", ignoreCase = true) || userPrompt.contains("کد", ignoreCase = true) -> {
                "کد مورد نظر:\n\n```kotlin\nval state by viewModel.uiState.collectAsStateWithLifecycle()\n```"
            }
            else -> {
                "درخواست شما با مدل ${_uiState.value.activeModelName} با موفقیت پردازش گردید."
            }
        }
    }

    private fun formatCurrentTime(): String {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        return String.format(java.util.Locale.US, "%02d:%02d", hour, minute)
    }
}
