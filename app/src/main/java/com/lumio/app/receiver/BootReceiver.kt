package com.lumio.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lumio.app.alarm.AlarmScheduler
import com.lumio.app.data.local.database.LumioDatabase
import com.lumio.app.data.local.entity.ReminderEntity
import com.lumio.app.domain.model.RepeatType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON" -> rescheduleAll(context)
        }
    }

    private fun rescheduleAll(context: Context) {
        scope.launch {
            try {
                val db        = LumioDatabase.buildDatabase(context)
                val scheduler = AlarmScheduler(context)
                val now       = System.currentTimeMillis()

                // Get all future reminders
                val reminders = db.reminderDao().getFutureReminders(now)

                reminders.forEach { entity ->
                    val category = entity.categoryId?.let {
                        db.categoryDao().getCategoryById(it)?.toDomain()
                    }
                    val reminder = entity.toDomain(category)
                    scheduler.schedule(reminder)
                }
            } catch (e: Exception) {
                // Log silently — don't crash on boot
            }
        }
    }
}
