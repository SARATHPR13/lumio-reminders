package com.lumio.app.presentation.screens.weather
import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.lumio.app.alarm.AlarmScheduler
import com.lumio.app.domain.model.*
import com.lumio.app.domain.repository.ReminderRepository
import com.lumio.app.weather.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class WeatherUiState(
    val isLoading: Boolean = false,
    val weather: WeatherInfo? = null,
    val alerts: List<WeatherAlert> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val cityInput: String = "",
    val addedAlerts: Set<String> = emptySet(),
    val hasApiKey: Boolean = false,
    val showApiKeyInfo: Boolean = false
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val weatherRepository: WeatherRepository,
    private val reminderRepository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val apiKey: String get() = try {
        val resId = context.resources.getIdentifier("weather_api_key","string",context.packageName)
        if (resId != 0) context.getString(resId) else ""
    } catch (e: Exception) { "" }

    init {
        val hasKey = apiKey.isNotBlank() && apiKey != "YOUR_OPENWEATHER_API_KEY"
        _uiState.update { it.copy(hasApiKey = hasKey) }
        if (hasKey) fetchByLocation()
    }

    fun fetchByLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) fetchForLocation(loc)
                    else _uiState.update { it.copy(isLoading = false, errorMessage = "Could not get location. Try city search.") }
                }
            } catch (e: SecurityException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Location permission needed.") }
            }
        }
    }

    private fun fetchForLocation(location: Location) {
        viewModelScope.launch {
            val result = weatherRepository.fetchWeather(location.latitude, location.longitude, apiKey)
            handleResult(result)
        }
    }

    fun fetchByCity() {
        val city = _uiState.value.cityInput.trim()
        if (city.isBlank()) { _uiState.update { it.copy(errorMessage = "Enter a city name") }; return }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            handleResult(weatherRepository.fetchWeatherByCity(city, apiKey))
        }
    }

    fun setCityInput(v: String) = _uiState.update { it.copy(cityInput = v) }
    fun showApiKeyInfo(v: Boolean) = _uiState.update { it.copy(showApiKeyInfo = v) }
    fun clearMessages() = _uiState.update { it.copy(successMessage = null, errorMessage = null) }

    fun addWeatherReminder(alert: WeatherAlert) {
        viewModelScope.launch {
            val cat = Category(11L, "Weather", "\uD83C\uDF24", "#FF0097A7")
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 7); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                if (timeInMillis < System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
            }
            val reminder = Reminder(
                title = "${alert.emoji} ${alert.title}",
                description = alert.message,
                dateTimeMillis = cal.timeInMillis,
                priority = when(alert.priority) {
                    AlertPriority.HIGH -> Priority.HIGH
                    AlertPriority.MEDIUM -> Priority.MEDIUM
                    AlertPriority.LOW -> Priority.LOW
                },
                category = cat,
                repeatType = RepeatType.DAILY,
                soundEnabled = true,
                vibrationEnabled = true
            )
            val id = reminderRepository.insertReminder(reminder)
            alarmScheduler.schedule(reminder.copy(id = id))
            _uiState.update { it.copy(addedAlerts = it.addedAlerts + alert.id, successMessage = "${alert.emoji} Reminder added!") }
        }
    }

    fun addAllReminders() {
        _uiState.value.alerts.filter { it.shouldCreateReminder && !_uiState.value.addedAlerts.contains(it.id) }
            .forEach { addWeatherReminder(it) }
        _uiState.update { it.copy(successMessage = "All weather reminders added!") }
    }

    private fun handleResult(result: WeatherResult) {
        when (result) {
            is WeatherResult.Success -> {
                val alerts = WeatherAnalyzer.analyze(result.data)
                _uiState.update { it.copy(isLoading = false, weather = result.data, alerts = alerts, errorMessage = null) }
            }
            is WeatherResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            is WeatherResult.NoApiKey -> _uiState.update { it.copy(isLoading = false, hasApiKey = false) }
            is WeatherResult.Loading -> _uiState.update { it.copy(isLoading = true) }
        }
    }
}
