package com.lumio.app.voice

import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.Reminder
import org.junit.Assert.*
import org.junit.Test

class SmartSuggestionsTest {

    @Test
    fun `empty reminders returns getting started suggestion`() {
        val suggestions = SmartSuggestions.generateSuggestions(emptyList())
        assertTrue("Should return at least 1 suggestion", suggestions.isNotEmpty())
        assertTrue("Should contain getting started message",
            suggestions.first().message.contains("first reminder", ignoreCase = true))
    }

    @Test
    fun `overdue reminders trigger warning suggestion`() {
        val reminders = listOf(
            Reminder(
                title          = "Past reminder",
                dateTimeMillis = System.currentTimeMillis() - 86_400_000L,
                isCompleted    = false
            )
        )
        val suggestions = SmartSuggestions.generateSuggestions(reminders)
        val hasOverdueWarning = suggestions.any {
            it.message.contains("overdue", ignoreCase = true)
        }
        assertTrue("Should warn about overdue reminders", hasOverdueWarning)
    }

    @Test
    fun `completed today reminders trigger streak suggestion`() {
        val reminders = listOf(
            Reminder(
                title          = "Done today",
                dateTimeMillis = System.currentTimeMillis(),
                isCompleted    = true
            )
        )
        val suggestions = SmartSuggestions.generateSuggestions(reminders)
        val hasStreak = suggestions.any {
            it.emoji == "🔥" || it.type == SmartSuggestions.SuggestionType.STREAK
        }
        assertTrue("Should show streak for completed today", hasStreak)
    }

    @Test
    fun `suggestions list max 4 items`() {
        val reminders = List(20) { i ->
            Reminder(
                id             = i.toLong(),
                title          = "Reminder $i",
                dateTimeMillis = System.currentTimeMillis() + i * 3_600_000L,
                isCompleted    = i % 2 == 0
            )
        }
        val suggestions = SmartSuggestions.generateSuggestions(reminders)
        assertTrue("Should not exceed 4 suggestions", suggestions.size <= 4)
    }

    @Test
    fun `all suggestions have non-empty messages`() {
        val reminders = listOf(
            Reminder(title = "Test", dateTimeMillis = System.currentTimeMillis() + 3_600_000L)
        )
        val suggestions = SmartSuggestions.generateSuggestions(reminders)
        suggestions.forEach { suggestion ->
            assertTrue("Suggestion message should not be empty",
                suggestion.message.isNotBlank())
        }
    }

    @Test
    fun `all suggestions have emoji`() {
        val reminders = listOf(
            Reminder(title = "Test", dateTimeMillis = System.currentTimeMillis() + 3_600_000L)
        )
        val suggestions = SmartSuggestions.generateSuggestions(reminders)
        suggestions.forEach { suggestion ->
            assertTrue("Suggestion should have emoji", suggestion.emoji.isNotBlank())
        }
    }

    @Test
    fun `health reminders trigger tip`() {
        val reminders = listOf(
            Reminder(
                title          = "Take medicine",
                dateTimeMillis = System.currentTimeMillis() + 3_600_000L
            )
        )
        val suggestions = SmartSuggestions.generateSuggestions(reminders)
        val hasMedicineTip = suggestions.any {
            it.message.contains("health", ignoreCase = true) ||
            it.emoji == "💊"
        }
        assertTrue("Should suggest tip for medicine reminders", hasMedicineTip)
    }
}
