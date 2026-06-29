package com.lumio.app.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lumio.app.presentation.navigation.Screen

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Appearance ────────────────────────────
            item {
                SettingsGroup(title = "Appearance", icon = Icons.Rounded.Palette) {

                    SettingsLabel("Theme Mode", Icons.Rounded.DarkMode,
                        uiState.themeMode.replaceFirstChar { it.uppercase() })

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "light"  to "Light",
                            "dark"   to "Dark",
                            "amoled" to "AMOLED",
                            "system" to "System"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = uiState.themeMode == key,
                                onClick  = { viewModel.setThemeMode(key) },
                                label    = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                shape    = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Divider()

                    SwitchItem(
                        icon     = Icons.Rounded.ColorLens,
                        title    = "Dynamic Colors",
                        subtitle = "Use wallpaper colors (Android 12+)",
                        checked  = uiState.dynamicColors,
                        onToggle = { viewModel.setDynamicColors(it) }
                    )
                }
            }

            // ── Language ──────────────────────────────
            item {
                SettingsGroup(title = "Language", icon = Icons.Rounded.Language) {
                    listOf(
                        "en" to "English",
                        "ml" to "Malayalam — മലയാളം",
                        "hi" to "Hindi — हिंदी",
                        "ta" to "Tamil — தமிழ்",
                        "te" to "Telugu — తెలుగు"
                    ).forEachIndexed { index, (code, label) ->
                        if (index > 0) Divider()
                        Surface(
                            onClick = { viewModel.setLanguage(code) },
                            color   = Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text     = when (code) {
                                        "en" -> "🇬🇧"
                                        "ml" -> "🇮🇳"
                                        "hi" -> "🇮🇳"
                                        "ta" -> "🇮🇳"
                                        "te" -> "🇮🇳"
                                        else -> "🌐"
                                    },
                                    fontSize = 22.sp
                                )
                                Text(
                                    text     = label,
                                    style    = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                if (uiState.language == code) {
                                    Icon(
                                        Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint     = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Notifications ─────────────────────────
            item {
                SettingsGroup(title = "Notifications", icon = Icons.Rounded.Notifications) {
                    SwitchItem(
                        icon     = Icons.Rounded.VolumeUp,
                        title    = "Sound",
                        subtitle = "Play sound for reminders",
                        checked  = uiState.defaultSound,
                        onToggle = { viewModel.setDefaultSound(it) }
                    )
                    Divider()
                    SwitchItem(
                        icon     = Icons.Rounded.Vibration,
                        title    = "Vibration",
                        subtitle = "Vibrate for reminders",
                        checked  = uiState.defaultVibration,
                        onToggle = { viewModel.setDefaultVibration(it) }
                    )
                }
            }

            // ── Features ──────────────────────────────
            item {
                SettingsGroup(title = "Features", icon = Icons.Rounded.Apps) {
                    ClickItem(
                        icon     = Icons.Rounded.LocationOn,
                        title    = "Location Reminders",
                        subtitle = "Set reminders by location",
                        onClick  = { navController.navigate(Screen.Location.route) }
                    )
                    Divider()
                    ClickItem(
                        icon     = Icons.Rounded.WbSunny,
                        title    = "Weather Reminders",
                        subtitle = "Smart weather-based alerts",
                        onClick  = { navController.navigate(Screen.Weather.route) }
                    )
                    Divider()
                    ClickItem(
                        icon     = Icons.Rounded.BarChart,
                        title    = "Statistics",
                        subtitle = "View your productivity stats",
                        onClick  = { navController.navigate(Screen.Stats.route) }
                    )
                }
            }

            // ── Security ─────────────────────────────
            item {
                SettingsGroup(title = "Security", icon = Icons.Rounded.Security) {
                    SwitchItem(
                        icon     = Icons.Rounded.Fingerprint,
                        title    = "Biometric Lock",
                        subtitle = "Use fingerprint to unlock app",
                        checked  = uiState.biometricEnabled,
                        onToggle = { viewModel.setBiometricEnabled(it) }
                    )
                }
            }

            // ── About ─────────────────────────────────
            item {
                SettingsGroup(title = "About", icon = Icons.Rounded.Info) {
                    SettingsLabel("App", Icons.Rounded.AppShortcut, "LUMIO v1.0.0")
                    Divider()
                    SettingsLabel("Developer", Icons.Rounded.Person, "SARATHPR13")
                    Divider()
                    SettingsLabel("Built with", Icons.Rounded.Code, "Kotlin + Jetpack Compose")
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun SettingsGroup(
    title   : String,
    icon    : ImageVector,
    content : @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier              = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(icon, null,
                tint     = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp))
            Text(title,
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary)
        }
        Card(
            shape  = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun SwitchItem(
    icon: ImageVector, title: String, subtitle: String,
    checked: Boolean, onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null,
            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary))
    }
}

@Composable
private fun ClickItem(
    icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit
) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(icon, null,
                tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.ChevronRight, null,
                tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SettingsLabel(title: String, icon: ImageVector, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null,
            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(
        modifier  = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}
