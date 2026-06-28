package com.lumio.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.repository.CategoryRepository
import com.lumio.app.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val reminderRepo: ReminderRepository,
    private val categoryRepo: CategoryRepository
) : ViewModel() {

    private val _filter      = MutableStateFlow(HomeFilter.TODAY)
    private val _searchQuery = MutableStateFlow("")
    private val _isSearching = MutableStateFlow(false)
    private val _uiState     = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Seed default categories if empty
        viewModelScope.launch {
            categoryRepo.insertDefaultCategories()
        }
        observeReminders()
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun observeReminders() {
        viewModelScope.launch {
            combine(
                reminderRepo.getAllReminders(),
                reminderRepo.getTodayReminders(),
                reminderRepo.getUpcomingReminders(),
                reminderRepo.getCompletedReminders(),
                reminderRepo.getPriorityReminders()
            ) { all, today, upcoming, completed, priority ->
                HomeUiState(
                    allReminders       = all,
                    todayReminders     = today,
                    upcomingReminders  = upcoming,
                    completedReminders = completed,
                    priorityReminders  = priority,
                    todayCount         = today.size,
                    totalCount         = all.filter { !it.isCompleted }.size,
                    completedCount     = completed.size,
                )
            }.combine(_filter) { state, filter ->
                state.copy(
                    activeFilter       = filter,
                    displayedReminders = applyFilter(state, filter, _searchQuery.value)
                )
            }.combine(_searchQuery.debounce(300)) { state, query ->
                state.copy(
                    searchQuery        = query,
                    displayedReminders = applyFilter(state, state.activeFilter, query)
                )
            }.combine(_isSearching) { state, searching ->
                state.copy(isSearchActive = searching)
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun applyFilter(
        state: HomeUiState,
        filter: HomeFilter,
        query: String
    ): List<Reminder> {
        val base = when (filter) {
            HomeFilter.TODAY     -> state.todayReminders
            HomeFilter.UPCOMING  -> state.upcomingReminders
            HomeFilter.COMPLETED -> state.completedReminders
            HomeFilter.PRIORITY  -> state.priorityReminders
            HomeFilter.ALL       -> state.allReminders.filter { !it.isCompleted }
        }
        return if (query.isBlank()) base
        else base.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true) ||
            it.category?.name?.contains(query, ignoreCase = true) == true
        }
    }

    fun setFilter(filter: HomeFilter) { _filter.value = filter }

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun toggleSearch() {
        _isSearching.value = !_isSearching.value
        if (!_isSearching.value) _searchQuery.value = ""
    }

    fun toggleComplete(id: Long, done: Boolean) {
        viewModelScope.launch {
            reminderRepo.setCompleted(id, done)
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            reminderRepo.deleteReminder(id)
        }
    }

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepo.insertReminder(reminder)
        }
    }

    fun deleteAllCompleted() {
        viewModelScope.launch {
            reminderRepo.deleteAllCompleted()
        }
    }
}
