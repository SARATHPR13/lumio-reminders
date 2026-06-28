package com.lumio.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.lumio.app.LumioApp
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.model.RepeatType
import com.lumio.app.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // ── Schedule a reminder alarm ─────────────────────
    fun schedule(reminder: Reminder) {
        if (reminder.isCompleted) return
        if (reminder.dateTimeMillis < System.currentTimeMillis()) {
            // Skip past reminders unless repeating
            if (reminder.repeatType == RepeatType.NONE) return
        }

        val intent = buildIntent(reminder)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = getNextTriggerTime(reminder)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    // Fallback for devices without exact alarm permission
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback if exact alarm not permitted
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    // ── Cancel a scheduled alarm ──────────────────────
    fun cancel(reminderId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = LumioApp.ACTION_REMINDER_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    // ── Schedule snooze alarm ─────────────────────────
    fun scheduleSnooze(reminderId: Long, title: String, snoozeMinutes: Int) {
        val triggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = LumioApp.ACTION_REMINDER_ALARM
            putExtra(LumioApp.EXTRA_REMINDER_ID, reminderId)
            putExtra("title",        title)
            putExtra("is_snooze",    true)
            putExtra("snooze_minutes", snoozeMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            // Use negative ID range for snooze to avoid conflicts
            -(reminderId.toInt()),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    // ── Schedule next repeating alarm ─────────────────
    fun scheduleNext(reminder: Reminder) {
        if (reminder.repeatType == RepeatType.NONE) return
        val nextTime = computeNextRepeat(reminder.dateTimeMillis, reminder.repeatType)
        schedule(reminder.copy(dateTimeMillis = nextTime))
    }

    // ── Cancel snooze alarm ───────────────────────────
    fun cancelSnooze(reminderId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            -(reminderId.toInt()),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    // ── Helper: Build Intent ──────────────────────────
    private fun buildIntent(reminder: Reminder): Intent =
        Intent(context, AlarmReceiver::class.java).apply {
            action = LumioApp.ACTION_REMINDER_ALARM
            putExtra(LumioApp.EXTRA_REMINDER_ID, reminder.id)
            putExtra("title",       reminder.title)
            putExtra("description", reminder.description)
            putExtra("priority",    reminder.priority.name)
            putExtra("repeat_type", reminder.repeatType.name)
            putExtra("sound",       reminder.soundEnabled)
            putExtra("vibration",   reminder.vibrationEnabled)
        }

    // ── Helper: Get next trigger time ─────────────────
    private fun getNextTriggerTime(reminder: Reminder): Long {
        val now = System.currentTimeMillis()
        return if (reminder.dateTimeMillis > now) {
            reminder.dateTimeMillis
        } else {
            computeNextRepeat(reminder.dateTimeMillis, reminder.repeatType)
        }
    }

    // ── Helper: Compute next repeat time ─────────────
    private fun computeNextRepeat(fromMillis: Long, repeatType: RepeatType): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = fromMillis }
        val now = Calendar.getInstance()
        while (cal.before(now)) {
            when (repeatType) {
                RepeatType.DAILY   -> cal.add(Calendar.DAY_OF_YEAR, 1)
                RepeatType.WEEKLY  -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                RepeatType.MONTHLY -> cal.add(Calendar.MONTH, 1)
                RepeatType.YEARLY  -> cal.add(Calendar.YEAR, 1)
                else               -> return fromMillis
            }
        }
        return cal.timeInMillis
    }
}
