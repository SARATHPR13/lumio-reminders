#!/data/data/com.termux/files/usr/bin/bash
# LUMIO — location merge, STEP 1: data foundation (safe, additive DB migration)
# Adds latitude/longitude/locationName to reminders. Existing reminders are
# preserved (migration only ADDS empty columns). No visible UI change yet.
set -e

if [ ! -d "app/src/main/java/com/lumio/app" ]; then
  echo "ERROR: run this from the root of the lumio-reminders repo (folder containing 'app/')."
  exit 1
fi

SRC="app/src/main/java/com/lumio/app"

echo "1/3  domain/model/Reminder.kt ..."
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

echo "2/3  data/local/entity/ReminderEntity.kt ..."
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

echo "3/3  data/local/database/LumioDatabase.kt ..."
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

echo ""
echo "Step 1 done (data foundation). Next:"
echo "  git add -A"
echo "  git commit -m \"Location merge step 1: add location fields + safe DB migration\""
echo "  git push"
