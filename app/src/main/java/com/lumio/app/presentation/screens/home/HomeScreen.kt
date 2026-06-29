package com.lumio.app.presentation.screens.home

import androidx.compose.animation.core.*
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
import java.util.Calendar

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState  by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // ── Premium Top Bar ────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text  = greeting(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text       = "LUMIO",
                                style      = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color      = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text     = " ✦",
                                fontSize = 14.sp,
                                color    = Color(0xFFA855F7)
                            )
                        }
                    }
                    // Action icons
                    IconButton(onClick = { navController.navigate(Screen.Voice.route) }) {
                        Icon(Icons.Rounded.Mic, "Voice",
                            tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                        Icon(Icons.Rounded.Search, "Search",
                            tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = { viewModel.deleteAllCompleted() }) {
                        Icon(Icons.Rounded.CleaningServices, "Clear",
                            tint = MaterialTheme.colorScheme.onBackground)
                    }
                }

                // ── Filter Chips ──────────────────────
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier              = Modifier.padding(bottom = 10.dp)
                ) {
                    items(HomeFilter.values()) { filter ->
                        val selected = uiState.activeFilter == filter
                        FilterChip(
                            selected = selected,
                            onClick  = { viewModel.setFilter(filter) },
                            label    = {
                                Text(
                                    "${filter.emoji} ${filter.label}",
                                    fontWeight = if (selected) FontWeight.Bold
                                               else FontWeight.Normal,
                                    fontSize   = 13.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor     = MaterialTheme.colorScheme.onPrimary,
                                containerColor         = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }
        },
        bottomBar = { LumioBottomNavBar(navController) },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallFloatingActionButton(
                    onClick        = { navController.navigate(Screen.Voice.route) },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor   = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape          = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.Mic, "Voice")
                }
                FloatingActionButton(
                    onClick        = { navController.navigate(Screen.AddReminder.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary,
                    shape          = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Rounded.Add, "Add", modifier = Modifier.size(28.dp))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            state          = listState,
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 140.dp, top = 8.dp)
        ) {
            // ── Stats Cards ───────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GradientStatCard(
                        title     = "Today",
                        value     = "${uiState.todayCount}",
                        icon      = Icons.Rounded.Today,
                        gradient  = listOf(Color(0xFF1A73E8), Color(0xFF4A90E2)),
                        modifier  = Modifier.weight(1f)
                    )
                    GradientStatCard(
                        title     = "Active",
                        value     = "${uiState.totalCount}",
                        icon      = Icons.AutoMirrored.Rounded.List,
                        gradient  = listOf(Color(0xFF7B2FBE), Color(0xFFA855F7)),
                        modifier  = Modifier.weight(1f)
                    )
                    GradientStatCard(
                        title     = "Done",
                        value     = "${uiState.completedCount}",
                        icon      = Icons.Rounded.CheckCircle,
                        gradient  = listOf(Color(0xFF2E7D32), Color(0xFF4CAF50)),
                        modifier  = Modifier.weight(1f)
                    )
                }
            }

            // ── Section Header ────────────────────────
            val count = uiState.displayedReminders.size
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text       = uiState.activeFilter.label,
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (count > 0) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .height(2.dp)
                                    .width(32.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                    if (count > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                "$count",
                                modifier  = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color     = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight= FontWeight.Bold,
                                fontSize  = 12.sp
                            )
                        }
                    }
                }
            }

            // ── Empty State ───────────────────────────
            if (uiState.displayedReminders.isEmpty()) {
                item { PremiumEmptyState(uiState.activeFilter) }
            }

            // ── Reminder Cards ────────────────────────
            items(uiState.displayedReminders, key = { it.id }) { reminder ->
                ReminderCard(
                    reminder   = reminder,
                    onTap      = {
                        navController.navigate(
                            Screen.ReminderDetail.createRoute(reminder.id)
                        )
                    },
                    onComplete = { viewModel.toggleComplete(reminder.id, it) },
                    onDelete   = { viewModel.deleteReminder(reminder.id) }
                )
            }
        }
    }
}

@Composable
private fun GradientStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(gradient))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, null,
                    tint     = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(20.dp))
                Text(value,
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Color.White)
                Text(title,
                    style  = MaterialTheme.typography.labelSmall,
                    color  = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun PremiumEmptyState(filter: HomeFilter) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Premium illustrated empty state card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(24.dp),
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = when (filter) {
                        HomeFilter.TODAY     -> "🎉"
                        HomeFilter.COMPLETED -> "📝"
                        HomeFilter.PRIORITY  -> "✅"
                        HomeFilter.UPCOMING  -> "⏰"
                        HomeFilter.ALL       -> "📋"
                    },
                    fontSize = 64.sp
                )
                Text(
                    text = when (filter) {
                        HomeFilter.TODAY     -> "Nothing due today!"
                        HomeFilter.COMPLETED -> "No completed reminders"
                        HomeFilter.PRIORITY  -> "No priority reminders"
                        HomeFilter.UPCOMING  -> "Nothing upcoming"
                        HomeFilter.ALL       -> "No reminders yet"
                    },
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (filter) {
                        HomeFilter.TODAY -> "Enjoy your free day 😊"
                        HomeFilter.COMPLETED -> "Complete some reminders"
                        else -> "Tap + to add a reminder"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Voice add hint
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Mic, null,
                            tint     = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp))
                        Icon(Icons.Rounded.GraphicEq, null,
                            tint     = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp))
                        Text(
                            "Or tap 🎙️ to add by voice",
                            style  = MaterialTheme.typography.bodySmall,
                            color  = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 0..11  -> "Good morning ☀️"
    in 12..16 -> "Good afternoon 🌤"
    in 17..20 -> "Good evening 🌙"
    else      -> "Good night 🌟"
}
