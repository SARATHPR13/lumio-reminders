package com.lumio.app.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lumio.app.data.preferences.FontSize
import com.lumio.app.presentation.components.LumioBottomNavBar
import com.lumio.app.presentation.theme.ThemeMode

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Show snackbar for messages
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar    = { LumioBottomNavBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // ── APPEARANCE ────────────────────────────
            item { SectionHeader("🎨  Appearance") }

            item {
                SettingsCard {
                    // Theme Mode
                    SettingsItem(
                        icon  = Icons.Rounded.DarkMode,
                        title = "Theme Mode",
                        subtitle = uiState.themeMode.name.lowercase()
                            .replaceFirstChar { it.uppercase() }
                    )
                    // Theme chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeMode.values().forEach { mode ->
                            val sel = uiState.themeMode == mode
                            FilterChip(
                                selected = sel,
                                onClick  = { viewModel.setThemeMode(mode) },
                                label    = {
                                    Text(
                                        when (mode) {
                                            ThemeMode.LIGHT  -> "☀️ Light"
                                            ThemeMode.DARK   -> "🌙 Dark"
                                            ThemeMode.AMOLED -> "⚫ AMOLED"
                                            ThemeMode.SYSTEM -> "📱 System"
                                        },
                                        fontSize = 11.sp
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Dynamic Colors
                    SettingsToggle(
                        icon     = Icons.Rounded.Palette,
                        title    = "Dynamic Colors",
                        subtitle = "Use wallpaper colors (Android 12+)",
                        checked  = uiState.dynamicColor,
                        onToggle = { viewModel.setDynamicColor(it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Font Size
                    SettingsItem(
                        icon     = Icons.Rounded.TextFields,
                        title    = "Font Size",
                        subtitle = uiState.fontSize.label
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FontSize.values().forEach { size ->
                            FilterChip(
                                selected = uiState.fontSize == size,
                                onClick  = { viewModel.setFontSize(size) },
                                label    = { Text(size.label, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // ── NOTIFICATIONS ─────────────────────────
            item { SectionHeader("🔔  Notifications") }

            item {
                SettingsCard {
                    SettingsToggle(
                        icon     = Icons.Rounded.VolumeUp,
                        title    = "Default Sound",
                        subtitle = "Play sound for new reminders",
                        checked  = uiState.defaultSound,
                        onToggle = { viewModel.setDefaultSound(it) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsToggle(
                        icon     = Icons.Rounded.Vibration,
                        title    = "Default Vibration",
                        subtitle = "Vibrate for new reminders",
                        checked  = uiState.defaultVibration,
                        onToggle = { viewModel.setDefaultVibration(it) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    // Snooze default
                    SettingsItem(
                        icon     = Icons.Rounded.Snooze,
                        title    = "Default Snooze",
                        subtitle = "${uiState.snoozeDefault} minutes"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 15, 30).forEach { mins ->
                            FilterChip(
                                selected = uiState.snoozeDefault == mins,
                                onClick  = { viewModel.setSnoozeDefault(mins) },
                                label    = { Text("${mins}m", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // ── SECURITY ──────────────────────────────
            item { SectionHeader("🔐  Security") }

            item {
                SettingsCard {
                    // PIN Lock
                    if (uiState.pinEnabled) {
                        SettingsAction(
                            icon     = Icons.Rounded.Lock,
                            title    = "PIN Lock",
                            subtitle = "PIN is enabled — tap to disable",
                            color    = Color(0xFF4CAF50),
                            label    = "Disable",
                            onClick  = { viewModel.disablePin() }
                        )
                    } else {
                        SettingsAction(
                            icon     = Icons.Rounded.LockOpen,
                            title    = "PIN Lock",
                            subtitle = "Set a 4-digit PIN to lock the app",
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            label    = "Set PIN",
                            onClick  = { viewModel.showPinSetup(true) }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Biometric
                    SettingsToggle(
                        icon     = Icons.Rounded.Fingerprint,
                        title    = "Fingerprint Lock",
                        subtitle = "Use fingerprint to unlock",
                        checked  = uiState.biometricEnabled,
                        onToggle = { viewModel.setBiometric(it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Hidden reminders
                    SettingsToggle(
                        icon     = Icons.Rounded.VisibilityOff,
                        title    = "Show Hidden Reminders",
                        subtitle = "Show reminders marked as hidden",
                        checked  = uiState.showHidden,
                        onToggle = { viewModel.setShowHidden(it) }
                    )
                }
            }

            // ── BACKUP ────────────────────────────────
            item { SectionHeader("💾  Backup & Restore") }

            item {
                SettingsCard {
                    // Last backup info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Rounded.History, null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text("Last Backup",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium)
                            Text(uiState.lastBackupText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsToggle(
                        icon     = Icons.Rounded.CloudSync,
                        title    = "Auto Backup",
                        subtitle = "Backup daily automatically",
                        checked  = uiState.autoBackup,
                        onToggle = { viewModel.setAutoBackup(it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Backup now button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick  = { viewModel.createBackup() },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Rounded.Backup, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Backup Now")
                        }
                        OutlinedButton(
                            onClick  = { viewModel.loadBackupFiles() },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Rounded.Restore, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Restore")
                        }
                    }
                }
            }

            // ── LANGUAGE ─────────────────────────────
            item { SectionHeader("🌐  Language") }

            item {
                SettingsCard {
                    val languages = listOf(
                        "English", "Hindi", "Spanish", "French",
                        "German", "Arabic", "Portuguese", "Tamil"
                    )
                    languages.forEachIndexed { index, lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lang,
                                style    = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (uiState.language == lang)
                                    FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f))
                            if (uiState.language == lang) {
                                Icon(Icons.Rounded.CheckCircle, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp))
                            }
                        }
                        if (index < languages.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }

            // ── ABOUT ─────────────────────────────────
            item { SectionHeader("ℹ️  About") }

            item {
                SettingsCard {
                    AboutRow("App Name",    "LUMIO — Smart Reminders")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    AboutRow("Version",     "1.0.0")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    AboutRow("Build",       "Phase 8 — Production")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    AboutRow("Developer",   "SARATHPR13")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    AboutRow("Platform",    "Android 8.0+")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    AboutRow("Architecture","MVVM + Room + Hilt + Compose")
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // ── PIN Setup Dialog ──────────────────────────────
    if (uiState.showPinSetup) {
        PinSetupDialog(
            uiState   = uiState,
            onDigit   = { viewModel.onPinInput(it) },
            onDelete  = { viewModel.onPinDelete() },
            onDismiss = { viewModel.showPinSetup(false) }
        )
    }

    // ── Restore Dialog ────────────────────────────────
    if (uiState.showRestoreDialog) {
        RestoreDialog(
            files     = uiState.backupFiles,
            onRestore = { viewModel.restoreBackup(it) },
            onDismiss = { viewModel.clearMessages() }
        )
    }
}

@Composable
private fun PinSetupDialog(
    uiState: SettingsUiState,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (uiState.pinStep == PinStep.ENTER) "Set PIN" else "Confirm PIN",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    if (uiState.pinStep == PinStep.ENTER)
                        "Enter a 4-digit PIN"
                    else "Re-enter your PIN to confirm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // PIN dots
                val currentPin = if (uiState.pinStep == PinStep.ENTER)
                    uiState.showPinInput else uiState.showPinConfirm

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (index < currentPin.length)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }

                // Number pad
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("", "0", "⌫")
                    ).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { digit ->
                                if (digit.isEmpty()) {
                                    Spacer(Modifier.size(64.dp))
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            if (digit == "⌫") onDelete()
                                            else onDigit(digit)
                                        },
                                        modifier = Modifier.size(64.dp),
                                        shape    = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            digit,
                                            fontSize   = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun RestoreDialog(
    files: List<java.io.File>,
    onRestore: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restore Backup", fontWeight = FontWeight.Bold) },
        text = {
            if (files.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("😔", fontSize = 40.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("No backup files found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select a backup to restore:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    files.forEach { file ->
                        OutlinedButton(
                            onClick  = { onRestore(file.absolutePath) },
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(10.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(file.name,
                                    fontWeight = FontWeight.Medium,
                                    fontSize   = 12.sp)
                                Text(
                                    java.text.SimpleDateFormat(
                                        "MMM dd, yyyy", java.util.Locale.getDefault()
                                    ).format(java.util.Date(file.lastModified())),
                                    fontSize = 10.sp,
                                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ── Reusable Components ───────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        text       = title,
        style      = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color      = MaterialTheme.colorScheme.primary,
        modifier   = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null,
            tint     = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium)
            Text(subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsToggle(
    icon: ImageVector, title: String, subtitle: String,
    checked: Boolean, onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null,
            tint     = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium)
            Text(subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
private fun SettingsAction(
    icon: ImageVector, title: String, subtitle: String,
    color: Color, label: String, onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium)
            Text(subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OutlinedButton(
            onClick = onClick,
            shape   = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(label, fontSize = 12.sp)
        }
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label,
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f))
        Text(value,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium)
    }
}
