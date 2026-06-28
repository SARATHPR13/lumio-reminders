package com.lumio.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.lumio.app.worker.BackupWorker
import com.lumio.app.worker.ReminderWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class LumioApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        schedulePeriodicWork()
    }

    private fun schedulePeriodicWork() {
        val workManager = WorkManager.getInstance(this)

        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(6, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            ).build()

        workManager.enqueueUniquePeriodicWork(
            ReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )

        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresStorageNotLow(true)
                    .build()
            ).build()

        workManager.enqueueUniquePeriodicWork(
            BackupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            backupRequest
        )
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description  = "Your scheduled reminders"
                enableVibration(true)
                enableLights(true)
                lightColor   = Color.parseColor("#FF1A73E8")
                setShowBadge(true)
            }

            val alarmChannel = NotificationChannel(
                CHANNEL_ALARMS,
                "Priority Alarms",
                NotificationManager.IMPORTANCE_MAX
            ).apply {
                description  = "High-priority alarms"
                enableVibration(true)
                enableLights(true)
                setBypassDnd(true)
                setShowBadge(true)
            }

            val silentChannel = NotificationChannel(
                CHANNEL_SILENT,
                "Silent Reminders",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description  = "Silent notifications"
                enableVibration(false)
                setSound(null, null)
                setShowBadge(false)
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
