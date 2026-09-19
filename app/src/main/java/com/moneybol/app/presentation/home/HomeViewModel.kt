package com.moneybol.app.presentation.home

import android.app.Application
import android.content.ComponentName
import android.provider.Settings
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moneybol.app.core.model.ListeningState
import com.moneybol.app.core.util.AmountParser
import com.moneybol.app.data.PaymentRepository
import com.moneybol.app.notification.MoneyBolNotificationService
import com.moneybol.app.presentation.home.model.HomeUiState
import com.moneybol.app.settings.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val paymentRepository: PaymentRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    init {
        observePayments()
        observeListeningState()
    }

    private fun observePayments() {
        viewModelScope.launch {
            combine(
                paymentRepository.getLastPayment(),
                paymentRepository.getTodayTotal(),
                paymentRepository.getTodayCount()
            ) { lastPayment, todayTotal, todayCount ->
                Triple(lastPayment, todayTotal, todayCount)
            }.collect { (lastPayment, todayTotal, todayCount) ->
                _uiState.update { state ->
                    state.copy(
                        lastPaymentProvider = lastPayment?.provider,
                        lastPaymentAmount = lastPayment?.let {
                            "Rs. ${AmountParser.formatForDisplay(it.amount)}"
                        },
                        lastPaymentTime = lastPayment?.let {
                            timeFormat.format(it.timestamp)
                        },
                        todayTotal = "Rs. ${AmountParser.formatForDisplay(todayTotal)}",
                        todayCount = todayCount
                    )
                }
            }
        }
    }

    private fun observeListeningState() {
        viewModelScope.launch {
            userPreferencesRepository.listeningEnabled.collect { enabled ->
                val hasPermission = isNotificationAccessEnabled()
                val state = when {
                    !hasPermission -> ListeningState.PERMISSION_REQUIRED
                    !enabled -> ListeningState.PAUSED
                    else -> ListeningState.LISTENING
                }
                _uiState.update { it.copy(listeningState = state) }
            }
        }
    }

    fun refreshListeningState() {
        val hasPermission = isNotificationAccessEnabled()
        viewModelScope.launch {
            userPreferencesRepository.listeningEnabled.collect { enabled ->
                val state = when {
                    !hasPermission -> ListeningState.PERMISSION_REQUIRED
                    !enabled -> ListeningState.PAUSED
                    else -> ListeningState.LISTENING
                }
                _uiState.update { it.copy(listeningState = state) }
            }
        }
    }

    fun onTestAnnouncement() {
        _uiState.update { it.copy(isTestPlaying = true) }
        viewModelScope.launch {
            // Will be connected to AnnouncementManager in Milestone 5
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(isTestPlaying = false) }
        }
    }

    fun onRepeatLastPayment() {
        _uiState.update { it.copy(isRepeatPlaying = true) }
        viewModelScope.launch {
            // Will be connected to AnnouncementManager in Milestone 5
            kotlinx.coroutines.delay(2000)
            _uiState.update { it.copy(isRepeatPlaying = false) }
        }
    }

    private fun isNotificationAccessEnabled(): Boolean {
        val pkgName = application.packageName
        val flat = Settings.Secure.getString(
            application.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":").toTypedArray()
            for (name in names) {
                val cn = ComponentName.unflattenFromString(name)
                if (cn != null && cn.packageName == pkgName) {
                    return true
                }
            }
        }
        return false
    }
}
