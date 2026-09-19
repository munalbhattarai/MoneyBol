package com.moneybol.app.presentation.settings

import android.app.Application
import android.content.ComponentName
import android.provider.Settings
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moneybol.app.core.model.AnnouncementFormat
import com.moneybol.app.settings.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val announcementFormat: AnnouncementFormat = AnnouncementFormat.PAYMENT_RECEIVED_AMOUNT,
    val speechRate: Float = 1.0f,
    val listeningEnabled: Boolean = true,
    val bluetoothEnabled: Boolean = false,
    val hasNotificationAccess: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val userPreferencesRepository: UserPreferencesRepository,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.announcementFormat,
                userPreferencesRepository.speechRate,
                userPreferencesRepository.listeningEnabled,
                userPreferencesRepository.bluetoothEnabled,
            ) { format, rate, listening, bluetooth ->
                SettingsUiState(
                    announcementFormat = format,
                    speechRate = rate,
                    listeningEnabled = listening,
                    bluetoothEnabled = bluetooth,
                    hasNotificationAccess = isNotificationAccessEnabled(),
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setAnnouncementFormat(format: AnnouncementFormat) {
        viewModelScope.launch {
            userPreferencesRepository.setAnnouncementFormat(format)
        }
    }

    fun setSpeechRate(rate: Float) {
        viewModelScope.launch {
            userPreferencesRepository.setSpeechRate(rate)
        }
    }

    fun setListeningEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setListeningEnabled(enabled)
        }
    }

    fun setBluetoothEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setBluetoothEnabled(enabled)
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
