package com.lumio.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lumio.app.presentation.screens.home.HomeScreen

@Composable
fun LumioNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.AddReminder.route) { HomeScreen(navController) }
        composable(Screen.Calendar.route) { HomeScreen(navController) }
        composable(Screen.Categories.route) { HomeScreen(navController) }
        composable(Screen.Settings.route) { HomeScreen(navController) }
        composable(Screen.Search.route) { HomeScreen(navController) }
        composable(
            route = Screen.EditReminder.route,
            arguments = listOf(navArgument("reminderId") {
                type = NavType.LongType; defaultValue = -1L
            })
        ) { HomeScreen(navController) }
        composable(
            route = Screen.ReminderDetail.route,
            arguments = listOf(navArgument("reminderId") {
                type = NavType.LongType; defaultValue = -1L
            })
        ) { HomeScreen(navController) }
        composable(
            route = Screen.CategoryDetail.route,
            arguments = listOf(navArgument("categoryId") {
                type = NavType.LongType; defaultValue = -1L
            })
        ) { HomeScreen(navController) }
    }
}
