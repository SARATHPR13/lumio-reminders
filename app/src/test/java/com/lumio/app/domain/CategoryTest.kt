package com.lumio.app.domain

import com.lumio.app.domain.model.Category
import org.junit.Assert.*
import org.junit.Test

class CategoryTest {

    @Test
    fun `default categories list has 10 items`() {
        assertEquals("Should have 10 default categories", 10, Category.defaults.size)
    }

    @Test
    fun `all default categories have unique IDs`() {
        val ids = Category.defaults.map { it.id }
        assertEquals("All IDs should be unique", ids.distinct().size, ids.size)
    }

    @Test
    fun `all default categories have non-empty names`() {
        Category.defaults.forEach { cat ->
            assertTrue("Category ${cat.id} should have name", cat.name.isNotBlank())
        }
    }

    @Test
    fun `all default categories have valid color hex`() {
        Category.defaults.forEach { cat ->
            assertTrue(
                "Category ${cat.name} should have valid hex color",
                cat.colorHex.startsWith("#") && cat.colorHex.length == 9
            )
        }
    }

    @Test
    fun `all default categories have emojis`() {
        Category.defaults.forEach { cat ->
            assertTrue(
                "Category ${cat.name} should have emoji",
                cat.emoji.isNotBlank()
            )
        }
    }

    @Test
    fun `work category exists`() {
        val work = Category.defaults.find { it.name == "Work" }
        assertNotNull("Work category should exist", work)
    }

    @Test
    fun `health category exists`() {
        val health = Category.defaults.find { it.name == "Health" }
        assertNotNull("Health category should exist", health)
    }

    @Test
    fun `all default categories are marked as default`() {
        Category.defaults.forEach { cat ->
            assertTrue("${cat.name} should be marked as default", cat.isDefault)
        }
    }
}
