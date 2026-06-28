package com.lumio.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lumio.app.presentation.screens.add.AddReminderScreen
import com.lumio.app.presentation.screens.calendar.CalendarScreen
import com.lumio.app.presentation.screens.categories.CategoriesScreen
import com.lumio.app.presentation.screens.home.HomeScreen
import com.lumio.app.presentation.screens.settings.SettingsScreen

@Composable
fun LumioNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.AddReminder.route) {
            AddReminderScreen(navController = navController)
        }
        composable(Screen.Calendar.route) {
            CalendarScreen(navController = navController)
        }
        composable(Screen.Categories.route) {
            CategoriesScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.Search.route) {
            HomeScreen(navController = navController)
        }
        composable(
            route = Screen.EditReminder.route,
            arguments = listOf(navArgument("reminderId") {
                type = NavType.LongType; defaultValue = -1L
            })
        ) { HomeScreen(navController = navController) }

        composable(
            route = Screen.ReminderDetail.route,
            arguments = listOf(navArgument("reminderId") {
                type = NavType.LongType; defaultValue = -1L
            })
        ) { HomeScreen(navController = navController) }

        composable(
            route = Screen.CategoryDetail.route,
            arguments = listOf(navArgument("categoryId") {
                type = NavType.LongType; defaultValue = -1L
            })
        ) { HomeScreen(navController = navController) }
    }
}
