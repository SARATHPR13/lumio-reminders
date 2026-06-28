package com.lumio.app.presentation.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.repository.ReminderRepository
import com.lumio.app.presentation.components.LumioBottomNavBar
import com.lumio.app.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class CalendarUiState(
    val currentMonth: Calendar         = Calendar.getInstance(),
    val selectedDate: Calendar         = Calendar.getInstance(),
    val remindersForDate: List<Reminder> = emptyList(),
    val datesWithReminders: Set<Int>   = emptySet()
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init { loadReminders() }

    private fun loadReminders() {
        viewModelScope.launch {
            reminderRepository.getAllReminders().collect { reminders ->
                val dates = reminders.map { r ->
                    Calendar.getInstance().apply {
                        timeInMillis = r.dateTimeMillis
                    }.get(Calendar.DAY_OF_MONTH)
                }.toSet()
                _uiState.update { it.copy(datesWithReminders = dates) }
                loadForDate(_uiState.value.selectedDate, reminders)
            }
        }
    }

    private fun loadForDate(date: Calendar, all: List<Reminder>) {
        val filtered = all.filter { r ->
            val rCal = Calendar.getInstance().apply { timeInMillis = r.dateTimeMillis }
            rCal.get(Calendar.YEAR)         == date.get(Calendar.YEAR) &&
            rCal.get(Calendar.MONTH)        == date.get(Calendar.MONTH) &&
            rCal.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)
        }
        _uiState.update { it.copy(remindersForDate = filtered) }
    }

    fun previousMonth() {
        val cal = _uiState.value.currentMonth.clone() as Calendar
        cal.add(Calendar.MONTH, -1)
        _uiState.update { it.copy(currentMonth = cal) }
    }

    fun nextMonth() {
        val cal = _uiState.value.currentMonth.clone() as Calendar
        cal.add(Calendar.MONTH, 1)
        _uiState.update { it.copy(currentMonth = cal) }
    }

    fun selectDate(day: Int) {
        val cal = _uiState.value.currentMonth.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, day)
        _uiState.update { it.copy(selectedDate = cal) }
        viewModelScope.launch {
            reminderRepository.getAllReminders().first().let { all ->
                loadForDate(cal, all)
            }
        }
    }
}

@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar    = { LumioBottomNavBar(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CalendarCard(
                    currentMonth       = uiState.currentMonth,
                    selectedDate       = uiState.selectedDate,
                    datesWithReminders = uiState.datesWithReminders,
                    onPrevMonth        = { viewModel.previousMonth() },
                    onNextMonth        = { viewModel.nextMonth() },
                    onDayClick         = { viewModel.selectDate(it) }
                )
            }

            // Selected date header
            item {
                val months = arrayOf("January","February","March","April","May",
                    "June","July","August","September","October","November","December")
                val selDate = uiState.selectedDate
                val dateStr = "${months[selDate.get(Calendar.MONTH)]} " +
                              "${selDate.get(Calendar.DAY_OF_MONTH)}, " +
                              "${selDate.get(Calendar.YEAR)}"

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text       = dateStr,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (uiState.remindersForDate.isNotEmpty()) {
                        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                            Text(
                                "${uiState.remindersForDate.size}",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            if (uiState.remindersForDate.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📅", fontSize = 48.sp)
                            Text("No reminders this day",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(uiState.remindersForDate) { reminder ->
                    Card(
                        onClick   = {
                            navController.navigate(
                                Screen.ReminderDetail.createRoute(reminder.id)
                            )
                        },
                        shape     = RoundedCornerShape(14.dp),
                        colors    = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val color = if (reminder.priority.colorHex.isNotEmpty())
                                Color(android.graphics.Color.parseColor(reminder.priority.colorHex))
                            else MaterialTheme.colorScheme.primary

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    reminder.category?.emoji ?: "🔔",
                                    fontSize = 20.sp
                                )
                            }

                            Column(Modifier.weight(1f)) {
                                Text(
                                    reminder.title,
                                    fontWeight = FontWeight.SemiBold,
                                    style      = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    reminder.formattedTime,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (reminder.isCompleted) {
                                Icon(
                                    Icons.Rounded.CheckCircle, null,
                                    tint     = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
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
private fun CalendarCard(
    currentMonth: Calendar,
    selectedDate: Calendar,
    datesWithReminders: Set<Int>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (Int) -> Unit
) {
    val months = arrayOf("January","February","March","April","May",
        "June","July","August","September","October","November","December")
    val days   = arrayOf("S","M","T","W","T","F","S")

    Card(
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month header
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onPrevMonth) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                }
                Text(
                    "${months[currentMonth.get(Calendar.MONTH)]} " +
                    "${currentMonth.get(Calendar.YEAR)}",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Day labels
            Row(modifier = Modifier.fillMaxWidth()) {
                days.forEach { day ->
                    Text(
                        text      = day,
                        modifier  = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style     = MaterialTheme.typography.labelSmall,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight= FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Calendar grid
            val firstDayOfMonth = currentMonth.clone() as Calendar
            firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)
            val startOffset  = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1
            val daysInMonth  = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
            val today        = Calendar.getInstance()

            val totalCells = startOffset + daysInMonth
            val rows       = (totalCells + 6) / 7

            repeat(rows) { row ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { col ->
                        val dayNum = row * 7 + col - startOffset + 1
                        Box(
                            modifier         = Modifier.weight(1f).aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayNum in 1..daysInMonth) {
                                val isSelected = selectedDate.get(Calendar.DAY_OF_MONTH) == dayNum &&
                                    selectedDate.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) &&
                                    selectedDate.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR)
                                val isToday = today.get(Calendar.DAY_OF_MONTH) == dayNum &&
                                    today.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) &&
                                    today.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR)
                                val hasReminder = datesWithReminders.contains(dayNum)

                                Column(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                isToday    -> MaterialTheme.colorScheme.primaryContainer
                                                else       -> Color.Transparent
                                            }
                                        )
                                        .clickable { onDayClick(dayNum) },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text  = "$dayNum",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected || isToday)
                                            FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            isToday    -> MaterialTheme.colorScheme.onPrimaryContainer
                                            else       -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    if (hasReminder && !isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
