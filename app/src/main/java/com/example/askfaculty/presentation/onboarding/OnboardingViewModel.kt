package com.example.askfaculty.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    fun completeOnboarding(onFinished: () -> Unit) {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted(true)
            onFinished()
        }
    }
}
