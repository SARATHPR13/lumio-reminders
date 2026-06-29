package com.lumio.app.presentation.screens.ai

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lumio.app.ai.ChatMessage
import com.lumio.app.ai.SmartTimeSuggester
import com.lumio.app.ai.SuggestedReminder
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.RepeatType

@Composable
fun AiChatScreen(
    navController: NavController,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()
    val listState  = rememberLazyListState()
    val snackbar   = remember { SnackbarHostState() }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty())
            listState.animateScrollToItem(uiState.messages.size - 1)
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF1A73E8), Color(0xFF7B2FBE))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) { Text("\uD83E\uDD16", fontSize = 18.sp) }
                        Column {
                            Text("LUMIO AI",
                                fontWeight = FontWeight.ExtraBold,
                                style      = MaterialTheme.typography.titleSmall)
                            Text("Smart Reminder Assistant",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearConversation() }) {
                        Icon(Icons.Rounded.RestartAlt, "Clear")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost   = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            LazyColumn(
                state          = listState,
                modifier       = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    MessageBubble(
                        message   = message,
                        onSave    = { viewModel.confirmSaveReminder() },
                        onDismiss = { viewModel.sendQuickMessage("no") }
                    )
                }

                if (uiState.isTyping) {
                    item { TypingDots() }
                }

                if (uiState.showTimeSuggestions && uiState.timeSuggestions.isNotEmpty()) {
                    item {
                        TimeSuggestCard(
                            suggestions = uiState.timeSuggestions,
                            onSelect    = { s -> viewModel.useTimeSuggestion(s.hour, s.minute, s.label) },
                            onDismiss   = { viewModel.dismissTimeSuggestions() }
                        )
                    }
                }
            }

            // Quick suggestions (shown only at start)
            if (uiState.messages.size <= 1) {
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val examples = listOf(
                        "Take medicine daily at 8 AM",
                        "Call mom tomorrow at 6 PM",
                        "Pay bills on Monday",
                        "Every Friday at 5 PM, submit report"
                    )
                    items(examples) { ex ->
                        SuggestionChip(
                            onClick = { viewModel.sendQuickMessage("Remind me to $ex") },
                            label   = { Text(ex, fontSize = 12.sp) }
                        )
                    }
                }
            }

            InputBar(
                text     = uiState.inputText,
                onText   = { viewModel.setInput(it) },
                onSend   = { viewModel.sendMessage() },
                isTyping = uiState.isTyping
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment     = Alignment.Bottom
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1A73E8), Color(0xFF7B2FBE))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) { Text("\uD83E\uDD16", fontSize = 14.sp) }
            Spacer(Modifier.width(8.dp))
        }

        Column(
            modifier            = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart    = if (message.isUser) 20.dp else 4.dp,
                            topEnd      = if (message.isUser) 4.dp else 20.dp,
                            bottomStart = 20.dp,
                            bottomEnd   = 20.dp
                        )
                    )
                    .background(
                        if (message.isUser)
                            Brush.linearGradient(
                                listOf(Color(0xFF1A73E8), Color(0xFF7B2FBE))
                            )
                        else
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text      = message.text,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = if (message.isUser) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }

            message.suggestedReminder?.let { reminder ->
                Spacer(Modifier.height(8.dp))
                ReminderCard(
                    reminder  = reminder,
                    onSave    = onSave,
                    onDismiss = onDismiss
                )
            }

            Text(
                text     = formatTime(message.timestamp),
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
            )
        }

        if (message.isUser) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Person, null,
                    tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: SuggestedReminder,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier            = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.Notifications, null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp))
                Text("Reminder Preview",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary,
                    modifier   = Modifier.weight(1f))
                val cColor = when {
                    reminder.confidence > 0.8f -> Color(0xFF4CAF50)
                    reminder.confidence > 0.6f -> Color(0xFFF9A825)
                    else                       -> Color(0xFFFF6B35)
                }
                Surface(shape = RoundedCornerShape(20.dp), color = cColor.copy(alpha = 0.15f)) {
                    Text("${(reminder.confidence * 100).toInt()}%",
                        modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize   = 11.sp, color = cColor, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider()

            Text(reminder.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            DetailRow("\uD83D\uDCC5", reminder.dateDescription)
            DetailRow("\u23F0", reminder.timeDescription)
            if (reminder.repeatType != RepeatType.NONE) DetailRow("\uD83D\uDD04", "Repeats: ${reminder.repeatType.label}")
            if (reminder.category != null) DetailRow(reminder.category.emoji, reminder.category.name)
            if (reminder.priority != Priority.NONE) DetailRow(reminder.priority.emoji, "${reminder.priority.label} Priority")

            HorizontalDivider()

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                    Text("Cancel")
                }
                Button(onClick = onSave, modifier = Modifier.weight(2f), shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Save Reminder", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(emoji: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(emoji, fontSize = 14.sp, modifier = Modifier.width(24.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TypingDots() {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF1A73E8), Color(0xFF7B2FBE)))),
            contentAlignment = Alignment.Center
        ) { Text("\uD83E\uDD16", fontSize = 14.sp) }
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { index ->
                    val inf = rememberInfiniteTransition(label = "d$index")
                    val y by inf.animateFloat(initialValue = 0f, targetValue = -6f,
                        animationSpec = infiniteRepeatable(tween(400, delayMillis = index * 130), RepeatMode.Reverse), label = "d$index")
                    Box(modifier = Modifier.size(8.dp).offset(y = y.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)))
                }
            }
        }
    }
}

@Composable
private fun TimeSuggestCard(
    suggestions: List<SmartTimeSuggester.TimeSuggestion>,
    onSelect: (SmartTimeSuggester.TimeSuggestion) -> Unit,
    onDismiss: () -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.AutoAwesome, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(16.dp))
                Text("AI Suggested Times", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Rounded.Close, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(16.dp))
                }
            }
            suggestions.take(4).forEach { s ->
                OutlinedButton(onClick = { onSelect(s) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                    Text(s.emoji, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                        Text(s.label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(s.reason, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun InputBar(text: String, onText: (String) -> Unit, onSend: () -> Unit, isTyping: Boolean) {
    Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value         = text,
                onValueChange = onText,
                placeholder   = { Text("Ask me to set a reminder...") },
                modifier      = Modifier.weight(1f),
                shape         = RoundedCornerShape(24.dp),
                maxLines      = 3
            )
            FloatingActionButton(
                onClick        = { if (!isTyping) onSend() },
                modifier       = Modifier.size(48.dp),
                containerColor = if (isTyping || text.isBlank()) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                contentColor   = if (isTyping || text.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary,
                shape          = CircleShape,
                elevation      = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                if (isTyping) CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                else Icon(Icons.AutoMirrored.Rounded.Send, "Send", modifier = Modifier.size(22.dp))
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val cal  = java.util.Calendar.getInstance().apply { timeInMillis = millis }
    val h    = cal.get(java.util.Calendar.HOUR).let { if (it == 0) 12 else it }
    val m    = cal.get(java.util.Calendar.MINUTE).toString().padStart(2, '0')
    val ampm = if (cal.get(java.util.Calendar.AM_PM) == 0) "AM" else "PM"
    return "$h:$m $ampm"
}
