package com.lumio.app.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lumio.app.presentation.components.LumioBottomNavBar
import com.lumio.app.presentation.components.ReminderCard
import com.lumio.app.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            // Branding "LUMIO" bar removed. This paints off-white behind the
            // status bar (background is applied BEFORE statusBarsPadding) so
            // there is no dark strip at the top, then shows the filter chips.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
            ) {
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(HomeFilter.values()) { filter ->
                        val selected = uiState.activeFilter == filter
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setFilter(filter) },
                            label = {
                                Text(
                                    text = "${filter.emoji} ${filter.label}",
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 0.5.dp
                )
            }
        },
        bottomBar = { LumioBottomNavBar(navController) },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { navController.navigate(Screen.AiChat.route) },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Rounded.AutoAwesome, "AI", modifier = Modifier.size(20.dp))
                }
                SmallFloatingActionButton(
                    onClick = { navController.navigate(Screen.Voice.route) },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Rounded.Mic, "Voice", modifier = Modifier.size(20.dp))
                }
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Screen.AddReminder.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    icon = { Icon(Icons.Rounded.Add, "Add") },
                    text = { Text("Add Reminder", fontWeight = FontWeight.Bold) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 160.dp, top = 12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard("Today", "${uiState.todayCount}", Icons.Rounded.WbSunny,
                        Color(0xFF2563EB), Color(0xFF7C3AED), Modifier.weight(1f))
                    StatCard("Active", "${uiState.totalCount}", Icons.AutoMirrored.Rounded.List,
                        Color(0xFF7C3AED), Color(0xFFDB2777), Modifier.weight(1f))
                    StatCard("Done", "${uiState.completedCount}", Icons.Rounded.TaskAlt,
                        Color(0xFF059669), Color(0xFF0D9488), Modifier.weight(1f))
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = uiState.activeFilter.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.displayedReminders.isNotEmpty()) {
                            Text(
                                text = "${uiState.displayedReminders.size} reminders",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (uiState.completedCount > 0) {
                        TextButton(onClick = { viewModel.deleteAllCompleted() }) {
                            Text(
                                text = "Clear done",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (uiState.displayedReminders.isEmpty()) {
                item { EmptyState(filter = uiState.activeFilter) }
            }

            items(
                items = uiState.displayedReminders,
                key = { it.id }
            ) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    onTap = { navController.navigate(Screen.ReminderDetail.createRoute(reminder.id)) },
                    onComplete = { viewModel.toggleComplete(reminder.id, it) },
                    onDelete = { viewModel.deleteReminder(reminder.id) }
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color1: Color,
    color2: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(color1, color2)))
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(icon, null, tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp))
                Column {
                    Text(value, style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black, color = Color.White)
                    Text(label, style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f))
                }
            }
        }
    }
}

@Composable
private fun EmptyState(filter: HomeFilter) {
    val emoji = when (filter) {
        HomeFilter.TODAY -> "🎉"
        HomeFilter.COMPLETED -> "✅"
        HomeFilter.PRIORITY -> "⚡"
        HomeFilter.UPCOMING -> "⏰"
        HomeFilter.ALL -> "📋"
    }
    val title = when (filter) {
        HomeFilter.TODAY -> "All clear today!"
        HomeFilter.COMPLETED -> "Nothing completed yet"
        HomeFilter.PRIORITY -> "No priority items"
        HomeFilter.UPCOMING -> "Nothing upcoming"
        HomeFilter.ALL -> "Your list is empty"
    }
    val subtitle = when (filter) {
        HomeFilter.TODAY -> "Enjoy your free day! Have a great one."
        else -> "Tap + to create your first reminder"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 52.sp)
        }
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.Mic, null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp))
                Text("Try voice: Remind me tomorrow at 5 PM",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
