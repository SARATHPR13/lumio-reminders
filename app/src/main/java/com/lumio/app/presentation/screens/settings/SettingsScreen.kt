package com.lumio.app.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lumio.app.presentation.components.LumioBottomNavBar

@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = { LumioBottomNavBar(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { SettingsHeader("Appearance") }
            item { SettingsItem(Icons.Rounded.DarkMode, "Dark Mode", "Choose theme") }
            item { SettingsItem(Icons.Rounded.Palette, "Theme Color", "Personalize app color") }
            item { SettingsItem(Icons.Rounded.TextFields, "Font Size", "Adjust text size") }
            item { Spacer(Modifier.height(8.dp)) }
            item { SettingsHeader("Notifications") }
            item { SettingsItem(Icons.Rounded.Notifications, "Default Sound", "Choose ringtone") }
            item { SettingsItem(Icons.Rounded.Vibration, "Vibration", "Vibration pattern") }
            item { Spacer(Modifier.height(8.dp)) }
            item { SettingsHeader("Security") }
            item { SettingsItem(Icons.Rounded.Fingerprint, "Fingerprint Lock", "Biometric authentication") }
            item { SettingsItem(Icons.Rounded.Lock, "PIN Lock", "Set a PIN code") }
            item { Spacer(Modifier.height(8.dp)) }
            item { SettingsHeader("Data") }
            item { SettingsItem(Icons.Rounded.Backup, "Backup", "Save reminders to storage") }
            item { SettingsItem(Icons.Rounded.Restore, "Restore", "Load from backup") }
            item { Spacer(Modifier.height(8.dp)) }
            item { SettingsHeader("About") }
            item { SettingsItem(Icons.Rounded.Info, "About Lumio", "Version 1.0.0") }
        }
    }
}

@Composable
private fun SettingsHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, subtitle: String) {
    ListItem(
        headlineContent   = { Text(title, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(subtitle) },
        leadingContent    = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent   = { Icon(Icons.Rounded.ChevronRight, null) },
        modifier = Modifier
    )
}
