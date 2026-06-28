package com.lumio.app.voice

import com.lumio.app.domain.model.Reminder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * LUMIO Smart Suggestions Engine
 * Analyzes reminder patterns and suggests better times
 */
object SmartSuggestions {

    data class Suggestion(
        val message: String,
        val emoji: String,
        val type: SuggestionType
    )

    enum class SuggestionType {
        PATTERN, TIMING, STREAK, TIP
    }

    fun generateSuggestions(reminders: List<Reminder>): List<Suggestion> {
        val suggestions = mutableListOf<Suggestion>()

        if (reminders.isEmpty()) {
            suggestions.add(
                Suggestion(
                    "Add your first reminder to get started! 🚀",
                    "🚀",
                    SuggestionType.TIP
                )
            )
            return suggestions
        }

        // ── Pattern: Detect common reminder times ─────
        val completedByHour = reminders
            .filter { it.isCompleted }
            .groupBy { r ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = r.dateTimeMillis
                cal.get(Calendar.HOUR_OF_DAY)
            }

        val peakHour = completedByHour.maxByOrNull { it.value.size }?.key
        if (peakHour != null && completedByHour[peakHour]!!.size >= 2) {
            val timeStr = if (peakHour < 12) "${peakHour} AM"
                          else if (peakHour == 12) "12 PM"
                          else "${peakHour - 12} PM"
            suggestions.add(
                Suggestion(
                    "You complete most reminders around $timeStr — great time to schedule!",
                    "⏰",
                    SuggestionType.PATTERN
                )
            )
        }

        // ── Pattern: Detect overdue reminders ─────────
        val overdueCount = reminders.count { it.isOverdue }
        if (overdueCount > 0) {
            suggestions.add(
                Suggestion(
                    "You have $overdueCount overdue reminder${if (overdueCount > 1) "s" else ""}. Consider rescheduling!",
                    "⚠️",
                    SuggestionType.TIMING
                )
            )
        }

        // ── Pattern: Completion streak ────────────────
        val completedToday = reminders.count { it.isCompleted && it.isToday }
        if (completedToday > 0) {
            suggestions.add(
                Suggestion(
                    "You've completed $completedToday reminder${if (completedToday > 1) "s" else ""} today. Keep it up! 🔥",
                    "🔥",
                    SuggestionType.STREAK
                )
            )
        }

        // ── Pattern: Medicine/Health reminders ────────
        val healthReminders = reminders.filter {
            it.title.lowercase().contains("medicine") ||
            it.title.lowercase().contains("tablet") ||
            it.title.lowercase().contains("vitamin") ||
            it.category?.name == "Health"
        }
        if (healthReminders.isNotEmpty()) {
            suggestions.add(
                Suggestion(
                    "Tip: Set health reminders as Daily repeat so you never miss them 💊",
                    "💊",
                    SuggestionType.TIP
                )
            )
        }

        // ── Pattern: Evening reminders ────────────────
        val eveningReminders = reminders.filter { r ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = r.dateTimeMillis
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            hour in 18..23 && !r.isCompleted
        }
        if (eveningReminders.isNotEmpty()) {
            suggestions.add(
                Suggestion(
                    "You have ${eveningReminders.size} evening reminder${if (eveningReminders.size > 1) "s" else ""} today 🌙",
                    "🌙",
                    SuggestionType.TIMING
                )
            )
        }

        // ── General tips ──────────────────────────────
        val tips = listOf(
            Suggestion("Use 🎙️ voice input to add reminders instantly!", "🎙️", SuggestionType.TIP),
            Suggestion("Set Priority to Urgent for time-sensitive tasks 🔴", "🔴", SuggestionType.TIP),
            Suggestion("Use categories to organize your reminders better 📂", "📂", SuggestionType.TIP),
        )
        if (suggestions.size < 3) {
            suggestions.add(tips.random())
        }

        return suggestions.take(4)
    }
}
