package com.lumio.app.presentation.screens.location

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.lumio.app.alarm.AlarmScheduler
import com.lumio.app.domain.model.Category
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.model.RepeatType
import com.lumio.app.domain.repository.ReminderRepository
import com.lumio.app.location.GeofenceManager
import com.lumio.app.location.GeofenceTrigger
import com.lumio.app.location.LocationReminder
import com.lumio.app.location.LocationPresets
import com.lumio.app.location.LocationReminderIdea
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class LocationUiState(
    val title: String                  = "",
    val description: String            = "",
    val locationName: String           = "",
    val latitude: Double               = 0.0,
    val longitude: Double              = 0.0,
    val radiusMeters: Float            = 200f,
    val triggerType: GeofenceTrigger   = GeofenceTrigger.ENTER,
    val hasLocationPermission: Boolean = false,
    val currentLocation: Location?     = null,
    val isSaving: Boolean              = false,
    val isSaved: Boolean               = false,
    val errorMessage: String?          = null,
    val successMessage: String?        = null,
    val titleError: Boolean            = false,
    val locationError: Boolean         = false,
    val presets: List<LocationReminderIdea> = LocationPresets.reminderIdeas
)

@HiltViewModel
class LocationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geofenceManager: GeofenceManager,
    private val reminderRepository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    init { checkPermissions() }

    fun checkPermissions() {
        val hasPermission = geofenceManager.hasLocationPermission()
        _uiState.update { it.copy(hasLocationPermission = hasPermission) }
        if (hasPermission) getCurrentLocation()
    }

    private fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { loc ->
                    _uiState.update { it.copy(currentLocation = loc) }
                }
            }
        } catch (e: SecurityException) { }
    }

    fun setTitle(v: String)            = _uiState.update { it.copy(title = v, titleError = false) }
    fun setDescription(v: String)      = _uiState.update { it.copy(description = v) }
    fun setLocationName(v: String)     = _uiState.update { it.copy(locationName = v, locationError = false) }
    fun setLatitude(v: Double)         = _uiState.update { it.copy(latitude = v) }
    fun setLongitude(v: Double)        = _uiState.update { it.copy(longitude = v) }
    fun setRadius(v: Float)            = _uiState.update { it.copy(radiusMeters = v) }
    fun setTrigger(v: GeofenceTrigger) = _uiState.update { it.copy(triggerType = v) }

    fun useCurrentLocation() {
        val loc = _uiState.value.currentLocation
        if (loc != null) {
            _uiState.update {
                it.copy(
                    latitude      = loc.latitude,
                    longitude     = loc.longitude,
                    locationName  = it.locationName.ifBlank { "Current Location" },
                    locationError = false
                )
            }
        } else {
            getCurrentLocation()
            _uiState.update {
                it.copy(errorMessage = "Getting your location — please wait and try again")
            }
        }
    }

    fun applyPreset(preset: LocationReminderIdea) {
        _uiState.update {
            it.copy(
                title        = preset.title,
                description  = preset.description,
                locationName = preset.placeName,
                triggerType  = preset.trigger,
                titleError   = false
            )
        }
    }

    fun saveLocationReminder() {
        val s = _uiState.value
        if (s.title.isBlank()) {
            _uiState.update { it.copy(titleError = true) }
            return
        }
        if (s.locationName.isBlank()) {
            _uiState.update { it.copy(locationError = true) }
            return
        }
        if (s.latitude == 0.0 && s.longitude == 0.0) {
            _uiState.update {
                it.copy(errorMessage = "Please set a location first")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val locationCategory = Category(
                id       = 12L,
                name     = "Location",
                emoji    = "📍",
                colorHex = "#FF1A73E8"
            )

            val reminder = Reminder(
                title            = "${s.triggerType.emoji} ${s.title}",
                description      = "${s.description}\n📍 ${s.locationName} — ${s.triggerType.label}",
                dateTimeMillis   = System.currentTimeMillis() + 3_600_000L,
                priority         = Priority.HIGH,
                category         = locationCategory,
                repeatType       = RepeatType.NONE,
                soundEnabled     = true,
                vibrationEnabled = true
            )

            val id = reminderRepository.insertReminder(reminder)

            val locationReminder = LocationReminder(
                id           = UUID.randomUUID().toString(),
                reminderId   = id,
                title        = s.title,
                description  = s.description,
                latitude     = s.latitude,
                longitude    = s.longitude,
                radiusMeters = s.radiusMeters,
                triggerType  = s.triggerType,
                locationName = s.locationName
            )

            if (s.hasLocationPermission) {
                geofenceManager.addGeofence(locationReminder)
            }

            _uiState.update {
                it.copy(
                    isSaving       = false,
                    isSaved        = true,
                    successMessage = "📍 Location reminder set for ${s.locationName}!"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
