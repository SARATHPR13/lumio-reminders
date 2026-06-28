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
import com.lumio.app.R
import com.lumio.app.domain.model.Priority

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            LumioApp.ACTION_REMINDER_ALARM -> handleAlarm(context, intent)
        }
    }

    private fun handleAlarm(context: Context, intent: Intent) {
        val reminderId  = intent.getLongExtra(LumioApp.EXTRA_REMINDER_ID, -1L)
        if (reminderId == -1L) return

        val title       = intent.getStringExtra("title")        ?: "Reminder"
        val description = intent.getStringExtra("description")  ?: ""
        val priorityStr = intent.getStringExtra("priority")     ?: Priority.NONE.name
        val soundOn     = intent.getBooleanExtra("sound",       true)
        val vibrationOn = intent.getBooleanExtra("vibration",   true)
        val priority    = Priority.values().find { it.name == priorityStr } ?: Priority.NONE

        // Vibrate if enabled
        if (vibrationOn) {
            vibrate(context)
        }

        // Show notification
        showNotification(
            context     = context,
            reminderId  = reminderId,
            title       = title,
            description = description,
            priority    = priority,
            soundOn     = soundOn
        )
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

        // ── Open app intent ───────────────────────────
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(LumioApp.EXTRA_REMINDER_ID, reminderId)
        }
        val openPending = PendingIntent.getActivity(
            context, reminderId.toInt(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ── Snooze 5 min intent ───────────────────────
        val snooze5Intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = LumioApp.ACTION_SNOOZE_5
            putExtra(LumioApp.EXTRA_REMINDER_ID, reminderId)
            putExtra("notification_id", reminderId.toInt())
            putExtra("title", title)
        }
        val snooze5Pending = PendingIntent.getBroadcast(
            context, (reminderId * 10 + 1).toInt(), snooze5Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ── Snooze 15 min intent ──────────────────────
        val snooze15Intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = LumioApp.ACTION_SNOOZE_15
            putExtra(LumioApp.EXTRA_REMINDER_ID, reminderId)
            putExtra("notification_id", reminderId.toInt())
            putExtra("title", title)
        }
        val snooze15Pending = PendingIntent.getBroadcast(
            context, (reminderId * 10 + 2).toInt(), snooze15Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ── Snooze 30 min intent ──────────────────────
        val snooze30Intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = LumioApp.ACTION_SNOOZE_30
            putExtra(LumioApp.EXTRA_REMINDER_ID, reminderId)
            putExtra("notification_id", reminderId.toInt())
            putExtra("title", title)
        }
        val snooze30Pending = PendingIntent.getBroadcast(
            context, (reminderId * 10 + 3).toInt(), snooze30Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ── Mark Done intent ──────────────────────────
        val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = LumioApp.ACTION_MARK_DONE
            putExtra(LumioApp.EXTRA_REMINDER_ID, reminderId)
            putExtra("notification_id", reminderId.toInt())
        }
        val donePending = PendingIntent.getBroadcast(
            context, (reminderId * 10 + 4).toInt(), doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ── Choose notification channel ───────────────
        val channelId = when (priority) {
            Priority.URGENT -> LumioApp.CHANNEL_ALARMS
            Priority.HIGH   -> LumioApp.CHANNEL_ALARMS
            else            -> if (soundOn) LumioApp.CHANNEL_REMINDERS else LumioApp.CHANNEL_SILENT
        }

        // ── Priority color ────────────────────────────
        val priorityColor = when (priority) {
            Priority.URGENT -> Color.parseColor("#FFD32F2F")
            Priority.HIGH   -> Color.parseColor("#FFFF6B35")
            Priority.MEDIUM -> Color.parseColor("#FFF9A825")
            Priority.LOW    -> Color.parseColor("#FF4CAF50")
            Priority.NONE   -> Color.parseColor("#FF1A73E8")
        }

        // ── Notification priority ─────────────────────
        val notifPriority = when (priority) {
            Priority.URGENT -> NotificationCompat.PRIORITY_MAX
            Priority.HIGH   -> NotificationCompat.PRIORITY_HIGH
            else            -> NotificationCompat.PRIORITY_DEFAULT
        }

        // ── Build notification ────────────────────────
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(
                if (description.isNotBlank()) description
                else "${priority.emoji} ${priority.label} reminder"
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(description.ifBlank { "Tap to open your reminder" })
                    .setBigContentTitle(title)
            )
            .setColor(priorityColor)
            .setColorized(true)
            .setPriority(notifPriority)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(openPending)
            // Snooze actions
            .addAction(
                android.R.drawable.ic_media_pause,
                "5 min",
                snooze5Pending
            )
            .addAction(
                android.R.drawable.ic_media_pause,
                "15 min",
                snooze15Pending
            )
            .addAction(
                android.R.drawable.ic_media_pause,
                "30 min",
                snooze30Pending
            )
            // Done action
            .addAction(
                android.R.drawable.checkbox_on_background,
                "Done",
                donePending
            )
            .build()

        manager.notify(reminderId.toInt(), notification)
    }

    private fun vibrate(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = manager.defaultVibrator
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 500, 200, 500, 200, 500),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 500, 200, 500, 200, 500),
                            -1
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
                }
            }
        } catch (e: Exception) {
            // Silently ignore vibration errors
        }
    }
}
