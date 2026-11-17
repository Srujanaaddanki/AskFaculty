package com.example.askfaculty.presentation.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.domain.model.Community
import com.example.askfaculty.domain.repository.AuthRepository
import com.example.askfaculty.domain.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentCommunitiesUiState(
    val isLoading: Boolean = true,
    val communities: List<Community> = emptyList(),
    val joinedCommunityIds: Set<String> = emptySet(),
    val selectedTab: Int = 0, // 0 = Explore All, 1 = Joined
    val selectedSubject: String = "All",
    val searchQuery: String = "",
    val error: String? = null,
)

@HiltViewModel
class StudentCommunitiesViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val communityRepository: CommunityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentCommunitiesUiState())
    val uiState: StateFlow<StudentCommunitiesUiState> = _uiState.asStateFlow()

    private var currentUserId: String = ""

    init {
        observeCurrentUser()
        observeCommunities()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    currentUserId = user.uid
                    _uiState.update {
                        it.copy(joinedCommunityIds = user.joinedCommunities.toSet())
                    }
                }
            }
        }
    }

    private fun observeCommunities() {
        viewModelScope.launch {
            communityRepository.getAllCommunities().collect { list ->
                _uiState.update {
                    it.copy(
                        communities = list,
                        isLoading = false,
                        error = null,
                    )
                }
            }
        }
    }

    fun selectTab(tab: Int) = _uiState.update { it.copy(selectedTab = tab) }

    fun selectSubject(subject: String) = _uiState.update { it.copy(selectedSubject = subject) }

    fun setSearchQuery(query: String) = _uiState.update { it.copy(searchQuery = query) }

    fun toggleJoinCommunity(community: Community) {
        val isJoined = _uiState.value.joinedCommunityIds.contains(community.id)
        viewModelScope.launch {
            if (isJoined) {
                communityRepository.leaveCommunity(community.id, currentUserId)
                _uiState.update {
                    it.copy(joinedCommunityIds = it.joinedCommunityIds - community.id)
                }
            } else {
                communityRepository.joinCommunity(community.id, currentUserId)
                _uiState.update {
                    it.copy(joinedCommunityIds = it.joinedCommunityIds + community.id)
                }
            }
        }
    }
}
