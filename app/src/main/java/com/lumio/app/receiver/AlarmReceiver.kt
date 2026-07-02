package com.lumio.app.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.lumio.app.LumioApp
import com.lumio.app.MainActivity
import com.lumio.app.R
import com.lumio.app.domain.model.Priority

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == LumioApp.ACTION_REMINDER_ALARM) {
            handleAlarm(context, intent)
        }
    }

    private fun handleAlarm(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(LumioApp.EXTRA_REMINDER_ID, -1L)
        if (reminderId == -1L) return

        val title = intent.getStringExtra("title") ?: context.getString(R.string.notif_default_title)
        val description = intent.getStringExtra("description") ?: ""
        val priorityStr = intent.getStringExtra("priority") ?: Priority.NONE.name
        val soundOn = intent.getBooleanExtra("sound", true)
        val vibrationOn = intent.getBooleanExtra("vibration", true)
        val priority = runCatching { Priority.valueOf(priorityStr) }.getOrDefault(Priority.NONE)

        showNotification(context, reminderId, title, description, priority, soundOn, vibrationOn)
    }

    private fun showNotification(
        context: Context,
        reminderId: Long,
        title: String,
        description: String,
        priority: Priority,
        soundOn: Boolean,
        vibrationOn: Boolean
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(LumioApp.EXTRA_REMINDER_ID, reminderId)
        }
        val openPending = PendingIntent.getActivity(
            context, reminderId.toInt(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        fun snoozeIntent(action: String, reqCode: Int) = PendingIntent.getBroadcast(
            context, reqCode,
            Intent(context, NotificationActionReceiver::class.java).apply {
                this.action = action
                putExtra(LumioApp.EXTRA_REMINDER_ID, reminderId)
                putExtra("notification_id", reminderId.toInt())
                putExtra("title", title)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Choose channel. If both sound AND vibration are off, use the silent
        // channel; otherwise use a channel that vibrates (and sounds).
        val channelId = when {
            !soundOn && !vibrationOn -> LumioApp.CHANNEL_SILENT
            priority == Priority.URGENT || priority == Priority.HIGH -> LumioApp.CHANNEL_ALARMS
            else -> LumioApp.CHANNEL_REMINDERS
        }

        val priorityColor = when (priority) {
            Priority.URGENT -> Color.parseColor("#FFD1453B")
            Priority.HIGH -> Color.parseColor("#FFE8833A")
            Priority.MEDIUM -> Color.parseColor("#FFF0A73F")
            Priority.LOW -> Color.parseColor("#FF6BA368")
            Priority.NONE -> Color.parseColor("#FF3B7A57")
        }

        val notifPriority = when (priority) {
            Priority.URGENT -> NotificationCompat.PRIORITY_MAX
            Priority.HIGH -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val bodyText = description.ifBlank {
            "${priority.emoji} ${context.getString(R.string.notif_tap_to_view)}"
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${priority.emoji} $title")
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setColor(priorityColor)
            .setColorized(true)
            .setPriority(notifPriority)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(openPending)
            .addAction(android.R.drawable.ic_media_pause, context.getString(R.string.notif_action_snooze_5),
                snoozeIntent(LumioApp.ACTION_SNOOZE_5, (reminderId * 10 + 1).toInt()))
            .addAction(android.R.drawable.ic_media_pause, context.getString(R.string.notif_action_snooze_15),
                snoozeIntent(LumioApp.ACTION_SNOOZE_15, (reminderId * 10 + 2).toInt()))
            .addAction(android.R.drawable.ic_media_pause, context.getString(R.string.notif_action_snooze_30),
                snoozeIntent(LumioApp.ACTION_SNOOZE_30, (reminderId * 10 + 3).toInt()))
            .addAction(
                android.R.drawable.checkbox_on_background, context.getString(R.string.notif_action_done),
                PendingIntent.getBroadcast(
                    context, (reminderId * 10 + 4).toInt(),
                    Intent(context, NotificationActionReceiver::class.java).apply {
                        action = LumioApp.ACTION_MARK_DONE
                        putExtra(LumioApp.EXTRA_REMINDER_ID, reminderId)
                        putExtra("notification_id", reminderId.toInt())
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

        // Pre-Android-8 devices have no channels, so set vibration on the
        // notification directly. On Android 8+ this is ignored and the
        // channel's vibration pattern is used instead.
        if (vibrationOn) {
            builder.setVibrate(longArrayOf(0, 600, 250, 600, 250, 600))
        }

        manager.notify(reminderId.toInt(), builder.build())
    }
}
