package com.example.askfaculty.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.domain.model.User
import com.example.askfaculty.domain.repository.AuthRepository
import com.example.askfaculty.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: String? = null,
    val loggedOut: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user == null) {
                    _uiState.update { it.copy(isLoading = false, user = null) }
                } else {
                    profileRepository.getUserProfile(user.uid).fold(
                        onSuccess = { profile -> _uiState.update { it.copy(isLoading = false, user = profile) } },
                        onFailure = { _uiState.update { it.copy(isLoading = false, user = user) } }
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(loggedOut = true) }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
