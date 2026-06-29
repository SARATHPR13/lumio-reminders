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
    reminder  : Reminder,
    onTap     : () -> Unit,
    onComplete: (Boolean) -> Unit,
    onDelete  : () -> Unit,
    modifier  : Modifier = Modifier
) {
    val priorityColor = when (reminder.priority) {
        Priority.URGENT -> Color(0xFFDC2626)
        Priority.HIGH   -> Color(0xFFEA580C)
        Priority.MEDIUM -> Color(0xFFD97706)
        Priority.LOW    -> Color(0xFF059669)
        Priority.NONE   -> MaterialTheme.colorScheme.outlineVariant
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) { onDelete(); true }
            else false
        }
    )

    SwipeToDismissBox(
        state                       = dismissState,
        modifier                    = modifier.padding(horizontal = 16.dp, vertical = 5.dp),
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFDC2626)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(
                    modifier            = Modifier.padding(end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Rounded.DeleteOutline, null,
                        tint     = Color.White,
                        modifier = Modifier.size(24.dp))
                    Text("Delete", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) {
        Card(
            onClick   = onTap,
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(
                containerColor = if (reminder.isCompleted)
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (reminder.isCompleted) 0.dp else 1.dp
            ),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier          = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority indicator
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(44.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(priorityColor.copy(alpha = if (reminder.isCompleted) 0.25f else 1f))
                )

                Spacer(Modifier.width(12.dp))

                // Checkbox
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (reminder.isCompleted)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else
                                priorityColor.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Checkbox(
                        checked        = reminder.isCompleted,
                        onCheckedChange= onComplete,
                        modifier       = Modifier.size(20.dp),
                        colors         = CheckboxDefaults.colors(
                            checkedColor   = MaterialTheme.colorScheme.primary,
                            uncheckedColor = priorityColor
                        )
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text           = reminder.title,
                        style          = MaterialTheme.typography.titleSmall,
                        fontWeight     = FontWeight.SemiBold,
                        maxLines       = 1,
                        overflow       = TextOverflow.Ellipsis,
                        color          = if (reminder.isCompleted)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null
                    )

                    if (reminder.description.isNotBlank() && !reminder.isCompleted) {
                        Text(
                            text     = reminder.description,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        // Time chip
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (reminder.isOverdue && !reminder.isCompleted)
                                Color(0xFFDC2626).copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier              = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    if (reminder.isOverdue && !reminder.isCompleted)
                                        Icons.Rounded.Warning
                                    else Icons.Rounded.Schedule,
                                    null,
                                    modifier = Modifier.size(10.dp),
                                    tint     = if (reminder.isOverdue && !reminder.isCompleted)
                                        Color(0xFFDC2626)
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text     = reminder.formattedDateTime,
                                    fontSize = 11.sp,
                                    color    = if (reminder.isOverdue && !reminder.isCompleted)
                                        Color(0xFFDC2626)
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (reminder.isOverdue && !reminder.isCompleted)
                                        FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }

                        // Category chip
                        reminder.category?.let { cat ->
                            val catColor = Color(android.graphics.Color.parseColor(cat.colorHex))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = catColor.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text     = "${cat.emoji} ${cat.name}",
                                    fontSize = 10.sp,
                                    color    = catColor,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Repeat indicator
                        if (reminder.repeatType != RepeatType.NONE) {
                            Icon(Icons.Rounded.Repeat, null,
                                modifier = Modifier.size(12.dp),
                                tint     = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
