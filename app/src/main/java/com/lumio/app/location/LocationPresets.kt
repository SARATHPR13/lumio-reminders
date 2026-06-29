package com.lumio.app.location

/**
 * Common location-based reminder presets
 * Users can select these as starting points
 */
object LocationPresets {

    val reminderIdeas = listOf(
        LocationReminderIdea(
            emoji       = "🏢",
            title       = "Call Manager",
            description = "Remind me to call manager when I reach office",
            trigger     = GeofenceTrigger.ENTER,
            placeName   = "Office"
        ),
        LocationReminderIdea(
            emoji       = "🏠",
            title       = "Turn off AC",
            description = "Remind me to turn off AC when I leave home",
            trigger     = GeofenceTrigger.EXIT,
            placeName   = "Home"
        ),
        LocationReminderIdea(
            emoji       = "🛒",
            title       = "Buy Groceries",
            description = "Remind me to buy milk and eggs",
            trigger     = GeofenceTrigger.ENTER,
            placeName   = "Supermarket"
        ),
        LocationReminderIdea(
            emoji       = "🏥",
            title       = "Take Medicine",
            description = "Take prescribed medicine before consultation",
            trigger     = GeofenceTrigger.ENTER,
            placeName   = "Hospital / Clinic"
        ),
        LocationReminderIdea(
            emoji       = "⛽",
            title       = "Fill Petrol",
            description = "Remind me to fill petrol when near petrol station",
            trigger     = GeofenceTrigger.ENTER,
            placeName   = "Petrol Station"
        ),
        LocationReminderIdea(
            emoji       = "🏦",
            title       = "Bank Work",
            description = "Submit documents at the bank",
            trigger     = GeofenceTrigger.ENTER,
            placeName   = "Bank"
        ),
        LocationReminderIdea(
            emoji       = "👨‍👩‍👧",
            title       = "Call Family",
            description = "Call home when I reach office",
            trigger     = GeofenceTrigger.ENTER,
            placeName   = "Office"
        ),
        LocationReminderIdea(
            emoji       = "📦",
            title       = "Pick Up Parcel",
            description = "Collect parcel from post office",
            trigger     = GeofenceTrigger.ENTER,
            placeName   = "Post Office"
        ),
        LocationReminderIdea(
            emoji       = "🔒",
            title       = "Lock Door",
            description = "Check if door is locked when leaving",
            trigger     = GeofenceTrigger.EXIT,
            placeName   = "Home"
        ),
        LocationReminderIdea(
            emoji       = "💊",
            title       = "Pharmacy",
            description = "Buy prescribed medicines",
            trigger     = GeofenceTrigger.ENTER,
            placeName   = "Pharmacy"
        )
    )
}

data class LocationReminderIdea(
    val emoji: String,
    val title: String,
    val description: String,
    val trigger: GeofenceTrigger,
    val placeName: String
)
