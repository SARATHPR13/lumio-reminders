package com.lumio.app.presentation.screens.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumio.app.alarm.AlarmScheduler
import com.lumio.app.domain.model.Category
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.model.RepeatType
import com.lumio.app.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class AddReminderUiState(
    val title            : String    = "",
    val description      : String    = "",
    val dateTimeMillis   : Long      = System.currentTimeMillis() + 3_600_000L,
    val priority         : Priority  = Priority.NONE,
    val category         : Category? = null,
    val repeatType       : RepeatType = RepeatType.NONE,
    val soundEnabled     : Boolean   = true,
    val vibrationEnabled : Boolean   = true,
    val showDatePicker   : Boolean   = false,
    val showTimePicker   : Boolean   = false,
    val dateDisplay      : String    = "Today",
    val timeDisplay      : String    = "9:00 AM",
    val hour             : Int       = 9,
    val minute           : Int       = 0,
    val isSaving         : Boolean   = false,
    val isSaved          : Boolean   = false,
    val titleError       : Boolean   = false,
    val isEditing        : Boolean   = false,
    val editingId        : Long      = -1L
)

@HiltViewModel
class AddReminderViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val alarmScheduler    : AlarmScheduler,
    savedStateHandle              : SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddReminderUiState())
    val uiState: StateFlow<AddReminderUiState> = _uiState.asStateFlow()

    init {
        val reminderId = savedStateHandle.get<Long>("reminderId") ?: -1L
        if (reminderId != -1L) {
            loadReminder(reminderId)
        } else {
            setInitialDateTime()
        }
    }

    private fun setInitialDateTime() {
        val cal = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val h   = cal.get(Calendar.HOUR_OF_DAY)
        val m   = cal.get(Calendar.MINUTE)
        _uiState.update {
            it.copy(
                dateTimeMillis = cal.timeInMillis,
                dateDisplay    = formatDate(cal),
                timeDisplay    = formatTime(h, m),
                hour           = h,
                minute         = m
            )
        }
    }

    private fun loadReminder(id: Long) {
        viewModelScope.launch {
            val reminder = reminderRepository.getReminderById(id) ?: return@launch
            val cal      = Calendar.getInstance().apply { timeInMillis = reminder.dateTimeMillis }
            val h        = cal.get(Calendar.HOUR_OF_DAY)
            val m        = cal.get(Calendar.MINUTE)
            _uiState.update {
                it.copy(
                    title            = reminder.title,
                    description      = reminder.description,
                    dateTimeMillis   = reminder.dateTimeMillis,
                    priority         = reminder.priority,
                    category         = reminder.category,
                    repeatType       = reminder.repeatType,
                    soundEnabled     = reminder.soundEnabled,
                    vibrationEnabled = reminder.vibrationEnabled,
                    dateDisplay      = formatDate(cal),
                    timeDisplay      = formatTime(h, m),
                    hour             = h,
                    minute           = m,
                    isEditing        = true,
                    editingId        = id
                )
            }
        }
    }

    fun setTitle(v: String) {
        _uiState.update { it.copy(title = v, titleError = false) }
    }

    fun setDescription(v: String) {
        _uiState.update { it.copy(description = v) }
    }

    fun setPriority(v: Priority) {
        _uiState.update { it.copy(priority = v) }
    }

    fun setCategory(v: Category?) {
        _uiState.update { it.copy(category = v) }
    }

    fun setRepeatType(v: RepeatType) {
        _uiState.update { it.copy(repeatType = v) }
    }

    fun setSoundEnabled(v: Boolean) {
        _uiState.update { it.copy(soundEnabled = v) }
    }

    fun setVibrationEnabled(v: Boolean) {
        _uiState.update { it.copy(vibrationEnabled = v) }
    }

    fun showDatePicker(show: Boolean) {
        _uiState.update { it.copy(showDatePicker = show) }
    }

    fun showTimePicker(show: Boolean) {
        _uiState.update { it.copy(showTimePicker = show) }
    }

    fun setDate(millis: Long) {
        val s   = _uiState.value
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, s.hour)
            set(Calendar.MINUTE, s.minute)
            set(Calendar.SECOND, 0)
        }
        _uiState.update {
            it.copy(
                dateTimeMillis = cal.timeInMillis,
                dateDisplay    = formatDate(cal)
            )
        }
    }

    fun setTime(h: Int, m: Int) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _uiState.value.dateTimeMillis
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
        }
        _uiState.update {
            it.copy(
                dateTimeMillis = cal.timeInMillis,
                timeDisplay    = formatTime(h, m),
                hour           = h,
                minute         = m
            )
        }
    }

    fun saveReminder() {
        val s = _uiState.value
        if (s.title.isBlank()) {
            _uiState.update { it.copy(titleError = true) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val reminder = Reminder(
                    id               = if (s.isEditing) s.editingId else 0L,
                    title            = s.title.trim(),
                    description      = s.description.trim(),
                    dateTimeMillis   = s.dateTimeMillis,
                    priority         = s.priority,
                    category         = s.category,
                    repeatType       = s.repeatType,
                    soundEnabled     = s.soundEnabled,
                    vibrationEnabled = s.vibrationEnabled
                )
                val id = if (s.isEditing) {
                    reminderRepository.updateReminder(reminder)
                    s.editingId
                } else {
                    reminderRepository.insertReminder(reminder)
                }
                alarmScheduler.schedule(reminder.copy(id = id))
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun formatDate(cal: Calendar): String {
        val today    = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        return when {
            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) -> "Today"
            cal.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR) &&
            cal.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) -> "Tomorrow"
            else -> "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH)+1}/${cal.get(Calendar.YEAR)}"
        }
    }

    private fun formatTime(h: Int, m: Int): String {
        val ampm = if (h < 12) "AM" else "PM"
        val hour = if (h % 12 == 0) 12 else h % 12
        return "$hour:${m.toString().padStart(2, '0')} $ampm"
    }
}
