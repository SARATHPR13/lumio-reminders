package com.lumio.app.health

import com.lumio.app.domain.model.Category
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.model.RepeatType
import java.util.Calendar

data class HealthTemplate(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val defaultHour: Int,
    val defaultMinute: Int,
    val repeatType: RepeatType,
    val priority: Priority,
    val category: String
)

object HealthTemplates {

    val healthCategory = Category(
        id       = 6L,
        name     = "Health",
        emoji    = "💊",
        colorHex = "#FF00897B"
    )

    val templates = listOf(
        HealthTemplate(
            id          = "morning_medicine",
            title       = "Morning Medicine",
            description = "Take your prescribed morning medication",
            emoji       = "💊",
            defaultHour = 8, defaultMinute = 0,
            repeatType  = RepeatType.DAILY,
            priority    = Priority.HIGH,
            category    = "Health"
        ),
        HealthTemplate(
            id          = "evening_medicine",
            title       = "Evening Medicine",
            description = "Take your prescribed evening medication",
            emoji       = "💊",
            defaultHour = 20, defaultMinute = 0,
            repeatType  = RepeatType.DAILY,
            priority    = Priority.HIGH,
            category    = "Health"
        ),
        HealthTemplate(
            id          = "drink_water_morning",
            title       = "Drink Water",
            description = "Start your day with 2 glasses of water 💧",
            emoji       = "💧",
            defaultHour = 7, defaultMinute = 0,
            repeatType  = RepeatType.DAILY,
            priority    = Priority.MEDIUM,
            category    = "Health"
        ),
        HealthTemplate(
            id          = "drink_water_noon",
            title       = "Stay Hydrated",
            description = "Drink a glass of water — stay hydrated! 💧",
            emoji       = "💧",
            defaultHour = 13, defaultMinute = 0,
            repeatType  = RepeatType.DAILY,
            priority    = Priority.LOW,
            category    = "Health"
        ),
        HealthTemplate(
            id          = "morning_walk",
            title       = "Morning Walk",
            description = "30-minute morning walk for a healthy start 🚶",
            emoji       = "🚶",
            defaultHour = 6, defaultMinute = 30,
            repeatType  = RepeatType.DAILY,
            priority    = Priority.MEDIUM,
            category    = "Health"
        ),
        HealthTemplate(
            id          = "gym_workout",
            title       = "Gym Workout",
            description = "Time for your workout session! 💪",
            emoji       = "💪",
            defaultHour = 6, defaultMinute = 0,
            repeatType  = RepeatType.DAILY,
            priority    = Priority.MEDIUM,
            category    = "Health"
        ),
        HealthTemplate(
            id          = "meditation",
            title       = "Morning Meditation",
            description = "10 minutes of mindfulness meditation 🧘",
            emoji       = "🧘",
            defaultHour = 7, defaultMinute = 0,
            repeatType  = RepeatType.DAILY,
            priority    = Priority.MEDIUM,
            category    = "Health"
        ),
        HealthTemplate(
            id          = "eye_rest",
            title       = "Eye Rest (20-20-20)",
            description = "Look 20 feet away for 20 seconds — eye rest rule 👁️",
            emoji       = "👁️",
            defaultHour = 10, defaultMinute = 0,
            repeatType  = RepeatType.DAILY,
            priority    = Priority.LOW,
            category    = "Health"
        ),
        HealthTemplate(
            id          = "brush_teeth",
            title       = "Brush Teeth",
            description = "Brush your teeth for 2 minutes 🦷",
            emoji       = "🦷",
            defaultHour = 21, defaultMinute = 30,
            repeatType  = RepeatType.DAILY,
            priority    = Priority.MEDIUM,
            category    = "Health"
        ),
        HealthTemplate(
            id          = "bedtime",
            title       = "Bedtime",
            description = "Time to sleep — get 7-8 hours of rest 😴",
            emoji       = "😴",
            defaultHour = 22, defaultMinute = 30,
            repeatType  = RepeatType.DAILY,
            priority    = Priority.MEDIUM,
            category    = "Health"
        ),
        HealthTemplate(
            id          = "vitamins",
            title       = "Take Vitamins",
            description = "Daily vitamin D, B12 and iron supplements 🌟",
            emoji       = "🌟",
            defaultHour = 9, defaultMinute = 0,
            repeatType  = RepeatType.DAILY,
            priority    = Priority.MEDIUM,
            category    = "Health"
        ),
        HealthTemplate(
            id          = "blood_pressure",
            title       = "Check Blood Pressure",
            description = "Monitor your blood pressure readings 🩺",
            emoji       = "🩺",
            defaultHour = 8, defaultMinute = 0,
            repeatType  = RepeatType.DAILY,
            priority    = Priority.HIGH,
            category    = "Health"
        ),
        HealthTemplate(
            id          = "sugar_check",
            title       = "Check Blood Sugar",
            description = "Measure your blood sugar levels 🩸",
            emoji       = "🩸",
            defaultHour = 7, defaultMinute = 30,
            repeatType  = RepeatType.DAILY,
            priority    = Priority.HIGH,
            category    = "Health"
        ),
        HealthTemplate(
            id          = "lunch_break",
            title       = "Lunch Break",
            description = "Take a proper lunch break — no eating at your desk! 🥗",
            emoji       = "🥗",
            defaultHour = 13, defaultMinute = 0,
            repeatType  = RepeatType.DAILY,
            priority    = Priority.LOW,
            category    = "Health"
        ),
        HealthTemplate(
            id          = "stretch_break",
            title       = "Stretch Break",
            description = "Stand up and stretch for 5 minutes 🤸",
            emoji       = "🤸",
            defaultHour = 15, defaultMinute = 0,
            repeatType  = RepeatType.DAILY,
            priority    = Priority.LOW,
            category    = "Health"
        )
    )

    fun buildReminder(template: HealthTemplate): Reminder {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, template.defaultHour)
            set(Calendar.MINUTE,      template.defaultMinute)
            set(Calendar.SECOND,      0)
        }
        return Reminder(
            title            = "${template.emoji} ${template.title}",
            description      = template.description,
            dateTimeMillis   = cal.timeInMillis,
            priority         = template.priority,
            category         = healthCategory,
            repeatType       = template.repeatType,
            soundEnabled     = true,
            vibrationEnabled = true
        )
    }
}
