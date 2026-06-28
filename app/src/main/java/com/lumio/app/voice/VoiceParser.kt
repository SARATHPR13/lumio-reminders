package com.lumio.app.voice

import com.lumio.app.domain.model.Category
import com.lumio.app.domain.model.Priority

/**
 * Parses voice text into structured reminder data
 * Example: "Remind me to call Rahul tomorrow at 6 PM"
 * Output: title="Call Rahul", date=tomorrow, time=6PM
 */
object VoiceParser {

    data class ParsedReminder(
        val title: String,
        val dateTimeMillis: Long,
        val dateDescription: String,
        val timeDescription: String,
        val priority: Priority,
        val suggestedCategory: Category?,
        val confidence: Float,
        val originalText: String
    )

    fun parse(voiceText: String): ParsedReminder {
        val text     = voiceText.trim()
        val lower    = text.lowercase()

        // Parse date/time
        val dateTime = DateTimeParser.parse(text)

        // Extract title
        val title    = DateTimeParser.extractTitle(text)

        // Detect priority from keywords
        val priority = detectPriority(lower)

        // Suggest category from keywords
        val category = suggestCategory(lower)

        return ParsedReminder(
            title              = title,
            dateTimeMillis     = dateTime.millis,
            dateDescription    = dateTime.dateDescription,
            timeDescription    = dateTime.timeDescription,
            priority           = priority,
            suggestedCategory  = category,
            confidence         = dateTime.confidence,
            originalText       = text
        )
    }

    private fun detectPriority(text: String): Priority = when {
        text.contains("urgent") || text.contains("immediately") ||
        text.contains("asap") || text.contains("emergency")     -> Priority.URGENT

        text.contains("important") || text.contains("critical") ||
        text.contains("must")                                    -> Priority.HIGH

        text.contains("soon") || text.contains("should")        -> Priority.MEDIUM

        else                                                     -> Priority.NONE
    }

    private fun suggestCategory(text: String): Category? = when {
        text.contains("meeting") || text.contains("work") ||
        text.contains("office") || text.contains("project") ||
        text.contains("deadline") || text.contains("email") ||
        text.contains("call") && text.contains("work")           -> Category.defaults[0]  // Work

        text.contains("home") || text.contains("house") ||
        text.contains("clean") || text.contains("cook")          -> Category.defaults[1]  // Home

        text.contains("buy") || text.contains("shop") ||
        text.contains("purchase") || text.contains("grocery") ||
        text.contains("market")                                  -> Category.defaults[2]  // Shopping

        text.contains("bill") || text.contains("pay") ||
        text.contains("payment") || text.contains("rent") ||
        text.contains("electricity") || text.contains("water")   -> Category.defaults[3]  // Bills

        text.contains("study") || text.contains("exam") ||
        text.contains("class") || text.contains("homework") ||
        text.contains("assignment") || text.contains("college")  -> Category.defaults[4]  // Study

        text.contains("medicine") || text.contains("doctor") ||
        text.contains("hospital") || text.contains("gym") ||
        text.contains("exercise") || text.contains("tablet") ||
        text.contains("vitamin") || text.contains("health")      -> Category.defaults[5]  // Health

        text.contains("birthday") || text.contains("party") ||
        text.contains("anniversary") || text.contains("celebrate")-> Category.defaults[6] // Birthday

        text.contains("travel") || text.contains("flight") ||
        text.contains("trip") || text.contains("hotel") ||
        text.contains("ticket") || text.contains("train")        -> Category.defaults[8]  // Travel

        else                                                      -> null
    }
}
