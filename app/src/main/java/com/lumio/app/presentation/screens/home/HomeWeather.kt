package com.lumio.app.presentation.screens.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.math.roundToInt

private data class WeatherInfo(
    val temperature: Int,
    val weatherCode: Int,
    val rainChance: Int
)

/**
 * Small weather card for the Home screen.
 * Self-contained on purpose: fetches from Open-Meteo (free, no API key)
 * so it works without any configuration, and turns the forecast into a
 * simple weather-based reminder line ("carry an umbrella", etc).
 */
@Composable
fun WeatherCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    var permitted by remember { mutableStateOf(hasPermission()) }
    var info by remember { mutableStateOf<WeatherInfo?>(null) }
    var failed by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> permitted = granted }

    LaunchedEffect(permitted) {
        if (!permitted || info != null) return@LaunchedEffect
        loading = true
        failed = false
        try {
            val fused = LocationServices.getFusedLocationProviderClient(context)
            val loc = suspendCancellableCoroutine<android.location.Location?> { cont ->
                try {
                    fused.lastLocation
                        .addOnSuccessListener { if (cont.isActive) cont.resume(it) }
                        .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                } catch (e: SecurityException) {
                    if (cont.isActive) cont.resume(null)
                }
            }
            if (loc == null) {
                failed = true
            } else {
                info = withContext(Dispatchers.IO) { fetchWeather(loc.latitude, loc.longitude) }
                if (info == null) failed = true
            }
        } catch (e: Exception) {
            failed = true
        }
        loading = false
    }

    when {
        !permitted -> {
            Surface(
                onClick = { permLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = modifier
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Rounded.Cloud, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Tap to show today's weather here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        loading -> {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = modifier
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Text(
                        "Checking today's weather…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        info != null -> {
            val w = info!!
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = modifier
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (w.weatherCode <= 1) Icons.Rounded.WbSunny
                        else Icons.Rounded.Cloud,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${w.temperature}°  ·  ${conditionLabel(w.weatherCode)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            weatherAdvice(w),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        else -> {
            // failed: stay quiet rather than nag — weather is a bonus, not core
            Spacer(modifier = Modifier.height(0.dp))
        }
    }
}

private fun fetchWeather(lat: Double, lng: Double): WeatherInfo? {
    return try {
        val url = URL(
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$lat&longitude=$lng" +
                "&current=temperature_2m,weather_code" +
                "&hourly=precipitation_probability" +
                "&forecast_days=1&timezone=auto"
        )
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        val body = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()

        val json = JSONObject(body)
        val current = json.getJSONObject("current")
        val temp = current.getDouble("temperature_2m").roundToInt()
        val code = current.getInt("weather_code")

        var rain = 0
        val hourly = json.optJSONObject("hourly")
        val probs = hourly?.optJSONArray("precipitation_probability")
        if (probs != null) {
            for (i in 0 until probs.length()) {
                rain = maxOf(rain, probs.optInt(i, 0))
            }
        }
        WeatherInfo(temperature = temp, weatherCode = code, rainChance = rain)
    } catch (e: Exception) {
        null
    }
}

private fun conditionLabel(code: Int): String = when (code) {
    0 -> "Clear"
    1, 2 -> "Partly cloudy"
    3 -> "Cloudy"
    45, 48 -> "Foggy"
    in 51..67 -> "Rainy"
    in 71..77 -> "Snow"
    in 80..82 -> "Showers"
    in 95..99 -> "Thunderstorm"
    else -> "Cloudy"
}

private fun weatherAdvice(w: WeatherInfo): String = when {
    w.weatherCode in 95..99 -> "Thunderstorm expected — plan around it ⛈️"
    w.rainChance >= 50 || w.weatherCode in 51..82 -> "Rain likely today — carry an umbrella ☔"
    w.temperature >= 35 -> "Very hot today — stay hydrated 💧"
    w.temperature <= 10 -> "Cold today — dress warm 🧥"
    else -> "Clear day ahead — nothing to worry about"
}
