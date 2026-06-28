package com.lumio.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lumio.app.data.preferences.AppPreferences
import com.lumio.app.presentation.screens.add.AddReminderScreen
import com.lumio.app.presentation.screens.calendar.CalendarScreen
import com.lumio.app.presentation.screens.categories.CategoriesScreen
import com.lumio.app.presentation.screens.detail.ReminderDetailScreen
import com.lumio.app.presentation.screens.home.HomeScreen
import com.lumio.app.presentation.screens.onboarding.OnboardingScreen
import com.lumio.app.presentation.screens.search.SearchScreen
import com.lumio.app.presentation.screens.settings.SettingsScreen
import com.lumio.app.presentation.screens.voice.VoiceScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@HiltViewModel
class NavViewModel @Inject constructor(
    private val prefs: AppPreferences
) : ViewModel() {
    private val _firstLaunch = MutableStateFlow(true)
    val firstLaunch: StateFlow<Boolean> = _firstLaunch.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.firstLaunch.collect { _firstLaunch.value = it }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch { prefs.setFirstLaunch(false) }
    }
}

@Composable
fun LumioNavGraph(navController: NavHostController = rememberNavController()) {
    val navViewModel: NavViewModel = hiltViewModel()
    val isFirstLaunch by navViewModel.firstLaunch.collectAsState()

    val start = if (isFirstLaunch) Screen.Onboarding.route else Screen.Home.route

    NavHost(navController = navController, startDestination = start) {

        // Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navViewModel.completeOnboarding()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Home
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        // Add Reminder
        composable(Screen.AddReminder.route) {
            AddReminderScreen(navController = navController)
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
            SearchScreen(navController = navController)
        }

        // Voice
        composable(Screen.Voice.route) {
            VoiceScreen(navController = navController)
        }

        // Reminder Detail
        composable(
            route     = Screen.ReminderDetail.route,
            arguments = listOf(
                navArgument("reminderId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { ReminderDetailScreen(navController = navController) }

        // Edit Reminder
        composable(
            route     = Screen.EditReminder.route,
            arguments = listOf(
                navArgument("reminderId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { AddReminderScreen(navController = navController) }

        // Category Detail
        composable(
            route     = Screen.CategoryDetail.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { HomeScreen(navController = navController) }
    }
}
