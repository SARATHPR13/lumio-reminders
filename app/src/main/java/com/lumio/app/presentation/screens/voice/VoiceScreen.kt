package com.lumio.app.presentation.screens.voice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lumio.app.domain.model.Priority
import com.lumio.app.voice.SpeechState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen(
    navController: NavController,
    viewModel: VoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }

    // Mic permission — request only when the user taps the mic.
    val recordPerm = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.startListening() }

    val onMic: () -> Unit = {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) viewModel.startListening()
        else recordPerm.launch(Manifest.permission.RECORD_AUDIO)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            viewModel.reset()
            navController.popBackStack()
        }
    }

    val listening = uiState.speechState is SpeechState.Listening
    val processing = uiState.speechState is SpeechState.Processing
    val hasPreview = uiState.parsedTitle.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quick add", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopListening()
                        navController.popBackStack()
                    }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                },
                actions = {
                    if (hasPreview) {
                        IconButton(onClick = {
                            input = ""
                            viewModel.reset()
                        }) { Icon(Icons.Rounded.Refresh, "Start over") }
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Tell me what to remember",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Type it or tap the mic — I'll work out the time for you.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(20.dp))

            // ── Text input ──
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Call Meera tomorrow at 5 PM") },
                trailingIcon = {
                    IconButton(
                        onClick = { if (input.isNotBlank()) viewModel.useSuggestion(input.trim()) },
                        enabled = input.isNotBlank()
                    ) { Icon(Icons.Rounded.Send, "Parse") }
                },
                shape = RoundedCornerShape(16.dp),
                maxLines = 3
            )

            Spacer(Modifier.height(20.dp))

            // ── Mic ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onMic,
                    shape = CircleShape,
                    color = if (listening) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (processing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(26.dp),
                                strokeWidth = 2.5.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                Icons.Rounded.Mic,
                                contentDescription = "Speak",
                                tint = if (listening) Color.White
                                else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }
            Text(
                text = when {
                    listening -> "Listening…"
                    processing -> "Thinking…"
                    else -> "Tap to speak"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // ── Suggestions (only before a preview exists) ──
            if (!hasPreview) {
                Spacer(Modifier.height(20.dp))
                Text(
                    "Try one of these",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                uiState.suggestions.forEach { s ->
                    Surface(
                        onClick = {
                            input = s
                            viewModel.useSuggestion(s)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(s, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // ── Preview card ──
            if (hasPreview) {
                Spacer(Modifier.height(20.dp))
                PreviewCard(uiState = uiState, viewModel = viewModel)
            }

            // ── Error ──
            uiState.errorMessage?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Text(
                    msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreviewCard(
    uiState: VoiceUiState,
    viewModel: VoiceViewModel
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            Text(
                "PREVIEW",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))

            // Editable title
            OutlinedTextField(
                value = uiState.parsedTitle,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Reminder") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(Modifier.height(14.dp))

            // Date + time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = listOf(uiState.parsedDate, uiState.parsedTime)
                        .filter { it.isNotBlank() }
                        .joinToString("  ·  ")
                        .ifBlank { "No time detected" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(16.dp))

            // Priority
            Text(
                "Priority",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val priorities = listOf(
                    Priority.NONE, Priority.LOW, Priority.MEDIUM, Priority.HIGH, Priority.URGENT
                )
                priorities.forEach { p ->
                    val selected = uiState.parsedPriority == p
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.updatePriority(p) },
                        label = {
                            Text(
                                text = when (p) {
                                    Priority.NONE -> "None"
                                    Priority.LOW -> "Low"
                                    Priority.MEDIUM -> "Med"
                                    Priority.HIGH -> "High"
                                    Priority.URGENT -> "Urgent"
                                }
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Location placeholder (real triggers come in the next step)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        "Location trigger — coming soon",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Save
            Button(
                onClick = { viewModel.saveReminder() },
                enabled = uiState.parsedTitle.isNotBlank() && !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save reminder", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
