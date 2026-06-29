package com.lumio.app.ai

import com.lumio.app.domain.model.Category
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.RepeatType
import com.lumio.app.voice.DateTimeParser
import java.util.Calendar

object NaturalLanguageProcessor {

    fun process(input: String): AiParseResult {
        val lower = input.lowercase().trim()

        if (isGreeting(lower)) {
            return AiParseResult(understood = true, response = getGreetingResponse())
        }
        if (lower.contains("help") || lower.contains("what can you do")) {
            return AiParseResult(understood = true, response = getHelpResponse())
        }
        if (lower.contains("show") || lower.contains("list my reminder")) {
            return AiParseResult(
                understood = true,
                response   = "Go to the Home screen to see all your reminders. Want me to create a new one?"
            )
        }
        return parseAsReminder(input, lower)
    }

    private fun parseAsReminder(original: String, lower: String): AiParseResult {
        val repeatType = detectRepeatType(lower)
        val priority   = detectPriority(lower)
        val category   = detectCategory(lower)
        val title      = extractTitle(original, lower)

        if (title.isBlank() || title.length < 2) {
            return AiParseResult(
                understood = false,
                response   = "I am not sure what you want to be reminded about.\n\nTry: \"Remind me to call mom tomorrow at 5 PM\""
            )
        }

        val dateTime   = DateTimeParser.parse(original)
        val confidence = (dateTime.confidence +
            (if (repeatType != RepeatType.NONE) 0.1f else 0f) +
            (if (title.length > 3) 0.1f else 0f) +
            (if (lower.contains("remind me")) 0.1f else 0f)
        ).coerceIn(0f, 1f)

        val dateDesc = when (repeatType) {
            RepeatType.DAILY   -> "Every Day"
            RepeatType.WEEKLY  -> "Every Week"
            RepeatType.MONTHLY -> "Every Month"
            RepeatType.YEARLY  -> "Every Year"
            else               -> dateTime.dateDescription
        }

        val suggested = SuggestedReminder(
            title           = title,
            description     = buildDesc(repeatType, category),
            dateTimeMillis  = dateTime.millis,
            priority        = priority,
            category        = category,
            repeatType      = repeatType,
            dateDescription = dateDesc,
            timeDescription = dateTime.timeDescription,
            confidence      = confidence
        )

        val repeatText = if (repeatType != RepeatType.NONE) "\nRepeats: ${repeatType.label}" else ""
        val catText    = category?.let { "\n${it.emoji} ${it.name}" } ?: ""
        val prioText   = if (priority != Priority.NONE) "\n${priority.label} Priority" else ""

        val response = "Got it!\n\n${title}\n${dateDesc}\n${dateTime.timeDescription}$repeatText$catText$prioText\n\nShall I save this reminder?"

        return AiParseResult(
            understood        = true,
            response          = response,
            suggestedReminder = suggested
        )
    }

    fun detectRepeatType(lower: String): RepeatType = when {
        lower.contains("every day") || lower.contains("daily") ||
        lower.contains("each day")  || lower.contains("everyday") -> RepeatType.DAILY

        lower.contains("every week")      || lower.contains("weekly")    ||
        lower.contains("every monday")    || lower.contains("every tuesday") ||
        lower.contains("every wednesday") || lower.contains("every thursday") ||
        lower.contains("every friday")    || lower.contains("every saturday") ||
        lower.contains("every sunday")    || lower.contains("last friday") ||
        lower.contains("every weekday")   -> RepeatType.WEEKLY

        lower.contains("every month") || lower.contains("monthly") -> RepeatType.MONTHLY
        lower.contains("every year")  || lower.contains("yearly")  -> RepeatType.YEARLY

        lower.contains("every") && (lower.contains("hour") ||
        lower.contains("2 week") || lower.contains("3 day")) -> RepeatType.CUSTOM

        else -> RepeatType.NONE
    }

    fun detectPriority(lower: String): Priority = when {
        lower.contains("urgent")   || lower.contains("asap") ||
        lower.contains("emergency") -> Priority.URGENT
        lower.contains("important") || lower.contains("critical") ||
        lower.contains("high priority") -> Priority.HIGH
        lower.contains("low priority")  || lower.contains("whenever") -> Priority.LOW
        else -> Priority.NONE
    }

    fun detectCategory(lower: String): Category? = when {
        lower.contains("meeting") || lower.contains("work") ||
        lower.contains("office")  || lower.contains("deadline") -> Category.defaults.getOrNull(0)

        lower.contains("home")  || lower.contains("house") ||
        lower.contains("clean") || lower.contains("cook")  -> Category.defaults.getOrNull(1)

        lower.contains("buy")     || lower.contains("shop") ||
        lower.contains("grocery") || lower.contains("market") -> Category.defaults.getOrNull(2)

        lower.contains("bill") || lower.contains("pay") ||
        lower.contains("rent") || lower.contains("emi")  -> Category.defaults.getOrNull(3)

        lower.contains("study")    || lower.contains("exam") ||
        lower.contains("class")    || lower.contains("homework") -> Category.defaults.getOrNull(4)

        lower.contains("medicine") || lower.contains("doctor") ||
        lower.contains("gym")      || lower.contains("exercise") ||
        lower.contains("tablet")   || lower.contains("vitamin") -> Category.defaults.getOrNull(5)

        lower.contains("birthday") || lower.contains("anniversary") ||
        lower.contains("party")    -> Category.defaults.getOrNull(6)

        lower.contains("call")   || lower.contains("message") ||
        lower.contains("friend") || lower.contains("family") -> Category.defaults.getOrNull(7)

        lower.contains("travel") || lower.contains("flight") ||
        lower.contains("trip")   || lower.contains("ticket") -> Category.defaults.getOrNull(8)

        lower.contains("money")  || lower.contains("bank") ||
        lower.contains("invest") -> Category.defaults.getOrNull(9)

        else -> null
    }

    private fun extractTitle(original: String, lower: String): String {
        var title = original
        val prefixes = listOf(
            "remind me to ", "remind me about ", "remind me ",
            "set a reminder to ", "set reminder to ",
            "remember to ", "please remind me to ",
            "can you remind me to ", "add reminder to "
        )
        for (prefix in prefixes) {
            if (lower.startsWith(prefix)) {
                title = original.substring(prefix.length)
                break
            }
        }
        val removes = listOf(
            "every day","everyday","daily","every week","weekly",
            "every month","monthly","every year","yearly",
            "every monday","every tuesday","every wednesday",
            "every thursday","every friday","every saturday",
            "every sunday","every weekday","last friday"
        )
        for (r in removes) {
            title = title.replace(r, "", ignoreCase = true)
        }
        val regexes = listOf(
            "\\s*tomorrow\\s*".toRegex(RegexOption.IGNORE_CASE),
            "\\s*today\\s*".toRegex(RegexOption.IGNORE_CASE),
            "\\s*at \\d{1,2}(:\\d{2})? ?(am|pm)\\s*".toRegex(RegexOption.IGNORE_CASE),
            "\\s*next (monday|tuesday|wednesday|thursday|friday|week|month)\\s*".toRegex(RegexOption.IGNORE_CASE),
            "\\s*(morning|afternoon|evening|night|noon)\\s*".toRegex(RegexOption.IGNORE_CASE),
            "\\s*(urgent|important|asap|high priority|low priority)\\s*".toRegex(RegexOption.IGNORE_CASE)
        )
        for (rx in regexes) {
            title = title.replace(rx, " ")
        }
        return title.trim()
            .replace("\\s+".toRegex(), " ")
            .replaceFirstChar { it.uppercase() }
            .ifBlank { "New Reminder" }
    }

    private fun buildDesc(repeat: RepeatType, cat: Category?): String {
        val parts = mutableListOf<String>()
        cat?.let { parts.add("${it.emoji} ${it.name}") }
        if (repeat != RepeatType.NONE) parts.add("Repeats: ${repeat.label}")
        parts.add("Created by AI")
        return parts.joinToString(" | ")
    }

    private fun isGreeting(lower: String): Boolean =
        lower in listOf("hi","hello","hey","good morning","good afternoon",
            "good evening","howdy","namaste","hai") ||
        lower.startsWith("hi ") || lower.startsWith("hello ") || lower.startsWith("hey ")

    private fun getGreetingResponse(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val g = when (hour) {
            in 0..11  -> "Good morning!"
            in 12..16 -> "Good afternoon!"
            in 17..20 -> "Good evening!"
            else      -> "Good night!"
        }
        return "$g I am LUMIO AI, your smart reminder assistant!\n\nTry saying:\n\"Remind me to call mom tomorrow at 5 PM\"\n\"Every Monday at 9 AM, team meeting\"\n\"Take medicine daily at 8 AM\""
    }

    private fun getHelpResponse(): String =
        "Here is what I can do:\n\nCreate reminders with natural language\nSet recurring reminders (daily, weekly, monthly)\nAuto-categorize your reminders\nDetect priority (urgent, important)\nDetect duplicate reminders\nSuggest best times\n\nExample:\n\"Remind me every Friday at 5 PM to submit report\""
}
