package com.lumio.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lumio.app.data.preferences.AppPreferences
import com.lumio.app.presentation.screens.add.AddReminderScreen
import com.lumio.app.presentation.screens.ai.AiChatScreen
import com.lumio.app.presentation.screens.calendar.CalendarScreen
import com.lumio.app.presentation.screens.categories.CategoriesScreen
import com.lumio.app.presentation.screens.detail.ReminderDetailScreen
import com.lumio.app.presentation.screens.health.HealthScreen
import com.lumio.app.presentation.screens.home.HomeScreen
import com.lumio.app.presentation.screens.location.LocationPickerScreen
import com.lumio.app.presentation.screens.onboarding.OnboardingScreen
import com.lumio.app.presentation.screens.search.SearchScreen
import com.lumio.app.presentation.screens.settings.SettingsScreen
import com.lumio.app.presentation.screens.stats.StatsScreen
import com.lumio.app.presentation.screens.voice.VoiceScreen
import com.lumio.app.presentation.screens.weather.WeatherScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    // Every route defined in Screen.kt MUST have a matching composable()
    // entry below. A route that compiles fine (Screen.kt is just objects
    // holding strings) but is never registered here will not be caught by
    // the Kotlin compiler at all -- it only fails at runtime, the moment
    // navController.navigate(...) is actually called for that route. That
    // silent gap is exactly what caused the AI crash: "ai_chat" compiled
    // fine everywhere it was referenced, but had no registration here.

    NavHost(navController = navController, startDestination = start) {

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
            SearchScreen(navController = navController)
        }

        composable(Screen.Voice.route) {
            VoiceScreen(navController = navController)
        }

        composable(Screen.Health.route) {
            HealthScreen(navController = navController)
        }

        composable(Screen.Stats.route) {
            StatsScreen(navController = navController)
        }

        composable(Screen.Location.route) {
            LocationPickerScreen(navController = navController)
        }

        composable(Screen.Weather.route) {
            WeatherScreen(navController = navController)
        }

        composable(Screen.AiChat.route) {
            AiChatScreen(navController = navController)
        }

        composable(
            route     = Screen.ReminderDetail.route,
            arguments = listOf(
                navArgument("reminderId") {
                    type         = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            ReminderDetailScreen(navController = navController)
        }

        composable(
            route     = Screen.EditReminder.route,
            arguments = listOf(
                navArgument("reminderId") {
                    type         = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            AddReminderScreen(navController = navController)
        }

        composable(
            route     = Screen.CategoryDetail.route,
            arguments = listOf(
                navArgument("categoryId") {
                    type         = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            HomeScreen(navController = navController)
        }
    }
}
