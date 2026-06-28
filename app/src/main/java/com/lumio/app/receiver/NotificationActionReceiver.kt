package com.lumio.app.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lumio.app.LumioApp
import com.lumio.app.alarm.AlarmScheduler
import com.lumio.app.data.local.database.LumioDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId     = intent.getLongExtra(LumioApp.EXTRA_REMINDER_ID, -1L)
        val notificationId = intent.getIntExtra("notification_id", reminderId.toInt())
        val title          = intent.getStringExtra("title") ?: "Reminder"

        if (reminderId == -1L) return

        // Dismiss the notification
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)

        val scheduler = AlarmScheduler(context)

        when (intent.action) {
            LumioApp.ACTION_SNOOZE_5 -> {
                scheduler.scheduleSnooze(reminderId, title, 5)
            }

            LumioApp.ACTION_SNOOZE_15 -> {
                scheduler.scheduleSnooze(reminderId, title, 15)
            }

            LumioApp.ACTION_SNOOZE_30 -> {
                scheduler.scheduleSnooze(reminderId, title, 30)
            }

            LumioApp.ACTION_MARK_DONE -> {
                scope.launch {
                    try {
                        val db = LumioDatabase.buildDatabase(context)
                        db.reminderDao().setCompleted(reminderId, true)
                    } catch (e: Exception) {
                        // Log error silently
                    }
                }
            }
        }
    }
}
