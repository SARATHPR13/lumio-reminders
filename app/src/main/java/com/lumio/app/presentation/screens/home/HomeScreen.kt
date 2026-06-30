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
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text  = getGreeting(),
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
                    IconButton(onClick = { navController.navigate(Screen.Voice.route) }) {
                        Icon(
                            imageVector        = Icons.Rounded.Mic,
                            contentDescription = "Add by voice",
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                        Icon(
                            imageVector        = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(
                            imageVector        = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

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
                                    text       = "${filter.emoji} ${filter.label}",
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize   = 13.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor     = Color.White,
                                containerColor         = MaterialTheme.colorScheme.surface,
                                labelColor             = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(
                    color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 0.5.dp
                )
            }
        },
        bottomBar = {
            LumioBottomNavBar(navController = navController)
        },
        floatingActionButton = {
            // Two buttons only — short enough to never reach the bottom nav
            // bar. AI has its own bottom-nav tab and Voice lives in the top
            // bar, so Location and Add are the only two that belong here.
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallFloatingActionButton(
                    onClick        = { navController.navigate(Screen.Location.route) },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor   = MaterialTheme.colorScheme.onTertiaryContainer,
                    shape          = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.LocationOn,
                        contentDescription = "Location reminder",
                        modifier           = Modifier.size(20.dp)
                    )
                }
                FloatingActionButton(
                    onClick        = { navController.navigate(Screen.AddReminder.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = Color.White,
                    shape          = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.Add,
                        contentDescription = "Add reminder",
                        modifier           = Modifier.size(26.dp)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            state          = listState,
            modifier       = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 110.dp, top = 12.dp)
        ) {
            item {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        label    = "Today",
                        value    = "${uiState.todayCount}",
                        icon     = Icons.Rounded.WbSunny,
                        color1   = Color(0xFF2563EB),
                        color2   = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label    = "Active",
                        value    = "${uiState.totalCount}",
                        icon     = Icons.AutoMirrored.Rounded.List,
                        color1   = Color(0xFF7C3AED),
                        color2   = Color(0xFFDB2777),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label    = "Done",
                        value    = "${uiState.completedCount}",
                        icon     = Icons.Rounded.TaskAlt,
                        color1   = Color(0xFF059669),
                        color2   = Color(0xFF0D9488),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            item {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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
                    if (uiState.completedCount > 0) {
                        TextButton(onClick = { viewModel.deleteAllCompleted() }) {
                            Text(
                                text  = "Clear done",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (uiState.displayedReminders.isEmpty()) {
                item {
                    EmptyState(filter = uiState.activeFilter)
                }
            }

            items(
                items = uiState.displayedReminders,
                key   = { it.id }
            ) { reminder ->
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
private fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color1: Color,
    color2: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier.height(96.dp),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(color1, color2)))
                .padding(14.dp)
        ) {
            Column(
                modifier            = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = Color.White.copy(alpha = 0.8f),
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
private fun EmptyState(filter: HomeFilter) {
    Column(
        modifier            = Modifier
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
            Text(
                text     = when (filter) {
                    HomeFilter.TODAY     -> "\uD83C\uDF89"
                    HomeFilter.COMPLETED -> "\u2705"
                    HomeFilter.PRIORITY  -> "\u26A1"
                    HomeFilter.UPCOMING  -> "\u23F0"
                    HomeFilter.ALL       -> "\uD83D\uDCCB"
                },
                fontSize = 52.sp
            )
        }
        Text(
            text       = when (filter) {
                HomeFilter.TODAY     -> "All clear today!"
                HomeFilter.COMPLETED -> "Nothing completed yet"
                HomeFilter.PRIORITY  -> "No priority items"
                HomeFilter.UPCOMING  -> "Nothing upcoming"
                HomeFilter.ALL       -> "Your list is empty"
            },
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text  = when (filter) {
                HomeFilter.TODAY -> "Enjoy your free day!"
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
                modifier              = Modifier.padding(
                    horizontal = 16.dp,
                    vertical   = 10.dp
                ),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector        = Icons.Rounded.Mic,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(16.dp)
                )
                Text(
                    text  = "Try: Remind me tomorrow at 5 PM",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11  -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..20 -> "Good evening"
        else      -> "Good night"
    }
}
