package com.lumio.app.ai

import com.lumio.app.domain.model.Category
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.RepeatType

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedReminder: SuggestedReminder? = null,
    val isTyping: Boolean = false
)

data class SuggestedReminder(
    val title: String,
    val description: String = "",
    val dateTimeMillis: Long,
    val priority: Priority,
    val category: Category?,
    val repeatType: RepeatType,
    val dateDescription: String,
    val timeDescription: String,
    val confidence: Float
)

data class AiParseResult(
    val understood: Boolean,
    val response: String,
    val suggestedReminder: SuggestedReminder? = null,
    val followUpQuestion: String? = null
)

enum class ConversationState {
    IDLE,
    WAITING_FOR_TIME,
    WAITING_FOR_DATE,
    WAITING_FOR_TITLE,
    WAITING_FOR_PRIORITY,
    CONFIRMING
}
