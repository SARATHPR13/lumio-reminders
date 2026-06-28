package com.lumio.app.domain.model

import java.text.SimpleDateFormat
import java.util.*

data class Reminder(
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val dateTimeMillis: Long = System.currentTimeMillis(),
    val priority: Priority = Priority.NONE,
    val category: Category? = null,
    val repeatType: RepeatType = RepeatType.NONE,
    val isCompleted: Boolean = false,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val imagePath: String? = null,
    val voiceNotePath: String? = null,
    val isHidden: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isOverdue: Boolean
        get() = !isCompleted && dateTimeMillis < System.currentTimeMillis()

    val isToday: Boolean
        get() {
            val r = Calendar.getInstance().apply { timeInMillis = dateTimeMillis }
            val t = Calendar.getInstance()
            return r.get(Calendar.YEAR) == t.get(Calendar.YEAR) &&
                   r.get(Calendar.DAY_OF_YEAR) == t.get(Calendar.DAY_OF_YEAR)
        }

    val formattedTime: String
        get() = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(dateTimeMillis))

    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(dateTimeMillis))

    val formattedDateTime: String
        get() = if (isToday) "Today · $formattedTime" else "$formattedDate · $formattedTime"
}
