package com.lumio.app.presentation.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumio.app.ai.AiModels.ChatMessage
import com.lumio.app.ai.AiModels.SuggestedReminder
import com.lumio.app.ai.DuplicateDetector
import com.lumio.app.ai.NaturalLanguageProcessor
import com.lumio.app.ai.SmartTimeSuggester
import com.lumio.app.alarm.AlarmScheduler
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AiChatUiState(
    val messages: List<ChatMessage>       = emptyList(),
    val inputText: String                 = "",
    val isTyping: Boolean                 = false,
    val pendingReminder: SuggestedReminder? = null,
    val showConfirmDialog: Boolean        = false,
    val savedReminderId: Long?            = null,
    val successMessage: String?           = null,
    val timeSuggestions: List<com.lumio.app.ai.SmartTimeSuggester.TimeSuggestion> = emptyList(),
    val showTimeSuggestions: Boolean      = false
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    init { addWelcomeMessage() }

    private fun addWelcomeMessage() {
        val welcome = ChatMessage(
            text   = "Hi! I\'m LUMIO AI \uD83E\uDD16\n\nTell me what you need to remember and I\'ll create the perfect reminder for you!\n\nTry: \"Remind me to take medicine every day at 8 AM\"",
            isUser = false
        )
        _uiState.update { it.copy(messages = listOf(welcome)) }
    }

    fun setInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return

        // Add user message
        val userMsg = ChatMessage(text = text, isUser = true)
        _uiState.update { state ->
            state.copy(
                messages  = state.messages + userMsg,
                inputText = "",
                isTyping  = true
            )
        }

        viewModelScope.launch {
            // Simulate AI thinking delay
            delay(800)

            // Check if confirming a reminder
            val pending = _uiState.value.pendingReminder
            if (pending != null) {
                handleConfirmation(text, pending)
                return@launch
            }

            // Process with NLP
            val result = NaturalLanguageProcessor.process(text)

            // Get time suggestions if reminder detected
            val suggestions = if (result.suggestedReminder != null) {
                SmartTimeSuggester.getSuggestions(
                    result.suggestedReminder.title,
                    reminderRepository.getAllReminders().first()
                )
            } else emptyList()

            // Check for duplicate if reminder detected
            var response = result.response
            if (result.suggestedReminder != null) {
                val allReminders = reminderRepository.getAllReminders().first()
                val newReminder  = Reminder(
                    title          = result.suggestedReminder.title,
                    dateTimeMillis = result.suggestedReminder.dateTimeMillis
                )
                val dupCheck = DuplicateDetector.checkDuplicate(newReminder, allReminders)
                if (dupCheck.isDuplicate) {
                    response += "\n\n\u26A0\uFE0F **Heads up:** You have a similar reminder: \"${dupCheck.similarReminder?.title}\". Do you still want to create this one?"
                }
            }

            // Add AI response
            val aiMsg = ChatMessage(
                text              = response,
                isUser            = false,
                suggestedReminder = result.suggestedReminder
            )

            _uiState.update { state ->
                state.copy(
                    messages           = state.messages + aiMsg,
                    isTyping           = false,
                    pendingReminder    = result.suggestedReminder,
                    timeSuggestions    = suggestions,
                    showTimeSuggestions = suggestions.isNotEmpty() && result.suggestedReminder != null
                )
            }
        }
    }

    private suspend fun handleConfirmation(text: String, pending: SuggestedReminder) {
        val lower = text.lowercase()
        when {
            lower.contains("yes") || lower.contains("save") ||
            lower.contains("ok") || lower.contains("sure") ||
            lower.contains("go ahead") || lower.contains("confirm") ||
            lower.contains("add it") || lower.contains("do it") -> {
                saveReminder(pending)
            }
            lower.contains("no") || lower.contains("cancel") ||
            lower.contains("don\'t") || lower.contains("skip") -> {
                val aiMsg = ChatMessage(
                    text   = "No problem! \uD83D\uDE0A The reminder was not saved.\n\nIs there anything else you\'d like to set up?",
                    isUser = false
                )
                _uiState.update { state ->
                    state.copy(
                        messages        = state.messages + aiMsg,
                        isTyping        = false,
                        pendingReminder = null,
                        showTimeSuggestions = false
                    )
                }
            }
            else -> {
                // Treat as a new message
                val result  = NaturalLanguageProcessor.process(text)
                val aiMsg   = ChatMessage(
                    text              = result.response,
                    isUser            = false,
                    suggestedReminder = result.suggestedReminder
                )
                _uiState.update { state ->
                    state.copy(
                        messages        = state.messages + aiMsg,
                        isTyping        = false,
                        pendingReminder = result.suggestedReminder ?: pending
                    )
                }
            }
        }
    }

    fun confirmSaveReminder() {
        val pending = _uiState.value.pendingReminder ?: return
        viewModelScope.launch { saveReminder(pending) }
    }

    private suspend fun saveReminder(suggested: SuggestedReminder) {
        val reminder = Reminder(
            title            = suggested.title,
            description      = suggested.description,
            dateTimeMillis   = suggested.dateTimeMillis,
            priority         = suggested.priority,
            category         = suggested.category,
            repeatType       = suggested.repeatType,
            soundEnabled     = true,
            vibrationEnabled = true
        )
        val id = reminderRepository.insertReminder(reminder)
        alarmScheduler.schedule(reminder.copy(id = id))

        val repeatInfo = if (suggested.repeatType != com.lumio.app.domain.model.RepeatType.NONE)
            "\n\uD83D\uDD04 Set to repeat: ${suggested.repeatType.label}" else ""

        val aiMsg = ChatMessage(
            text = "\u2705 **Reminder saved!**\n\n\uD83D\uDD14 ${suggested.title}\n\uD83D\uDCC5 ${suggested.dateDescription}\n\u23F0 ${suggested.timeDescription}$repeatInfo\n\nYou\'ll be notified at the right time! Is there anything else you need?",
            isUser = false
        )
        _uiState.update { state ->
            state.copy(
                messages        = state.messages + aiMsg,
                isTyping        = false,
                pendingReminder = null,
                showTimeSuggestions = false,
                successMessage  = "Reminder saved!"
            )
        }
    }

    fun useTimeSuggestion(hour: Int, minute: Int, label: String) {
        val pending = _uiState.value.pendingReminder ?: return
        val cal = java.util.Calendar.getInstance().apply {
            timeInMillis = pending.dateTimeMillis
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
        }
        val updated = pending.copy(
            dateTimeMillis  = cal.timeInMillis,
            timeDescription = label
        )
        val aiMsg = ChatMessage(
            text   = "\u2705 Updated time to **$label** for \"${updated.title}\"\n\nShall I save it now?",
            isUser = false,
            suggestedReminder = updated
        )
        _uiState.update { state ->
            state.copy(
                messages            = state.messages + aiMsg,
                pendingReminder     = updated,
                showTimeSuggestions = false
            )
        }
    }

    fun dismissTimeSuggestions() {
        _uiState.update { it.copy(showTimeSuggestions = false) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun clearConversation() {
        _uiState.update { AiChatUiState() }
        addWelcomeMessage()
    }

    fun sendQuickMessage(text: String) {
        _uiState.update { it.copy(inputText = text) }
        sendMessage()
    }
}
