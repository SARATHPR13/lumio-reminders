package com.lumio.app.location

data class LocationReminder(
    val id: String,
    val reminderId: Long,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float = 200f,
    val triggerType: GeofenceTrigger = GeofenceTrigger.ENTER,
    val locationName: String = "",
    val isActive: Boolean = true
)

enum class GeofenceTrigger(
    val label: String,
    val emoji: String
) {
    ENTER  ("When I arrive",  "📍"),
    EXIT   ("When I leave",  "🚶"),
    BOTH   ("Arrive & Leave","🔄")
}

data class SavedLocation(
    val id: String,
    val name: String,
    val emoji: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float = 200f
)
