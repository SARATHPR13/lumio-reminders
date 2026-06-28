package com.lumio.app.presentation.screens.add

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lumio.app.domain.model.Category
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.RepeatType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddReminderScreen(
    navController: NavController,
    viewModel: AddReminderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigate back when saved
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) navController.popBackStack()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.dateTimeMillis
    )
    val timePickerState = rememberTimePickerState(
        initialHour   = Calendar.getInstance()
            .apply { timeInMillis = uiState.dateTimeMillis }.get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance()
            .apply { timeInMillis = uiState.dateTimeMillis }.get(Calendar.MINUTE)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Reminder", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(end = 16.dp))
                    } else {
                        TextButton(onClick = { viewModel.saveReminder() }) {
                            Text("Save", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.setTitle(it) },
                label = { Text("Title *") },
                placeholder = { Text("What do you need to remember?") },
                isError = uiState.titleError,
                supportingText = { if (uiState.titleError) Text("Title cannot be empty") },
                leadingIcon = { Icon(Icons.Rounded.Title, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.setDescription(it) },
                label = { Text("Description") },
                placeholder = { Text("Add details (optional)") },
                leadingIcon = { Icon(Icons.Rounded.Description, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                maxLines = 4
            )

            // Date & Time Row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedCard(
                    onClick = { viewModel.showDate(true) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.CalendarMonth, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                "Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                    .format(Date(uiState.dateTimeMillis)),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                OutlinedCard(
                    onClick = { viewModel.showTime(true) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Schedule, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                "Time",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                    .format(Date(uiState.dateTimeMillis)),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Priority
            SectionLabel("Priority")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Priority.values()) { p ->
                    val sel = uiState.priority == p
                    val color = Color(android.graphics.Color.parseColor(p.colorHex))
                    FilterChip(
                        selected = sel,
                        onClick  = { viewModel.setPriority(p) },
                        label    = { Text("${p.emoji} ${p.label}", fontSize = 12.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.2f),
                            selectedLabelColor     = color
                        )
                    )
                }
            }

            // Category
            SectionLabel("Category")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = uiState.category == null,
                        onClick  = { viewModel.setCategory(null) },
                        label    = { Text("None") }
                    )
                }
                items(Category.defaults) { cat ->
                    val sel = uiState.category?.id == cat.id
                    val color = Color(android.graphics.Color.parseColor(cat.colorHex))
                    FilterChip(
                        selected = sel,
                        onClick  = { viewModel.setCategory(cat) },
                        label    = { Text("${cat.emoji} ${cat.name}", fontSize = 12.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.2f),
                            selectedLabelColor     = color
                        )
                    )
                }
            }

            // Repeat
            SectionLabel("Repeat")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RepeatType.values()) { r ->
                    FilterChip(
                        selected = uiState.repeatType == r,
                        onClick  = { viewModel.setRepeat(r) },
                        label    = { Text(r.label, fontSize = 12.sp) }
                    )
                }
            }

            // Sound & Vibration
            SectionLabel("Notifications")
            Card(
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column {
                    ListItem(
                        headlineContent   = { Text("Sound") },
                        supportingContent = { Text("Play notification sound") },
                        leadingContent    = { Icon(Icons.AutoMirrored.Rounded.VolumeUp, null) },
                        trailingContent   = {
                            Switch(
                                checked = uiState.soundEnabled,
                                onCheckedChange = { viewModel.setSound(it) }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent   = { Text("Vibration") },
                        supportingContent = { Text("Vibrate on notification") },
                        leadingContent    = { Icon(Icons.Rounded.Vibration, null) },
                        trailingContent   = {
                            Switch(
                                checked = uiState.vibrationEnabled,
                                onCheckedChange = { viewModel.setVibration(it) }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            // Save Button
            Button(
                onClick = { viewModel.saveReminder() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Rounded.Check, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save Reminder", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // Date Picker Dialog
    if (uiState.showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { viewModel.showDate(false) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.setDate(it) }
                    viewModel.showDate(false)
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showDate(false) }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // Time Picker Dialog
    if (uiState.showTimePicker) {
        AlertDialog(
            onDismissRequest = { viewModel.showTime(false) },
            title = { Text("Select Time") },
            text = {
                Box(Modifier.fillMaxWidth(), Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setTime(timePickerState.hour, timePickerState.minute)
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showTime(false) }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
