package com.lumio.app.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lumio.app.data.local.dao.CategoryDao
import com.lumio.app.data.local.dao.ReminderDao
import com.lumio.app.data.local.entity.CategoryEntity
import com.lumio.app.data.local.entity.ReminderEntity
import com.lumio.app.domain.model.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities  = [ReminderEntity::class, CategoryEntity::class],
    version   = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LumioDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "lumio_database"

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
            .fallbackToDestructiveMigration()
            .build()
        }
    }
}
