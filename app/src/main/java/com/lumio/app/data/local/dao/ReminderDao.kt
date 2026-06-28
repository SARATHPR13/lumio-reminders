package com.lumio.app.data.local.dao

import androidx.room.*
import com.lumio.app.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    // ── Get All ──────────────────────────────────────
    @Query("SELECT * FROM reminders WHERE isHidden = 0 ORDER BY dateTimeMillis ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isHidden = 0 AND isCompleted = 0 ORDER BY dateTimeMillis ASC")
    fun getActiveReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 1 ORDER BY updatedAt DESC")
    fun getCompletedReminders(): Flow<List<ReminderEntity>>

    // ── Get Today ────────────────────────────────────
    @Query("""
        SELECT * FROM reminders 
        WHERE isHidden = 0 
          AND isCompleted = 0
          AND dateTimeMillis BETWEEN :startOfDay AND :endOfDay
        ORDER BY dateTimeMillis ASC
    """)
    fun getTodayReminders(startOfDay: Long, endOfDay: Long): Flow<List<ReminderEntity>>

    // ── Get Upcoming ─────────────────────────────────
    @Query("""
        SELECT * FROM reminders 
        WHERE isHidden = 0 
          AND isCompleted = 0
          AND dateTimeMillis > :after
        ORDER BY dateTimeMillis ASC
    """)
    fun getUpcomingReminders(after: Long): Flow<List<ReminderEntity>>

    // ── Get Priority ─────────────────────────────────
    @Query("""
        SELECT * FROM reminders
        WHERE isHidden = 0
          AND isCompleted = 0
          AND priority IN ('URGENT','HIGH')
        ORDER BY priority DESC, dateTimeMillis ASC
    """)
    fun getPriorityReminders(): Flow<List<ReminderEntity>>

    // ── Get by Category ───────────────────────────────
    @Query("SELECT * FROM reminders WHERE categoryId = :categoryId AND isHidden = 0 ORDER BY dateTimeMillis ASC")
    fun getRemindersByCategory(categoryId: Long): Flow<List<ReminderEntity>>

    // ── Get by ID ────────────────────────────────────
    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: Long): ReminderEntity?

    // ── Search ───────────────────────────────────────
    @Query("""
        SELECT * FROM reminders
        WHERE isHidden = 0
          AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        ORDER BY dateTimeMillis ASC
    """)
    fun searchReminders(query: String): Flow<List<ReminderEntity>>

    // ── Get Due Reminders (for AlarmManager) ─────────
    @Query("""
        SELECT * FROM reminders
        WHERE isCompleted = 0
          AND dateTimeMillis BETWEEN :from AND :to
        ORDER BY dateTimeMillis ASC
    """)
    suspend fun getDueReminders(from: Long, to: Long): List<ReminderEntity>

    // ── Get All Active for Reschedule (after reboot) ──
    @Query("SELECT * FROM reminders WHERE isCompleted = 0 AND dateTimeMillis > :now")
    suspend fun getFutureReminders(now: Long): List<ReminderEntity>

    // ── Counts ────────────────────────────────────────
    @Query("SELECT COUNT(*) FROM reminders WHERE isCompleted = 0 AND isHidden = 0")
    fun getActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM reminders WHERE isCompleted = 1")
    fun getCompletedCount(): Flow<Int>

    // ── Insert / Update / Delete ──────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderEntity>)

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isCompleted = :done, updatedAt = :now WHERE id = :id")
    suspend fun setCompleted(id: Long, done: Boolean, now: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("DELETE FROM reminders WHERE isCompleted = 1")
    suspend fun deleteAllCompleted()
}
