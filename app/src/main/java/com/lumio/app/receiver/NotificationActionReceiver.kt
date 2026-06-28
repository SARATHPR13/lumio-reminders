package com.lumio.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", -1L)
        when (intent.action) {
            "com.lumio.app.ACTION_SNOOZE_5"  -> { }
            "com.lumio.app.ACTION_SNOOZE_15" -> { }
            "com.lumio.app.ACTION_SNOOZE_30" -> { }
            "com.lumio.app.ACTION_MARK_DONE" -> { }
        }
    }
}
