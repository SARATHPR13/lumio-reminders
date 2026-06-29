package com.lumio.app.presentation.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumio.app.ai.ChatMessage
import com.lumio.app.ai.DuplicateDetector
import com.lumio.app.ai.NaturalLanguageProcessor
import com.lumio.app.ai.SmartTimeSuggester
import com.lumio.app.ai.SuggestedReminder
import com.lumio.app.ai.TimeSuggestion
import com.lumio.app.alarm.AlarmScheduler
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.model.RepeatType
import com.lumio.app.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiChatUiState(
    val messages: List<ChatMessage>           = emptyList(),
    val inputText: String                     = "",
    val isTyping: Boolean                     = false,
    val pendingReminder: SuggestedReminder?   = null,
    val successMessage: String?               = null,
    val timeSuggestions: List<TimeSuggestion> = emptyList(),
    val showTimeSuggestions: Boolean          = false
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    init {
        addWelcomeMessage()
    }

    private fun addWelcomeMessage() {
        _uiState.update {
            it.copy(
                messages = listOf(
                    ChatMessage(
                        text   = "Hi! I am LUMIO AI\n\nTell me what you need to remember!\n\nExample: Remind me to take medicine every day at 8 AM",
                        isUser = false
                    )
                )
            )
        }
    }

    fun setInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

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
            try {
                delay(800)

                val pending = _uiState.value.pendingReminder
                if (pending != null) {
                    handleConfirmation(text, pending)
                    return@launch
                }

                val result = NaturalLanguageProcessor.process(text)

                val allReminders = try {
                    reminderRepository.getAllReminders().first()
                } catch (e: Exception) {
                    emptyList()
                }

                val suggestions: List<TimeSuggestion> = if (result.suggestedReminder != null) {
                    try {
                        SmartTimeSuggester.getSuggestions(
                            result.suggestedReminder.title,
                            allReminders
                        )
                    } catch (e: Exception) { emptyList() }
                } else emptyList()

                var response = result.response
                if (result.suggestedReminder != null && allReminders.isNotEmpty()) {
                    try {
                        val newReminder = Reminder(
                            title          = result.suggestedReminder.title,
                            dateTimeMillis = result.suggestedReminder.dateTimeMillis
                        )
                        val dupCheck = DuplicateDetector.checkDuplicate(newReminder, allReminders)
                        if (dupCheck.isDuplicate) {
                            response += "\n\nNote: Similar reminder already exists: \"${dupCheck.similarReminder?.title}\""
                        }
                    } catch (e: Exception) { }
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
            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    text   = "Sorry, something went wrong. Please try again.",
                    isUser = false
                )
                _uiState.update { state ->
                    state.copy(
                        messages = state.messages + errorMsg,
                        isTyping = false
                    )
                }
            }
        }
    }

    private suspend fun handleConfirmation(text: String, pending: SuggestedReminder) {
        val lower = text.lowercase()
        when {
            lower.contains("yes") || lower.contains("save") ||
            lower.contains("ok")  || lower.contains("sure") ||
            lower.contains("add") || lower.contains("confirm") -> {
                saveReminder(pending)
            }
            lower.contains("no")  || lower.contains("cancel") ||
            lower.contains("skip") -> {
                val aiMsg = ChatMessage(
                    text   = "Cancelled! Reminder not saved. Anything else?",
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
        viewModelScope.launch {
            try {
                saveReminder(pending)
            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    text   = "Failed to save reminder. Please try again.",
                    isUser = false
                )
                _uiState.update { state ->
                    state.copy(
                        messages = state.messages + errorMsg,
                        isTyping = false
                    )
                }
            }
        }
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
            "\nRepeats: ${suggested.repeatType.label}" else ""

        val aiMsg = ChatMessage(
            text   = "Reminder saved successfully!\n\n${suggested.title}\n${suggested.dateDescription} at ${suggested.timeDescription}$repeatInfo\n\nAnything else?",
            isUser = false
        )
        _uiState.update { state ->
            state.copy(
                messages            = state.messages + aiMsg,
                isTyping            = false,
                pendingReminder     = null,
                showTimeSuggestions = false,
                successMessage      = "Reminder saved!"
            )
        }
    }

    fun useTimeSuggestion(suggestion: TimeSuggestion) {
        val pending = _uiState.value.pendingReminder ?: return
        val cal     = java.util.Calendar.getInstance().apply {
            timeInMillis = pending.dateTimeMillis
            set(java.util.Calendar.HOUR_OF_DAY, suggestion.hour)
            set(java.util.Calendar.MINUTE, suggestion.minute)
            set(java.util.Calendar.SECOND, 0)
        }
        val updated = pending.copy(
            dateTimeMillis  = cal.timeInMillis,
            timeDescription = suggestion.label
        )
        val aiMsg = ChatMessage(
            text              = "Time updated to ${suggestion.label}\n\nShall I save the reminder?",
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
