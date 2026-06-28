package com.lumio.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.lumio.app.presentation.screens.home.HomeViewModel
import com.lumio.app.presentation.screens.settings.SettingsScreen

@Composable
fun LumioNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Home.route
    ) {
        // Home
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        // Add Reminder — shares HomeViewModel from Home back stack entry
        composable(Screen.AddReminder.route) { backStackEntry ->
            val homeEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val homeViewModel: HomeViewModel = hiltViewModel(homeEntry)
            AddReminderScreen(
                navController = navController,
                homeViewModel = homeViewModel
            )
        }

        // Calendar
        composable(Screen.Calendar.route) {
            CalendarScreen(navController = navController)
        }

        // Categories
        composable(Screen.Categories.route) {
            CategoriesScreen(navController = navController)
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        // Search
        composable(Screen.Search.route) {
            HomeScreen(navController = navController)
        }

        // Edit Reminder
        composable(
            route = Screen.EditReminder.route,
            arguments = listOf(navArgument("reminderId") { type = NavType.LongType; defaultValue = -1L })
        ) { HomeScreen(navController = navController) }

        // Reminder Detail
        composable(
            route = Screen.ReminderDetail.route,
            arguments = listOf(navArgument("reminderId") { type = NavType.LongType; defaultValue = -1L })
        ) { HomeScreen(navController = navController) }

        // Category Detail
        composable(
            route = Screen.CategoryDetail.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType; defaultValue = -1L })
        ) { HomeScreen(navController = navController) }
    }
}
