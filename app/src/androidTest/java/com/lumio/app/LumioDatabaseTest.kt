package com.lumio.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lumio.app.data.local.dao.CategoryDao
import com.lumio.app.data.local.dao.ReminderDao
import com.lumio.app.data.local.database.LumioDatabase
import com.lumio.app.data.local.entity.CategoryEntity
import com.lumio.app.data.local.entity.ReminderEntity
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.RepeatType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LumioDatabaseTest {

    private lateinit var db: LumioDatabase
    private lateinit var reminderDao: ReminderDao
    private lateinit var categoryDao: CategoryDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LumioDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        reminderDao = db.reminderDao()
        categoryDao = db.categoryDao()
    }

    @After
    fun closeDb() { db.close() }

    // ── Insert & Retrieve ─────────────────────────────
    @Test
    fun insertAndRetrieveReminder() = runBlocking {
        val entity = ReminderEntity(
            title          = "Test Reminder",
            description    = "Test description",
            dateTimeMillis = System.currentTimeMillis() + 3_600_000L,
            priority       = Priority.HIGH,
            repeatType     = RepeatType.NONE
        )
        val id = reminderDao.insertReminder(entity)
        assertTrue("ID should be positive", id > 0)

        val retrieved = reminderDao.getReminderById(id)
        assertNotNull("Retrieved reminder should not be null", retrieved)
        assertEquals("Title should match", "Test Reminder", retrieved?.title)
        assertEquals("Description should match", "Test description", retrieved?.description)
        assertEquals("Priority should match", Priority.HIGH, retrieved?.priority)
    }

    // ── Update ────────────────────────────────────────
    @Test
    fun updateReminder() = runBlocking {
        val entity = ReminderEntity(title = "Original")
        val id     = reminderDao.insertReminder(entity)
        val updated = entity.copy(id = id, title = "Updated Title")
        reminderDao.updateReminder(updated)

        val retrieved = reminderDao.getReminderById(id)
        assertEquals("Title should be updated", "Updated Title", retrieved?.title)
    }

    // ── Mark Completed ────────────────────────────────
    @Test
    fun markReminderCompleted() = runBlocking {
        val entity = ReminderEntity(title = "Test", isCompleted = false)
        val id     = reminderDao.insertReminder(entity)
        reminderDao.setCompleted(id, true)

        val retrieved = reminderDao.getReminderById(id)
        assertTrue("Reminder should be completed", retrieved?.isCompleted == true)
    }

    // ── Delete ────────────────────────────────────────
    @Test
    fun deleteReminder() = runBlocking {
        val entity = ReminderEntity(title = "To Delete")
        val id     = reminderDao.insertReminder(entity)
        reminderDao.deleteReminderById(id)

        val retrieved = reminderDao.getReminderById(id)
        assertNull("Deleted reminder should be null", retrieved)
    }

    // ── Search ────────────────────────────────────────
    @Test
    fun searchReminders() = runBlocking {
        reminderDao.insertReminder(ReminderEntity(title = "Call Doctor"))
        reminderDao.insertReminder(ReminderEntity(title = "Buy Milk"))
        reminderDao.insertReminder(ReminderEntity(title = "Call Mom"))

        val results = reminderDao.searchReminders("call").first()
        assertEquals("Should find 2 reminders with 'call'", 2, results.size)
    }

    @Test
    fun searchIsCaseInsensitive() = runBlocking {
        reminderDao.insertReminder(ReminderEntity(title = "Call Doctor"))
        val results = reminderDao.searchReminders("CALL").first()
        assertTrue("Search should be case insensitive", results.isNotEmpty())
    }

    // ── Get All ───────────────────────────────────────
    @Test
    fun getAllReminders() = runBlocking {
        reminderDao.insertReminder(ReminderEntity(title = "Reminder 1"))
        reminderDao.insertReminder(ReminderEntity(title = "Reminder 2"))
        reminderDao.insertReminder(ReminderEntity(title = "Reminder 3"))

        val all = reminderDao.getAllReminders().first()
        assertEquals("Should have 3 reminders", 3, all.size)
    }

    // ── Delete All Completed ──────────────────────────
    @Test
    fun deleteAllCompleted() = runBlocking {
        reminderDao.insertReminder(ReminderEntity(title = "Active",    isCompleted = false))
        reminderDao.insertReminder(ReminderEntity(title = "Done 1",   isCompleted = true))
        reminderDao.insertReminder(ReminderEntity(title = "Done 2",   isCompleted = true))

        reminderDao.deleteAllCompleted()
        val all = reminderDao.getAllReminders().first()
        assertEquals("Should only have 1 active reminder", 1, all.size)
        assertEquals("Remaining should be Active", "Active", all.first().title)
    }

    // ── Category ──────────────────────────────────────
    @Test
    fun insertAndRetrieveCategory() = runBlocking {
        val cat = CategoryEntity(name = "Work", emoji = "💼", colorHex = "#FF1A73E8")
        val id  = categoryDao.insertCategory(cat)
        assertTrue("Category ID should be positive", id > 0)

        val retrieved = categoryDao.getCategoryById(id)
        assertNotNull("Category should not be null", retrieved)
        assertEquals("Name should match", "Work", retrieved?.name)
    }

    @Test
    fun deleteCategory() = runBlocking {
        val cat = CategoryEntity(name = "Temp", emoji = "📌", colorHex = "#FF000000")
        val id  = categoryDao.insertCategory(cat)
        categoryDao.deleteCategoryById(id)

        val retrieved = categoryDao.getCategoryById(id)
        assertNull("Deleted category should be null", retrieved)
    }

    @Test
    fun getCategoryCount() = runBlocking {
        categoryDao.insertCategory(CategoryEntity(name = "A", emoji = "A", colorHex = "#FF000000"))
        categoryDao.insertCategory(CategoryEntity(name = "B", emoji = "B", colorHex = "#FF000000"))
        val count = categoryDao.getCategoryCount()
        assertEquals("Should have 2 categories", 2, count)
    }

    // ── Future Reminders ─────────────────────────────
    @Test
    fun getFutureReminders() = runBlocking {
        val past   = ReminderEntity(title = "Past",   dateTimeMillis = System.currentTimeMillis() - 3_600_000L, isCompleted = false)
        val future = ReminderEntity(title = "Future", dateTimeMillis = System.currentTimeMillis() + 3_600_000L, isCompleted = false)
        val done   = ReminderEntity(title = "Done",   dateTimeMillis = System.currentTimeMillis() + 3_600_000L, isCompleted = true)
        reminderDao.insertReminder(past)
        reminderDao.insertReminder(future)
        reminderDao.insertReminder(done)

        val futures = reminderDao.getFutureReminders(System.currentTimeMillis())
        assertEquals("Should return only 1 future active reminder", 1, futures.size)
        assertEquals("Should be the future reminder", "Future", futures.first().title)
    }
}
