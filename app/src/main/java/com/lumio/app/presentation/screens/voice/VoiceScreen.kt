package com.lumio.app.presentation.screens.voice

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lumio.app.domain.model.Category
import com.lumio.app.domain.model.Priority
import com.lumio.app.presentation.components.LumioBottomNavBar
import com.lumio.app.voice.SpeechState

@Composable
fun VoiceScreen(
    navController: NavController,
    viewModel: VoiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Reminder", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopListening()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (uiState.showPreview) {
                        IconButton(onClick = { viewModel.reset() }) {
                            Icon(Icons.Rounded.Refresh, "Reset")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = { LumioBottomNavBar(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (!uiState.isAvailable) {
                NotAvailableState()
                return@Column
            }

            Spacer(Modifier.height(24.dp))

            // ── Mic Button ────────────────────────────
            MicSection(
                speechState = uiState.speechState,
                spokenText  = uiState.spokenText,
                onMicClick  = {
                    when (uiState.speechState) {
                        is SpeechState.Listening -> viewModel.stopListening()
                        else                     -> viewModel.startListening()
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            // ── Error message ──────────────────────────
            uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Rounded.Warning, null,
                            tint = MaterialTheme.colorScheme.onErrorContainer)
                        Text(error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Parsed Preview ─────────────────────────
            if (uiState.showPreview) {
                ParsedPreview(
                    uiState   = uiState,
                    onTitle   = { viewModel.updateTitle(it) },
                    onPriority= { viewModel.updatePriority(it) },
                    onCategory= { viewModel.updateCategory(it) },
                    onSave    = { viewModel.saveReminder() }
                )
            } else {
                // ── Suggestions ────────────────────────
                SuggestionsSection(
                    suggestions = uiState.suggestions,
                    onTap       = { viewModel.useSuggestion(it) }
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun MicSection(
    speechState: SpeechState,
    spokenText: String,
    onMicClick: () -> Unit
) {
    val isListening = speechState is SpeechState.Listening

    // Pulsing animation
    val infiniteAnim = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteAnim.animateFloat(
        initialValue   = 1f,
        targetValue    = 1.25f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {

        // Mic button
        Box(contentAlignment = Alignment.Center) {
            // Outer pulse ring
            if (isListening) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                )
            }

            // Inner button
            Surface(
                onClick    = onMicClick,
                shape      = CircleShape,
                color      = when (speechState) {
                    is SpeechState.Listening  -> MaterialTheme.colorScheme.error
                    is SpeechState.Processing -> MaterialTheme.colorScheme.secondary
                    else                      -> MaterialTheme.colorScheme.primary
                },
                modifier   = Modifier.size(100.dp),
                shadowElevation = if (isListening) 12.dp else 4.dp
            ) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    when (speechState) {
                        is SpeechState.Processing -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color    = Color.White
                            )
                        }
                        is SpeechState.Listening -> {
                            Icon(
                                Icons.Rounded.MicOff,
                                "Stop",
                                tint     = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        else -> {
                            Icon(
                                Icons.Rounded.Mic,
                                "Start",
                                tint     = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                }
            }
        }

        // Status text
        Text(
            text = when (speechState) {
                is SpeechState.Idle       -> "Tap to speak"
                is SpeechState.Listening  -> "Listening…"
                is SpeechState.Processing -> "Processing…"
                is SpeechState.Result     -> "Got it!"
                is SpeechState.Error      -> "Try again"
            },
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color      = when (speechState) {
                is SpeechState.Listening  -> MaterialTheme.colorScheme.error
                is SpeechState.Processing -> MaterialTheme.colorScheme.secondary
                is SpeechState.Result     -> Color(0xFF4CAF50)
                is SpeechState.Error      -> MaterialTheme.colorScheme.error
                else                      -> MaterialTheme.colorScheme.onBackground
            }
        )

        // Example hint
        if (speechState is SpeechState.Idle) {
            Card(
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text      = "Say: \"Remind me to call Rahul\ntomorrow at 6 PM\"",
                    modifier  = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center,
                    style     = MaterialTheme.typography.bodySmall,
                    color     = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Spoken text display
        if (spokenText.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Rounded.FormatQuote, null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp))
                    Text(
                        text  = "\"$spokenText\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun ParsedPreview(
    uiState: VoiceUiState,
    onTitle: (String) -> Unit,
    onPriority: (Priority) -> Unit,
    onCategory: (Category?) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.AutoAwesome,
                null,
                tint     = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "AI Parsed Result",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.weight(1f))

            // Confidence badge
            val confColor = when {
                uiState.confidence > 0.8f -> Color(0xFF4CAF50)
                uiState.confidence > 0.5f -> Color(0xFFF9A825)
                else                      -> Color(0xFFFF6B35)
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = confColor.copy(alpha = 0.15f)
            ) {
                Text(
                    "${(uiState.confidence * 100).toInt()}% confident",
                    modifier  = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize  = 11.sp,
                    fontWeight= FontWeight.SemiBold,
                    color     = confColor
                )
            }
        }

        // Title field
        OutlinedTextField(
            value         = uiState.parsedTitle,
            onValueChange = onTitle,
            label         = { Text("Title") },
            leadingIcon   = { Icon(Icons.Rounded.Title, null) },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            singleLine    = true
        )

        // Date & Time display
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(12.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("📅 Date", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        uiState.parsedDate,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(12.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("⏰ Time", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        uiState.parsedTime,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Priority
        Text("Priority", style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(Priority.values()) { p ->
                val sel   = uiState.parsedPriority == p
                val color = Color(android.graphics.Color.parseColor(p.colorHex))
                FilterChip(
                    selected = sel,
                    onClick  = { onPriority(p) },
                    label    = { Text("${p.emoji} ${p.label}", fontSize = 12.sp) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color.copy(alpha = 0.2f),
                        selectedLabelColor     = color
                    )
                )
            }
        }

        // Category
        Text("Category", style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = uiState.parsedCategory == null,
                    onClick  = { onCategory(null) },
                    label    = { Text("None") }
                )
            }
            items(Category.defaults) { cat ->
                val sel   = uiState.parsedCategory?.id == cat.id
                val color = Color(android.graphics.Color.parseColor(cat.colorHex))
                FilterChip(
                    selected = sel,
                    onClick  = { onCategory(cat) },
                    label    = { Text("${cat.emoji} ${cat.name}", fontSize = 12.sp) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color.copy(alpha = 0.2f),
                        selectedLabelColor     = color
                    )
                )
            }
        }

        // Save button
        Button(
            onClick  = onSave,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(12.dp),
            enabled  = !uiState.isSaving
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color    = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(Icons.Rounded.Check, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save Reminder", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun SuggestionsSection(
    suggestions: List<String>,
    onTap: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Lightbulb, null,
                tint = Color(0xFFF9A825),
                modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "Try saying…",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        suggestions.forEach { suggestion ->
            Card(
                onClick = { onTap(suggestion) },
                shape   = RoundedCornerShape(12.dp),
                colors  = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border  = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🎙️", fontSize = 16.sp)
                    Text(
                        "\"$suggestion\"",
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.AutoMirrored.Rounded.PlayArrow, null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotAvailableState() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("🎤", fontSize = 72.sp)
            Text(
                "Voice Not Available",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Speech recognition is not available on this device. Please use the keyboard to add reminders.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
