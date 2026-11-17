package com.example.askfaculty.presentation.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.domain.model.NotificationPreferences
import com.example.askfaculty.domain.model.StudentDashboardStats
import com.example.askfaculty.domain.model.User
import com.example.askfaculty.domain.repository.AuthRepository
import com.example.askfaculty.domain.repository.DoubtRepository
import com.example.askfaculty.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val stats: StudentDashboardStats = StudentDashboardStats(),
    val notificationPrefs: NotificationPreferences = NotificationPreferences(),
    val isLoggingOut: Boolean = false,
    val error: String? = null,
    val message: String? = null,
)

@HiltViewModel
class StudentProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val doubtRepository: DoubtRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentProfileUiState())
    val uiState: StateFlow<StudentProfileUiState> = _uiState.asStateFlow()

    init {
        loadStudentData()
    }

    private fun loadStudentData() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    _uiState.update { it.copy(user = user, isLoading = false) }
                    loadStats(user.uid)
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private fun loadStats(studentId: String) {
        viewModelScope.launch {
            doubtRepository.getStudentStats(studentId).fold(
                onSuccess = { stats ->
                    _uiState.update { it.copy(stats = stats) }
                },
                onFailure = { /* keep default stats */ }
            )
        }
    }

    fun toggleNotificationPref(key: String, enabled: Boolean) {
        val current = _uiState.value.notificationPrefs
        val updated = when (key) {
            "doubtReplies" -> current.copy(doubtReplies = enabled)
            "communityAnnouncements" -> current.copy(communityAnnouncements = enabled)
            "newResources" -> current.copy(newResources = enabled)
            "emailDigest" -> current.copy(emailDigest = enabled)
            else -> current
        }
        _uiState.update { it.copy(notificationPrefs = updated) }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            authRepository.logout()
            _uiState.update { it.copy(isLoggingOut = false) }
            onLoggedOut()
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null, error = null) }
}
