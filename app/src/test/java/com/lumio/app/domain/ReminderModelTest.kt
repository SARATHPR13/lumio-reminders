package com.lumio.app.domain

import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.model.RepeatType
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ReminderModelTest {

    // ── isToday ───────────────────────────────────────
    @Test
    fun `reminder scheduled today returns isToday true`() {
        val reminder = Reminder(
            title          = "Test",
            dateTimeMillis = System.currentTimeMillis() + 3_600_000L
        )
        assertTrue("Reminder scheduled today should be isToday", reminder.isToday)
    }

    @Test
    fun `reminder scheduled tomorrow returns isToday false`() {
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis
        val reminder = Reminder(title = "Test", dateTimeMillis = tomorrow)
        assertFalse("Tomorrow reminder should not be isToday", reminder.isToday)
    }

    // ── isOverdue ─────────────────────────────────────
    @Test
    fun `past uncompleted reminder is overdue`() {
        val reminder = Reminder(
            title          = "Test",
            dateTimeMillis = System.currentTimeMillis() - 3_600_000L,
            isCompleted    = false
        )
        assertTrue("Past uncompleted reminder should be overdue", reminder.isOverdue)
    }

    @Test
    fun `completed reminder is not overdue even if past`() {
        val reminder = Reminder(
            title          = "Test",
            dateTimeMillis = System.currentTimeMillis() - 3_600_000L,
            isCompleted    = true
        )
        assertFalse("Completed reminder should never be overdue", reminder.isOverdue)
    }

    @Test
    fun `future reminder is not overdue`() {
        val reminder = Reminder(
            title          = "Test",
            dateTimeMillis = System.currentTimeMillis() + 3_600_000L,
            isCompleted    = false
        )
        assertFalse("Future reminder should not be overdue", reminder.isOverdue)
    }

    // ── formattedTime ─────────────────────────────────
    @Test
    fun `formattedTime returns non empty string`() {
        val reminder = Reminder(
            title          = "Test",
            dateTimeMillis = System.currentTimeMillis()
        )
        assertTrue("formattedTime should not be empty", reminder.formattedTime.isNotBlank())
    }

    // ── formattedDate ─────────────────────────────────
    @Test
    fun `formattedDate returns non empty string`() {
        val reminder = Reminder(
            title          = "Test",
            dateTimeMillis = System.currentTimeMillis()
        )
        assertTrue("formattedDate should not be empty", reminder.formattedDate.isNotBlank())
    }

    // ── Priority ──────────────────────────────────────
    @Test
    fun `priority level ordering is correct`() {
        assertTrue(Priority.URGENT.level > Priority.HIGH.level)
        assertTrue(Priority.HIGH.level   > Priority.MEDIUM.level)
        assertTrue(Priority.MEDIUM.level > Priority.LOW.level)
        assertTrue(Priority.LOW.level    > Priority.NONE.level)
    }

    @Test
    fun `priority color hex values are valid`() {
        Priority.values().forEach { priority ->
            assertTrue(
                "Priority ${priority.name} should have valid color hex",
                priority.colorHex.startsWith("#") && priority.colorHex.length == 9
            )
        }
    }

    // ── RepeatType ────────────────────────────────────
    @Test
    fun `all repeat types have non empty labels`() {
        RepeatType.values().forEach { repeat ->
            assertTrue(
                "RepeatType ${repeat.name} should have non-empty label",
                repeat.label.isNotBlank()
            )
        }
    }

    // ── Default values ────────────────────────────────
    @Test
    fun `reminder default values are correct`() {
        val reminder = Reminder(title = "Test")
        assertFalse("New reminder should not be completed", reminder.isCompleted)
        assertFalse("New reminder should not be hidden", reminder.isHidden)
        assertTrue("New reminder should have sound enabled", reminder.soundEnabled)
        assertTrue("New reminder should have vibration enabled", reminder.vibrationEnabled)
        assertEquals("Default priority should be NONE", Priority.NONE, reminder.priority)
        assertEquals("Default repeat should be NONE", RepeatType.NONE, reminder.repeatType)
    }

    // ── Copy ──────────────────────────────────────────
    @Test
    fun `copy preserves all fields correctly`() {
        val original = Reminder(
            id             = 1L,
            title          = "Original",
            description    = "Description",
            priority       = Priority.HIGH,
            isCompleted    = false
        )
        val copy = original.copy(isCompleted = true)
        assertEquals("Title should be preserved", original.title, copy.title)
        assertEquals("Description should be preserved", original.description, copy.description)
        assertEquals("Priority should be preserved", original.priority, copy.priority)
        assertTrue("isCompleted should be updated", copy.isCompleted)
    }
}
