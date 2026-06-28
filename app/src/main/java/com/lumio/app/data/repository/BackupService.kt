package com.lumio.app.data.repository

import android.content.Context
import android.os.Environment
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.lumio.app.domain.model.Category
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.repository.CategoryRepository
import com.lumio.app.domain.repository.ReminderRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

data class BackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val reminders: List<Reminder> = emptyList(),
    val categories: List<Category> = emptyList()
)

@Singleton
class BackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderRepo: ReminderRepository,
    private val categoryRepo: CategoryRepository
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun createBackup(): Result<String> = runCatching {
        val reminders  = reminderRepo.getAllReminders().first()
        val categories = categoryRepo.getAllCategories().first()
        val backup     = BackupData(reminders = reminders, categories = categories)
        val json       = gson.toJson(backup)

        val timestamp  = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName   = "lumio_backup_$timestamp.json"
        val dir        = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Lumio")
        dir.mkdirs()
        val file = File(dir, fileName)
        file.writeText(json)
        file.absolutePath
    }

    suspend fun restoreBackup(filePath: String): Result<Int> = runCatching {
        val json   = File(filePath).readText()
        val backup = gson.fromJson(json, BackupData::class.java)

        backup.categories.forEach { categoryRepo.insertCategory(it) }
        backup.reminders.forEach { reminderRepo.insertReminder(it.copy(id = 0)) }
        backup.reminders.size
    }
}
