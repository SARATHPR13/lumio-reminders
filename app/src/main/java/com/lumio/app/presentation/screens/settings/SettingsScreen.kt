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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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

            // ── Appearance ───────────────────────────
            item {
                SettingsSection(title = "Appearance", icon = Icons.Rounded.Palette) {

                    // Theme
                    SettingsItemRow(
                        icon     = Icons.Rounded.DarkMode,
                        title    = "Theme",
                        subtitle = uiState.themeMode.replaceFirstChar { it.uppercase() }
                    )
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                label    = {
                                    Text(label, fontSize = 12.sp)
                                },
                                modifier = Modifier.weight(1f),
                                shape    = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier  = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp
                    )

                    // Dynamic Colors
                    SwitchRow(
                        icon     = Icons.Rounded.ColorLens,
                        title    = "Dynamic Colors",
                        subtitle = "Use wallpaper colors (Android 12+)",
                        checked  = uiState.dynamicColors,
                        onToggle = { viewModel.setDynamicColors(it) }
                    )
                }
            }

            // ── Notifications ─────────────────────────
            item {
                SettingsSection(title = "Notifications", icon = Icons.Rounded.Notifications) {
                    SwitchRow(
                        icon     = Icons.Rounded.VolumeUp,
                        title    = "Default Sound",
                        subtitle = "Play sound for reminders",
                        checked  = uiState.defaultSound,
                        onToggle = { viewModel.setDefaultSound(it) }
                    )
                    HorizontalDivider(
                        modifier  = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp
                    )
                    SwitchRow(
                        icon     = Icons.Rounded.Vibration,
                        title    = "Default Vibration",
                        subtitle = "Vibrate for reminders",
                        checked  = uiState.defaultVibration,
                        onToggle = { viewModel.setDefaultVibration(it) }
                    )
                }
            }

            // ── Security ─────────────────────────────
            item {
                SettingsSection(title = "Security", icon = Icons.Rounded.Security) {
                    SwitchRow(
                        icon     = Icons.Rounded.Fingerprint,
                        title    = "Biometric Lock",
                        subtitle = "Use fingerprint to unlock",
                        checked  = uiState.biometricEnabled,
                        onToggle = { viewModel.setBiometricEnabled(it) }
                    )
                }
            }

            // ── Data ─────────────────────────────────
            item {
                SettingsSection(title = "Data", icon = Icons.Rounded.Storage) {
                    SettingsButton(
                        icon     = Icons.Rounded.Backup,
                        title    = "Backup Reminders",
                        subtitle = "Save reminders to file",
                        onClick  = { viewModel.backupData() }
                    )
                    HorizontalDivider(
                        modifier  = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp
                    )
                    SettingsButton(
                        icon     = Icons.Rounded.RestoreFromTrash,
                        title    = "Restore Reminders",
                        subtitle = "Load reminders from file",
                        onClick  = { viewModel.restoreData() }
                    )
                    HorizontalDivider(
                        modifier  = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp
                    )
                    SettingsButton(
                        icon     = Icons.Rounded.DeleteForever,
                        title    = "Clear All Reminders",
                        subtitle = "Delete all your reminders",
                        onClick  = { viewModel.clearAllReminders() },
                        isDestructive = true
                    )
                }
            }

            // ── About ─────────────────────────────────
            item {
                SettingsSection(title = "About", icon = Icons.Rounded.Info) {
                    SettingsItemRow(
                        icon     = Icons.Rounded.AppShortcut,
                        title    = "LUMIO",
                        subtitle = "Version 1.0.0"
                    )
                    HorizontalDivider(
                        modifier  = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp
                    )
                    SettingsItemRow(
                        icon     = Icons.Rounded.Code,
                        title    = "Built with",
                        subtitle = "Kotlin + Jetpack Compose"
                    )
                    HorizontalDivider(
                        modifier  = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp
                    )
                    SettingsItemRow(
                        icon     = Icons.Rounded.Person,
                        title    = "Developer",
                        subtitle = "SARATHPR13"
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun SettingsSection(
    title   : String,
    icon    : ImageVector,
    content : @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier              = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(18.dp)
            )
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
        }
        Card(
            shape  = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun SwitchRow(
    icon     : ImageVector,
    title    : String,
    subtitle : String,
    checked  : Boolean,
    onToggle : (Boolean) -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked         = checked,
            onCheckedChange = onToggle,
            colors          = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun SettingsItemRow(
    icon     : ImageVector,
    title    : String,
    subtitle : String
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsButton(
    icon          : ImageVector,
    title         : String,
    subtitle      : String,
    onClick       : () -> Unit,
    isDestructive : Boolean = false
) {
    Surface(
        onClick = onClick,
        color   = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = if (isDestructive)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector        = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(18.dp)
            )
        }
    }
}
