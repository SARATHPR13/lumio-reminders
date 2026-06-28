package com.lumio.app.domain.model

enum class Priority(
    val label: String,
    val emoji: String,
    val colorHex: String,
    val level: Int
) {
    URGENT("Urgent",      "🔴", "#FFD32F2F", 4),
    HIGH  ("High",        "🟠", "#FFFF6B35", 3),
    MEDIUM("Medium",      "🟡", "#FFF9A825", 2),
    LOW   ("Low",         "🟢", "#FF4CAF50", 1),
    NONE  ("No Priority", "⚪", "#FF9E9E9E", 0)
}
