package com.lumio.app.presentation.screens.home

import androidx.compose.animation.*
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
import com.lumio.app.presentation.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
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
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // ── Top Bar ──────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text  = greeting(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text       = "LUMIO",
                                style      = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color      = MaterialTheme.colorScheme.primary
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(MaterialTheme.colorScheme.secondary)
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                            Icon(Icons.Rounded.Search, "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(Icons.Rounded.Settings, "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // ── Filter Chips ─────────────────────
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(HomeFilter.values()) { filter ->
                        val selected = uiState.activeFilter == filter
                        FilterChip(
                            selected = selected,
                            onClick  = { viewModel.setFilter(filter) },
                            label    = {
                                Text(
                                    "${filter.emoji} ${filter.label}",
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize   = 13.sp
                                )
                            },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor     = Color.White,
                                containerColor         = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (selected) null else FilterChipDefaults.filterChipBorder(
                                enabled          = true,
                                selected         = false,
                                borderColor      = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                selectedBorderColor = Color.Transparent
                            ),
                            shape    = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                HorizontalDivider(
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
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
                // AI FAB
                SmallFloatingActionButton(
                    onClick        = { navController.navigate(Screen.AiChat.route) },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor   = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape          = RoundedCornerShape(14.dp),
                    modifier       = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Rounded.AutoAwesome, "AI", modifier = Modifier.size(20.dp))
                }
                // Voice FAB
                SmallFloatingActionButton(
                    onClick        = { navController.navigate(Screen.Voice.route) },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor   = MaterialTheme.colorScheme.onTertiaryContainer,
                    shape          = RoundedCornerShape(14.dp),
                    modifier       = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Rounded.Mic, "Voice", modifier = Modifier.size(20.dp))
                }
                // Main Add FAB
                FloatingActionButton(
                    onClick        = { navController.navigate(Screen.AddReminder.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = Color.White,
                    shape          = RoundedCornerShape(18.dp),
                    modifier       = Modifier.size(58.dp),
                    elevation      = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
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
            contentPadding = PaddingValues(bottom = 160.dp, top = 12.dp)
        ) {
            // ── Stats Row ─────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PremiumStatCard(
                        label    = "Today",
                        value    = "${uiState.todayCount}",
                        icon     = Icons.Rounded.WbSunny,
                        gradient = GradientBlue,
                        modifier = Modifier.weight(1f)
                    )
                    PremiumStatCard(
                        label    = "Active",
                        value    = "${uiState.totalCount}",
                        icon     = Icons.AutoMirrored.Rounded.List,
                        gradient = GradientPurple,
                        modifier = Modifier.weight(1f)
                    )
                    PremiumStatCard(
                        label    = "Done",
                        value    = "${uiState.completedCount}",
                        icon     = Icons.Rounded.TaskAlt,
                        gradient = GradientGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            // ── Section Header ────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text       = uiState.activeFilter.label,
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.displayedReminders.isNotEmpty()) {
                            Text(
                                text  = "${uiState.displayedReminders.size} reminders",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (uiState.displayedReminders.isNotEmpty()) {
                        TextButton(onClick = { viewModel.deleteAllCompleted() }) {
                            Text("Clear done",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // ── Empty State ───────────────────────────
            if (uiState.displayedReminders.isEmpty()) {
                item { PremiumEmptyState(filter = uiState.activeFilter) }
            }

            // ── Reminders ─────────────────────────────
            items(
                uiState.displayedReminders,
                key = { it.id }
            ) { reminder ->
                ReminderCard(
                    reminder   = reminder,
                    onTap      = { navController.navigate(Screen.ReminderDetail.createRoute(reminder.id)) },
                    onComplete = { viewModel.toggleComplete(reminder.id, it) },
                    onDelete   = { viewModel.deleteReminder(reminder.id) }
                )
            }
        }
    }
}

@Composable
private fun PremiumStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier.height(96.dp),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradient))
                .padding(14.dp)
        ) {
            Column(
                modifier              = Modifier.fillMaxSize(),
                verticalArrangement   = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = Color.White.copy(alpha = 0.85f),
                    modifier           = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text       = value,
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color      = Color.White
                    )
                    Text(
                        text  = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumEmptyState(filter: HomeFilter) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(32.dp),
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
            Text(
                text     = when (filter) {
                    HomeFilter.TODAY     -> "🎉"
                    HomeFilter.COMPLETED -> "✅"
                    HomeFilter.PRIORITY  -> "⚡"
                    HomeFilter.UPCOMING  -> "⏰"
                    HomeFilter.ALL       -> "📋"
                },
                fontSize = 52.sp
            )
        }

        Text(
            text       = when (filter) {
                HomeFilter.TODAY    -> "All clear today!"
                HomeFilter.COMPLETED-> "Nothing completed yet"
                HomeFilter.PRIORITY -> "No priority items"
                HomeFilter.UPCOMING -> "Nothing upcoming"
                HomeFilter.ALL      -> "Your reminder list is empty"
            },
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text  = when (filter) {
                HomeFilter.TODAY -> "Enjoy your free day \uD83D\uDE0A"
                else             -> "Tap + to create your first reminder"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier              = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.Mic, null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp))
                Text("Try voice: "Remind me tomorrow at 5 PM"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11  -> "Good morning \u2600\uFE0F"
    in 12..16 -> "Good afternoon \uD83C\uDF24\uFE0F"
    in 17..20 -> "Good evening \uD83C\uDF19"
    else      -> "Good night \uD83C\uDF1F"
}
