package com.lumio.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumio.app.domain.model.Priority

@Composable
fun PriorityBadge(
    priority: Priority,
    showLabel: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (priority == Priority.NONE) return
    val color = Color(android.graphics.Color.parseColor(priority.colorHex))
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.13f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(text = priority.emoji, fontSize = 10.sp)
        if (showLabel) {
            Text(
                text = priority.label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}
