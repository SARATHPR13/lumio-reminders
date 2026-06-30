package com.lumio.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lumio.app.R
import com.lumio.app.presentation.navigation.Screen

private data class NavItem(
    val screen   : Screen,
    val labelRes : Int,
    val icon     : ImageVector
)

@Composable
fun LumioBottomNavBar(navController: NavController) {
    val items = listOf(
        NavItem(Screen.Home,     R.string.nav_home,     Icons.Rounded.Home),
        NavItem(Screen.Calendar, R.string.nav_calendar,  Icons.Rounded.DateRange),
        NavItem(Screen.Health,   R.string.nav_health,    Icons.Rounded.Favorite),
        NavItem(Screen.AiChat,   R.string.nav_ai,        Icons.Rounded.AutoAwesome),
        NavItem(Screen.Settings, R.string.nav_settings,  Icons.Rounded.Settings)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute      = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier       = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.screen.route
            val scale by animateFloatAsState(
                targetValue   = if (selected) 1.15f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label         = "nav_scale"
            )
            val label = stringResource(item.labelRes)

            NavigationBarItem(
                selected = selected,
                onClick  = {
                    if (currentRoute != item.screen.route) {
                        navController.navigate(item.screen.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                icon  = {
                    Icon(
                        imageVector        = item.icon,
                        contentDescription = label,
                        modifier           = Modifier.scale(scale).size(22.dp)
                    )
                },
                label = {
                    Text(
                        text       = label,
                        fontSize   = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
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
