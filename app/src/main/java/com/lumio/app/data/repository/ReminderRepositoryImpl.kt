package com.lumio.app.data.repository

import com.lumio.app.data.local.dao.CategoryDao
import com.lumio.app.data.local.dao.ReminderDao
import com.lumio.app.data.local.entity.ReminderEntity
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao,
    private val categoryDao: CategoryDao
) : ReminderRepository {

    private fun startOfDay(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun endOfDay(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    private suspend fun ReminderEntity.withCategory(): Reminder {
        val category = categoryId?.let { categoryDao.getCategoryById(it)?.toDomain() }
        return toDomain(category)
    }

    override fun getAllReminders(): Flow<List<Reminder>> =
        reminderDao.getAllReminders().map { list ->
            list.map { entity ->
                val cat = entity.categoryId?.let { categoryDao.getCategoryById(it)?.toDomain() }
                entity.toDomain(cat)
            }
        }

    override fun getActiveReminders(): Flow<List<Reminder>> =
        reminderDao.getActiveReminders().map { list ->
            list.map { entity ->
                val cat = entity.categoryId?.let { categoryDao.getCategoryById(it)?.toDomain() }
                entity.toDomain(cat)
            }
        }

    override fun getCompletedReminders(): Flow<List<Reminder>> =
        reminderDao.getCompletedReminders().map { list ->
            list.map { entity ->
                val cat = entity.categoryId?.let { categoryDao.getCategoryById(it)?.toDomain() }
                entity.toDomain(cat)
            }
        }

    override fun getTodayReminders(): Flow<List<Reminder>> =
        reminderDao.getTodayReminders(startOfDay(), endOfDay()).map { list ->
            list.map { entity ->
                val cat = entity.categoryId?.let { categoryDao.getCategoryById(it)?.toDomain() }
                entity.toDomain(cat)
            }
        }

    override fun getUpcomingReminders(): Flow<List<Reminder>> =
        reminderDao.getUpcomingReminders(endOfDay()).map { list ->
            list.map { entity ->
                val cat = entity.categoryId?.let { categoryDao.getCategoryById(it)?.toDomain() }
                entity.toDomain(cat)
            }
        }

    override fun getPriorityReminders(): Flow<List<Reminder>> =
        reminderDao.getPriorityReminders().map { list ->
            list.map { entity ->
                val cat = entity.categoryId?.let { categoryDao.getCategoryById(it)?.toDomain() }
                entity.toDomain(cat)
            }
        }

    override fun searchReminders(query: String): Flow<List<Reminder>> =
        reminderDao.searchReminders(query).map { list ->
            list.map { entity ->
                val cat = entity.categoryId?.let { categoryDao.getCategoryById(it)?.toDomain() }
                entity.toDomain(cat)
            }
        }

    override fun getRemindersByCategory(categoryId: Long): Flow<List<Reminder>> =
        reminderDao.getRemindersByCategory(categoryId).map { list ->
            list.map { entity ->
                val cat = entity.categoryId?.let { categoryDao.getCategoryById(it)?.toDomain() }
                entity.toDomain(cat)
            }
        }

    override fun getActiveCount(): Flow<Int> = reminderDao.getActiveCount()

    override fun getCompletedCount(): Flow<Int> = reminderDao.getCompletedCount()

    override suspend fun getReminderById(id: Long): Reminder? {
        val entity = reminderDao.getReminderById(id) ?: return null
        val cat = entity.categoryId?.let { categoryDao.getCategoryById(it)?.toDomain() }
        return entity.toDomain(cat)
    }

    override suspend fun insertReminder(reminder: Reminder): Long =
        reminderDao.insertReminder(ReminderEntity.fromDomain(reminder))

    override suspend fun updateReminder(reminder: Reminder) =
        reminderDao.updateReminder(ReminderEntity.fromDomain(reminder))

    override suspend fun deleteReminder(id: Long) =
        reminderDao.deleteReminderById(id)

    override suspend fun setCompleted(id: Long, done: Boolean) =
        reminderDao.setCompleted(id, done)

    override suspend fun deleteAllCompleted() =
        reminderDao.deleteAllCompleted()

    override suspend fun getFutureReminders(): List<Reminder> =
        reminderDao.getFutureReminders(System.currentTimeMillis()).map { entity ->
            val cat = entity.categoryId?.let { categoryDao.getCategoryById(it)?.toDomain() }
            entity.toDomain(cat)
        }
}
