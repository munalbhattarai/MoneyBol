package com.moneybol.app.presentation.navigation

/**
 * Navigation routes for MoneyBol.
 */
sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object History : Screen("history")
    data object Settings : Screen("settings")
}
