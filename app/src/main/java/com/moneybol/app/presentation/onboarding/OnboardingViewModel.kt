package com.moneybol.app.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneybol.app.audio.AnnouncementManager
import com.moneybol.app.settings.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val announcementManager: AnnouncementManager,
) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted(true)
        }
    }

    fun playTestAnnouncement() {
        announcementManager.playTestAnnouncement()
    }
}
