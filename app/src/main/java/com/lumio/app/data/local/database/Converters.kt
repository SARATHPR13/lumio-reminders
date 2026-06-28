package com.lumio.app.data.local.database

import androidx.room.TypeConverter
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.RepeatType

class Converters {

    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): Priority =
        Priority.values().find { it.name == value } ?: Priority.NONE

    @TypeConverter
    fun fromRepeatType(repeatType: RepeatType): String = repeatType.name

    @TypeConverter
    fun toRepeatType(value: String): RepeatType =
        RepeatType.values().find { it.name == value } ?: RepeatType.NONE
}
