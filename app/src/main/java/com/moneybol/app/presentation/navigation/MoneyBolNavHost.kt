package com.moneybol.app.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.moneybol.app.presentation.history.HistoryScreen
import com.moneybol.app.presentation.home.HomeScreen
import com.moneybol.app.presentation.onboarding.OnboardingScreen
import com.moneybol.app.presentation.settings.SettingsScreen

private const val TRANSITION_DURATION_MS = 250

/**
 * MoneyBol navigation host with smooth transitions.
 */
@Composable
fun MoneyBolNavHost(
    navController: NavHostController,
    startDestination: String,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) +
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(TRANSITION_DURATION_MS)
                )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(TRANSITION_DURATION_MS)) +
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(TRANSITION_DURATION_MS)
                )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) +
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(TRANSITION_DURATION_MS)
                )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(TRANSITION_DURATION_MS)) +
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(TRANSITION_DURATION_MS)
                )
        }
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
