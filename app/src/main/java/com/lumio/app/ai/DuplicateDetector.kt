package com.lumio.app.ai

import com.lumio.app.domain.model.Reminder
import java.util.Calendar

data class DuplicateResult(
    val isDuplicate: Boolean,
    val similarReminder: Reminder?,
    val similarityScore: Float,
    val reason: String
)

object DuplicateDetector {

    fun checkDuplicate(
        newReminder: Reminder,
        existingReminders: List<Reminder>
    ): DuplicateResult {
        var bestMatch: Reminder? = null
        var bestScore = 0f
        var bestReason = ""

        existingReminders
            .filter { !it.isCompleted }
            .forEach { existing ->
                val titleScore = titleSimilarity(
                    newReminder.title.lowercase(),
                    existing.title.lowercase()
                )
                val timeScore = timeSimilarity(
                    newReminder.dateTimeMillis,
                    existing.dateTimeMillis
                )
                val combinedScore = (titleScore * 0.7f) + (timeScore * 0.3f)

                if (combinedScore > bestScore) {
                    bestScore  = combinedScore
                    bestMatch  = existing
                    bestReason = when {
                        titleScore > 0.85f && timeScore > 0.8f ->
                            "Very similar title and time to an existing reminder"
                        titleScore > 0.85f ->
                            "Very similar title to an existing reminder"
                        timeScore > 0.9f ->
                            "Scheduled at nearly the same time as another reminder"
                        else ->
                            "Similar to an existing reminder"
                    }
                }
            }

        val isDuplicate = bestScore > 0.75f

        return DuplicateResult(
            isDuplicate     = isDuplicate,
            similarReminder = if (isDuplicate) bestMatch else null,
            similarityScore = bestScore,
            reason          = if (isDuplicate) bestReason else ""
        )
    }

    // Calculates how similar two strings are (0.0 = different, 1.0 = identical)
    private fun titleSimilarity(a: String, b: String): Float {
        if (a == b) return 1.0f
        if (a.isBlank() || b.isBlank()) return 0f

        // Check if one contains the other
        if (a.contains(b) || b.contains(a)) return 0.9f

        // Word overlap
        val wordsA = a.split(" ").filter { it.length > 2 }.toSet()
        val wordsB = b.split(" ").filter { it.length > 2 }.toSet()

        if (wordsA.isEmpty() || wordsB.isEmpty()) return 0f

        val intersection = wordsA.intersect(wordsB).size
        val union        = wordsA.union(wordsB).size

        return if (union == 0) 0f else intersection.toFloat() / union.toFloat()
    }

    // Checks if two times are within 30 minutes of each other
    private fun timeSimilarity(timeA: Long, timeB: Long): Float {
        val calA  = Calendar.getInstance().apply { timeInMillis = timeA }
        val calB  = Calendar.getInstance().apply { timeInMillis = timeB }
        val sameDay = calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR) &&
                      calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR)

        if (!sameDay) return 0f

        val diffMinutes = Math.abs(timeA - timeB) / 60_000L
        return when {
            diffMinutes == 0L -> 1.0f
            diffMinutes <= 5  -> 0.95f
            diffMinutes <= 15 -> 0.85f
            diffMinutes <= 30 -> 0.7f
            diffMinutes <= 60 -> 0.4f
            else              -> 0f
        }
    }
}
