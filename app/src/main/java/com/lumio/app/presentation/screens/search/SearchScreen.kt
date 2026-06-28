package com.lumio.app.presentation.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.lumio.app.domain.model.Category
import com.lumio.app.presentation.components.ReminderCard
import com.lumio.app.presentation.navigation.Screen

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState      by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try { focusRequester.requestFocus() } catch (_: Exception) {}
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value         = uiState.query,
                            onValueChange = { viewModel.setQuery(it) },
                            placeholder   = { Text("Search reminders, categories…") },
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth().focusRequester(focusRequester),
                            colors        = TextFieldDefaults.colors(
                                focusedContainerColor   = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor   = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            leadingIcon = {
                                if (uiState.isSearching)
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                else
                                    Icon(Icons.Rounded.Search, null)
                            },
                            trailingIcon = {
                                if (uiState.query.isNotBlank()) {
                                    IconButton(onClick = { viewModel.clearQuery() }) {
                                        Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                                    }
                                }
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                // Category filter chips
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategory == null,
                            onClick  = { viewModel.setCategory(null) },
                            label    = { Text("All") }
                        )
                    }
                    items(Category.defaults) { cat ->
                        val sel   = uiState.selectedCategory?.id == cat.id
                        val color = Color(android.graphics.Color.parseColor(cat.colorHex))
                        FilterChip(
                            selected = sel,
                            onClick  = { viewModel.setCategory(cat) },
                            label    = { Text("${cat.emoji} ${cat.name}", fontSize = 12.sp) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color.copy(alpha = 0.2f),
                                selectedLabelColor     = color
                            )
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                !uiState.hasSearched -> {
                    // Prompt to type
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("🔍", fontSize = 64.sp)
                        Text(
                            "Search your reminders",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Type to search by title, description or category",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        // Quick tips
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("💡 Search Tips", fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text("• Search by title: \"call doctor\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text("• Filter by category using chips above",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text("• Find completed reminders too",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }
                }

                uiState.isSearching -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Searching…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                uiState.results.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("😔", fontSize = 56.sp)
                        Text(
                            "No results for",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                "\"${uiState.query}\"",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            "Try different keywords or remove the category filter",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${uiState.results.size} result${if (uiState.results.size != 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                if (uiState.selectedCategory != null) {
                                    val color = Color(android.graphics.Color.parseColor(
                                        uiState.selectedCategory!!.colorHex
                                    ))
                                    AssistChip(
                                        onClick = { viewModel.setCategory(null) },
                                        label   = {
                                            Text(
                                                "${uiState.selectedCategory!!.emoji} ${uiState.selectedCategory!!.name}",
                                                fontSize = 11.sp
                                            )
                                        },
                                        trailingIcon = { Icon(Icons.Rounded.Close, null, Modifier.size(14.dp)) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = color.copy(alpha = 0.1f),
                                            labelColor     = color
                                        )
                                    )
                                }
                            }
                        }

                        items(uiState.results, key = { it.id }) { reminder ->
                            ReminderCard(
                                reminder   = reminder,
                                onTap      = {
                                    navController.navigate(Screen.ReminderDetail.createRoute(reminder.id))
                                },
                                onComplete = { viewModel.toggleComplete(reminder.id, it) },
                                onDelete   = { viewModel.deleteReminder(reminder.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
