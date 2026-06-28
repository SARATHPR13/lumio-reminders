package com.lumio.app.voice

import java.util.Calendar

/**
 * Parses natural language date/time expressions
 * Examples:
 * "tomorrow at 6 PM"
 * "next Monday at 9 AM"
 * "today at 3:30 PM"
 * "in 2 hours"
 * "next week"
 */
object DateTimeParser {

    data class ParsedDateTime(
        val millis: Long,
        val dateDescription: String,
        val timeDescription: String,
        val confidence: Float
    )

    fun parse(text: String): ParsedDateTime {
        val lower = text.lowercase().trim()
        val cal   = Calendar.getInstance()
        var dateConfidence = 0.5f
        var timeConfidence = 0.5f

        // ── Parse Date ────────────────────────────────
        when {
            lower.contains("today") -> {
                // Keep today's date
                dateConfidence = 1.0f
            }
            lower.contains("tomorrow") -> {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                dateConfidence = 1.0f
            }
            lower.contains("day after tomorrow") -> {
                cal.add(Calendar.DAY_OF_YEAR, 2)
                dateConfidence = 1.0f
            }
            lower.contains("next monday") || lower.contains("next mon") -> {
                setNextWeekday(cal, Calendar.MONDAY)
                dateConfidence = 1.0f
            }
            lower.contains("next tuesday") || lower.contains("next tue") -> {
                setNextWeekday(cal, Calendar.TUESDAY)
                dateConfidence = 1.0f
            }
            lower.contains("next wednesday") || lower.contains("next wed") -> {
                setNextWeekday(cal, Calendar.WEDNESDAY)
                dateConfidence = 1.0f
            }
            lower.contains("next thursday") || lower.contains("next thu") -> {
                setNextWeekday(cal, Calendar.THURSDAY)
                dateConfidence = 1.0f
            }
            lower.contains("next friday") || lower.contains("next fri") -> {
                setNextWeekday(cal, Calendar.FRIDAY)
                dateConfidence = 1.0f
            }
            lower.contains("next saturday") || lower.contains("next sat") -> {
                setNextWeekday(cal, Calendar.SATURDAY)
                dateConfidence = 1.0f
            }
            lower.contains("next sunday") || lower.contains("next sun") -> {
                setNextWeekday(cal, Calendar.SUNDAY)
                dateConfidence = 1.0f
            }
            lower.contains("next week") -> {
                cal.add(Calendar.WEEK_OF_YEAR, 1)
                dateConfidence = 0.8f
            }
            lower.contains("next month") -> {
                cal.add(Calendar.MONTH, 1)
                dateConfidence = 0.8f
            }
            lower.contains("in \\d+ day".toRegex()) -> {
                val days = extractNumber(lower, "day")
                if (days > 0) {
                    cal.add(Calendar.DAY_OF_YEAR, days)
                    dateConfidence = 1.0f
                }
            }
            lower.contains("in \\d+ week".toRegex()) -> {
                val weeks = extractNumber(lower, "week")
                if (weeks > 0) {
                    cal.add(Calendar.WEEK_OF_YEAR, weeks)
                    dateConfidence = 1.0f
                }
            }
            lower.contains("monday") -> { setNextWeekday(cal, Calendar.MONDAY);    dateConfidence = 0.9f }
            lower.contains("tuesday") -> { setNextWeekday(cal, Calendar.TUESDAY);  dateConfidence = 0.9f }
            lower.contains("wednesday") -> { setNextWeekday(cal, Calendar.WEDNESDAY); dateConfidence = 0.9f }
            lower.contains("thursday") -> { setNextWeekday(cal, Calendar.THURSDAY); dateConfidence = 0.9f }
            lower.contains("friday") -> { setNextWeekday(cal, Calendar.FRIDAY);    dateConfidence = 0.9f }
            lower.contains("saturday") -> { setNextWeekday(cal, Calendar.SATURDAY); dateConfidence = 0.9f }
            lower.contains("sunday") -> { setNextWeekday(cal, Calendar.SUNDAY);    dateConfidence = 0.9f }
        }

        // ── Parse Time ────────────────────────────────
        val timeResult = parseTime(lower, cal)
        if (timeResult) timeConfidence = 1.0f

        // ── Handle relative time ──────────────────────
        when {
            lower.contains("in \\d+ hour".toRegex()) -> {
                val hours = extractNumber(lower, "hour")
                if (hours > 0) {
                    cal.add(Calendar.HOUR_OF_DAY, hours)
                    timeConfidence = 1.0f
                }
            }
            lower.contains("in \\d+ minute".toRegex()) -> {
                val mins = extractNumber(lower, "minute")
                if (mins > 0) {
                    cal.add(Calendar.MINUTE, mins)
                    timeConfidence = 1.0f
                }
            }
            lower.contains("morning") && !timeResult -> {
                cal.set(Calendar.HOUR_OF_DAY, 9)
                cal.set(Calendar.MINUTE, 0)
                timeConfidence = 0.8f
            }
            lower.contains("afternoon") && !timeResult -> {
                cal.set(Calendar.HOUR_OF_DAY, 14)
                cal.set(Calendar.MINUTE, 0)
                timeConfidence = 0.8f
            }
            lower.contains("evening") && !timeResult -> {
                cal.set(Calendar.HOUR_OF_DAY, 18)
                cal.set(Calendar.MINUTE, 0)
                timeConfidence = 0.8f
            }
            lower.contains("night") && !timeResult -> {
                cal.set(Calendar.HOUR_OF_DAY, 21)
                cal.set(Calendar.MINUTE, 0)
                timeConfidence = 0.8f
            }
            lower.contains("midnight") && !timeResult -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                timeConfidence = 1.0f
            }
            lower.contains("noon") && !timeResult -> {
                cal.set(Calendar.HOUR_OF_DAY, 12)
                cal.set(Calendar.MINUTE, 0)
                timeConfidence = 1.0f
            }
        }

        // Reset seconds
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // If time is in past, push to tomorrow
        if (cal.timeInMillis < System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val dateDesc = formatDateDescription(cal)
        val timeDesc = formatTimeDescription(cal)

        return ParsedDateTime(
            millis           = cal.timeInMillis,
            dateDescription  = dateDesc,
            timeDescription  = timeDesc,
            confidence       = (dateConfidence + timeConfidence) / 2f
        )
    }

    /**
     * Extracts title from voice text by removing date/time phrases
     */
    fun extractTitle(text: String): String {
        var title = text

        // Remove common voice prefixes
        val prefixes = listOf(
            "remind me to ", "remind me ", "set a reminder to ",
            "set reminder to ", "add reminder to ", "remember to ",
            "don't forget to ", "alert me to "
        )
        for (prefix in prefixes) {
            if (title.lowercase().startsWith(prefix)) {
                title = title.substring(prefix.length)
                break
            }
        }

        // Remove date/time phrases
        val removals = listOf(
            "\\s*tomorrow\\s*".toRegex(RegexOption.IGNORE_CASE),
            "\\s*today\\s*".toRegex(RegexOption.IGNORE_CASE),
            "\\s*at \\d{1,2}(:\\d{2})? ?(am|pm)\\s*".toRegex(RegexOption.IGNORE_CASE),
            "\\s*at \\d{1,2} (am|pm)\\s*".toRegex(RegexOption.IGNORE_CASE),
            "\\s*next (monday|tuesday|wednesday|thursday|friday|saturday|sunday|week|month)\\s*".toRegex(RegexOption.IGNORE_CASE),
            "\\s*on (monday|tuesday|wednesday|thursday|friday|saturday|sunday)\\s*".toRegex(RegexOption.IGNORE_CASE),
            "\\s*in \\d+ (hour|minute|day|week)s?\\s*".toRegex(RegexOption.IGNORE_CASE),
            "\\s*(this )?(morning|afternoon|evening|night|noon|midnight)\\s*".toRegex(RegexOption.IGNORE_CASE),
        )

        for (regex in removals) {
            title = title.replace(regex, " ")
        }

        return title.trim()
            .replace("\\s+".toRegex(), " ")
            .replaceFirstChar { it.uppercase() }
            .ifBlank { "New Reminder" }
    }

    // ── Private Helpers ───────────────────────────────

    private fun setNextWeekday(cal: Calendar, weekday: Int) {
        val today = cal.get(Calendar.DAY_OF_WEEK)
        var daysUntil = weekday - today
        if (daysUntil <= 0) daysUntil += 7
        cal.add(Calendar.DAY_OF_YEAR, daysUntil)
    }

    private fun parseTime(text: String, cal: Calendar): Boolean {
        // Pattern: "at 6:30 PM", "at 6 PM", "at 18:30"
        val timePattern = Regex(
            "at (\\d{1,2})(?::(\\d{2}))? ?(am|pm|AM|PM)?",
            RegexOption.IGNORE_CASE
        )
        val match = timePattern.find(text) ?: return false

        var hour   = match.groupValues[1].toIntOrNull() ?: return false
        val minute = match.groupValues[2].toIntOrNull() ?: 0
        val ampm   = match.groupValues[3].lowercase()

        when {
            ampm == "pm" && hour != 12 -> hour += 12
            ampm == "am" && hour == 12 -> hour = 0
            ampm.isEmpty() && hour < 7 -> hour += 12  // Assume PM for ambiguous times
        }

        if (hour > 23 || minute > 59) return false

        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE,      minute)
        return true
    }

    private fun extractNumber(text: String, unit: String): Int {
        val pattern = Regex("in (\\d+) $unit", RegexOption.IGNORE_CASE)
        return pattern.find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }

    private fun formatDateDescription(cal: Calendar): String {
        val today = Calendar.getInstance()
        val diff  = ((cal.timeInMillis - today.timeInMillis) / 86_400_000).toInt()
        return when (diff) {
            0    -> "Today"
            1    -> "Tomorrow"
            else -> {
                val days = arrayOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")
                val months = arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
                "${days[cal.get(Calendar.DAY_OF_WEEK)-1]}, ${months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}"
            }
        }
    }

    private fun formatTimeDescription(cal: Calendar): String {
        val hour   = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val ampm   = if (hour < 12) "AM" else "PM"
        val h      = if (hour % 12 == 0) 12 else hour % 12
        return "$h:${minute.toString().padStart(2, '0')} $ampm"
    }
}
