package com.lumio.app.location

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeofenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val geofencingClient: GeofencingClient =
        LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = "com.lumio.app.ACTION_GEOFENCE_EVENT"
        }
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    fun hasForegroundLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun hasBackgroundLocationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    fun hasLocationPermission(): Boolean =
        hasForegroundLocationPermission() && hasBackgroundLocationPermission()

    fun addGeofence(
        locationReminder: LocationReminder,
        onResult: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        if (!hasForegroundLocationPermission()) {
            onResult(false, "Location permission is required.")
            return
        }
        if (!hasBackgroundLocationPermission()) {
            onResult(
                false,
                "Background location (\"Allow all the time\") is required " +
                    "for this reminder to trigger while LUMIO is closed."
            )
            return
        }

        val transitionTypes = when (locationReminder.triggerType) {
            GeofenceTrigger.ENTER -> Geofence.GEOFENCE_TRANSITION_ENTER
            GeofenceTrigger.EXIT  -> Geofence.GEOFENCE_TRANSITION_EXIT
            GeofenceTrigger.BOTH  ->
                Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
        }

        val geofence = Geofence.Builder()
            .setRequestId(locationReminder.id)
            .setCircularRegion(
                locationReminder.latitude,
                locationReminder.longitude,
                locationReminder.radiusMeters
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(transitionTypes)
            .setLoiteringDelay(30_000)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        try {
            geofencingClient.addGeofences(request, geofencePendingIntent)
                .addOnSuccessListener { onResult(true, null) }
                .addOnFailureListener { e ->
                    onResult(false, e.message ?: "Failed to register the location trigger.")
                }
        } catch (e: SecurityException) {
            onResult(false, "Location permission was denied by the system.")
        }
    }

    fun removeGeofence(geofenceId: String) {
        geofencingClient.removeGeofences(listOf(geofenceId))
    }

    fun removeAllGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent)
    }
}
