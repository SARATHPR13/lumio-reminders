package com.lumio.app.presentation.screens.home

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                TopAppBar(
                    title = {
                        if (uiState.isSearchActive) {
                            TextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text("Search reminders…") },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor   = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor   = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Column {
                                Text(
                                    text = greeting(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "LUMIO",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleSearch() }) {
                            Icon(
                                imageVector = if (uiState.isSearchActive) Icons.Rounded.Close else Icons.Rounded.Search,
                                contentDescription = "Search"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                // Filter chips
                if (!uiState.isSearchActive) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        items(HomeFilter.values()) { filter ->
                            val selected = uiState.activeFilter == filter
                            FilterChip(
                                selected = selected,
                                onClick  = { viewModel.setFilter(filter) },
                                label    = {
                                    Text(
                                        text = "${filter.emoji} ${filter.label}",
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }
        },
        bottomBar = { LumioBottomNavBar(navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.AddReminder.route) },
                icon    = { Icon(Icons.Rounded.Add, contentDescription = "Add") },
                text    = { Text("New Reminder", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp)
        ) {
            // Stats row
            if (!uiState.isSearchActive) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard("Today",   "${uiState.todayCount}",     Icons.Rounded.Today,       Color(0xFF1A73E8), Modifier.weight(1f))
                        StatCard("Total",   "${uiState.totalCount}",     Icons.Rounded.List,        Color(0xFF7B2FBE), Modifier.weight(1f))
                        StatCard("Done",    "${uiState.completedCount}", Icons.Rounded.CheckCircle, Color(0xFF2E7D32), Modifier.weight(1f))
                    }
                }
            }

            // Section header
            val count = uiState.displayedReminders.size
            if (count > 0) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.isSearchActive) "Results" else uiState.activeFilter.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                            Text("$count", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }

            // Empty state
            if (uiState.displayedReminders.isEmpty()) {
                item { EmptyState(uiState.activeFilter, uiState.isSearchActive) }
            }

            // Reminder cards
            items(uiState.displayedReminders, key = { it.id }) { reminder ->
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
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun EmptyState(filter: HomeFilter, isSearching: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = when {
                isSearching                   -> "🔍"
                filter == HomeFilter.TODAY    -> "🎉"
                filter == HomeFilter.COMPLETED-> "📝"
                filter == HomeFilter.PRIORITY -> "✅"
                else                          -> "⏰"
            },
            fontSize = 56.sp
        )
        Text(
            text = when {
                isSearching                   -> "No results found"
                filter == HomeFilter.TODAY    -> "Nothing due today!"
                filter == HomeFilter.COMPLETED-> "No completed reminders"
                else                          -> "No reminders here"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = when {
                isSearching                -> "Try different keywords"
                filter == HomeFilter.TODAY -> "Enjoy your free day 😊"
                else                       -> "Tap + to add a reminder"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 0..11  -> "Good morning ☀️"
    in 12..16 -> "Good afternoon 🌤"
    in 17..20 -> "Good evening 🌙"
    else      -> "Good night 🌟"
}
