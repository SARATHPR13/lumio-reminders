package com.lumio.app.domain.model

data class Category(
    val id: Long = 0L,
    val name: String,
    val emoji: String,
    val colorHex: String,
    val isDefault: Boolean = true
) {
    companion object {
        val defaults = listOf(
            Category(1L,  "Work",     "💼", "#FF1A73E8"),
            Category(2L,  "Home",     "🏠", "#FF4CAF50"),
            Category(3L,  "Shopping", "🛒", "#FFFF9800"),
            Category(4L,  "Bills",    "💳", "#FFD32F2F"),
            Category(5L,  "Study",    "📚", "#FF7B2FBE"),
            Category(6L,  "Health",   "💊", "#FF00897B"),
            Category(7L,  "Birthday", "🎂", "#FFFF6B35"),
            Category(8L,  "Personal", "👤", "#FFE91E63"),
            Category(9L,  "Travel",   "✈️", "#FF0097A7"),
            Category(10L, "Finance",  "💰", "#FF795548"),
        )
    }
}
