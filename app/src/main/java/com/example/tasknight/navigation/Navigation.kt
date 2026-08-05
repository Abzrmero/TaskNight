package com.example.tasknight.navigation
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object Set : Screen("set")
    object Log : Screen("log")
    object History : Screen("history")
    object Settings : Screen("settings")
}
enum class AppScreen {
    SPLASH, ONBOARDING, AUTH, DASHBOARD, SETTINGS, HISTORY, SET, LOG
}