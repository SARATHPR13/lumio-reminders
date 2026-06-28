package com.lumio.app.widget

import android.content.Context
import com.lumio.app.data.local.database.LumioDatabase
import com.lumio.app.data.local.entity.ReminderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

object WidgetDataProvider {

    suspend fun getNextReminder(context: Context): ReminderEntity? {
        return withContext(Dispatchers.IO) {
            try {
                val db  = LumioDatabase.buildDatabase(context)
                val now = System.currentTimeMillis()
                db.reminderDao().getFutureReminders(now).firstOrNull()
            } catch (e: Exception) { null }
        }
    }

    suspend fun getTodayReminders(context: Context): List<ReminderEntity> {
        return withContext(Dispatchers.IO) {
            try {
                val db         = LumioDatabase.buildDatabase(context)
                val startOfDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val endOfDay = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.timeInMillis
                db.reminderDao().getDueReminders(startOfDay, endOfDay)
            } catch (e: Exception) { emptyList() }
        }
    }
}
