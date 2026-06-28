package com.lumio.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumio.app.domain.model.Priority
import com.lumio.app.domain.model.Reminder
import com.lumio.app.domain.model.RepeatType

@Composable
fun ReminderCard(
    reminder: Reminder,
    onTap: () -> Unit,
    onComplete: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priorityColor = if (reminder.priority != Priority.NONE)
        Color(android.graphics.Color.parseColor(reminder.priority.colorHex))
    else MaterialTheme.colorScheme.outlineVariant

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(); true }
            else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(end = 24.dp)
                )
            }
        }
    ) {
        Card(
            onClick = onTap,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (reminder.isCompleted)
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (reminder.isCompleted) 0.dp else 2.dp
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 12.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority color bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(52.dp)
                        .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                        .background(priorityColor.copy(alpha = if (reminder.isCompleted) 0.3f else 1f))
                )
                Spacer(Modifier.width(10.dp))
                // Checkbox
                Checkbox(
                    checked = reminder.isCompleted,
                    onCheckedChange = onComplete,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = priorityColor
                    )
                )
                Spacer(Modifier.width(8.dp))
                // Text content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (reminder.isCompleted)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null
                    )
                    if (reminder.description.isNotBlank() && !reminder.isCompleted) {
                        Text(
                            text = reminder.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (reminder.isOverdue && !reminder.isCompleted)
                                Icons.Rounded.Warning else Icons.Rounded.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = if (reminder.isOverdue && !reminder.isCompleted)
                                Color(0xFFD32F2F)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = reminder.formattedDateTime,
                            fontSize = 11.sp,
                            color = if (reminder.isOverdue && !reminder.isCompleted)
                                Color(0xFFD32F2F)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (reminder.category != null) {
                            val catColor = Color(android.graphics.Color.parseColor(reminder.category.colorHex))
                            Text(
                                text = "${reminder.category.emoji} ${reminder.category.name}",
                                fontSize = 10.sp,
                                color = catColor,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(catColor.copy(alpha = 0.1f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                        if (reminder.repeatType != RepeatType.NONE) {
                            Icon(
                                imageVector = Icons.Rounded.Repeat,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                if (reminder.priority != Priority.NONE && !reminder.isCompleted) {
                    PriorityBadge(priority = reminder.priority, showLabel = false)
                }
            }
        }
    }
}
