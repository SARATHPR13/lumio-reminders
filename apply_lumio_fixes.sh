#!/data/data/com.termux/files/usr/bin/bash
# Lumio fixes: long vibration + language string externalization (Settings screen)
# Run this from the ROOT of your lumio-reminders repo in Termux.

set -e

if [ ! -d "app/src/main/java/com/lumio/app" ]; then
  echo "ERROR: run this from the root of the lumio-reminders repo (folder containing 'app/')"
  exit 1
fi

SRC="app/src/main/java/com/lumio/app"
RES="app/src/main/res/values"

mkdir -p "$RES"

echo "1) Writing strings.xml..."
cat > "$RES/strings.xml" << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- App -->
    <string name="app_name">Lumio</string>

    <!-- Notification channels (LumioApp.kt) -->
    <string name="channel_reminders_name">Reminders</string>
    <string name="channel_reminders_desc">Your scheduled reminders</string>
    <string name="channel_alarms_name">Priority Alarms</string>
    <string name="channel_alarms_desc">High-priority alarms</string>
    <string name="channel_silent_name">Silent Reminders</string>
    <string name="channel_silent_desc">Silent notifications</string>

    <!-- Reminder notification (AlarmReceiver.kt / NotificationHelper.kt) -->
    <string name="notif_default_title">Reminder</string>
    <string name="notif_tap_to_open">Tap to open your reminder</string>
    <string name="notif_tap_to_view">Tap to view your reminder</string>
    <string name="notif_action_snooze_5">5 min</string>
    <string name="notif_action_snooze_15">15 min</string>
    <string name="notif_action_snooze_30">30 min</string>
    <string name="notif_action_done">Done ✓</string>

    <!-- Settings screen -->
    <string name="settings_title">Settings</string>

    <string name="section_appearance">Appearance</string>
    <string name="theme_mode_label">Theme Mode</string>
    <string name="theme_light">Light</string>
    <string name="theme_dark">Dark</string>
    <string name="theme_amoled">AMOLED</string>
    <string name="theme_system">System</string>
    <string name="dynamic_colors_title">Dynamic Colors</string>
    <string name="dynamic_colors_subtitle">Use wallpaper colors (Android 12+)</string>

    <string name="section_language">Language</string>

    <string name="section_notifications">Notifications</string>
    <string name="sound_title">Sound</string>
    <string name="sound_subtitle">Play sound for reminders</string>
    <string name="vibration_title">Vibration</string>
    <string name="vibration_subtitle">Vibrate for reminders</string>

    <string name="section_features">Features</string>
    <string name="location_reminders_title">Location Reminders</string>
    <string name="location_reminders_subtitle">Set reminders by location</string>
    <string name="weather_reminders_title">Weather Reminders</string>
    <string name="weather_reminders_subtitle">Smart weather-based alerts</string>
    <string name="statistics_title">Statistics</string>
    <string name="statistics_subtitle">View your productivity stats</string>

    <string name="section_security">Security</string>
    <string name="biometric_title">Biometric Lock</string>
    <string name="biometric_subtitle">Use fingerprint to unlock app</string>

    <string name="section_about">About</string>
    <string name="about_app_label">App</string>
    <string name="about_app_value">LUMIO v1.0.0</string>
    <string name="about_developer_label">Developer</string>
    <string name="about_built_with_label">Built with</string>
    <string name="about_built_with_value">Kotlin + Jetpack Compose</string>
</resources>
EOF

echo "2) Writing LumioApp.kt..."
cat > "$SRC/LumioApp.kt" << 'EOF'
package com.lumio.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.lumio.app.crash.CrashHandler
import com.lumio.app.worker.BackupWorker
import com.lumio.app.worker.ReminderWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class LumioApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        CrashHandler.install(this)
        createNotificationChannels()
        schedulePeriodicWork()
    }

    private fun schedulePeriodicWork() {
        val workManager = WorkManager.getInstance(this)

        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(6, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            ).build()

        workManager.enqueueUniquePeriodicWork(
            ReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )

        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresStorageNotLow(true)
                    .build()
            ).build()

        workManager.enqueueUniquePeriodicWork(
            BackupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            backupRequest
        )
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS, getString(R.string.channel_reminders_name), NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_reminders_desc)
                enableVibration(false)
                enableLights(true)
                lightColor = Color.parseColor("#FF1A73E8")
                setShowBadge(true)
            }

            val alarmChannel = NotificationChannel(
                CHANNEL_ALARMS, getString(R.string.channel_alarms_name), NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = getString(R.string.channel_alarms_desc)
                enableVibration(false)
                enableLights(true)
                setBypassDnd(true)
                setShowBadge(true)
            }

            val silentChannel = NotificationChannel(
                CHANNEL_SILENT, getString(R.string.channel_silent_name), NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_silent_desc)
                enableVibration(false)
                setSound(null, null)
                setShowBadge(false)
            }

            manager.createNotificationChannels(listOf(reminderChannel, alarmChannel, silentChannel))
        }
    }

    companion object {
        const val CHANNEL_REMINDERS = "lumio_reminders_channel"
        const val CHANNEL_ALARMS = "lumio_alarms_channel"
        const val CHANNEL_SILENT = "lumio_silent_channel"
        const val ACTION_SNOOZE_5 = "com.lumio.app.ACTION_SNOOZE_5"
        const val ACTION_SNOOZE_15 = "com.lumio.app.ACTION_SNOOZE_15"
        const val ACTION_SNOOZE_30 = "com.lumio.app.ACTION_SNOOZE_30"
        const val ACTION_MARK_DONE = "com.lumio.app.ACTION_MARK_DONE"
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val ACTION_REMINDER_ALARM = "com.lumio.app.REMINDER_ALARM"
        const val DATABASE_NAME = "lumio_database"
    }
}
EOF

echo "3) Writing receiver/AlarmReceiver.kt..."
cat > "$SRC/receiver/AlarmReceiver.kt" << 'EOF'
package com.lumio.app.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.lumio.app.LumioApp
import com.lumio.app.MainActivity
import com.lumio.app.R
import com.lumio.app.domain.model.Priority

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == LumioApp.ACTION_REMINDER_ALARM) {
            handleAlarm(context, intent)
        }
    }

    private fun handleAlarm(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(LumioApp.EXTRA_REMINDER_ID, -1L)
        if (reminderId == -1L) return

        val title = intent.getStringExtra("title") ?: context.getString(R.string.notif_default_title)
        val description = intent.getStringExtra("description") ?: ""
        val priorityStr = intent.getStringExtra("priority") ?: Priority.NONE.name
        val soundOn = intent.getBooleanExtra("sound", true)
        val vibrationOn = intent.getBooleanExtra("vibration", true)
        val priority = runCatching {
            Priority.valueOf(priorityStr)
        }.getOrDefault(Priority.NONE)

        if (vibrationOn) vibrate(context, priority)
        showNotification(context, reminderId, title, description, priority, soundOn)
    }

    private fun showNotification(
        context: Context,
        reminderId: Long,
        title: String,
        description: String,
        priority: Priority,
        soundOn: Boolean
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(LumioApp.EXTRA_REMINDER_ID, reminderId)
        }
        val openPending = PendingIntent.getActivity(
            context, reminderId.toInt(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        fun snoozeIntent(action: String, reqCode: Int) = PendingIntent.getBroadcast(
            context, reqCode,
            Intent(context, NotificationActionReceiver::class.java).apply {
                this.action = action
                putExtra(LumioApp.EXTRA_REMINDER_ID, reminderId)
                putExtra("notification_id", reminderId.toInt())
                putExtra("title", title)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = when (priority) {
            Priority.URGENT, Priority.HIGH -> LumioApp.CHANNEL_ALARMS
            else -> if (soundOn) LumioApp.CHANNEL_REMINDERS else LumioApp.CHANNEL_SILENT
        }

        val priorityColor = when (priority) {
            Priority.URGENT -> Color.parseColor("#FFD32F2F")
            Priority.HIGH -> Color.parseColor("#FFFF6B35")
            Priority.MEDIUM -> Color.parseColor("#FFF9A825")
            Priority.LOW -> Color.parseColor("#FF4CAF50")
            Priority.NONE -> Color.parseColor("#FF1A73E8")
        }

        val notifPriority = when (priority) {
            Priority.URGENT -> NotificationCompat.PRIORITY_MAX
            Priority.HIGH -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val bodyText = description.ifBlank {
            "${priority.emoji} ${context.getString(R.string.notif_tap_to_view)}"
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${priority.emoji} $title")
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setColor(priorityColor)
            .setColorized(true)
            .setPriority(notifPriority)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(openPending)
            .addAction(android.R.drawable.ic_media_pause, context.getString(R.string.notif_action_snooze_5),
                snoozeIntent(LumioApp.ACTION_SNOOZE_5, (reminderId * 10 + 1).toInt()))
            .addAction(android.R.drawable.ic_media_pause, context.getString(R.string.notif_action_snooze_15),
                snoozeIntent(LumioApp.ACTION_SNOOZE_15, (reminderId * 10 + 2).toInt()))
            .addAction(android.R.drawable.ic_media_pause, context.getString(R.string.notif_action_snooze_30),
                snoozeIntent(LumioApp.ACTION_SNOOZE_30, (reminderId * 10 + 3).toInt()))
            .addAction(
                android.R.drawable.checkbox_on_background, context.getString(R.string.notif_action_done),
                PendingIntent.getBroadcast(
                    context, (reminderId * 10 + 4).toInt(),
                    Intent(context, NotificationActionReceiver::class.java).apply {
                        action = LumioApp.ACTION_MARK_DONE
                        putExtra(LumioApp.EXTRA_REMINDER_ID, reminderId)
                        putExtra("notification_id", reminderId.toInt())
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

        manager.notify(reminderId.toInt(), notification)
    }

    private fun vibrate(context: Context, priority: Priority) {
        try {
            val pattern = when (priority) {
                Priority.URGENT, Priority.HIGH ->
                    longArrayOf(0, 900, 250, 900, 250, 900, 250, 900, 250, 900)
                else ->
                    longArrayOf(0, 900, 250, 900, 250, 900)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val mgr = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                mgr.defaultVibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, -1)
                }
            }
        } catch (_: Exception) {}
    }
}
EOF

echo "4) Writing notification/NotificationHelper.kt..."
cat > "$SRC/notification/NotificationHelper.kt" << 'EOF'
package com.lumio.app.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.lumio.app.LumioApp
import com.lumio.app.MainActivity
import com.lumio.app.R
import com.lumio.app.domain.model.Priority
import com.lumio.app.receiver.NotificationActionReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val manager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showReminderNotification(
        reminderId: Long,
        title: String,
        description: String,
        priority: Priority,
        soundEnabled: Boolean
    ) {
        val openPending = buildOpenIntent(reminderId)
        val snooze5Pending = buildSnoozeIntent(reminderId, 5)
        val snooze15Pending = buildSnoozeIntent(reminderId, 15)
        val snooze30Pending = buildSnoozeIntent(reminderId, 30)
        val donePending = buildDoneIntent(reminderId)

        val channelId = when (priority) {
            Priority.URGENT,
            Priority.HIGH -> LumioApp.CHANNEL_ALARMS
            else -> if (soundEnabled) LumioApp.CHANNEL_REMINDERS else LumioApp.CHANNEL_SILENT
        }

        val notifPriority = when (priority) {
            Priority.URGENT -> NotificationCompat.PRIORITY_MAX
            Priority.HIGH -> NotificationCompat.PRIORITY_HIGH
            Priority.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
            else -> NotificationCompat.PRIORITY_LOW
        }

        val priorityColor = when (priority) {
            Priority.URGENT -> Color.parseColor("#FFD32F2F")
            Priority.HIGH -> Color.parseColor("#FFFF6B35")
            Priority.MEDIUM -> Color.parseColor("#FFF9A825")
            Priority.LOW -> Color.parseColor("#FF4CAF50")
            Priority.NONE -> Color.parseColor("#FF1A73E8")
        }

        val bodyText = description.ifBlank {
            "${priority.emoji} ${context.getString(R.string.notif_tap_to_open)}"
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(bodyText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bodyText)
                    .setBigContentTitle("${priority.emoji} $title")
            )
            .setColor(priorityColor)
            .setColorized(true)
            .setPriority(notifPriority)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(openPending)
            .addAction(android.R.drawable.ic_media_pause, context.getString(R.string.notif_action_snooze_5), snooze5Pending)
            .addAction(android.R.drawable.ic_media_pause, context.getString(R.string.notif_action_snooze_15), snooze15Pending)
            .addAction(android.R.drawable.ic_media_pause, context.getString(R.string.notif_action_snooze_30), snooze30Pending)
            .addAction(android.R.drawable.checkbox_on_background, context.getString(R.string.notif_action_done), donePending)
            .build()

        manager.notify(reminderId.toInt(), notification)
    }

    fun cancelNotification(reminderId: Long) {
        manager.cancel(reminderId.toInt())
    }

    private fun buildOpenIntent(reminderId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(LumioApp.EXTRA_REMINDER_ID, reminderId)
        }
        return PendingIntent.getActivity(
            context, reminderId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildSnoozeIntent(reminderId: Long, minutes: Int): PendingIntent {
        val action = when (minutes) {
            5 -> LumioApp.ACTION_SNOOZE_5
            15 -> LumioApp.ACTION_SNOOZE_15
            else -> LumioApp.ACTION_SNOOZE_30
        }
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra(LumioApp.EXTRA_REMINDER_ID, reminderId)
            putExtra("notification_id", reminderId.toInt())
        }
        return PendingIntent.getBroadcast(
            context,
            (reminderId * 10 + minutes).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildDoneIntent(reminderId: Long): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = LumioApp.ACTION_MARK_DONE
            putExtra(LumioApp.EXTRA_REMINDER_ID, reminderId)
            putExtra("notification_id", reminderId.toInt())
        }
        return PendingIntent.getBroadcast(
            context,
            (reminderId * 10 + 99).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
EOF

echo "5) Writing presentation/screens/settings/SettingsScreen.kt..."
mkdir -p "$SRC/presentation/screens/settings"
cat > "$SRC/presentation/screens/settings/SettingsScreen.kt" << 'EOF'
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lumio.app.R
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
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Appearance ────────────────────────────
            item {
                SettingsGroup(title = stringResource(R.string.section_appearance), icon = Icons.Rounded.Palette) {

                    SettingsLabel(
                        stringResource(R.string.theme_mode_label), Icons.Rounded.DarkMode,
                        uiState.themeMode.replaceFirstChar { it.uppercase() }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "light" to stringResource(R.string.theme_light),
                            "dark" to stringResource(R.string.theme_dark),
                            "amoled" to stringResource(R.string.theme_amoled),
                            "system" to stringResource(R.string.theme_system)
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = uiState.themeMode == key,
                                onClick = { viewModel.setThemeMode(key) },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Divider()

                    SwitchItem(
                        icon = Icons.Rounded.ColorLens,
                        title = stringResource(R.string.dynamic_colors_title),
                        subtitle = stringResource(R.string.dynamic_colors_subtitle),
                        checked = uiState.dynamicColors,
                        onToggle = { viewModel.setDynamicColors(it) }
                    )
                }
            }

            // ── Language ──────────────────────────────
            // Language display names intentionally stay as literal native-script
            // strings (not stringResource) — each entry IS a language name in
            // its own language, so translating them would defeat the point.
            item {
                SettingsGroup(title = stringResource(R.string.section_language), icon = Icons.Rounded.Language) {
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
                            color = Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = when (code) {
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
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                if (uiState.language == code) {
                                    Icon(
                                        Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
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
                SettingsGroup(title = stringResource(R.string.section_notifications), icon = Icons.Rounded.Notifications) {
                    SwitchItem(
                        icon = Icons.Rounded.VolumeUp,
                        title = stringResource(R.string.sound_title),
                        subtitle = stringResource(R.string.sound_subtitle),
                        checked = uiState.defaultSound,
                        onToggle = { viewModel.setDefaultSound(it) }
                    )
                    Divider()
                    SwitchItem(
                        icon = Icons.Rounded.Vibration,
                        title = stringResource(R.string.vibration_title),
                        subtitle = stringResource(R.string.vibration_subtitle),
                        checked = uiState.defaultVibration,
                        onToggle = { viewModel.setDefaultVibration(it) }
                    )
                }
            }

            // ── Features ──────────────────────────────
            item {
                SettingsGroup(title = stringResource(R.string.section_features), icon = Icons.Rounded.Apps) {
                    ClickItem(
                        icon = Icons.Rounded.LocationOn,
                        title = stringResource(R.string.location_reminders_title),
                        subtitle = stringResource(R.string.location_reminders_subtitle),
                        onClick = { navController.navigate(Screen.Location.route) }
                    )
                    Divider()
                    ClickItem(
                        icon = Icons.Rounded.WbSunny,
                        title = stringResource(R.string.weather_reminders_title),
                        subtitle = stringResource(R.string.weather_reminders_subtitle),
                        onClick = { navController.navigate(Screen.Weather.route) }
                    )
                    Divider()
                    ClickItem(
                        icon = Icons.Rounded.BarChart,
                        title = stringResource(R.string.statistics_title),
                        subtitle = stringResource(R.string.statistics_subtitle),
                        onClick = { navController.navigate(Screen.Stats.route) }
                    )
                }
            }

            // ── Security ─────────────────────────────
            item {
                SettingsGroup(title = stringResource(R.string.section_security), icon = Icons.Rounded.Security) {
                    SwitchItem(
                        icon = Icons.Rounded.Fingerprint,
                        title = stringResource(R.string.biometric_title),
                        subtitle = stringResource(R.string.biometric_subtitle),
                        checked = uiState.biometricEnabled,
                        onToggle = { viewModel.setBiometricEnabled(it) }
                    )
                }
            }

            // ── About ─────────────────────────────────
            item {
                SettingsGroup(title = stringResource(R.string.section_about), icon = Icons.Rounded.Info) {
                    SettingsLabel(stringResource(R.string.about_app_label), Icons.Rounded.AppShortcut, stringResource(R.string.about_app_value))
                    Divider()
                    SettingsLabel(stringResource(R.string.about_developer_label), Icons.Rounded.Person, "SARATHPR13")
                    Divider()
                    SettingsLabel(stringResource(R.string.about_built_with_label), Icons.Rounded.Code, stringResource(R.string.about_built_with_value))
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(icon, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp))
            Text(title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
        }
        Card(
            shape = RoundedCornerShape(16.dp),
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(icon, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.ChevronRight, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}
EOF

echo ""
echo "Done. Now review with: git status && git diff --stat"
echo "Then: git add -A && git commit -m \"Fix vibration double-buzz + long pattern; externalize strings for Settings screen\" && git push"
