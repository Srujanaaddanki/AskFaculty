package com.example.askfaculty.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.domain.model.Notification
import com.example.askfaculty.domain.repository.AuthRepository
import com.example.askfaculty.domain.repository.NotificationRepository
import com.example.askfaculty.core.util.isToday
import com.example.askfaculty.core.util.isYesterday
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val todayNotifs: List<Notification> = emptyList(),
    val yesterdayNotifs: List<Notification> = emptyList(),
    val olderNotifs: List<Notification> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.filterNotNull().collect { user ->
                notificationRepository.getNotifications(user.uid).collect { notifs ->
                    val today = notifs.filter { it.createdAt?.isToday() == true }
                    val yesterday = notifs.filter { it.createdAt?.isYesterday() == true }
                    val older = notifs.filter {
                        it.createdAt?.isToday() == false && it.createdAt?.isYesterday() == false
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            todayNotifs = today,
                            yesterdayNotifs = yesterday,
                            olderNotifs = older,
                        )
                    }
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch { notificationRepository.markAsRead(notificationId) }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            authRepository.currentUser.firstOrNull()?.let { user ->
                notificationRepository.markAllAsRead(user.uid)
            }
        }
    }
}
