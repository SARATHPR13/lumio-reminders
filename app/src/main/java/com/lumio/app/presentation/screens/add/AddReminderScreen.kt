package com.lumio.app.presentation.screens.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.Category
import com.lumio.app.domain.model.RepeatType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    navController: NavController,
    viewModel: AddReminderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isEditing) "Edit Reminder" else "New Reminder",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick  = { viewModel.saveReminder() },
                        enabled  = uiState.title.isNotBlank() && !uiState.isSaving
                    ) {
                        Text(
                            "Save",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp,
                            color      = if (uiState.title.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                value         = uiState.title,
                onValueChange = { viewModel.setTitle(it) },
                label         = { Text("What to remember? *") },
                placeholder   = { Text("e.g. Call mom, Buy milk...") },
                isError       = uiState.titleError,
                supportingText = {
                    if (uiState.titleError) Text("Please enter a title")
                },
                leadingIcon   = { Icon(Icons.Rounded.Title, null) },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(14.dp),
                singleLine    = true
            )

            // Description
            OutlinedTextField(
                value         = uiState.description,
                onValueChange = { viewModel.setDescription(it) },
                label         = { Text("Notes (optional)") },
                leadingIcon   = { Icon(Icons.Rounded.Notes, null) },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(14.dp),
                minLines      = 2,
                maxLines      = 4
            )

            // Date & Time Row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick  = { viewModel.showDatePicker() },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.CalendarMonth, null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(uiState.dateText, fontWeight = FontWeight.Medium)
                }
                OutlinedButton(
                    onClick  = { viewModel.showTimePicker() },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Schedule, null,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(uiState.timeText, fontWeight = FontWeight.Medium)
                }
            }

            // Priority Section
            SectionLabel(text = "Priority", icon = Icons.Rounded.Flag)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Priority.values()) { priority ->
                    val selected = uiState.priority == priority
                    val color    = Color(android.graphics.Color.parseColor(priority.colorHex))
                    FilterChip(
                        selected = selected,
                        onClick  = { viewModel.setPriority(priority) },
                        label    = {
                            Text(
                                "${priority.emoji} ${priority.label}",
                                fontSize   = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.2f),
                            selectedLabelColor     = color,
                            containerColor         = MaterialTheme.colorScheme.surface,
                            labelColor             = MaterialTheme.colorScheme.onSurface
                        ),
                        shape    = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Category Section
            SectionLabel(text = "Category", icon = Icons.Rounded.Category)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = uiState.category == null,
                        onClick  = { viewModel.setCategory(null) },
                        label    = { Text("None") },
                        shape    = RoundedCornerShape(10.dp)
                    )
                }
                items(Category.defaults) { cat ->
                    val selected = uiState.category?.id == cat.id
                    val color    = Color(android.graphics.Color.parseColor(cat.colorHex))
                    FilterChip(
                        selected = selected,
                        onClick  = { viewModel.setCategory(cat) },
                        label    = {
                            Text(
                                "${cat.emoji} ${cat.name}",
                                fontSize   = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.15f),
                            selectedLabelColor     = color,
                            containerColor         = MaterialTheme.colorScheme.surface,
                            labelColor             = MaterialTheme.colorScheme.onSurface
                        ),
                        shape    = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Repeat Section
            SectionLabel(text = "Repeat", icon = Icons.Rounded.Repeat)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RepeatType.values()) { repeat ->
                    val selected = uiState.repeatType == repeat
                    FilterChip(
                        selected = selected,
                        onClick  = { viewModel.setRepeatType(repeat) },
                        label    = {
                            Text(
                                repeat.label,
                                fontSize   = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor     = Color.White,
                            containerColor         = MaterialTheme.colorScheme.surface,
                            labelColor             = MaterialTheme.colorScheme.onSurface
                        ),
                        shape    = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Notification Toggles
            SectionLabel(text = "Notifications", icon = Icons.Rounded.NotificationsActive)
            Card(
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column {
                    SwitchRow(
                        label    = "Sound",
                        icon     = Icons.Rounded.VolumeUp,
                        checked  = uiState.soundEnabled,
                        onToggle = { viewModel.setSoundEnabled(it) }
                    )
                    HorizontalDivider(
                        modifier  = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp
                    )
                    SwitchRow(
                        label    = "Vibration",
                        icon     = Icons.Rounded.Vibration,
                        checked  = uiState.vibrationEnabled,
                        onToggle = { viewModel.setVibrationEnabled(it) }
                    )
                }
            }

            // Save Button
            Button(
                onClick  = { viewModel.saveReminder() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape    = RoundedCornerShape(16.dp),
                enabled  = uiState.title.isNotBlank() && !uiState.isSaving,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(22.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        modifier           = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        if (uiState.isEditing) "Update Reminder" else "Save Reminder",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Date Picker Dialog
    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dateTimeMillis
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.hideDatePicker() },
            confirmButton    = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.setDate(it)
                    }
                    viewModel.hideDatePicker()
                }) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton    = {
                TextButton(onClick = { viewModel.hideDatePicker() }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (uiState.showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour   = uiState.selectedHour,
            initialMinute = uiState.selectedMinute,
            is24Hour      = false
        )
        AlertDialog(
            onDismissRequest = { viewModel.hideTimePicker() },
            title            = { Text("Select Time", fontWeight = FontWeight.Bold) },
            text             = {
                Box(
                    modifier        = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton    = {
                TextButton(onClick = {
                    viewModel.setTime(timePickerState.hour, timePickerState.minute)
                    viewModel.hideTimePicker()
                }) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton    = {
                TextButton(onClick = { viewModel.hideTimePicker() }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SectionLabel(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(18.dp)
        )
        Text(
            text       = text,
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SwitchRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(20.dp)
        )
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked         = checked,
            onCheckedChange = onToggle,
            colors          = SwitchDefaults.colors(
                checkedThumbColor  = Color.White,
                checkedTrackColor  = MaterialTheme.colorScheme.primary
            )
        )
    }
}
