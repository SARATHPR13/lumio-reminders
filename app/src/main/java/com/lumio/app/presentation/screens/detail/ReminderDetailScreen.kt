package com.lumio.app.presentation.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.model.RepeatType
import com.lumio.app.presentation.navigation.Screen

@Composable
fun ReminderDetailScreen(
    navController: NavController,
    viewModel: ReminderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminder", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    uiState.reminder?.let {
                        IconButton(onClick = {
                            navController.navigate(Screen.EditReminder.createRoute(it.id))
                        }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { viewModel.showDeleteDialog(true) }) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.reminder == null -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("😔", fontSize = 56.sp)
                        Text("Reminder not found", style = MaterialTheme.typography.titleMedium)
                        Button(onClick = { navController.popBackStack() }) { Text("Go Back") }
                    }
                }
            }
            else -> {
                DetailContent(
                    reminder        = uiState.reminder!!,
                    padding         = padding,
                    onToggleComplete = { viewModel.toggleComplete() }
                )
            }
        }
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showDeleteDialog(false) },
            icon  = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Reminder") },
            text  = {
                Text("Are you sure you want to delete \"${uiState.reminder?.title}\"? This cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteReminder() },
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showDeleteDialog(false) }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DetailContent(
    reminder: Reminder,
    padding: PaddingValues,
    onToggleComplete: () -> Unit
) {
    val priorityColor = if (reminder.priority != Priority.NONE)
        Color(android.graphics.Color.parseColor(reminder.priority.colorHex))
    else MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Priority banner
        if (reminder.priority != Priority.NONE) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(priorityColor.copy(alpha = 0.12f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(reminder.priority.emoji, fontSize = 22.sp)
                Text(
                    "${reminder.priority.label} Priority",
                    fontWeight = FontWeight.SemiBold,
                    color      = priorityColor
                )
            }
        }

        // Title + Description card
        Card(
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text       = reminder.title,
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = if (reminder.isCompleted)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    else MaterialTheme.colorScheme.onSurface
                )
                if (reminder.description.isNotBlank()) {
                    HorizontalDivider()
                    Text(
                        text  = reminder.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Info card
        Card(
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                InfoRow(Icons.Rounded.CalendarMonth, "Date", reminder.formattedDate, MaterialTheme.colorScheme.primary)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoRow(Icons.Rounded.Schedule, "Time", reminder.formattedTime, MaterialTheme.colorScheme.primary)

                if (reminder.category != null) {
                    val catColor = Color(android.graphics.Color.parseColor(reminder.category.colorHex))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    InfoRow(Icons.Rounded.Category, "Category",
                        "${reminder.category.emoji}  ${reminder.category.name}", catColor)
                }

                if (reminder.repeatType != RepeatType.NONE) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    InfoRow(Icons.Rounded.Repeat, "Repeat", reminder.repeatType.label, MaterialTheme.colorScheme.tertiary)
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoRow(
                    icon  = if (reminder.soundEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                    label = "Sound",
                    value = if (reminder.soundEnabled) "Enabled" else "Disabled",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                InfoRow(
                    icon  = Icons.Rounded.Vibration,
                    label = "Vibration",
                    value = if (reminder.vibrationEnabled) "Enabled" else "Disabled",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Status card
        val statusColor = when {
            reminder.isCompleted -> Color(0xFF4CAF50)
            reminder.isOverdue   -> Color(0xFFD32F2F)
            else                 -> MaterialTheme.colorScheme.primary
        }

        Card(
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = when {
                        reminder.isCompleted -> Icons.Rounded.CheckCircle
                        reminder.isOverdue   -> Icons.Rounded.Warning
                        else                 -> Icons.Rounded.NotificationsActive
                    },
                    contentDescription = null,
                    tint     = statusColor,
                    modifier = Modifier.size(32.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            reminder.isCompleted -> "Completed ✅"
                            reminder.isOverdue   -> "Overdue ⚠️"
                            else                 -> "Scheduled 🔔"
                        },
                        fontWeight = FontWeight.SemiBold,
                        color      = statusColor
                    )
                    Text(
                        text  = reminder.formattedDateTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Complete / Undo button
        Button(
            onClick  = onToggleComplete,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = if (reminder.isCompleted)
                    MaterialTheme.colorScheme.surfaceVariant
                else Color(0xFF4CAF50)
            )
        ) {
            Icon(
                imageVector = if (reminder.isCompleted) Icons.Rounded.Refresh else Icons.Rounded.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint     = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text       = if (reminder.isCompleted) "Mark as Active" else "Mark as Done",
                fontWeight = FontWeight.Bold,
                fontSize   = 16.sp,
                color      = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}
