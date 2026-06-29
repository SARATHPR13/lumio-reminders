package com.lumio.app.presentation.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumio.app.ai.AiParseResult
import com.lumio.app.ai.ChatMessage
import com.lumio.app.ai.DuplicateDetector
import com.lumio.app.ai.NaturalLanguageProcessor
import com.lumio.app.ai.SmartTimeSuggester
import com.lumio.app.ai.SuggestedReminder
import com.lumio.app.alarm.AlarmScheduler
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.model.RepeatType
import com.lumio.app.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiChatUiState(
    val messages: List<ChatMessage>         = emptyList(),
    val inputText: String                   = "",
    val isTyping: Boolean                   = false,
    val pendingReminder: SuggestedReminder? = null,
    val successMessage: String?             = null,
    val timeSuggestions: List<SmartTimeSuggester.TimeSuggestion> = emptyList(),
    val showTimeSuggestions: Boolean        = false
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
        _uiState.update {
            it.copy(
                messages = listOf(
                    ChatMessage(
                        text   = "Hi! I'm LUMIO AI \uD83E\uDD16\n\nTell me what you need to remember and I'll create the perfect reminder!\n\nExample:\n\"Remind me to take medicine every day at 8 AM\"",
                        isUser = false
                    )
                )
            )
        }
    }

    fun setInput(text: String) = _uiState.update { it.copy(inputText = text) }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return

        val userMsg = ChatMessage(text = text, isUser = true)
        _uiState.update { state ->
            state.copy(
                messages  = state.messages + userMsg,
                inputText = "",
                isTyping  = true
            )
        }

        viewModelScope.launch {
            delay(800)

            val pending = _uiState.value.pendingReminder
            if (pending != null) {
                handleConfirmation(text, pending)
                return@launch
            }

            val result      = NaturalLanguageProcessor.process(text)
            val allReminders = reminderRepository.getAllReminders().first()

            val suggestions = if (result.suggestedReminder != null) {
                SmartTimeSuggester.getSuggestions(
                    result.suggestedReminder.title,
                    allReminders
                )
            } else emptyList()

            var response = result.response
            if (result.suggestedReminder != null) {
                val newReminder = Reminder(
                    title          = result.suggestedReminder.title,
                    dateTimeMillis = result.suggestedReminder.dateTimeMillis
                )
                val dupCheck = DuplicateDetector.checkDuplicate(newReminder, allReminders)
                if (dupCheck.isDuplicate) {
                    response += "\n\n\u26A0\uFE0F Similar reminder exists: \"${dupCheck.similarReminder?.title}\". Save anyway?"
                }
            }

            val aiMsg = ChatMessage(
                text              = response,
                isUser            = false,
                suggestedReminder = result.suggestedReminder
            )

            _uiState.update { state ->
                state.copy(
                    messages            = state.messages + aiMsg,
                    isTyping            = false,
                    pendingReminder     = result.suggestedReminder,
                    timeSuggestions     = suggestions,
                    showTimeSuggestions = suggestions.isNotEmpty() && result.suggestedReminder != null
                )
            }
        }
    }

    private suspend fun handleConfirmation(text: String, pending: SuggestedReminder) {
        val lower = text.lowercase()
        when {
            lower.contains("yes") || lower.contains("save") ||
            lower.contains("ok")  || lower.contains("sure") ||
            lower.contains("go ahead") || lower.contains("confirm") ||
            lower.contains("add") || lower.contains("do it") -> {
                saveReminder(pending)
            }
            lower.contains("no") || lower.contains("cancel") ||
            lower.contains("skip") -> {
                val aiMsg = ChatMessage(
                    text   = "No problem! \uD83D\uDE0A Reminder not saved.\n\nAnything else?",
                    isUser = false
                )
                _uiState.update { state ->
                    state.copy(
                        messages            = state.messages + aiMsg,
                        isTyping            = false,
                        pendingReminder     = null,
                        showTimeSuggestions = false
                    )
                }
            }
            else -> {
                val result = NaturalLanguageProcessor.process(text)
                val aiMsg  = ChatMessage(
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

        val repeatInfo = if (suggested.repeatType != RepeatType.NONE)
            "\n\uD83D\uDD04 Repeats: ${suggested.repeatType.label}" else ""

        val aiMsg = ChatMessage(
            text   = "\u2705 Reminder saved!\n\n\uD83D\uDD14 ${suggested.title}\n\uD83D\uDCC5 ${suggested.dateDescription}\n\u23F0 ${suggested.timeDescription}$repeatInfo\n\nYou\'ll be notified on time! Anything else?",
            isUser = false
        )
        _uiState.update { state ->
            state.copy(
                messages            = state.messages + aiMsg,
                isTyping            = false,
                pendingReminder     = null,
                showTimeSuggestions = false,
                successMessage      = "\u2705 Reminder saved!"
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
            text              = "\u2705 Time updated to $label for \"${updated.title}\"\n\nShall I save it?",
            isUser            = false,
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

    fun dismissTimeSuggestions() = _uiState.update { it.copy(showTimeSuggestions = false) }

    fun clearMessages() = _uiState.update { it.copy(successMessage = null) }

    fun clearConversation() {
        _uiState.update { AiChatUiState() }
        addWelcomeMessage()
    }

    fun sendQuickMessage(text: String) {
        _uiState.update { it.copy(inputText = text) }
        sendMessage()
    }
}
