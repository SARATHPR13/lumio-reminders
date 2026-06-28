package com.lumio.app.presentation.screens.voice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumio.app.alarm.AlarmScheduler
import com.lumio.app.domain.model.Category
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.model.RepeatType
import com.lumio.app.domain.repository.ReminderRepository
import com.lumio.app.voice.SpeechRecognitionManager
import com.lumio.app.voice.SpeechState
import com.lumio.app.voice.VoiceParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoiceUiState(
    val speechState: SpeechState       = SpeechState.Idle,
    val spokenText: String             = "",
    val parsedTitle: String            = "",
    val parsedDate: String             = "",
    val parsedTime: String             = "",
    val parsedPriority: Priority       = Priority.NONE,
    val parsedCategory: Category?      = null,
    val dateTimeMillis: Long           = System.currentTimeMillis() + 3_600_000L,
    val confidence: Float              = 0f,
    val isAvailable: Boolean           = true,
    val isSaving: Boolean              = false,
    val isSaved: Boolean               = false,
    val showPreview: Boolean           = false,
    val errorMessage: String?          = null,
    val suggestions: List<String>      = listOf(
        "Remind me to call mom tomorrow at 5 PM",
        "Remind me to take medicine today at 8 PM",
        "Remind me to pay electricity bill next Monday",
        "Remind me to study for exam tomorrow morning",
        "Remind me to buy groceries today at 6 PM"
    )
)

@HiltViewModel
class VoiceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderRepository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val speechManager = SpeechRecognitionManager(context)

    private val _uiState = MutableStateFlow(
        VoiceUiState(isAvailable = speechManager.isAvailable())
    )
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    init {
        observeSpeechState()
    }

    private fun observeSpeechState() {
        viewModelScope.launch {
            speechManager.state.collect { state ->
                _uiState.update { it.copy(speechState = state) }

                when (state) {
                    is SpeechState.Result -> {
                        processVoiceResult(state.text)
                    }
                    is SpeechState.Error  -> {
                        _uiState.update { it.copy(errorMessage = state.message) }
                    }
                    else -> {}
                }
            }
        }
    }

    fun startListening() {
        _uiState.update { it.copy(errorMessage = null, showPreview = false, spokenText = "") }
        speechManager.startListening()
    }

    fun stopListening() {
        speechManager.stopListening()
    }

    fun reset() {
        speechManager.reset()
        _uiState.update {
            it.copy(
                spokenText    = "",
                parsedTitle   = "",
                parsedDate    = "",
                parsedTime    = "",
                parsedPriority= Priority.NONE,
                parsedCategory= null,
                showPreview   = false,
                errorMessage  = null,
                isSaved       = false
            )
        }
    }

    fun useSuggestion(text: String) {
        processVoiceResult(text)
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(parsedTitle = title) }
    }

    fun updatePriority(priority: Priority) {
        _uiState.update { it.copy(parsedPriority = priority) }
    }

    fun updateCategory(category: Category?) {
        _uiState.update { it.copy(parsedCategory = category) }
    }

    fun saveReminder() {
        val state = _uiState.value
        if (state.parsedTitle.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please provide a reminder title") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val reminder = Reminder(
                title          = state.parsedTitle,
                description    = "Created via voice: \"${state.spokenText}\"",
                dateTimeMillis = state.dateTimeMillis,
                priority       = state.parsedPriority,
                category       = state.parsedCategory,
                repeatType     = RepeatType.NONE,
                soundEnabled   = true,
                vibrationEnabled = true
            )

            val id = reminderRepository.insertReminder(reminder)
            alarmScheduler.schedule(reminder.copy(id = id))

            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    private fun processVoiceResult(text: String) {
        val parsed = VoiceParser.parse(text)
        _uiState.update { state ->
            state.copy(
                spokenText     = text,
                parsedTitle    = parsed.title,
                parsedDate     = parsed.dateDescription,
                parsedTime     = parsed.timeDescription,
                parsedPriority = parsed.priority,
                parsedCategory = parsed.suggestedCategory,
                dateTimeMillis = parsed.dateTimeMillis,
                confidence     = parsed.confidence,
                showPreview    = true,
                errorMessage   = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
    }
}
