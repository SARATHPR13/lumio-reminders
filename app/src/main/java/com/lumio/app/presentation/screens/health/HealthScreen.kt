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
import com.lumio.app.domain.repository.ReminderRepository
import com.lumio.app.health.HealthTemplate
import com.lumio.app.health.HealthTemplates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HealthUiState(
    val addedTemplates: Set<String> = emptySet(),
    val successMessage: String?     = null
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
            val reminder = HealthTemplates.buildReminder(template)
            val id = reminderRepository.insertReminder(reminder)
            alarmScheduler.schedule(reminder.copy(id = id))
            _uiState.update {
                it.copy(
                    addedTemplates  = it.addedTemplates + template.id,
                    successMessage  = "${template.emoji} ${template.title} added!"
                )
            }
        }
    }

    fun addAllTemplates() {
        viewModelScope.launch {
            HealthTemplates.templates.forEach { template ->
                val reminder = HealthTemplates.buildReminder(template)
                val id = reminderRepository.insertReminder(reminder)
                alarmScheduler.schedule(reminder.copy(id = id))
            }
            _uiState.update {
                it.copy(
                    addedTemplates = HealthTemplates.templates.map { t -> t.id }.toSet(),
                    successMessage = "✅ All health reminders added!"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

@Composable
fun HealthScreen(
    navController: NavController,
    viewModel: HealthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarState.showSnackbar(it)
            viewModel.clearMessage()
        }
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
        snackbarHost  = { SnackbarHost(snackbarState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00897B), Color(0xFF4CAF50))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("💚 Health First",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Color.White)
                        Text("Add pre-built health reminders with one tap. Stay healthy every day!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f))
                        Spacer(Modifier.height(4.dp))
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

            // Templates
            items(HealthTemplates.templates) { template ->
                val isAdded = uiState.addedTemplates.contains(template.id)

                Card(
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(
                        containerColor = if (isAdded)
                            Color(0xFF00897B).copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Emoji icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF00897B).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(template.emoji, fontSize = 24.sp)
                        }

                        // Content
                        Column(Modifier.weight(1f)) {
                            Text(
                                template.title,
                                fontWeight = FontWeight.SemiBold,
                                style      = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                template.description,
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val timeStr = String.format("%02d:%02d %s",
                                    if (template.defaultHour % 12 == 0) 12
                                    else template.defaultHour % 12,
                                    template.defaultMinute,
                                    if (template.defaultHour < 12) "AM" else "PM"
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(timeStr,
                                        modifier  = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize  = 10.sp,
                                        fontWeight= FontWeight.Medium,
                                        color     = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(template.repeatType.label,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        color    = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }

                        // Add button
                        if (isAdded) {
                            Icon(
                                Icons.Rounded.CheckCircle, null,
                                tint     = Color(0xFF4CAF50),
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            IconButton(
                                onClick = { viewModel.addTemplate(template) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(10.dp)
                                    )
                            ) {
                                Icon(
                                    Icons.Rounded.Add, null,
                                    tint     = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
