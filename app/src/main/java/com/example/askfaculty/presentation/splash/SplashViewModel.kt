package com.example.askfaculty.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.data.repository.UserPreferencesRepository
import com.example.askfaculty.domain.repository.AuthRepository
import com.example.askfaculty.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    object Onboarding : SplashDestination()
    object Login : SplashDestination()
    object FacultyHome : SplashDestination()
    object StudentHome : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination.asStateFlow()

    init {
        resolveStartupDestination()
    }

    private fun resolveStartupDestination() {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser.first()
            if (currentUser != null) {
                val profileResult = profileRepository.getUserProfile(currentUser.uid)
                val role = profileResult.getOrNull()?.role ?: currentUser.role
                when (role) {
                    Constants.ROLE_FACULTY -> _destination.value = SplashDestination.FacultyHome
                    Constants.ROLE_STUDENT -> _destination.value = SplashDestination.StudentHome
                    else -> _destination.value = SplashDestination.Login
                }
            } else {
                val onboardingDone = userPreferencesRepository.isOnboardingCompleted.first()
                if (!onboardingDone) {
                    _destination.value = SplashDestination.Onboarding
                } else {
                    _destination.value = SplashDestination.Login
                }
            }
        }
    }
}
