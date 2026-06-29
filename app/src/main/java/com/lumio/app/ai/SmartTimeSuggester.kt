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
        val lower       = title.lowercase()
        val suggestions = mutableListOf<TimeSuggestion>()

        // ── Keyword-based suggestions ──────────────────
        when {
            lower.contains("medicine") || lower.contains("tablet") ||
            lower.contains("vitamin") || lower.contains("pill") -> {
                suggestions.add(TimeSuggestion(8,  0, "After Breakfast",  "Best time for medicine after breakfast",  "💊"))
                suggestions.add(TimeSuggestion(20, 0, "After Dinner",     "Best time for evening medicine",           "💊"))
                suggestions.add(TimeSuggestion(13, 0, "After Lunch",      "Midday medicine reminder",                 "💊"))
            }
            lower.contains("exercise") || lower.contains("gym") ||
            lower.contains("workout") || lower.contains("run") -> {
                suggestions.add(TimeSuggestion(6,  0, "Early Morning",    "Best energy for morning workout",          "🏃"))
                suggestions.add(TimeSuggestion(18, 0, "Evening",          "Great time for evening exercise",          "💪"))
                suggestions.add(TimeSuggestion(7,  0, "Morning",          "Start your day active",                    "🌅"))
            }
            lower.contains("meeting") || lower.contains("call") ||
            lower.contains("zoom") || lower.contains("conference") -> {
                suggestions.add(TimeSuggestion(10, 0,  "Mid Morning",     "Peak focus time for meetings",             "📞"))
                suggestions.add(TimeSuggestion(14, 0,  "Early Afternoon", "Good post-lunch meeting time",             "🤝"))
                suggestions.add(TimeSuggestion(9,  30, "Morning Standup", "Classic standup time",                     "📋"))
            }
            lower.contains("study") || lower.contains("read") ||
            lower.contains("homework") || lower.contains("learn") -> {
                suggestions.add(TimeSuggestion(9,  0, "Morning",          "Peak concentration in the morning",        "📚"))
                suggestions.add(TimeSuggestion(20, 0, "Evening",          "Quiet evening study session",              "📖"))
                suggestions.add(TimeSuggestion(15, 0, "Afternoon",        "Good afternoon study slot",                "✏️"))
            }
            lower.contains("water") || lower.contains("hydrat") -> {
                suggestions.add(TimeSuggestion(7,  0,  "Morning",         "Start day hydrated",                      "💧"))
                suggestions.add(TimeSuggestion(10, 0,  "Mid Morning",     "Stay hydrated at work",                   "💧"))
                suggestions.add(TimeSuggestion(14, 0,  "Afternoon",       "Afternoon hydration",                     "💧"))
                suggestions.add(TimeSuggestion(16, 30, "Late Afternoon",  "Pre-evening hydration",                   "💧"))
            }
            lower.contains("birthday") || lower.contains("anniversary") -> {
                suggestions.add(TimeSuggestion(9,  0, "Morning",          "Wish them early in the day!",              "🎂"))
                suggestions.add(TimeSuggestion(8,  0, "Early Morning",    "First thing in the morning",               "🎉"))
            }
            lower.contains("bill") || lower.contains("payment") ||
            lower.contains("pay") -> {
                suggestions.add(TimeSuggestion(10, 0, "Morning",          "Pay bills when banks are open",            "💳"))
                suggestions.add(TimeSuggestion(11, 0, "Late Morning",     "Good time to handle payments",             "💰"))
            }
            lower.contains("sleep") || lower.contains("bed") -> {
                suggestions.add(TimeSuggestion(22, 0,  "10 PM",           "Ideal bedtime for 7-8 hours sleep",       "😴"))
                suggestions.add(TimeSuggestion(22, 30, "10:30 PM",        "Slightly later bedtime",                  "🌙"))
                suggestions.add(TimeSuggestion(23, 0,  "11 PM",           "Late bedtime",                            "⭐"))
            }
            else -> {
                // Pattern-based suggestions from existing reminders
                val peakHour = findPeakCompletionHour(existingReminders)
                if (peakHour != null) {
                    suggestions.add(TimeSuggestion(peakHour, 0,
                        "Your peak time",
                        "You usually complete reminders around this time",
                        "⏰"))
                }
                suggestions.add(TimeSuggestion(9,  0, "Morning",   "Start your day with this task",    "🌅"))
                suggestions.add(TimeSuggestion(13, 0, "Afternoon", "Good afternoon reminder",           "☀️"))
                suggestions.add(TimeSuggestion(18, 0, "Evening",   "Evening reminder",                  "🌆"))
            }
        }

        return suggestions.take(4)
    }

    private fun findPeakCompletionHour(reminders: List<Reminder>): Int? {
        val hourCounts = reminders
            .filter { it.isCompleted }
            .groupBy { r ->
                Calendar.getInstance().apply {
                    timeInMillis = r.dateTimeMillis
                }.get(Calendar.HOUR_OF_DAY)
            }
        return hourCounts.maxByOrNull { it.value.size }?.key
    }
}
