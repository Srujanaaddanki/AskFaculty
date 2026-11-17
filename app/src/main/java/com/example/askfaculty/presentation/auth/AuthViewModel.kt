package com.example.askfaculty.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.core.util.Constants
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
    val isRegistrationSuccess: Boolean = false,
    val passwordResetSent: Boolean = false,
    val selectedRoleTab: String = Constants.ROLE_STUDENT, // STUDENT or FACULTY
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

    fun setRoleTab(role: String) {
        _uiState.update { it.copy(selectedRoleTab = role, error = null) }
    }

    fun loginWithStudentId(studentId: String, password: String) {
        val trimmedId = studentId.trim()
        if (trimmedId.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your Student ID or Roll Number") }
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
            authRepository.loginWithStudentId(trimmedId, password).fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, user = user) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = friendlyError(e.message)) }
                }
            )
        }
    }

    fun loginWithFacultyId(facultyId: String, password: String) {
        val trimmedId = facultyId.trim()
        if (trimmedId.length != 5 || !trimmedId.all { it.isDigit() }) {
            _uiState.update { it.copy(error = "Faculty ID must be exactly 5 numeric digits (e.g. 10245 or 30456)") }
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
                    _uiState.update { it.copy(isLoading = false, error = friendlyError(e.message)) }
                }
            )
        }
    }

    fun registerStudent(
        name: String,
        studentId: String,
        email: String,
        department: String,
        academicYear: String,
        password: String,
        confirmPassword: String,
    ) {
        val trimmedName = name.trim()
        val trimmedId = studentId.trim()
        val trimmedEmail = email.trim()

        when {
            trimmedName.isBlank() -> { _uiState.update { it.copy(error = "Full Name is required") }; return }
            trimmedId.isBlank() -> { _uiState.update { it.copy(error = "Student ID / College Roll No is required") }; return }
            trimmedEmail.isBlank() || !trimmedEmail.contains("@") -> { _uiState.update { it.copy(error = "Valid college email is required") }; return }
            department.isBlank() -> { _uiState.update { it.copy(error = "Please select or enter your department") }; return }
            password.length < 6 -> { _uiState.update { it.copy(error = "Password must be at least 6 characters") }; return }
            password != confirmPassword -> { _uiState.update { it.copy(error = "Passwords do not match") }; return }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.registerStudent(
                name = trimmedName,
                studentId = trimmedId,
                email = trimmedEmail,
                department = department,
                academicYear = academicYear,
                password = password,
            ).fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            isRegistrationSuccess = true,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = friendlyError(e.message)) }
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
                    _uiState.update { it.copy(isLoading = false, error = friendlyError(e.message)) }
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

    fun sendPasswordResetForStudentId(studentId: String) {
        val trimmed = studentId.trim()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your Student ID") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.sendPasswordResetForStudentId(trimmed).fold(
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

    fun clearRegistrationSuccess() = _uiState.update { it.copy(isRegistrationSuccess = false) }

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
            "Incorrect credentials. Please verify your ID/email and password."
        raw.contains("email-already-in-use") ->
            "This email is already registered. Please sign in or use forgot password."
        raw.contains("network") || raw.contains("NETWORK") ->
            "Unable to connect. Please check your internet connection."
        raw.contains("disabled") -> "Your account has been deactivated. Please contact academic IT."
        raw.contains("exclusively", ignoreCase = true) || raw.contains("faculty", ignoreCase = true) || raw.contains("restricted", ignoreCase = true) -> raw
        raw.contains("too-many-requests") -> "Too many failed attempts. Please wait a few minutes before trying again."
        else -> raw
    }
}
