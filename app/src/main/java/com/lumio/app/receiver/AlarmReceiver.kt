package com.lumio.app.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.lumio.app.LumioApp
import com.lumio.app.MainActivity
import com.lumio.app.domain.model.Priority
import com.lumio.app.receiver.NotificationActionReceiver

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == LumioApp.ACTION_REMINDER_ALARM) {
            handleAlarm(context, intent)
        }
    }

    private fun handleAlarm(context: Context, intent: Intent) {
        val reminderId  = intent.getLongExtra(LumioApp.EXTRA_REMINDER_ID, -1L)
        if (reminderId == -1L) return

        val title       = intent.getStringExtra("title")       ?: "Reminder"
        val description = intent.getStringExtra("description") ?: ""
        val priorityStr = intent.getStringExtra("priority")    ?: Priority.NONE.name
        val soundOn     = intent.getBooleanExtra("sound",      true)
        val vibrationOn = intent.getBooleanExtra("vibration",  true)
        val priority    = runCatching {
            Priority.valueOf(priorityStr)
        }.getOrDefault(Priority.NONE)

        if (vibrationOn) vibrate(context)
        showNotification(context, reminderId, title, description, priority, soundOn)
    }

    private fun showNotification(
        context: Context,
        reminderId: Long,
        title: String,
        description: String,
        priority: Priority,
        soundOn: Boolean
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

        val channelId = when (priority) {
            Priority.URGENT, Priority.HIGH -> LumioApp.CHANNEL_ALARMS
            else -> if (soundOn) LumioApp.CHANNEL_REMINDERS else LumioApp.CHANNEL_SILENT
        }

        val priorityColor = when (priority) {
            Priority.URGENT -> Color.parseColor("#FFD32F2F")
            Priority.HIGH   -> Color.parseColor("#FFFF6B35")
            Priority.MEDIUM -> Color.parseColor("#FFF9A825")
            Priority.LOW    -> Color.parseColor("#FF4CAF50")
            Priority.NONE   -> Color.parseColor("#FF1A73E8")
        }

        val notifPriority = when (priority) {
            Priority.URGENT -> NotificationCompat.PRIORITY_MAX
            Priority.HIGH   -> NotificationCompat.PRIORITY_HIGH
            else            -> NotificationCompat.PRIORITY_DEFAULT
        }

        val bodyText = description.ifBlank { "${priority.emoji} Tap to view your reminder" }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${priority.emoji}  $title")
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setColor(priorityColor)
            .setColorized(true)
            .setPriority(notifPriority)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(openPending)
            .addAction(android.R.drawable.ic_media_pause, "5 min",
                snoozeIntent(LumioApp.ACTION_SNOOZE_5, (reminderId * 10 + 1).toInt()))
            .addAction(android.R.drawable.ic_media_pause, "15 min",
                snoozeIntent(LumioApp.ACTION_SNOOZE_15, (reminderId * 10 + 2).toInt()))
            .addAction(android.R.drawable.ic_media_pause, "30 min",
                snoozeIntent(LumioApp.ACTION_SNOOZE_30, (reminderId * 10 + 3).toInt()))
            .addAction(
                android.R.drawable.checkbox_on_background, "Done ✓",
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
            .build()

        manager.notify(reminderId.toInt(), notification)
    }

    private fun vibrate(context: Context) {
        try {
            val pattern = longArrayOf(0, 400, 200, 400, 200, 400)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val mgr = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                mgr.defaultVibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        } catch (_: Exception) {}
    }
}
