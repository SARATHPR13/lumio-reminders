package com.lumio.app.presentation.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class StatsUiState(
    val totalReminders: Int    = 0,
    val completedToday: Int    = 0,
    val completedThisWeek: Int = 0,
    val completedTotal: Int    = 0,
    val overdueCount: Int      = 0,
    val completionRate: Float  = 0f,
    val priorityBreakdown: Map<Priority, Int> = emptyMap(),
    val categoryBreakdown: Map<String, Int>   = emptyMap(),
    val streakDays: Int        = 0,
    val bestStreak: Int        = 0,
    val mostProductiveHour: Int? = null,
    val mostProductiveDay: String = ""
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init { loadStats() }

    private fun loadStats() {
        viewModelScope.launch {
            reminderRepository.getAllReminders().collect { reminders ->
                _uiState.value = computeStats(reminders)
            }
        }
    }

    private fun computeStats(reminders: List<Reminder>): StatsUiState {
        val now       = System.currentTimeMillis()
        val today     = Calendar.getInstance()
        val weekStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }.timeInMillis

        val completed      = reminders.filter { it.isCompleted }
        val active         = reminders.filter { !it.isCompleted }
        val completedToday = completed.filter { r ->
            val cal = Calendar.getInstance().apply { timeInMillis = r.dateTimeMillis }
            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
            cal.get(Calendar.YEAR)        == today.get(Calendar.YEAR)
        }
        val completedWeek  = completed.filter { it.dateTimeMillis >= weekStart }
        val overdue        = active.filter { it.isOverdue }
        val total          = reminders.size
        val rate           = if (total > 0) completed.size.toFloat() / total.toFloat() else 0f

        val priorityBreakdown = Priority.values().associateWith { p ->
            reminders.count { it.priority == p && !it.isCompleted }
        }

        val categoryBreakdown = reminders
            .filter { !it.isCompleted && it.category != null }
            .groupBy { it.category!!.name }
            .mapValues { it.value.size }

        val peakHour = completed
            .groupBy { r ->
                Calendar.getInstance().apply {
                    timeInMillis = r.dateTimeMillis
                }.get(Calendar.HOUR_OF_DAY)
            }
            .maxByOrNull { it.value.size }?.key

        val dayNames = arrayOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")
        val peakDay  = completed
            .groupBy { r ->
                Calendar.getInstance().apply {
                    timeInMillis = r.dateTimeMillis
                }.get(Calendar.DAY_OF_WEEK)
            }
            .maxByOrNull { it.value.size }
            ?.let { dayNames[it.key - 1] } ?: ""

        return StatsUiState(
            totalReminders      = total,
            completedToday      = completedToday.size,
            completedThisWeek   = completedWeek.size,
            completedTotal      = completed.size,
            overdueCount        = overdue.size,
            completionRate      = rate,
            priorityBreakdown   = priorityBreakdown,
            categoryBreakdown   = categoryBreakdown,
            mostProductiveHour  = peakHour,
            mostProductiveDay   = peakDay
        )
    }
}

@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Statistics", fontWeight = FontWeight.Bold) },
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
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Overview Cards ────────────────────────
            item {
                Text("Overview",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GradientNumCard("Today",
                        "${uiState.completedToday}",
                        "Completed",
                        listOf(Color(0xFF1A73E8), Color(0xFF4A90E2)),
                        Modifier.weight(1f))
                    GradientNumCard("This Week",
                        "${uiState.completedThisWeek}",
                        "Completed",
                        listOf(Color(0xFF7B2FBE), Color(0xFFA855F7)),
                        Modifier.weight(1f))
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GradientNumCard("Total",
                        "${uiState.completedTotal}",
                        "Done",
                        listOf(Color(0xFF2E7D32), Color(0xFF4CAF50)),
                        Modifier.weight(1f))
                    GradientNumCard("Overdue",
                        "${uiState.overdueCount}",
                        "Need attention",
                        listOf(Color(0xFFD32F2F), Color(0xFFFF6B35)),
                        Modifier.weight(1f))
                }
            }

            // ── Completion Rate ───────────────────────
            item {
                Text("Completion Rate",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }

            item {
                Card(
                    shape     = RoundedCornerShape(18.dp),
                    colors    = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${(uiState.completionRate * 100).toInt()}%",
                                style      = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color      = MaterialTheme.colorScheme.primary,
                                modifier   = Modifier.weight(1f)
                            )
                            Text(
                                when {
                                    uiState.completionRate >= 0.8f -> "Excellent! 🏆"
                                    uiState.completionRate >= 0.6f -> "Good! 👍"
                                    uiState.completionRate >= 0.4f -> "Keep going! 💪"
                                    else                           -> "Just starting! 🌱"
                                },
                                style  = MaterialTheme.typography.titleSmall,
                                color  = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        LinearProgressIndicator(
                            progress = { uiState.completionRate },
                            modifier = Modifier.fillMaxWidth().height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color            = MaterialTheme.colorScheme.primary,
                            trackColor       = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Text(
                            "${uiState.completedTotal} of ${uiState.totalReminders} reminders completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Productivity Insights ─────────────────
            if (uiState.mostProductiveHour != null || uiState.mostProductiveDay.isNotEmpty()) {
                item {
                    Text("Productivity Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                }

                item {
                    Card(
                        shape  = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.mostProductiveHour?.let { hour ->
                                val ampm  = if (hour < 12) "AM" else "PM"
                                val h     = if (hour % 12 == 0) 12 else hour % 12
                                InsightRow("⏰", "Most Productive Time",
                                    "$h:00 $ampm")
                            }
                            if (uiState.mostProductiveDay.isNotEmpty()) {
                                InsightRow("📅", "Best Day", uiState.mostProductiveDay)
                            }
                            InsightRow("✅", "Tasks Completed Today",
                                "${uiState.completedToday}")
                            InsightRow("🔥", "This Week",
                                "${uiState.completedThisWeek} completed")
                        }
                    }
                }
            }

            // ── Priority Breakdown ────────────────────
            item {
                Text("Active by Priority",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }

            item {
                Card(
                    shape  = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        uiState.priorityBreakdown
                            .filter { it.value > 0 }
                            .entries
                            .sortedByDescending { it.key.level }
                            .forEach { (priority, count) ->
                                val color = Color(
                                    android.graphics.Color.parseColor(priority.colorHex)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(priority.emoji, fontSize = 18.sp)
                                    Spacer(Modifier.width(10.dp))
                                    Text(priority.label,
                                        style    = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f))
                                    Text("$count",
                                        fontWeight = FontWeight.Bold,
                                        color      = color)
                                }
                            }

                        if (uiState.priorityBreakdown.values.all { it == 0 }) {
                            Box(
                                Modifier.fillMaxWidth().padding(16.dp),
                                Alignment.Center
                            ) {
                                Text("No active reminders 🎉",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun GradientNumCard(
    title: String,
    value: String,
    subtitle: String,
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
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f))
                Text(value,
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Color.White)
                Text(subtitle, style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun InsightRow(emoji: String, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 18.sp, modifier = Modifier.width(32.dp))
        Text(label,
            style    = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f))
        Text(value,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.primary)
    }
}
