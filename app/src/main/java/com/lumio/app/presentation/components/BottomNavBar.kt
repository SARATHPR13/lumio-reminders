package com.lumio.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lumio.app.presentation.navigation.Screen

private data class NavItem(val screen: Screen, val label: String, val icon: ImageVector)

@Composable
fun LumioBottomNavBar(navController: NavController) {
    val items = listOf(
        NavItem(Screen.Home,       "Home",       Icons.Rounded.Home),
        NavItem(Screen.Calendar,   "Calendar",   Icons.Rounded.CalendarMonth),
        NavItem(Screen.Categories, "Categories", Icons.Rounded.Category),
        NavItem(Screen.Settings,   "Settings",   Icons.Rounded.Settings),
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.screen.route
            val scale by animateFloatAsState(
                targetValue = if (selected) 1.1f else 1f,
                animationSpec = tween(200), label = "scale"
            )
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.screen.route) {
                        navController.navigate(item.screen.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.scale(scale)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = MaterialTheme.colorScheme.primary,
                    selectedTextColor   = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor      = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
