package com.lumio.app.domain.repository

import com.lumio.app.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {

    fun getAllReminders(): Flow<List<Reminder>>
    fun getActiveReminders(): Flow<List<Reminder>>
    fun getCompletedReminders(): Flow<List<Reminder>>
    fun getTodayReminders(): Flow<List<Reminder>>
    fun getUpcomingReminders(): Flow<List<Reminder>>
    fun getPriorityReminders(): Flow<List<Reminder>>
    fun searchReminders(query: String): Flow<List<Reminder>>
    fun getRemindersByCategory(categoryId: Long): Flow<List<Reminder>>
    fun getActiveCount(): Flow<Int>
    fun getCompletedCount(): Flow<Int>

    suspend fun getReminderById(id: Long): Reminder?
    suspend fun insertReminder(reminder: Reminder): Long
    suspend fun updateReminder(reminder: Reminder)
    suspend fun deleteReminder(id: Long)
    suspend fun setCompleted(id: Long, done: Boolean)
    suspend fun deleteAllCompleted()
    suspend fun getFutureReminders(): List<Reminder>
}
