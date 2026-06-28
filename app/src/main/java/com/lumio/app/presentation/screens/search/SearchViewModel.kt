package com.lumio.app.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumio.app.domain.model.Category
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String            = "",
    val results: List<Reminder>  = emptyList(),
    val isSearching: Boolean     = false,
    val selectedCategory: Category? = null,
    val hasSearched: Boolean     = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _query    = MutableStateFlow("")
    private val _category = MutableStateFlow<Category?>(null)
    private val _uiState  = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init { observeSearch() }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeSearch() {
        viewModelScope.launch {
            _query
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { q ->
                    if (q.isBlank()) flowOf(emptyList())
                    else reminderRepository.searchReminders(q)
                }
                .combine(_category) { results, cat ->
                    if (cat == null) results
                    else results.filter { it.category?.id == cat.id }
                }
                .collect { results ->
                    _uiState.update { it.copy(results = results, isSearching = false) }
                }
        }
    }

    fun setQuery(q: String) {
        _query.value = q
        _uiState.update { it.copy(query = q, isSearching = q.isNotBlank(), hasSearched = q.isNotBlank()) }
    }

    fun setCategory(cat: Category?) {
        _category.value = cat
        _uiState.update { it.copy(selectedCategory = cat) }
    }

    fun clearQuery() {
        _query.value = ""
        _uiState.update { it.copy(query = "", results = emptyList(), isSearching = false, hasSearched = false) }
    }

    fun toggleComplete(id: Long, done: Boolean) {
        viewModelScope.launch { reminderRepository.setCompleted(id, done) }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch { reminderRepository.deleteReminder(id) }
    }
}
