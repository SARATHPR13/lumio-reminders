package com.lumio.app.domain.model

enum class RepeatType(val label: String) {
    NONE   ("One Time"),
    DAILY  ("Every Day"),
    WEEKLY ("Every Week"),
    MONTHLY("Every Month"),
    YEARLY ("Every Year"),
    CUSTOM ("Custom")
}
