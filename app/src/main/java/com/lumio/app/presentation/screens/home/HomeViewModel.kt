package com.lumio.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.model.SampleData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _reminders = MutableStateFlow<List<Reminder>>(SampleData.reminders)
    private val _uiState   = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _reminders.collect { list ->
                updateState(list, _uiState.value.activeFilter, _uiState.value.searchQuery)
            }
        }
    }

    private fun updateState(list: List<Reminder>, filter: HomeFilter, query: String) {
        val now       = System.currentTimeMillis()
        val today     = list.filter {  it.isToday && !it.isCompleted }.sortedBy { it.dateTimeMillis }
        val upcoming  = list.filter { !it.isToday && !it.isCompleted && it.dateTimeMillis > now }.sortedBy { it.dateTimeMillis }
        val completed = list.filter {  it.isCompleted }.sortedByDescending { it.dateTimeMillis }
        val priority  = list.filter { (it.priority == Priority.URGENT || it.priority == Priority.HIGH) && !it.isCompleted }
                            .sortedByDescending { it.priority.level }

        _uiState.update { state ->
            state.copy(
                allReminders       = list,
                todayReminders     = today,
                upcomingReminders  = upcoming,
                completedReminders = completed,
                priorityReminders  = priority,
                displayedReminders = filtered(list, today, upcoming, completed, priority, filter, query),
                todayCount         = today.size,
                totalCount         = list.filter { !it.isCompleted }.size,
                completedCount     = completed.size,
            )
        }
    }

    private fun filtered(
        all: List<Reminder>, today: List<Reminder>, upcoming: List<Reminder>,
        completed: List<Reminder>, priority: List<Reminder>,
        filter: HomeFilter, query: String
    ): List<Reminder> {
        val base = when (filter) {
            HomeFilter.TODAY     -> today
            HomeFilter.UPCOMING  -> upcoming
            HomeFilter.COMPLETED -> completed
            HomeFilter.PRIORITY  -> priority
            HomeFilter.ALL       -> all.filter { !it.isCompleted }
        }
        return if (query.isBlank()) base
        else base.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true) ||
            it.category?.name?.contains(query, ignoreCase = true) == true
        }
    }

    fun setFilter(filter: HomeFilter) {
        _uiState.update { s ->
            s.copy(
                activeFilter = filter,
                displayedReminders = filtered(
                    s.allReminders, s.todayReminders, s.upcomingReminders,
                    s.completedReminders, s.priorityReminders, filter, s.searchQuery
                )
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { s ->
            s.copy(
                searchQuery = query,
                displayedReminders = filtered(
                    s.allReminders, s.todayReminders, s.upcomingReminders,
                    s.completedReminders, s.priorityReminders, s.activeFilter, query
                )
            )
        }
    }

    fun toggleSearch() {
        val nowSearching = !_uiState.value.isSearchActive
        _uiState.update { it.copy(isSearchActive = nowSearching, searchQuery = "") }
        if (!nowSearching) setFilter(_uiState.value.activeFilter)
    }

    fun toggleComplete(id: Long, done: Boolean) {
        viewModelScope.launch {
            _reminders.update { list -> list.map { if (it.id == id) it.copy(isCompleted = done) else it } }
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            _reminders.update { list -> list.filter { it.id != id } }
        }
    }

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            val newId = (_reminders.value.maxOfOrNull { it.id } ?: 0L) + 1L
            _reminders.update { list -> list + reminder.copy(id = newId) }
        }
    }
}
