package com.lumio.app.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.lumio.app.LumioApp
import com.lumio.app.MainActivity
import com.lumio.app.domain.model.Priority
import com.lumio.app.receiver.NotificationActionReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val manager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showReminderNotification(
        reminderId: Long,
        title: String,
        description: String,
        priority: Priority,
        soundEnabled: Boolean
    ) {
        val openPending    = buildOpenIntent(reminderId)
        val snooze5Pending = buildSnoozeIntent(reminderId, 5)
        val snooze15Pending= buildSnoozeIntent(reminderId, 15)
        val snooze30Pending= buildSnoozeIntent(reminderId, 30)
        val donePending    = buildDoneIntent(reminderId)

        val channelId = when (priority) {
            Priority.URGENT,
            Priority.HIGH -> LumioApp.CHANNEL_ALARMS
            else -> if (soundEnabled) LumioApp.CHANNEL_REMINDERS else LumioApp.CHANNEL_SILENT
        }

        val notifPriority = when (priority) {
            Priority.URGENT -> NotificationCompat.PRIORITY_MAX
            Priority.HIGH   -> NotificationCompat.PRIORITY_HIGH
            Priority.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
            else            -> NotificationCompat.PRIORITY_LOW
        }

        val priorityColor = when (priority) {
            Priority.URGENT -> Color.parseColor("#FFD32F2F")
            Priority.HIGH   -> Color.parseColor("#FFFF6B35")
            Priority.MEDIUM -> Color.parseColor("#FFF9A825")
            Priority.LOW    -> Color.parseColor("#FF4CAF50")
            Priority.NONE   -> Color.parseColor("#FF1A73E8")
        }

        val bodyText = description.ifBlank { "${priority.emoji} Tap to open your reminder" }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(bodyText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bodyText)
                    .setBigContentTitle("${priority.emoji}  $title")
            )
            .setColor(priorityColor)
            .setColorized(true)
            .setPriority(notifPriority)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(openPending)
            .addAction(android.R.drawable.ic_media_pause, "5 min",  snooze5Pending)
            .addAction(android.R.drawable.ic_media_pause, "15 min", snooze15Pending)
            .addAction(android.R.drawable.ic_media_pause, "30 min", snooze30Pending)
            .addAction(android.R.drawable.checkbox_on_background, "Done ✓", donePending)
            .build()

        manager.notify(reminderId.toInt(), notification)
    }

    fun cancelNotification(reminderId: Long) {
        manager.cancel(reminderId.toInt())
    }

    private fun buildOpenIntent(reminderId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(LumioApp.EXTRA_REMINDER_ID, reminderId)
        }
        return PendingIntent.getActivity(
            context, reminderId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildSnoozeIntent(reminderId: Long, minutes: Int): PendingIntent {
        val action = when (minutes) {
            5    -> LumioApp.ACTION_SNOOZE_5
            15   -> LumioApp.ACTION_SNOOZE_15
            else -> LumioApp.ACTION_SNOOZE_30
        }
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra(LumioApp.EXTRA_REMINDER_ID, reminderId)
            putExtra("notification_id", reminderId.toInt())
        }
        return PendingIntent.getBroadcast(
            context,
            (reminderId * 10 + minutes).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildDoneIntent(reminderId: Long): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = LumioApp.ACTION_MARK_DONE
            putExtra(LumioApp.EXTRA_REMINDER_ID, reminderId)
            putExtra("notification_id", reminderId.toInt())
        }
        return PendingIntent.getBroadcast(
            context,
            (reminderId * 10 + 99).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
