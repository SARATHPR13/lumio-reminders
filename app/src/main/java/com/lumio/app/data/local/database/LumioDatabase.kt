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
