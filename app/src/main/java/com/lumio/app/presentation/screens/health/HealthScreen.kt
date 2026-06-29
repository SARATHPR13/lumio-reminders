package com.lumio.app.presentation.screens.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lumio.app.alarm.AlarmScheduler
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.repository.ReminderRepository
import com.lumio.app.health.HealthTemplate
import com.lumio.app.health.HealthTemplates
import com.lumio.app.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HealthUiState(
    val addedTemplates: Set<String> = emptySet(),
    val successMessage: String?     = null,
    val errorMessage: String?       = null
)

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthUiState())
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    fun addTemplate(template: HealthTemplate) {
        viewModelScope.launch {
            try {
                val reminder = HealthTemplates.buildReminder(template)
                val id = reminderRepository.insertReminder(reminder)
                alarmScheduler.schedule(reminder.copy(id = id))
                _uiState.update {
                    it.copy(
                        addedTemplates = it.addedTemplates + template.id,
                        successMessage = "${template.emoji} ${template.title} added!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to add reminder") }
            }
        }
    }

    fun addAllTemplates() {
        viewModelScope.launch {
            HealthTemplates.templates.forEach { template ->
                try {
                    val reminder = HealthTemplates.buildReminder(template)
                    val id = reminderRepository.insertReminder(reminder)
                    alarmScheduler.schedule(reminder.copy(id = id))
                } catch (e: Exception) { }
            }
            _uiState.update {
                it.copy(
                    addedTemplates = HealthTemplates.templates.map { t -> t.id }.toSet(),
                    successMessage = "All health reminders added!"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}

@Composable
fun HealthScreen(
    navController: NavController,
    viewModel: HealthViewModel = hiltViewModel()
) {
    val uiState      by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }
    var editingTemplate by remember { mutableStateOf<HealthTemplate?>(null) }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { snackbarState.showSnackbar(it); viewModel.clearMessages() }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarState.showSnackbar(it); viewModel.clearMessages() }
    }

    // Edit Dialog
    editingTemplate?.let { template ->
        EditHealthReminderDialog(
            template  = template,
            onSave    = { viewModel.addTemplate(it); editingTemplate = null },
            onDismiss = { editingTemplate = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Reminders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost   = { SnackbarHost(snackbarState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF00897B), Color(0xFF4CAF50))))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Health First",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Color.White)
                        Text("Tap to customize and add health reminders",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f))
                        Button(
                            onClick = { viewModel.addAllTemplates() },
                            colors  = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor   = Color(0xFF00897B)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Rounded.AddCircle, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Add All Health Reminders", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            items(HealthTemplates.templates) { template ->
                val isAdded = uiState.addedTemplates.contains(template.id)
                Card(
                    shape  = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAdded) Color(0xFF00897B).copy(alpha = 0.08f)
                                        else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF00897B).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) { Text(template.emoji, fontSize = 24.sp) }

                        Column(Modifier.weight(1f)) {
                            Text(template.title,
                                fontWeight = FontWeight.SemiBold,
                                style      = MaterialTheme.typography.bodyMedium)
                            Text(template.description,
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val h = template.defaultHour
                                val m = template.defaultMinute
                                val timeStr = "${if (h % 12 == 0) 12 else h % 12}:${m.toString().padStart(2,'0')} ${if (h < 12) "AM" else "PM"}"
                                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                    Text(timeStr, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                                    Text(template.repeatType.label, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Edit button - always visible
                            IconButton(
                                onClick  = { editingTemplate = template },
                                modifier = Modifier.size(36.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                            ) {
                                Icon(Icons.Rounded.Edit, "Edit",
                                    tint     = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp))
                            }
                            // Add/Done button
                            if (isAdded) {
                                Icon(Icons.Rounded.CheckCircle, null,
                                    tint     = Color(0xFF4CAF50),
                                    modifier = Modifier.size(28.dp))
                            } else {
                                IconButton(
                                    onClick  = { viewModel.addTemplate(template) },
                                    modifier = Modifier.size(36.dp)
                                        .background(Color(0xFF00897B), RoundedCornerShape(10.dp))
                                ) {
                                    Icon(Icons.Rounded.Add, null,
                                        tint     = Color.White,
                                        modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun EditHealthReminderDialog(
    template: HealthTemplate,
    onSave: (HealthTemplate) -> Unit,
    onDismiss: () -> Unit
) {
    var title       by remember { mutableStateOf(template.title) }
    var description by remember { mutableStateOf(template.description) }
    var hour        by remember { mutableStateOf(template.defaultHour) }
    var minute      by remember { mutableStateOf(template.defaultMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(template.emoji, fontSize = 24.sp)
                Text("Edit Reminder", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = title,
                    onValueChange = { title = it },
                    label         = { Text("Title") },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    singleLine    = true
                )
                OutlinedTextField(
                    value         = description,
                    onValueChange = { description = it },
                    label         = { Text("Description") },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    minLines      = 2
                )
                Text("Time", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value         = hour.toString(),
                        onValueChange = { it.toIntOrNull()?.let { v -> if (v in 0..23) hour = v } },
                        label         = { Text("Hour (0-23)") },
                        modifier      = Modifier.weight(1f),
                        shape         = RoundedCornerShape(12.dp),
                        singleLine    = true
                    )
                    OutlinedTextField(
                        value         = minute.toString(),
                        onValueChange = { it.toIntOrNull()?.let { v -> if (v in 0..59) minute = v } },
                        label         = { Text("Minute") },
                        modifier      = Modifier.weight(1f),
                        shape         = RoundedCornerShape(12.dp),
                        singleLine    = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(template.copy(
                        title         = title,
                        description   = description,
                        defaultHour   = hour,
                        defaultMinute = minute
                    ))
                },
                shape = RoundedCornerShape(10.dp)
            ) { Text("Save & Add", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
