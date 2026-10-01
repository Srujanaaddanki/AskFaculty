package com.example.askfaculty.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.domain.model.User
import com.example.askfaculty.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val passwordResetSent: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
    }

    fun loginWithFacultyId(facultyId: String, password: String) {
        val trimmedId = facultyId.trim()
        if (trimmedId.length != 5 || !trimmedId.all { it.isDigit() }) {
            _uiState.update { it.copy(error = "Faculty ID must be exactly 5 numeric digits (e.g. 10245)") }
            return
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(error = "Password is required") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.loginWithFacultyId(trimmedId, password).fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, user = user) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = friendlyError(e.message))
                    }
                }
            )
        }
    }

    fun login(email: String, password: String) {
        if (!validate(email, password)) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.login(email.trim(), password).fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, user = user) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = friendlyError(e.message))
                    }
                }
            )
        }
    }

    fun sendPasswordResetForFacultyId(facultyId: String) {
        val trimmed = facultyId.trim()
        if (trimmed.length != 5 || !trimmed.all { it.isDigit() }) {
            _uiState.update { it.copy(error = "Please enter your 5-digit Faculty ID (e.g. 10245)") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.sendPasswordResetForFacultyId(trimmed).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, passwordResetSent = true) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = friendlyError(e.message)) }
                }
            )
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email address") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.sendPasswordReset(email.trim()).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, passwordResetSent = true) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = friendlyError(e.message)) }
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun clearPasswordResetFlag() = _uiState.update { it.copy(passwordResetSent = false) }

    private fun validate(email: String, password: String): Boolean {
        return when {
            email.isBlank() -> { _uiState.update { it.copy(error = "Email is required") }; false }
            password.isBlank() -> { _uiState.update { it.copy(error = "Password is required") }; false }
            password.length < 6 -> { _uiState.update { it.copy(error = "Password must be at least 6 characters") }; false }
            else -> true
        }
    }

    private fun friendlyError(raw: String?): String = when {
        raw == null -> "Unable to authenticate. Please check your credentials and try again."
        raw.contains("INVALID_LOGIN_CREDENTIALS") || raw.contains("invalid-credential") || raw.contains("user-not-found") || raw.contains("wrong-password") ->
            "Incorrect Faculty ID or password. Please verify your credentials."
        raw.contains("network") || raw.contains("NETWORK") ->
            "Unable to connect. Please check your internet connection."
        raw.contains("disabled") -> "Your faculty account has been deactivated. Please contact academic IT."
        raw.contains("exclusively", ignoreCase = true) || raw.contains("faculty", ignoreCase = true) -> raw
        raw.contains("too-many-requests") -> "Too many failed attempts. Please wait a few minutes before trying again."
        else -> "Authentication failed. Please verify your details."
    }
}
