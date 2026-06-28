package com.lumio.app.voice

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class DateTimeParserTest {

    private val now = System.currentTimeMillis()
    private val oneHour = 3_600_000L
    private val oneDay  = 86_400_000L

    // ── Date parsing ──────────────────────────────────
    @Test
    fun `parses today correctly`() {
        val result = DateTimeParser.parse("remind me today at 3 PM")
        val today  = Calendar.getInstance()
        val parsed = Calendar.getInstance().apply { timeInMillis = result.millis }
        assertEquals("Day should be today",
            today.get(Calendar.DAY_OF_YEAR), parsed.get(Calendar.DAY_OF_YEAR))
    }

    @Test
    fun `parses tomorrow correctly`() {
        val result   = DateTimeParser.parse("remind me tomorrow at 9 AM")
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val parsed   = Calendar.getInstance().apply { timeInMillis = result.millis }
        assertEquals("Day should be tomorrow",
            tomorrow.get(Calendar.DAY_OF_YEAR), parsed.get(Calendar.DAY_OF_YEAR))
    }

    @Test
    fun `parses next monday correctly`() {
        val result = DateTimeParser.parse("remind me next Monday at 10 AM")
        val parsed = Calendar.getInstance().apply { timeInMillis = result.millis }
        assertEquals("Day should be Monday",
            Calendar.MONDAY, parsed.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun `parses next friday correctly`() {
        val result = DateTimeParser.parse("call doctor next Friday")
        val parsed = Calendar.getInstance().apply { timeInMillis = result.millis }
        assertEquals("Day should be Friday",
            Calendar.FRIDAY, parsed.get(Calendar.DAY_OF_WEEK))
    }

    // ── Time parsing ──────────────────────────────────
    @Test
    fun `parses 6 PM correctly`() {
        val result = DateTimeParser.parse("remind me tomorrow at 6 PM")
        val parsed = Calendar.getInstance().apply { timeInMillis = result.millis }
        assertEquals("Hour should be 18", 18, parsed.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `parses 9 AM correctly`() {
        val result = DateTimeParser.parse("remind me tomorrow at 9 AM")
        val parsed = Calendar.getInstance().apply { timeInMillis = result.millis }
        assertEquals("Hour should be 9", 9, parsed.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `parses 12 PM as noon`() {
        val result = DateTimeParser.parse("remind me tomorrow at 12 PM")
        val parsed = Calendar.getInstance().apply { timeInMillis = result.millis }
        assertEquals("Hour should be 12", 12, parsed.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `parses 12 AM as midnight`() {
        val result = DateTimeParser.parse("remind me tomorrow at 12 AM")
        val parsed = Calendar.getInstance().apply { timeInMillis = result.millis }
        assertEquals("Hour should be 0", 0, parsed.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `parses time with minutes correctly`() {
        val result = DateTimeParser.parse("remind me tomorrow at 6:30 PM")
        val parsed = Calendar.getInstance().apply { timeInMillis = result.millis }
        assertEquals("Hour should be 18",  18, parsed.get(Calendar.HOUR_OF_DAY))
        assertEquals("Minute should be 30", 30, parsed.get(Calendar.MINUTE))
    }

    // ── Relative time ────────────────────────────────
    @Test
    fun `parses morning as 9 AM`() {
        val result = DateTimeParser.parse("remind me tomorrow morning")
        val parsed = Calendar.getInstance().apply { timeInMillis = result.millis }
        assertEquals("Morning should be 9 AM", 9, parsed.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `parses evening as 6 PM`() {
        val result = DateTimeParser.parse("remind me tomorrow evening")
        val parsed = Calendar.getInstance().apply { timeInMillis = result.millis }
        assertEquals("Evening should be 18", 18, parsed.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `parses noon correctly`() {
        val result = DateTimeParser.parse("remind me tomorrow at noon")
        val parsed = Calendar.getInstance().apply { timeInMillis = result.millis }
        assertEquals("Noon should be 12", 12, parsed.get(Calendar.HOUR_OF_DAY))
    }

    // ── Date descriptions ─────────────────────────────
    @Test
    fun `date description for today is Today`() {
        val result = DateTimeParser.parse("today at 8 PM")
        assertEquals("Date description should be Today", "Today", result.dateDescription)
    }

    @Test
    fun `date description for tomorrow is Tomorrow`() {
        val result = DateTimeParser.parse("tomorrow at 8 PM")
        assertEquals("Date description should be Tomorrow", "Tomorrow", result.dateDescription)
    }

    // ── Future guarantee ─────────────────────────────
    @Test
    fun `parsed time is always in the future`() {
        val result = DateTimeParser.parse("today at 3 AM")
        assertTrue("Parsed time should be in the future",
            result.millis > System.currentTimeMillis())
    }

    @Test
    fun `confidence is between 0 and 1`() {
        val result = DateTimeParser.parse("remind me tomorrow at 6 PM")
        assertTrue("Confidence should be >= 0", result.confidence >= 0f)
        assertTrue("Confidence should be <= 1", result.confidence <= 1f)
    }
}
