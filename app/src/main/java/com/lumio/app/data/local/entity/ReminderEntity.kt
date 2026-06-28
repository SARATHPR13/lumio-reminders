package com.lumio.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.model.RepeatType

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity        = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns  = ["categoryId"],
            onDelete      = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("categoryId"),
        Index("dateTimeMillis"),
        Index("isCompleted"),
        Index("priority")
    ]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String      = "",
    val dateTimeMillis: Long     = System.currentTimeMillis(),
    val priority: Priority       = Priority.NONE,
    val categoryId: Long?        = null,
    val repeatType: RepeatType   = RepeatType.NONE,
    val isCompleted: Boolean     = false,
    val soundEnabled: Boolean    = true,
    val vibrationEnabled: Boolean= true,
    val imagePath: String?       = null,
    val voiceNotePath: String?   = null,
    val isHidden: Boolean        = false,
    val createdAt: Long          = System.currentTimeMillis(),
    val updatedAt: Long          = System.currentTimeMillis()
) {
    fun toDomain(category: com.lumio.app.domain.model.Category? = null) = Reminder(
        id               = id,
        title            = title,
        description      = description,
        dateTimeMillis   = dateTimeMillis,
        priority         = priority,
        category         = category,
        repeatType       = repeatType,
        isCompleted      = isCompleted,
        soundEnabled     = soundEnabled,
        vibrationEnabled = vibrationEnabled,
        imagePath        = imagePath,
        voiceNotePath    = voiceNotePath,
        isHidden         = isHidden,
        createdAt        = createdAt
    )

    companion object {
        fun fromDomain(reminder: Reminder) = ReminderEntity(
            id               = reminder.id,
            title            = reminder.title,
            description      = reminder.description,
            dateTimeMillis   = reminder.dateTimeMillis,
            priority         = reminder.priority,
            categoryId       = reminder.category?.id,
            repeatType       = reminder.repeatType,
            isCompleted      = reminder.isCompleted,
            soundEnabled     = reminder.soundEnabled,
            vibrationEnabled = reminder.vibrationEnabled,
            imagePath        = reminder.imagePath,
            voiceNotePath    = reminder.voiceNotePath,
            isHidden         = reminder.isHidden,
            createdAt        = reminder.createdAt,
            updatedAt        = System.currentTimeMillis()
        )
    }
}
