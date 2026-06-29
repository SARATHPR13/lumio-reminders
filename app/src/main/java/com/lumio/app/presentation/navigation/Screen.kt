package com.lumio.app.presentation.navigation

sealed class Screen(val route: String) {
    object Onboarding      : Screen("onboarding")
    object Home            : Screen("home")
    object Calendar        : Screen("calendar")
    object Categories      : Screen("categories")
    object Settings        : Screen("settings")
    object AddReminder     : Screen("add_reminder")
    object Search          : Screen("search")
    object Voice           : Screen("voice")
    object Health          : Screen("health")
    object Stats           : Screen("stats")
    object Location        : Screen("location")

    object EditReminder : Screen("edit_reminder/{reminderId}") {
        fun createRoute(id: Long) = "edit_reminder/$id"
    }
    object ReminderDetail : Screen("reminder_detail/{reminderId}") {
        fun createRoute(id: Long) = "reminder_detail/$id"
    }
    object CategoryDetail : Screen("category_detail/{categoryId}") {
        fun createRoute(id: Long) = "category_detail/$id"
    }
}
