package com.lumio.app.presentation.screens.add

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
    val title: String             = "",
    val description: String       = "",
    val dateDisplay: String = "Today",
    val timeDisplay: String = "Set Time",
    val hour: Int = 9,
    val minute: Int = 0,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val dateTimeMillis: Long      = System.currentTimeMillis() + 3_600_000L,
    val priority: Priority        = Priority.NONE,
    val category: Category?       = null,
    val repeatType: RepeatType    = RepeatType.NONE,
    val soundEnabled: Boolean     = true,
    val vibrationEnabled: Boolean = true,
    val showDatePicker: Boolean   = false,
    val showTimePicker: Boolean   = false,
    val titleError: Boolean       = false,
    val isSaving: Boolean         = false,
    val isSaved: Boolean          = false
)

@HiltViewModel
class AddReminderViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(AddReminderUiState())
    val uiState: StateFlow<AddReminderUiState> = _state.asStateFlow()

    fun setTitle(v: String)        = _state.update { it.copy(title = v,        titleError = false) }
    fun setDescription(v: String)  = _state.update { it.copy(description = v)  }
    fun setPriority(p: Priority)   = _state.update { it.copy(priority = p)     }
    fun setCategory(c: Category?)  = _state.update { it.copy(category = c)     }
    fun setRepeat(r: RepeatType)   = _state.update { it.copy(repeatType = r)   }
    fun setSound(v: Boolean)       = _state.update { it.copy(soundEnabled = v) }
    fun setVibration(v: Boolean)   = _state.update { it.copy(vibrationEnabled = v) }
    fun showDate(v: Boolean)       = _state.update { it.copy(showDatePicker = v) }
    fun showTime(v: Boolean)       = _state.update { it.copy(showTimePicker = v) }

    fun setDate(millis: Long) {
        val cur = Calendar.getInstance().apply { timeInMillis = _state.value.dateTimeMillis }
        val sel = Calendar.getInstance().apply { timeInMillis = millis }
        cur.set(Calendar.YEAR,         sel.get(Calendar.YEAR))
        cur.set(Calendar.MONTH,        sel.get(Calendar.MONTH))
        cur.set(Calendar.DAY_OF_MONTH, sel.get(Calendar.DAY_OF_MONTH))
        _state.update { it.copy(dateTimeMillis = cur.timeInMillis, showDatePicker = false) }
    }

    fun setTime(hour: Int, minute: Int) {
        val cal = Calendar.getInstance().apply { timeInMillis = _state.value.dateTimeMillis }
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        _state.update { it.copy(dateTimeMillis = cal.timeInMillis, showTimePicker = false) }
    }

    fun saveReminder() {
        val s = _state.value
        if (s.title.isBlank()) {
            _state.update { it.copy(titleError = true) }
            return
        }

        val reminder = Reminder(
            title            = s.title.trim(),
            description      = s.description.trim(),
            dateTimeMillis   = s.dateTimeMillis,
            priority         = s.priority,
            category         = s.category,
            repeatType       = s.repeatType,
            soundEnabled     = s.soundEnabled,
            vibrationEnabled = s.vibrationEnabled
        )

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val id = reminderRepository.insertReminder(reminder)
            val savedReminder = reminder.copy(id = id)
            // Schedule the alarm immediately after saving
            alarmScheduler.schedule(savedReminder)
            _state.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    fun showDatePicker(show: Boolean) {
        _uiState.update { it.copy(showDatePicker = show) }
    }

    fun showTimePicker(show: Boolean) {
        _uiState.update { it.copy(showTimePicker = show) }
    }

    fun setDate(millis: Long) {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
        val display = "${cal.get(java.util.Calendar.DAY_OF_MONTH)}/${cal.get(java.util.Calendar.MONTH)+1}/${cal.get(java.util.Calendar.YEAR)}"
        _uiState.update {
            it.copy(
                dateTimeMillis = millis,
                dateDisplay    = display
            )
        }
    }

    fun setTime(h: Int, m: Int) {
        val ampm  = if (h < 12) "AM" else "PM"
        val hour  = if (h % 12 == 0) 12 else h % 12
        val display = "$hour:${m.toString().padStart(2,'0')} $ampm"
        val cal = java.util.Calendar.getInstance().apply {
            timeInMillis = _uiState.value.dateTimeMillis
            set(java.util.Calendar.HOUR_OF_DAY, h)
            set(java.util.Calendar.MINUTE, m)
        }
        _uiState.update {
            it.copy(
                dateTimeMillis = cal.timeInMillis,
                timeDisplay    = display,
                hour           = h,
                minute         = m
            )
        }
    }

}
