package com.lumio.app.presentation.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumio.app.alarm.AlarmScheduler
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderDetailUiState(
    val reminder: Reminder?       = null,
    val isLoading: Boolean        = true,
    val isDeleted: Boolean        = false,
    val showDeleteDialog: Boolean = false,
    val errorMessage: String?     = null
)

@HiltViewModel
class ReminderDetailViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reminderId: Long =
        savedStateHandle.get<Long>("reminderId") ?: -1L

    private val _uiState = MutableStateFlow(ReminderDetailUiState())
    val uiState: StateFlow<ReminderDetailUiState> = _uiState.asStateFlow()

    init { loadReminder() }

    private fun loadReminder() {
        viewModelScope.launch {
            try {
                val reminder = reminderRepository.getReminderById(reminderId)
                _uiState.update { it.copy(reminder = reminder, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun toggleComplete() {
        val reminder = _uiState.value.reminder ?: return
        viewModelScope.launch {
            val done = !reminder.isCompleted
            reminderRepository.setCompleted(reminderId, done)
            if (done) alarmScheduler.cancel(reminderId)
            else alarmScheduler.schedule(reminder.copy(isCompleted = false))
            _uiState.update { it.copy(reminder = reminder.copy(isCompleted = done)) }
        }
    }

    fun showDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = show) }
    }

    fun deleteReminder() {
        viewModelScope.launch {
            alarmScheduler.cancel(reminderId)
            reminderRepository.deleteReminder(reminderId)
            _uiState.update { it.copy(isDeleted = true, showDeleteDialog = false) }
        }
    }
}
