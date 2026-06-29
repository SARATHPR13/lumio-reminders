package com.lumio.app.location

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.lumio.app.LumioApp
import com.lumio.app.MainActivity

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) return

        val transition = geofencingEvent.geofenceTransition
        val geofences  = geofencingEvent.triggeringGeofences ?: return

        geofences.forEach { geofence ->
            val geofenceId = geofence.requestId
            showLocationNotification(context, geofenceId, transition)
        }
    }

    private fun showLocationNotification(
        context: Context,
        geofenceId: String,
        transition: Int
    ) {
        val transitionText = when (transition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "You have arrived!"
            Geofence.GEOFENCE_TRANSITION_EXIT  -> "You are leaving!"
            else -> return
        }

        val openIntent  = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            context, geofenceId.hashCode(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context, LumioApp.CHANNEL_REMINDERS
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📍 Location Reminder")
            .setContentText(transitionText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "$transitionText\nGeofence ID: $geofenceId"
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPending)
            .build()

        val manager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        manager.notify(geofenceId.hashCode(), notification)
    }
}
