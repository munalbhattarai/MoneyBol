package com.moneybol.app.presentation.home.model

import com.moneybol.app.core.model.ListeningState

/**
 * UI state for the Home (Dashboard) screen.
 */
data class HomeUiState(
    val listeningState: ListeningState = ListeningState.PERMISSION_REQUIRED,
    val lastPaymentProvider: String? = null,
    val lastPaymentAmount: String? = null,
    val lastPaymentTime: String? = null,
    val todayTotal: String = "Rs. 0",
    val todayCount: Int = 0,
    val isTestPlaying: Boolean = false,
    val isRepeatPlaying: Boolean = false,
)
