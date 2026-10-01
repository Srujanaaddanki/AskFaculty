package com.example.askfaculty.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.domain.model.DashboardStats
import com.example.askfaculty.domain.model.Doubt
import com.example.askfaculty.domain.model.FrequentTopic
import com.example.askfaculty.domain.model.User
import com.example.askfaculty.domain.repository.AuthRepository
import com.example.askfaculty.domain.repository.CommunityRepository
import com.example.askfaculty.domain.repository.DoubtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val stats: DashboardStats = DashboardStats(),
    val priorityDoubts: List<Doubt> = emptyList(),
    val frequentTopics: List<FrequentTopic> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val doubtRepository: DoubtRepository,
    private val communityRepository: CommunityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.filterNotNull().collect { user ->
                _uiState.update { it.copy(user = user, isLoading = true) }
                loadDashboard(user.uid)
            }
        }
    }

    private fun loadDashboard(facultyId: String) {
        // Stats
        viewModelScope.launch {
            doubtRepository.getDashboardStats(facultyId).fold(
                onSuccess = { baseStats ->
                    // Get community count
                    communityRepository.getCommunities(facultyId)
                        .take(1)
                        .collect { communities ->
                            _uiState.update {
                                it.copy(
                                    stats = baseStats.copy(activeCommunities = communities.size),
                                    isLoading = false,
                                )
                            }
                        }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Couldn't load dashboard. Please try again.",
                        )
                    }
                }
            )
        }

        // Priority doubts
        viewModelScope.launch {
            doubtRepository.getPriorityDoubts(facultyId).collect { doubts ->
                _uiState.update { it.copy(priorityDoubts = doubts) }
            }
        }

        // Frequent topics
        viewModelScope.launch {
            doubtRepository.getFrequentTopics(facultyId).collect { topics ->
                _uiState.update { it.copy(frequentTopics = topics) }
            }
        }
    }

    fun refresh() {
        val facultyId = _uiState.value.user?.uid ?: return
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadDashboard(facultyId)
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
