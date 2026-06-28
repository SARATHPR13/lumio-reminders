package com.lumio.app.presentation.screens.home

import com.lumio.app.domain.model.Reminder

enum class HomeFilter(val label: String, val emoji: String) {
    TODAY    ("Today",     "📅"),
    UPCOMING ("Upcoming",  "⏰"),
    PRIORITY ("Priority",  "🔥"),
    COMPLETED("Completed", "✅"),
    ALL      ("All",       "📋"),
}

data class HomeUiState(
    val allReminders: List<Reminder>       = emptyList(),
    val todayReminders: List<Reminder>     = emptyList(),
    val upcomingReminders: List<Reminder>  = emptyList(),
    val completedReminders: List<Reminder> = emptyList(),
    val priorityReminders: List<Reminder>  = emptyList(),
    val displayedReminders: List<Reminder> = emptyList(),
    val activeFilter: HomeFilter           = HomeFilter.TODAY,
    val searchQuery: String                = "",
    val isSearchActive: Boolean            = false,
    val isLoading: Boolean                 = false,
    val todayCount: Int                    = 0,
    val totalCount: Int                    = 0,
    val completedCount: Int                = 0,
)
