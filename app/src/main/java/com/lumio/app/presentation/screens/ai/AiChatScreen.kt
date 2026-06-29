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
        uiState.successMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessages()
        }
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
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFF1A73E8), Color(0xFF7B2FBE)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("AI", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                        Column {
                            Text("LUMIO AI", fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleSmall)
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

            // Messages
            LazyColumn(
                state          = listState,
                modifier       = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
                        verticalAlignment     = Alignment.Bottom
                    ) {
                        if (!message.isUser) {
                            Box(
                                modifier = Modifier.size(30.dp).clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Color(0xFF1A73E8), Color(0xFF7B2FBE)))),
                                contentAlignment = Alignment.Center
                            ) { Text("AI", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                            Spacer(Modifier.width(8.dp))
                        }

                        Column(
                            modifier            = Modifier.widthIn(max = 280.dp),
                            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(
                                        topStart    = if (message.isUser) 18.dp else 4.dp,
                                        topEnd      = if (message.isUser) 4.dp else 18.dp,
                                        bottomStart = 18.dp, bottomEnd = 18.dp
                                    ))
                                    .background(
                                        if (message.isUser)
                                            Brush.linearGradient(listOf(Color(0xFF1A73E8), Color(0xFF7B2FBE)))
                                        else
                                            Brush.linearGradient(listOf(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                MaterialTheme.colorScheme.surfaceVariant
                                            ))
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text       = message.text,
                                    style      = MaterialTheme.typography.bodyMedium,
                                    color      = if (message.isUser) Color.White
                                                 else MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 22.sp
                                )
                            }

                            // Reminder preview card
                            message.suggestedReminder?.let { reminder ->
                                Spacer(Modifier.height(8.dp))
                                Card(
                                    shape  = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(
                                        modifier            = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Notifications, null,
                                                tint     = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Reminder Preview",
                                                style      = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color      = MaterialTheme.colorScheme.primary,
                                                modifier   = Modifier.weight(1f))
                                            Surface(shape = RoundedCornerShape(20.dp),
                                                color = Color(0xFF4CAF50).copy(alpha = 0.15f)) {
                                                Text("${(reminder.confidence * 100).toInt()}%",
                                                    modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize   = 10.sp,
                                                    color      = Color(0xFF4CAF50),
                                                    fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        HorizontalDivider()
                                        Text(reminder.title, fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium)
                                        Text("Date: ${reminder.dateDescription}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Time: ${reminder.timeDescription}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (reminder.repeatType != RepeatType.NONE) {
                                            Text("Repeats: ${reminder.repeatType.label}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary)
                                        }
                                        reminder.category?.let {
                                            Text("${it.emoji} ${it.name}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (reminder.priority != Priority.NONE) {
                                            Text("${reminder.priority.emoji} ${reminder.priority.label} Priority",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        HorizontalDivider()
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick  = { viewModel.sendQuickMessage("no") },
                                                modifier = Modifier.weight(1f),
                                                shape    = RoundedCornerShape(10.dp)
                                            ) { Text("Cancel") }
                                            Button(
                                                onClick  = { viewModel.confirmSaveReminder() },
                                                modifier = Modifier.weight(2f),
                                                shape    = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(Icons.Rounded.Check, null, modifier = Modifier.size(14.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("Save", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (message.isUser) {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier.size(30.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Person, null,
                                    tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Typing indicator
                if (uiState.isTyping) {
                    item {
                        Row(
                            verticalAlignment     = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(30.dp).clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Color(0xFF1A73E8), Color(0xFF7B2FBE)))),
                                contentAlignment = Alignment.Center
                            ) { Text("AI", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold) }

                            Card(
                                shape  = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier              = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    repeat(3) { index ->
                                        val inf = rememberInfiniteTransition(label = "d$index")
                                        val y by inf.animateFloat(
                                            initialValue  = 0f,
                                            targetValue   = -5f,
                                            animationSpec = infiniteRepeatable(
                                                animation  = tween(350, delayMillis = index * 120),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "d$index"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .offset(y = y.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick suggestions shown at start
            if (uiState.messages.size <= 1) {
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val chips = listOf(
                        "Medicine daily at 8 AM",
                        "Call mom tomorrow at 6 PM",
                        "Every Friday 5 PM report",
                        "Pay bills on Monday"
                    )
                    items(chips) { chip ->
                        SuggestionChip(
                            onClick = { viewModel.sendQuickMessage("Remind me to $chip") },
                            label   = { Text(chip, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Input bar
            Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value         = uiState.inputText,
                        onValueChange = { viewModel.setInput(it) },
                        placeholder   = { Text("Ask me to set a reminder...") },
                        modifier      = Modifier.weight(1f),
                        shape         = RoundedCornerShape(24.dp),
                        maxLines      = 3
                    )
                    FloatingActionButton(
                        onClick        = { if (!uiState.isTyping) viewModel.sendMessage() },
                        modifier       = Modifier.size(48.dp),
                        containerColor = if (uiState.isTyping || uiState.inputText.isBlank())
                            MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.primary,
                        contentColor   = if (uiState.isTyping || uiState.inputText.isBlank())
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onPrimary,
                        shape          = CircleShape,
                        elevation      = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        if (uiState.isTyping) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.AutoMirrored.Rounded.Send, "Send",
                                modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}
