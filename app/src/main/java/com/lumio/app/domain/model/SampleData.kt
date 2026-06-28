package com.lumio.app.domain.model

object SampleData {
    private val hour = 3_600_000L
    private val day  = 86_400_000L
    private val now  get() = System.currentTimeMillis()

    val reminders: List<Reminder>
        get() = listOf(
            Reminder(
                id = 1L, title = "Take medicine",
                description = "Vitamin D and iron tablets",
                dateTimeMillis = now + hour,
                priority = Priority.HIGH,
                category = Category.defaults[5],
                repeatType = RepeatType.DAILY
            ),
            Reminder(
                id = 2L, title = "Team standup",
                description = "Daily sync with engineering team",
                dateTimeMillis = now + 2 * hour,
                priority = Priority.URGENT,
                category = Category.defaults[0],
                repeatType = RepeatType.DAILY
            ),
            Reminder(
                id = 3L, title = "Pay electricity bill",
                description = "Due today — online banking",
                dateTimeMillis = now + 3 * hour,
                priority = Priority.HIGH,
                category = Category.defaults[3]
            ),
            Reminder(
                id = 4L, title = "Buy groceries",
                description = "Milk, eggs, bread, vegetables",
                dateTimeMillis = now + day,
                priority = Priority.MEDIUM,
                category = Category.defaults[2]
            ),
            Reminder(
                id = 5L, title = "Mom birthday",
                description = "Order cake and buy gift",
                dateTimeMillis = now + 2 * day,
                priority = Priority.URGENT,
                category = Category.defaults[6]
            ),
            Reminder(
                id = 6L, title = "Study chapter 5",
                description = "Revision and practice problems",
                dateTimeMillis = now + 3 * day,
                priority = Priority.HIGH,
                category = Category.defaults[4],
                repeatType = RepeatType.DAILY
            ),
            Reminder(
                id = 7L, title = "Gym workout",
                description = "Leg day + 20 min cardio",
                dateTimeMillis = now - hour,
                priority = Priority.MEDIUM,
                category = Category.defaults[5],
                isCompleted = true
            ),
            Reminder(
                id = 8L, title = "Call dentist",
                description = "Schedule annual checkup",
                dateTimeMillis = now - 2 * hour,
                priority = Priority.LOW,
                category = Category.defaults[5],
                isCompleted = true
            )
        )
}
