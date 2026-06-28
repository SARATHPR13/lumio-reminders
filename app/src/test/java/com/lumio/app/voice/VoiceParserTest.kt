package com.lumio.app.voice

import com.lumio.app.domain.model.Priority
import org.junit.Assert.*
import org.junit.Test

class VoiceParserTest {

    // ── Title extraction ──────────────────────────────
    @Test
    fun `extracts title from remind me to prefix`() {
        val result = VoiceParser.parse("Remind me to call mom tomorrow at 5 PM")
        assertTrue("Title should contain 'call'",
            result.title.lowercase().contains("call"))
        assertTrue("Title should contain 'mom'",
            result.title.lowercase().contains("mom"))
    }

    @Test
    fun `extracts title from remember to prefix`() {
        val result = VoiceParser.parse("Remember to buy milk today at 6 PM")
        assertTrue("Title should contain 'buy'",
            result.title.lowercase().contains("buy"))
        assertTrue("Title should contain 'milk'",
            result.title.lowercase().contains("milk"))
    }

    @Test
    fun `title is not empty`() {
        val result = VoiceParser.parse("Remind me to study tomorrow")
        assertTrue("Title should not be empty", result.title.isNotBlank())
    }

    @Test
    fun `title first letter is uppercase`() {
        val result = VoiceParser.parse("remind me to call john tomorrow")
        assertTrue("Title should start with uppercase",
            result.title[0].isUpperCase())
    }

    // ── Priority detection ────────────────────────────
    @Test
    fun `detects urgent priority`() {
        val result = VoiceParser.parse("Urgent meeting tomorrow at 10 AM")
        assertEquals("Priority should be URGENT", Priority.URGENT, result.priority)
    }

    @Test
    fun `detects high priority from important keyword`() {
        val result = VoiceParser.parse("Important doctor appointment tomorrow")
        assertEquals("Priority should be HIGH", Priority.HIGH, result.priority)
    }

    @Test
    fun `no priority keywords results in NONE priority`() {
        val result = VoiceParser.parse("Remind me to call mom tomorrow")
        assertEquals("Priority should be NONE", Priority.NONE, result.priority)
    }

    @Test
    fun `detects asap as urgent`() {
        val result = VoiceParser.parse("Call the office ASAP tomorrow")
        assertEquals("ASAP should be URGENT", Priority.URGENT, result.priority)
    }

    // ── Category suggestions ──────────────────────────
    @Test
    fun `suggests Work category for meeting`() {
        val result = VoiceParser.parse("Remind me about meeting tomorrow")
        assertNotNull("Should suggest a category", result.suggestedCategory)
        assertEquals("Should suggest Work", "Work", result.suggestedCategory?.name)
    }

    @Test
    fun `suggests Shopping category for buy`() {
        val result = VoiceParser.parse("Remind me to buy groceries tomorrow")
        assertNotNull("Should suggest Shopping category", result.suggestedCategory)
        assertEquals("Should suggest Shopping", "Shopping", result.suggestedCategory?.name)
    }

    @Test
    fun `suggests Health category for medicine`() {
        val result = VoiceParser.parse("Take medicine tomorrow at 8 PM")
        assertNotNull("Should suggest Health category", result.suggestedCategory)
        assertEquals("Should suggest Health", "Health", result.suggestedCategory?.name)
    }

    @Test
    fun `suggests Bills category for pay`() {
        val result = VoiceParser.parse("Pay electricity bill next Monday")
        assertNotNull("Should suggest Bills category", result.suggestedCategory)
        assertEquals("Should suggest Bills", "Bills", result.suggestedCategory?.name)
    }

    @Test
    fun `suggests Study category for exam`() {
        val result = VoiceParser.parse("Study for exam tomorrow morning")
        assertNotNull("Should suggest Study category", result.suggestedCategory)
        assertEquals("Should suggest Study", "Study", result.suggestedCategory?.name)
    }

    // ── Original text ──────────────────────────────────
    @Test
    fun `preserves original voice text`() {
        val input  = "Remind me to call Rahul tomorrow at 6 PM"
        val result = VoiceParser.parse(input)
        assertEquals("Original text should be preserved", input, result.originalText)
    }

    // ── Confidence ────────────────────────────────────
    @Test
    fun `high confidence for clear date and time`() {
        val result = VoiceParser.parse("Remind me tomorrow at 6 PM")
        assertTrue("Confidence should be > 0.7 for clear input",
            result.confidence > 0.7f)
    }

    // ── DateTime ──────────────────────────────────────
    @Test
    fun `parsed date is in the future`() {
        val result = VoiceParser.parse("Remind me tomorrow at 6 PM")
        assertTrue("DateTime should be in the future",
            result.dateTimeMillis > System.currentTimeMillis())
    }
}
