package com.lumio.app.ai

import com.lumio.app.domain.model.Reminder
import java.util.Calendar

data class TimeSuggestion(
    val hour: Int,
    val minute: Int,
    val label: String,
    val reason: String,
    val emoji: String
)

object SmartTimeSuggester {

    fun getSuggestions(
        title: String,
        existingReminders: List<Reminder>
    ): List<TimeSuggestion> {
        val lower = title.lowercase()
        val suggestions = mutableListOf<TimeSuggestion>()

        when {
            lower.contains("medicine") || lower.contains("tablet") ||
            lower.contains("vitamin")  || lower.contains("pill") -> {
                suggestions.add(TimeSuggestion(8,  0, "8:00 AM",  "After breakfast",  "💊"))
                suggestions.add(TimeSuggestion(20, 0, "8:00 PM",  "After dinner",     "💊"))
                suggestions.add(TimeSuggestion(13, 0, "1:00 PM",  "After lunch",      "💊"))
            }
            lower.contains("exercise") || lower.contains("gym") ||
            lower.contains("workout")  || lower.contains("run") -> {
                suggestions.add(TimeSuggestion(6,  0, "6:00 AM",  "Morning energy",   "🏃"))
                suggestions.add(TimeSuggestion(18, 0, "6:00 PM",  "Evening workout",  "💪"))
            }
            lower.contains("meeting") || lower.contains("call") ||
            lower.contains("work")    || lower.contains("office") -> {
                suggestions.add(TimeSuggestion(10, 0,  "10:00 AM", "Peak focus time", "📞"))
                suggestions.add(TimeSuggestion(14, 0,  "2:00 PM",  "Post-lunch slot", "🤝"))
                suggestions.add(TimeSuggestion(9,  30, "9:30 AM",  "Morning standup", "📋"))
            }
            lower.contains("study") || lower.contains("read") ||
            lower.contains("homework") -> {
                suggestions.add(TimeSuggestion(9,  0, "9:00 AM",  "Morning focus",   "📚"))
                suggestions.add(TimeSuggestion(20, 0, "8:00 PM",  "Evening study",   "📖"))
            }
            lower.contains("water") || lower.contains("hydrat") -> {
                suggestions.add(TimeSuggestion(7,  0, "7:00 AM",  "Morning hydration","💧"))
                suggestions.add(TimeSuggestion(10, 0, "10:00 AM", "Mid-morning",     "💧"))
                suggestions.add(TimeSuggestion(14, 0, "2:00 PM",  "Afternoon",       "💧"))
            }
            lower.contains("sleep") || lower.contains("bed") -> {
                suggestions.add(TimeSuggestion(22, 0,  "10:00 PM", "Ideal bedtime",  "😴"))
                suggestions.add(TimeSuggestion(22, 30, "10:30 PM", "Late bedtime",   "🌙"))
            }
            else -> {
                val peakHour = findPeakHour(existingReminders)
                if (peakHour != null) {
                    val h    = if (peakHour > 12) peakHour - 12 else peakHour
                    val ampm = if (peakHour < 12) "AM" else "PM"
                    suggestions.add(TimeSuggestion(peakHour, 0, "$h:00 $ampm", "Your peak time", "⏰"))
                }
                suggestions.add(TimeSuggestion(9,  0, "9:00 AM",  "Morning",   "🌅"))
                suggestions.add(TimeSuggestion(13, 0, "1:00 PM",  "Afternoon", "☀️"))
                suggestions.add(TimeSuggestion(18, 0, "6:00 PM",  "Evening",   "🌆"))
            }
        }
        return suggestions.take(4)
    }

    private fun findPeakHour(reminders: List<Reminder>): Int? =
        reminders
            .filter { it.isCompleted }
            .groupBy {
                Calendar.getInstance()
                    .apply { timeInMillis = it.dateTimeMillis }
                    .get(Calendar.HOUR_OF_DAY)
            }
            .maxByOrNull { it.value.size }?.key
}
