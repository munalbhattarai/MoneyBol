package com.moneybol.app.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.moneybol.app.core.model.AnnouncementFormat
import com.moneybol.app.presentation.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "moneybol_preferences")

/**
 * Repository for user preferences backed by DataStore.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // ── Keys ──
    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LISTENING_ENABLED = booleanPreferencesKey("listening_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ANNOUNCEMENT_FORMAT = stringPreferencesKey("announcement_format")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val TTS_LANGUAGE = stringPreferencesKey("tts_language")
        val BLUETOOTH_ENABLED = booleanPreferencesKey("bluetooth_enabled")
        val REPEAT_ENABLED = booleanPreferencesKey("repeat_enabled")
    }

    // ── Onboarding ──
    val onboardingCompleted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    // ── Listening ──
    val listeningEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.LISTENING_ENABLED] ?: true
    }

    suspend fun setListeningEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.LISTENING_ENABLED] = enabled
        }
    }

    // ── Theme ──
    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        val modeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        try { ThemeMode.valueOf(modeStr) } catch (_: Exception) { ThemeMode.SYSTEM }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    // ── Announcement Format ──
    val announcementFormat: Flow<AnnouncementFormat> = dataStore.data.map { prefs ->
        val formatStr = prefs[Keys.ANNOUNCEMENT_FORMAT]
            ?: AnnouncementFormat.PAYMENT_RECEIVED_AMOUNT.name
        try {
            AnnouncementFormat.valueOf(formatStr)
        } catch (_: Exception) {
            AnnouncementFormat.PAYMENT_RECEIVED_AMOUNT
        }
    }

    suspend fun setAnnouncementFormat(format: AnnouncementFormat) {
        dataStore.edit { prefs ->
            prefs[Keys.ANNOUNCEMENT_FORMAT] = format.name
        }
    }

    // ── Speech Rate ──
    val speechRate: Flow<Float> = dataStore.data.map { prefs ->
        prefs[Keys.SPEECH_RATE] ?: 1.0f
    }

    suspend fun setSpeechRate(rate: Float) {
        dataStore.edit { prefs ->
            prefs[Keys.SPEECH_RATE] = rate.coerceIn(0.5f, 2.0f)
        }
    }

    // ── TTS Language ──
    val ttsLanguage: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.TTS_LANGUAGE] ?: "en"
    }

    suspend fun setTtsLanguage(language: String) {
        dataStore.edit { prefs ->
            prefs[Keys.TTS_LANGUAGE] = language
        }
    }

    // ── Bluetooth ──
    val bluetoothEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.BLUETOOTH_ENABLED] ?: false
    }

    suspend fun setBluetoothEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.BLUETOOTH_ENABLED] = enabled
        }
    }

    // ── Repeat ──
    val repeatEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.REPEAT_ENABLED] ?: true
    }

    suspend fun setRepeatEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.REPEAT_ENABLED] = enabled
        }
    }
}
