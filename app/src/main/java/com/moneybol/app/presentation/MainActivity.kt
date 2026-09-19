package com.moneybol.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.moneybol.app.presentation.navigation.MoneyBolNavHost
import com.moneybol.app.presentation.navigation.Screen
import com.moneybol.app.presentation.theme.MoneyBolTheme
import com.moneybol.app.settings.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val onboardingCompleted by userPreferencesRepository.onboardingCompleted
                .collectAsState(initial = false)

            MoneyBolTheme {
                val navController = rememberNavController()
                val startDestination = if (onboardingCompleted) {
                    Screen.Home.route
                } else {
                    Screen.Onboarding.route
                }

                MoneyBolNavHost(
                    navController = navController,
                    startDestination = startDestination,
                )
            }
        }
    }
}
