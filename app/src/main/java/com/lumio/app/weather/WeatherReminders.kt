package com.lumio.app.weather

import com.lumio.app.domain.model.Category
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.model.RepeatType
import java.util.Calendar

data class WeatherCondition(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val triggerCondition: String,
    val priority: Priority
)

object WeatherReminders {

    val weatherCategory = Category(
        id       = 11L,
        name     = "Weather",
        emoji    = "🌤",
        colorHex = "#FF0097A7"
    )

    val conditions = listOf(
        WeatherCondition(
            id               = "rain_umbrella",
            title            = "Carry Umbrella ☔",
            description      = "Rain expected today — don't forget your umbrella!",
            emoji            = "☔",
            triggerCondition = "Rain > 60%",
            priority         = Priority.HIGH
        ),
        WeatherCondition(
            id               = "hot_water",
            title            = "Drink More Water 💧",
            description      = "It's very hot outside — drink extra water to stay hydrated!",
            emoji            = "🌡️",
            triggerCondition = "Temp > 35°C",
            priority         = Priority.HIGH
        ),
        WeatherCondition(
            id               = "cold_jacket",
            title            = "Wear Warm Clothes 🧥",
            description      = "Cold weather today — wear a jacket before going out!",
            emoji            = "❄️",
            triggerCondition = "Temp < 15°C",
            priority         = Priority.MEDIUM
        ),
        WeatherCondition(
            id               = "pollution_mask",
            title            = "Wear Mask 😷",
            description      = "Air quality is poor today — wear a mask outside!",
            emoji            = "😷",
            triggerCondition = "AQI > 150",
            priority         = Priority.HIGH
        ),
        WeatherCondition(
            id               = "sunny_sunscreen",
            title            = "Apply Sunscreen 🌞",
            description      = "Strong UV rays today — apply sunscreen before going out!",
            emoji            = "🌞",
            triggerCondition = "UV Index > 7",
            priority         = Priority.MEDIUM
        ),
        WeatherCondition(
            id               = "storm_stay_home",
            title            = "Storm Warning ⛈️",
            description      = "Thunderstorm expected — avoid going out if possible!",
            emoji            = "⛈️",
            triggerCondition = "Storm Warning",
            priority         = Priority.URGENT
        ),
        WeatherCondition(
            id               = "foggy_drive_safe",
            title            = "Drive Carefully 🌫️",
            description      = "Dense fog today — drive slowly and use fog lights!",
            emoji            = "🌫️",
            triggerCondition = "Visibility < 100m",
            priority         = Priority.HIGH
        ),
        WeatherCondition(
            id               = "windy_close_windows",
            title            = "Close Windows 💨",
            description      = "Strong winds expected — close your windows and secure loose items!",
            emoji            = "💨",
            triggerCondition = "Wind > 50 kmph",
            priority         = Priority.MEDIUM
        )
    )

    fun buildReminder(condition: WeatherCondition): Reminder {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return Reminder(
            title            = condition.title,
            description      = condition.description,
            dateTimeMillis   = cal.timeInMillis,
            priority         = condition.priority,
            category         = weatherCategory,
            repeatType       = RepeatType.DAILY,
            soundEnabled     = true,
            vibrationEnabled = true
        )
    }
}
