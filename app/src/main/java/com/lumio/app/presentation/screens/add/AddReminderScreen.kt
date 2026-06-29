package com.lumio.app.presentation.screens.add

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lumio.app.domain.model.Category
import com.lumio.app.domain.model.Priority
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
                        text       = "New Reminder",
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
                        onClick = { viewModel.saveReminder() },
                        enabled = uiState.title.isNotBlank() && !uiState.isSaving
                    ) {
                        Text(
                            text       = "Save",
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
                value          = uiState.title,
                onValueChange  = { viewModel.setTitle(it) },
                label          = { Text("What to remember? *") },
                placeholder    = { Text("e.g. Call mom, Buy milk...") },
                isError        = uiState.titleError,
                supportingText = {
                    if (uiState.titleError) Text("Please enter a title")
                },
                leadingIcon    = { Icon(Icons.Rounded.Title, null) },
                modifier       = Modifier.fillMaxWidth(),
                shape          = RoundedCornerShape(14.dp),
                singleLine     = true
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

            // Date & Time
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick  = { viewModel.showDatePicker(true) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.CalendarMonth, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(uiState.dateDisplay, fontWeight = FontWeight.Medium)
                }
                OutlinedButton(
                    onClick  = { viewModel.showTimePicker(true) },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Schedule, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(uiState.timeDisplay, fontWeight = FontWeight.Medium)
                }
            }

            // Priority
            FormLabel(text = "Priority", icon = Icons.Rounded.Flag)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Priority.values()) { priority ->
                    val selected = uiState.priority == priority
                    val color    = Color(
                        android.graphics.Color.parseColor(priority.colorHex)
                    )
                    FilterChip(
                        selected = selected,
                        onClick  = { viewModel.setPriority(priority) },
                        label    = {
                            Text(
                                text       = "${priority.emoji} ${priority.label}",
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
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Category
            FormLabel(text = "Category", icon = Icons.Rounded.Category)
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
                    val color    = Color(
                        android.graphics.Color.parseColor(cat.colorHex)
                    )
                    FilterChip(
                        selected = selected,
                        onClick  = { viewModel.setCategory(cat) },
                        label    = {
                            Text(
                                text       = "${cat.emoji} ${cat.name}",
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
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Repeat
            FormLabel(text = "Repeat", icon = Icons.Rounded.Repeat)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RepeatType.values()) { repeat ->
                    val selected = uiState.repeatType == repeat
                    FilterChip(
                        selected = selected,
                        onClick  = { viewModel.setRepeatType(repeat) },
                        label    = {
                            Text(
                                text       = repeat.label,
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
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Sound & Vibration
            FormLabel(text = "Notifications", icon = Icons.Rounded.NotificationsActive)
            Card(
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Rounded.VolumeUp,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(22.dp)
                        )
                        Text(
                            text     = "Sound",
                            style    = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked         = uiState.soundEnabled,
                            onCheckedChange = { viewModel.setSoundEnabled(it) }
                        )
                    }
                    HorizontalDivider(
                        modifier  = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp
                    )
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Vibration,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(22.dp)
                        )
                        Text(
                            text     = "Vibration",
                            style    = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked         = uiState.vibrationEnabled,
                            onCheckedChange = { viewModel.setVibrationEnabled(it) }
                        )
                    }
                }
            }

            // Save Button
            Button(
                onClick  = { viewModel.saveReminder() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape    = RoundedCornerShape(16.dp),
                enabled  = uiState.title.isNotBlank() && !uiState.isSaving
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
                        text       = "Save Reminder",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Date Picker
    if (uiState.showDatePicker) {
        val dpState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dateTimeMillis
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.showDatePicker(false) },
            confirmButton    = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { viewModel.setDate(it) }
                    viewModel.showDatePicker(false)
                }) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton    = {
                TextButton(onClick = { viewModel.showDatePicker(false) }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = dpState)
        }
    }

    // Time Picker
    if (uiState.showTimePicker) {
        val tpState = rememberTimePickerState(
            initialHour   = uiState.hour,
            initialMinute = uiState.minute,
            is24Hour      = false
        )
        AlertDialog(
            onDismissRequest = { viewModel.showTimePicker(false) },
            title            = { Text("Select Time", fontWeight = FontWeight.Bold) },
            text             = {
                Box(
                    modifier         = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = tpState)
                }
            },
            confirmButton    = {
                TextButton(onClick = {
                    viewModel.setTime(tpState.hour, tpState.minute)
                    viewModel.showTimePicker(false)
                }) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton    = {
                TextButton(onClick = { viewModel.showTimePicker(false) }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun FormLabel(text: String, icon: ImageVector) {
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
