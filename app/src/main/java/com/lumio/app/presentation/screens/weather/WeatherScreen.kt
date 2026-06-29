package com.lumio.app.presentation.screens.weather
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lumio.app.weather.*

@Composable
fun WeatherScreen(navController: NavController, viewModel: WeatherViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(uiState.successMessage) { uiState.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() } }
    LaunchedEffect(uiState.errorMessage) { uiState.errorMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() } }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather Reminders", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack,"Back") } },
                actions = { IconButton(onClick = { viewModel.fetchByLocation() }) { Icon(Icons.Rounded.Refresh,"Refresh") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (!uiState.hasApiKey) {
                item {
                    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("\uD83C\uDF24\uFE0F", fontSize = 36.sp)
                                Column {
                                    Text("Set Up Weather", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("Get smart weather reminders", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                            Text("Add your free API key from openweathermap.org to enable weather features!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Button(onClick = { viewModel.showApiKeyInfo(true) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) { Text("How to Set Up") }
                        }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = uiState.cityInput, onValueChange = { viewModel.setCityInput(it) }, placeholder = { Text("Search city...") }, leadingIcon = { Icon(Icons.Rounded.Search,null) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true)
                            Button(onClick = { viewModel.fetchByCity() }, shape = RoundedCornerShape(12.dp), modifier = Modifier.height(56.dp), enabled = uiState.hasApiKey) { Text("Go") }
                        }
                        OutlinedButton(onClick = { viewModel.fetchByLocation() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), enabled = uiState.hasApiKey) {
                            Icon(Icons.Rounded.MyLocation, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Use My Location")
                        }
                    }
                }
            }
            if (uiState.isLoading) {
                item { Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) { CircularProgressIndicator(); Text("Getting weather...") } } }
            }
            uiState.weather?.let { weather ->
                item {
                    val gradient = when {
                        weather.isStormy -> listOf(Color(0xFF37474F), Color(0xFF546E7A))
                        weather.isRaining -> listOf(Color(0xFF1565C0), Color(0xFF1976D2))
                        weather.isHot -> listOf(Color(0xFFE65100), Color(0xFFEF6C00))
                        weather.isCold -> listOf(Color(0xFF0277BD), Color(0xFF0288D1))
                        else -> listOf(Color(0xFF1A73E8), Color(0xFF00897B))
                    }
                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Brush.verticalGradient(gradient)).padding(24.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(weather.cityName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    Text(weather.description.replaceFirstChar{it.uppercase()}, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                                }
                                Text(weather.weatherEmoji, fontSize = 52.sp)
                            }
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("${weather.temperatureCelsius}\u00B0C", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                Text("Feels like ${weather.feelsLikeCelsius}\u00B0C", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.padding(bottom = 4.dp))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("\uD83D\uDCA7", fontSize = 18.sp); Text("${weather.humidity}%", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp); Text("Humidity", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp) }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("\uD83D\uDCA8", fontSize = 18.sp); Text("${weather.windSpeed.toInt()}km/h", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp); Text("Wind", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp) }
                            }
                        }
                    }
                }
                if (uiState.alerts.isNotEmpty()) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("\uD83D\uDD14 Alerts & Tips", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            val addable = uiState.alerts.count { it.shouldCreateReminder && !uiState.addedAlerts.contains(it.id) }
                            if (addable > 0) TextButton(onClick = { viewModel.addAllReminders() }) { Text("Add All ($addable)") }
                        }
                    }
                    items(uiState.alerts) { alert ->
                        val bgColor = when(alert.priority) { AlertPriority.HIGH -> Color(0xFFD32F2F).copy(alpha=0.08f); AlertPriority.MEDIUM -> Color(0xFFF9A825).copy(alpha=0.08f); AlertPriority.LOW -> Color(0xFF4CAF50).copy(alpha=0.08f) }
                        val borderColor = when(alert.priority) { AlertPriority.HIGH -> Color(0xFFD32F2F); AlertPriority.MEDIUM -> Color(0xFFF9A825); AlertPriority.LOW -> Color(0xFF4CAF50) }
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = bgColor), elevation = CardDefaults.cardElevation(0.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(borderColor.copy(alpha=0.15f)), contentAlignment = Alignment.Center) { Text(alert.emoji, fontSize = 24.sp) }
                                Column(Modifier.weight(1f)) {
                                    Text(alert.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(alert.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                }
                                if (alert.shouldCreateReminder) {
                                    if (uiState.addedAlerts.contains(alert.id)) Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(32.dp))
                                    else IconButton(onClick = { viewModel.addWeatherReminder(alert) }, modifier = Modifier.size(36.dp).background(borderColor, RoundedCornerShape(10.dp))) { Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                                }
                            }
                        }
                    }
                }
            }
            if (!uiState.isLoading && uiState.weather == null && uiState.hasApiKey) {
                item { Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("\uD83C\uDF24\uFE0F", fontSize = 64.sp); Text("Search a city or use location", textAlign = TextAlign.Center) } } }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
    if (uiState.showApiKeyInfo) {
        AlertDialog(
            onDismissRequest = { viewModel.showApiKeyInfo(false) },
            title = { Text("Get Free Weather API Key") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("1. Go to: openweathermap.org"); Text("2. Click Sign Up (free)"); Text("3. Check email and verify"); Text("4. Go to API keys section"); Text("5. Copy your API key")
                Text("6. Open: app/src/main/res/values/weather_config.xml"); Text("7. Replace YOUR_OPENWEATHER_API_KEY with your key"); Text("8. Push and rebuild")
                Spacer(Modifier.height(4.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text("Free: 1000 calls/day — more than enough!", modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.bodySmall)
                }
            }},
            confirmButton = { TextButton(onClick = { viewModel.showApiKeyInfo(false) }) { Text("Got it!") } }
        )
    }
}
