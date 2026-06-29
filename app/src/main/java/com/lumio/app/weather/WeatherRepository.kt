package com.lumio.app.weather
import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
sealed class WeatherResult {
    object Loading : WeatherResult()
    data class Success(val data: WeatherInfo) : WeatherResult()
    data class Error(val message: String) : WeatherResult()
    object NoApiKey : WeatherResult()
}
@Singleton
class WeatherRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private var cachedWeather: WeatherInfo? = null
    private var lastFetchTime: Long = 0L
    private val cacheValidMs = 30 * 60 * 1000L
    suspend fun fetchWeather(latitude: Double, longitude: Double, apiKey: String): WeatherResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "YOUR_OPENWEATHER_API_KEY") return@withContext WeatherResult.NoApiKey
        val now = System.currentTimeMillis()
        cachedWeather?.let { if (now - lastFetchTime < cacheValidMs) return@withContext WeatherResult.Success(it) }
        return@withContext try {
            val url = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=$apiKey"
            val json = URL(url).readText()
            val resp = gson.fromJson(json, WeatherResponse::class.java)
            val info = WeatherAnalyzer.fromResponse(resp)
            cachedWeather = info; lastFetchTime = now
            WeatherResult.Success(info)
        } catch (e: Exception) {
            WeatherResult.Error(when {
                e.message?.contains("401") == true -> "Invalid API key"
                e.message?.contains("UnknownHost") == true -> "No internet connection"
                else -> "Weather unavailable: ${e.message}"
            })
        }
    }
    suspend fun fetchWeatherByCity(cityName: String, apiKey: String): WeatherResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "YOUR_OPENWEATHER_API_KEY") return@withContext WeatherResult.NoApiKey
        return@withContext try {
            val url = "https://api.openweathermap.org/data/2.5/weather?q=${cityName.replace(" ","+")}&appid=$apiKey"
            val json = URL(url).readText()
            val resp = gson.fromJson(json, WeatherResponse::class.java)
            val info = WeatherAnalyzer.fromResponse(resp)
            cachedWeather = info; lastFetchTime = System.currentTimeMillis()
            WeatherResult.Success(info)
        } catch (e: Exception) {
            WeatherResult.Error("City not found or error: ${e.message}")
        }
    }
    fun getCachedWeather(): WeatherInfo? = cachedWeather
}
