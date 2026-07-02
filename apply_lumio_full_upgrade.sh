#!/data/data/com.termux/files/usr/bin/bash
# LUMIO — FULL UPGRADE (safe to run even if earlier scripts were or weren't run;
# it simply rewrites these files to the correct final state).
#
# Includes:
#  1) Location on reminders + safe DB migration (keeps your existing reminders)
#  2) Quick-add: attach your current location; geofence "arrive" trigger on save
#  3) Home screen: timeline design + weather card + location badges
#  4) Notification vibration fix (channel-level long pattern, new channel IDs)
set -e

if [ ! -d "app/src/main/java/com/lumio/app" ]; then
  echo "ERROR: run this from the root of the lumio-reminders repo (folder containing 'app/')."
  exit 1
fi

SRC="app/src/main/java/com/lumio/app"

echo "1/9  domain/model/Reminder.kt ..."
mkdir -p "$(dirname "$SRC/domain/model/Reminder.kt")"
cat > "$SRC/domain/model/Reminder.kt" << 'EOF'
package com.lumio.app.domain.model

import java.text.SimpleDateFormat
import java.util.*

data class Reminder(
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val dateTimeMillis: Long = System.currentTimeMillis(),
    val priority: Priority = Priority.NONE,
    val category: Category? = null,
    val repeatType: RepeatType = RepeatType.NONE,
    val isCompleted: Boolean = false,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val imagePath: String? = null,
    val voiceNotePath: String? = null,
    val isHidden: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    // ── Location (optional) — added in the voice/location merge ──
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null
) {
    val hasLocation: Boolean
        get() = latitude != null && longitude != null

    val isOverdue: Boolean
        get() = !isCompleted && dateTimeMillis < System.currentTimeMillis()

    val isToday: Boolean
        get() {
            val r = Calendar.getInstance().apply { timeInMillis = dateTimeMillis }
            val t = Calendar.getInstance()
            return r.get(Calendar.YEAR) == t.get(Calendar.YEAR) &&
                r.get(Calendar.DAY_OF_YEAR) == t.get(Calendar.DAY_OF_YEAR)
        }

    val formattedTime: String
        get() = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(dateTimeMillis))

    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(dateTimeMillis))

    val formattedDateTime: String
        get() = if (isToday) "Today · $formattedTime" else "$formattedDate · $formattedTime"
}
EOF

echo "2/9  data/local/entity/ReminderEntity.kt ..."
mkdir -p "$(dirname "$SRC/data/local/entity/ReminderEntity.kt")"
cat > "$SRC/data/local/entity/ReminderEntity.kt" << 'EOF'
package com.lumio.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.model.RepeatType

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity        = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns  = ["categoryId"],
            onDelete      = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("categoryId"),
        Index("dateTimeMillis"),
        Index("isCompleted"),
        Index("priority")
    ]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String      = "",
    val dateTimeMillis: Long     = System.currentTimeMillis(),
    val priority: Priority       = Priority.NONE,
    val categoryId: Long?        = null,
    val repeatType: RepeatType   = RepeatType.NONE,
    val isCompleted: Boolean     = false,
    val soundEnabled: Boolean    = true,
    val vibrationEnabled: Boolean= true,
    val imagePath: String?       = null,
    val voiceNotePath: String?   = null,
    val isHidden: Boolean        = false,
    val createdAt: Long          = System.currentTimeMillis(),
    val updatedAt: Long          = System.currentTimeMillis(),
    // ── Location (optional) — added in the voice/location merge ──
    val latitude: Double?        = null,
    val longitude: Double?       = null,
    val locationName: String?    = null
) {
    fun toDomain(category: com.lumio.app.domain.model.Category? = null) = Reminder(
        id               = id,
        title            = title,
        description      = description,
        dateTimeMillis   = dateTimeMillis,
        priority         = priority,
        category         = category,
        repeatType       = repeatType,
        isCompleted      = isCompleted,
        soundEnabled     = soundEnabled,
        vibrationEnabled = vibrationEnabled,
        imagePath        = imagePath,
        voiceNotePath    = voiceNotePath,
        isHidden         = isHidden,
        createdAt        = createdAt,
        latitude         = latitude,
        longitude        = longitude,
        locationName     = locationName
    )

    companion object {
        fun fromDomain(reminder: Reminder) = ReminderEntity(
            id               = reminder.id,
            title            = reminder.title,
            description      = reminder.description,
            dateTimeMillis   = reminder.dateTimeMillis,
            priority         = reminder.priority,
            categoryId       = reminder.category?.id,
            repeatType       = reminder.repeatType,
            isCompleted      = reminder.isCompleted,
            soundEnabled     = reminder.soundEnabled,
            vibrationEnabled = reminder.vibrationEnabled,
            imagePath        = reminder.imagePath,
            voiceNotePath    = reminder.voiceNotePath,
            isHidden         = reminder.isHidden,
            createdAt        = reminder.createdAt,
            updatedAt        = System.currentTimeMillis(),
            latitude         = reminder.latitude,
            longitude        = reminder.longitude,
            locationName     = reminder.locationName
        )
    }
}
EOF

echo "3/9  data/local/database/LumioDatabase.kt ..."
mkdir -p "$(dirname "$SRC/data/local/database/LumioDatabase.kt")"
cat > "$SRC/data/local/database/LumioDatabase.kt" << 'EOF'
package com.lumio.app.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lumio.app.data.local.dao.CategoryDao
import com.lumio.app.data.local.dao.ReminderDao
import com.lumio.app.data.local.entity.CategoryEntity
import com.lumio.app.data.local.entity.ReminderEntity

@Database(
    entities  = [ReminderEntity::class, CategoryEntity::class],
    version   = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LumioDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "lumio_database"

        // Safe, additive migration: only ADDS empty nullable columns.
        // Existing reminders are kept intact.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminders ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE reminders ADD COLUMN longitude REAL")
                db.execSQL("ALTER TABLE reminders ADD COLUMN locationName TEXT")
            }
        }

        fun buildDatabase(context: android.content.Context): LumioDatabase {
            return Room.databaseBuilder(
                context,
                LumioDatabase::class.java,
                DATABASE_NAME
            )
            .addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Pre-populate default categories on first launch
                }
            })
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
        }
    }
}
EOF

echo "4/9  LumioApp.kt ..."
mkdir -p "$(dirname "$SRC/LumioApp.kt")"
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

            // Vibration is enabled at the CHANNEL level so it works no matter
            // which code path posts the notification. A long pulsing pattern
            // reads clearly as "reminder". (AlarmReceiver no longer vibrates
            // manually, so there is no double-buzz.)

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS, getString(R.string.channel_reminders_name), NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_reminders_desc)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 600, 250, 600, 250, 600)
                enableLights(true)
                lightColor = Color.parseColor("#FF3B7A57")
                setShowBadge(true)
            }

            val alarmChannel = NotificationChannel(
                CHANNEL_ALARMS, getString(R.string.channel_alarms_name), NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = getString(R.string.channel_alarms_desc)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 300, 800, 300, 800, 300, 800)
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
        // v2 IDs force Android to create fresh channels with the corrected
        // (vibration-on) settings, even on an update install.
        const val CHANNEL_REMINDERS = "lumio_reminders_v2"
        const val CHANNEL_ALARMS = "lumio_alarms_v2"
        const val CHANNEL_SILENT = "lumio_silent_v2"
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

echo "5/9  receiver/AlarmReceiver.kt ..."
mkdir -p "$(dirname "$SRC/receiver/AlarmReceiver.kt")"
cat > "$SRC/receiver/AlarmReceiver.kt" << 'EOF'
package com.lumio.app.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
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
        val priority = runCatching { Priority.valueOf(priorityStr) }.getOrDefault(Priority.NONE)

        showNotification(context, reminderId, title, description, priority, soundOn, vibrationOn)
    }

    private fun showNotification(
        context: Context,
        reminderId: Long,
        title: String,
        description: String,
        priority: Priority,
        soundOn: Boolean,
        vibrationOn: Boolean
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

        // Choose channel. If both sound AND vibration are off, use the silent
        // channel; otherwise use a channel that vibrates (and sounds).
        val channelId = when {
            !soundOn && !vibrationOn -> LumioApp.CHANNEL_SILENT
            priority == Priority.URGENT || priority == Priority.HIGH -> LumioApp.CHANNEL_ALARMS
            else -> LumioApp.CHANNEL_REMINDERS
        }

        val priorityColor = when (priority) {
            Priority.URGENT -> Color.parseColor("#FFD1453B")
            Priority.HIGH -> Color.parseColor("#FFE8833A")
            Priority.MEDIUM -> Color.parseColor("#FFF0A73F")
            Priority.LOW -> Color.parseColor("#FF6BA368")
            Priority.NONE -> Color.parseColor("#FF3B7A57")
        }

        val notifPriority = when (priority) {
            Priority.URGENT -> NotificationCompat.PRIORITY_MAX
            Priority.HIGH -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val bodyText = description.ifBlank {
            "${priority.emoji} ${context.getString(R.string.notif_tap_to_view)}"
        }

        val builder = NotificationCompat.Builder(context, channelId)
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

        // Pre-Android-8 devices have no channels, so set vibration on the
        // notification directly. On Android 8+ this is ignored and the
        // channel's vibration pattern is used instead.
        if (vibrationOn) {
            builder.setVibrate(longArrayOf(0, 600, 250, 600, 250, 600))
        }

        manager.notify(reminderId.toInt(), builder.build())
    }
}
EOF

echo "6/9  presentation/screens/voice/VoiceViewModel.kt ..."
mkdir -p "$(dirname "$SRC/presentation/screens/voice/VoiceViewModel.kt")"
cat > "$SRC/presentation/screens/voice/VoiceViewModel.kt" << 'EOF'
package com.lumio.app.presentation.screens.voice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumio.app.alarm.AlarmScheduler
import com.lumio.app.domain.model.Category
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.model.RepeatType
import com.lumio.app.domain.repository.ReminderRepository
import com.lumio.app.location.GeofenceManager
import com.lumio.app.location.GeofenceTrigger
import com.lumio.app.location.LocationReminder
import com.lumio.app.voice.SpeechRecognitionManager
import com.lumio.app.voice.SpeechState
import com.lumio.app.voice.VoiceParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoiceUiState(
    val speechState: SpeechState = SpeechState.Idle,
    val spokenText: String = "",
    val parsedTitle: String = "",
    val parsedDate: String = "",
    val parsedTime: String = "",
    val parsedPriority: Priority = Priority.NONE,
    val parsedCategory: Category? = null,
    val dateTimeMillis: Long = System.currentTimeMillis() + 3_600_000L,
    val confidence: Float = 0f,
    val isAvailable: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val showPreview: Boolean = false,
    val errorMessage: String? = null,
    // ── Optional attached location (voice/location merge) ──
    val pickedLatitude: Double? = null,
    val pickedLongitude: Double? = null,
    val pickedLocationName: String? = null,
    val suggestions: List<String> = listOf(
        "Remind me to call mom tomorrow at 5 PM",
        "Remind me to take medicine today at 8 PM",
        "Remind me to pay electricity bill next Monday",
        "Remind me to study for exam tomorrow morning",
        "Remind me to buy groceries today at 6 PM"
    )
)

@HiltViewModel
class VoiceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderRepository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler,
    private val geofenceManager: GeofenceManager
) : ViewModel() {

    private val speechManager = SpeechRecognitionManager(context)

    private val _uiState = MutableStateFlow(
        VoiceUiState(isAvailable = speechManager.isAvailable())
    )
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    init {
        observeSpeechState()
    }

    private fun observeSpeechState() {
        viewModelScope.launch {
            speechManager.state.collect { state ->
                _uiState.update { it.copy(speechState = state) }

                when (state) {
                    is SpeechState.Result -> processVoiceResult(state.text)
                    is SpeechState.Error ->
                        _uiState.update { it.copy(errorMessage = state.message) }
                    else -> {}
                }
            }
        }
    }

    fun startListening() {
        _uiState.update { it.copy(errorMessage = null, showPreview = false, spokenText = "") }
        speechManager.startListening()
    }

    fun stopListening() {
        speechManager.stopListening()
    }

    fun reset() {
        speechManager.reset()
        _uiState.update {
            it.copy(
                spokenText = "",
                parsedTitle = "",
                parsedDate = "",
                parsedTime = "",
                parsedPriority = Priority.NONE,
                parsedCategory = null,
                showPreview = false,
                errorMessage = null,
                isSaved = false,
                pickedLatitude = null,
                pickedLongitude = null,
                pickedLocationName = null
            )
        }
    }

    fun useSuggestion(text: String) {
        processVoiceResult(text)
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(parsedTitle = title) }
    }

    fun updatePriority(priority: Priority) {
        _uiState.update { it.copy(parsedPriority = priority) }
    }

    fun updateCategory(category: Category?) {
        _uiState.update { it.copy(parsedCategory = category) }
    }

    // ── Location attachment ──
    fun setLocation(latitude: Double, longitude: Double, name: String) {
        _uiState.update {
            it.copy(
                pickedLatitude = latitude,
                pickedLongitude = longitude,
                pickedLocationName = name
            )
        }
    }

    fun updateLocationName(name: String) {
        _uiState.update { it.copy(pickedLocationName = name) }
    }

    fun clearLocation() {
        _uiState.update {
            it.copy(pickedLatitude = null, pickedLongitude = null, pickedLocationName = null)
        }
    }

    fun saveReminder() {
        val state = _uiState.value
        if (state.parsedTitle.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please provide a reminder title") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val reminder = Reminder(
                title = state.parsedTitle,
                description = if (state.spokenText.isBlank()) ""
                else "Created via voice: \"${state.spokenText}\"",
                dateTimeMillis = state.dateTimeMillis,
                priority = state.parsedPriority,
                category = state.parsedCategory,
                repeatType = RepeatType.NONE,
                soundEnabled = true,
                vibrationEnabled = true,
                latitude = state.pickedLatitude,
                longitude = state.pickedLongitude,
                locationName = state.pickedLocationName
            )

            val id = reminderRepository.insertReminder(reminder)
            alarmScheduler.schedule(reminder.copy(id = id))

            // Register the arrival trigger if a location is attached.
            // Failure (e.g. missing "Allow all the time" permission) does not
            // block saving — the time-based alarm above still fires.
            val lat = state.pickedLatitude
            val lng = state.pickedLongitude
            if (lat != null && lng != null) {
                geofenceManager.addGeofence(
                    LocationReminder(
                        id = "reminder_$id",
                        reminderId = id,
                        title = state.parsedTitle,
                        description = "",
                        latitude = lat,
                        longitude = lng,
                        radiusMeters = 200f,
                        triggerType = GeofenceTrigger.ENTER,
                        locationName = state.pickedLocationName ?: "",
                        isActive = true
                    )
                ) { _, _ -> /* best effort; time alarm is already scheduled */ }
            }

            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    private fun processVoiceResult(text: String) {
        val parsed = VoiceParser.parse(text)
        _uiState.update { state ->
            state.copy(
                spokenText = text,
                parsedTitle = parsed.title,
                parsedDate = parsed.dateDescription,
                parsedTime = parsed.timeDescription,
                parsedPriority = parsed.priority,
                parsedCategory = parsed.suggestedCategory,
                dateTimeMillis = parsed.dateTimeMillis,
                confidence = parsed.confidence,
                showPreview = true,
                errorMessage = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
    }
}
EOF

echo "7/9  presentation/screens/voice/VoiceScreen.kt ..."
mkdir -p "$(dirname "$SRC/presentation/screens/voice/VoiceScreen.kt")"
cat > "$SRC/presentation/screens/voice/VoiceScreen.kt" << 'EOF'
package com.lumio.app.presentation.screens.voice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lumio.app.domain.model.Priority
import com.lumio.app.voice.SpeechState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen(
    navController: NavController,
    viewModel: VoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }

    // Mic permission — request only when the user taps the mic.
    val recordPerm = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.startListening() }

    val onMic: () -> Unit = {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) viewModel.startListening()
        else recordPerm.launch(Manifest.permission.RECORD_AUDIO)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            viewModel.reset()
            navController.popBackStack()
        }
    }

    val listening = uiState.speechState is SpeechState.Listening
    val processing = uiState.speechState is SpeechState.Processing
    val hasPreview = uiState.parsedTitle.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quick add", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopListening()
                        navController.popBackStack()
                    }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                },
                actions = {
                    if (hasPreview) {
                        IconButton(onClick = {
                            input = ""
                            viewModel.reset()
                        }) { Icon(Icons.Rounded.Refresh, "Start over") }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Tell me what to remember",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Type it or tap the mic — I'll work out the time for you.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(20.dp))

            // ── Text input ──
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Call Meera tomorrow at 5 PM") },
                trailingIcon = {
                    IconButton(
                        onClick = { if (input.isNotBlank()) viewModel.useSuggestion(input.trim()) },
                        enabled = input.isNotBlank()
                    ) { Icon(Icons.Rounded.Send, "Parse") }
                },
                shape = RoundedCornerShape(16.dp),
                maxLines = 3
            )

            Spacer(Modifier.height(20.dp))

            // ── Mic ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onMic,
                    shape = CircleShape,
                    color = if (listening) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (processing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(26.dp),
                                strokeWidth = 2.5.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                Icons.Rounded.Mic,
                                contentDescription = "Speak",
                                tint = if (listening) Color.White
                                else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }
            Text(
                text = when {
                    listening -> "Listening…"
                    processing -> "Thinking…"
                    else -> "Tap to speak"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // ── Suggestions (only before a preview exists) ──
            if (!hasPreview) {
                Spacer(Modifier.height(20.dp))
                Text(
                    "Try one of these",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                uiState.suggestions.forEach { s ->
                    Surface(
                        onClick = {
                            input = s
                            viewModel.useSuggestion(s)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(s, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // ── Preview card ──
            if (hasPreview) {
                Spacer(Modifier.height(20.dp))
                PreviewCard(uiState = uiState, viewModel = viewModel)
            }

            // ── Error ──
            uiState.errorMessage?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Text(
                    msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreviewCard(
    uiState: VoiceUiState,
    viewModel: VoiceViewModel
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            Text(
                "PREVIEW",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))

            // Editable title
            OutlinedTextField(
                value = uiState.parsedTitle,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Reminder") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(Modifier.height(14.dp))

            // Date + time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = listOf(uiState.parsedDate, uiState.parsedTime)
                        .filter { it.isNotBlank() }
                        .joinToString("  ·  ")
                        .ifBlank { "No time detected" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(16.dp))

            // Priority
            Text(
                "Priority",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val priorities = listOf(
                    Priority.NONE, Priority.LOW, Priority.MEDIUM, Priority.HIGH, Priority.URGENT
                )
                priorities.forEach { p ->
                    val selected = uiState.parsedPriority == p
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.updatePriority(p) },
                        label = {
                            Text(
                                text = when (p) {
                                    Priority.NONE -> "None"
                                    Priority.LOW -> "Low"
                                    Priority.MEDIUM -> "Med"
                                    Priority.HIGH -> "High"
                                    Priority.URGENT -> "Urgent"
                                }
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Location trigger (real) ──
            LocationSection(uiState = uiState, viewModel = viewModel)

            Spacer(Modifier.height(18.dp))

            // Save
            Button(
                onClick = { viewModel.saveReminder() },
                enabled = uiState.parsedTitle.isNotBlank() && !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save reminder", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationSection(
    uiState: VoiceUiState,
    viewModel: VoiceViewModel
) {
    val context = LocalContext.current
    var locError by remember { mutableStateOf<String?>(null) }

    fun fetchLocation() {
        try {
            val fused = LocationServices.getFusedLocationProviderClient(context)
            fused.lastLocation
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        locError = null
                        viewModel.setLocation(loc.latitude, loc.longitude, "My location")
                    } else {
                        locError = "Couldn't read location. Turn on GPS, open any map app once, then try again."
                    }
                }
                .addOnFailureListener {
                    locError = "Couldn't read location. Check that GPS is on."
                }
        } catch (e: SecurityException) {
            locError = "Location permission was denied."
        }
    }

    val locPerm = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchLocation()
        else locError = "Location permission is needed to attach a place."
    }

    if (uiState.pickedLatitude == null) {
        OutlinedButton(
            onClick = {
                val granted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) fetchLocation()
                else locPerm.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Rounded.Place, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Attach my current location")
        }
        Text(
            "Also reminds you when you arrive here. For that to work while the app is closed, set Location to \"Allow all the time\" in phone Settings.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp, start = 4.dp, end = 4.dp)
        )
    } else {
        OutlinedTextField(
            value = uiState.pickedLocationName ?: "",
            onValueChange = { viewModel.updateLocationName(it) },
            label = { Text("Place name") },
            leadingIcon = {
                Icon(Icons.Rounded.Place, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = {
                IconButton(onClick = { viewModel.clearLocation() }) {
                    Icon(Icons.Rounded.Close, contentDescription = "Remove location")
                }
            },
            supportingText = { Text("Reminds you when you arrive (within ~200 m)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )
    }

    locError?.let {
        Text(
            it,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 6.dp, start = 4.dp)
        )
    }
}
EOF

echo "8/9  presentation/screens/home/HomeScreen.kt ..."
mkdir -p "$(dirname "$SRC/presentation/screens/home/HomeScreen.kt")"
cat > "$SRC/presentation/screens/home/HomeScreen.kt" << 'EOF'
package com.lumio.app.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.Reminder
import com.lumio.app.presentation.components.LumioBottomNavBar
import com.lumio.app.presentation.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            // Painting the background (incl. behind the status bar via
            // statusBarsPadding) with the off-white theme color removes the
            // dark strip that appeared on dark-mode phones.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 12.dp, top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = greeting(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = today(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { navController.navigate(Screen.Voice.route) }) {
                        Icon(Icons.Rounded.Mic, "Quick add", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                        Icon(Icons.Rounded.Search, "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Rounded.Settings, "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(start = 20.dp, end = 32.dp, top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(HomeFilter.values()) { filter ->
                        val selected = uiState.activeFilter == filter
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setFilter(filter) },
                            label = {
                                Text(
                                    "${filter.emoji} ${filter.label}",
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        },
        bottomBar = { LumioBottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddReminder.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) { Icon(Icons.Rounded.Add, "Add reminder", modifier = Modifier.size(26.dp)) }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val sorted = remember(uiState.displayedReminders) {
            uiState.displayedReminders.sortedBy { it.dateTimeMillis }
        }
        val now = System.currentTimeMillis()
        val nowIndex = sorted.indexOfFirst { it.dateTimeMillis > now }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(top = 12.dp, bottom = 120.dp)
        ) {
            item {
                WeatherCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Summary("${uiState.totalCount}", "scheduled", MaterialTheme.colorScheme.onSurface)
                    Summary("${uiState.completedCount}", "done", MaterialTheme.colorScheme.primary)
                    Summary("${uiState.todayCount}", "today", MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.weight(1f))
                    if (uiState.completedCount > 0) {
                        TextButton(onClick = { viewModel.deleteAllCompleted() }) {
                            Text("Clear done", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }

            if (sorted.isEmpty()) {
                item { EmptyState() }
            } else {
                itemsIndexed(sorted) { index, reminder ->
                    if (index == nowIndex) NowMarker()
                    TimelineRow(
                        reminder = reminder,
                        onTap = { navController.navigate(Screen.ReminderDetail.createRoute(reminder.id)) },
                        onToggle = { viewModel.toggleComplete(reminder.id, it) }
                    )
                }
                if (nowIndex == -1) {
                    item { NowMarker() }
                }
            }
        }
    }
}

@Composable
private fun Summary(value: String, label: String, valueColor: Color) {
    Column {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = valueColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun NowMarker() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Text(
            "NOW · ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        )
    }
}

@Composable
private fun TimelineRow(
    reminder: Reminder,
    onTap: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val muted = reminder.isCompleted || (reminder.isOverdue)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clickable { onTap() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.width(58.dp)) {
                Text(
                    reminder.formattedTime,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .width(3.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(priorityColor(reminder.priority).copy(alpha = if (reminder.isCompleted) 0.3f else 1f))
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    reminder.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null
                )
                if (reminder.hasLocation) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(Icons.Rounded.Place, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp))
                        Text(
                            reminder.locationName ?: "Location",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (reminder.description.isNotBlank()) {
                    Text(
                        reminder.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            IconButton(onClick = { onToggle(!reminder.isCompleted) }) {
                Icon(
                    imageVector = if (reminder.isCompleted) Icons.Rounded.CheckCircle
                    else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = "Toggle done",
                    tint = if (reminder.isCompleted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.WbSunny, null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp))
        }
        Text("Nothing scheduled", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "Your day is clear. Tap + or the mic to add a reminder.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun priorityColor(p: Priority): Color = when (p) {
    Priority.URGENT -> Color(0xFFD1453B)
    Priority.HIGH -> Color(0xFFE8833A)
    Priority.MEDIUM -> Color(0xFFF0A73F)
    Priority.LOW -> Color(0xFF6BA368)
    Priority.NONE -> Color(0xFF3B7A57)
}

private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    in 17..20 -> "Good evening"
    else -> "Good night"
}

private fun today(): String =
    SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
EOF

echo "9/9  presentation/screens/home/HomeWeather.kt ..."
mkdir -p "$(dirname "$SRC/presentation/screens/home/HomeWeather.kt")"
cat > "$SRC/presentation/screens/home/HomeWeather.kt" << 'EOF'
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
EOF

echo ""
echo "All 9 files written. Next:"
echo "  git add -A"
echo "  git commit -m \"Full upgrade: location reminders + weather on home + timeline UI + vibration fix\""
echo "  git push"
