package com.lumio.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LumioApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS, "Reminders", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Your scheduled reminders"
                enableVibration(true)
                enableLights(true)
            }
            val alarmChannel = NotificationChannel(
                CHANNEL_ALARMS, "Priority Alarms", NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = "High-priority reminder alarms"
                enableVibration(true)
                setBypassDnd(true)
            }
            val silentChannel = NotificationChannel(
                CHANNEL_SILENT, "Silent Reminders", NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Silent notifications"
                enableVibration(false)
                setSound(null, null)
            }
            manager.createNotificationChannels(
                listOf(reminderChannel, alarmChannel, silentChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_REMINDERS     = "lumio_reminders_channel"
        const val CHANNEL_ALARMS        = "lumio_alarms_channel"
        const val CHANNEL_SILENT        = "lumio_silent_channel"
        const val ACTION_SNOOZE_5       = "com.lumio.app.ACTION_SNOOZE_5"
        const val ACTION_SNOOZE_15      = "com.lumio.app.ACTION_SNOOZE_15"
        const val ACTION_SNOOZE_30      = "com.lumio.app.ACTION_SNOOZE_30"
        const val ACTION_MARK_DONE      = "com.lumio.app.ACTION_MARK_DONE"
        const val EXTRA_REMINDER_ID     = "reminder_id"
        const val ACTION_REMINDER_ALARM = "com.lumio.app.REMINDER_ALARM"
        const val DATABASE_NAME         = "lumio_database"
    }
}
