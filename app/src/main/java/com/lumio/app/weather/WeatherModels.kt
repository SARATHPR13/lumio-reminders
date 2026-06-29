package com.lumio.app.weather

data class WeatherResponse(
    val name: String = "",
    val main: WeatherMain = WeatherMain(),
    val weather: List<WeatherDescription> = emptyList(),
    val wind: WindData = WindData(),
    val visibility: Int = 10000,
    val dt: Long = 0L
)
data class WeatherMain(
    val temp: Double = 0.0,
    val feels_like: Double = 0.0,
    val humidity: Int = 0,
    val pressure: Int = 0
)
data class WeatherDescription(
    val id: Int = 0,
    val main: String = "",
    val description: String = "",
    val icon: String = ""
)
data class WindData(val speed: Double = 0.0, val deg: Int = 0)
data class WeatherInfo(
    val cityName: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val description: String,
    val icon: String,
    val windSpeed: Double,
    val visibility: Int,
    val isRaining: Boolean,
    val isStormy: Boolean,
    val isCold: Boolean,
    val isHot: Boolean,
    val isFoggy: Boolean,
    val isWindy: Boolean,
    val uvIndex: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    val temperatureCelsius: Int get() = temperature.toInt()
    val feelsLikeCelsius: Int  get() = feelsLike.toInt()
    val weatherEmoji: String get() = when {
        isStormy  -> "\u26C8\uFE0F"
        isRaining -> "\uD83C\uDF27\uFE0F"
        isFoggy   -> "\uD83C\uDF2B\uFE0F"
        isWindy   -> "\uD83D\uDCA8"
        isHot     -> "\uD83C\uDF21\uFE0F"
        isCold    -> "\u2744\uFE0F"
        else      -> "\u2600\uFE0F"
    }
}
data class WeatherAlert(
    val id: String,
    val emoji: String,
    val title: String,
    val message: String,
    val priority: AlertPriority,
    val shouldCreateReminder: Boolean = true
)
enum class AlertPriority { HIGH, MEDIUM, LOW }
