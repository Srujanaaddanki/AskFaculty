package com.example.askfaculty.presentation.communities

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.domain.model.Community
import com.example.askfaculty.domain.model.Post
import com.example.askfaculty.domain.model.Resource
import com.example.askfaculty.domain.repository.AuthRepository
import com.example.askfaculty.domain.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityDetailUiState(
    val isLoading: Boolean = true,
    val community: Community? = null,
    val posts: List<Post> = emptyList(),
    val resources: List<Resource> = emptyList(),
    val selectedTab: Int = 0,
    val currentUserRole: String = Constants.ROLE_STUDENT,
    val currentUserId: String = "",
    val isMember: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class CommunityDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val communityRepository: CommunityRepository,
) : ViewModel() {

    private val communityId: String = checkNotNull(savedStateHandle["communityId"])
    private val _uiState = MutableStateFlow(CommunityDetailUiState())
    val uiState: StateFlow<CommunityDetailUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser()
        loadCommunity()
        observePosts()
        observeResources()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    val isMember = user.joinedCommunities.contains(communityId)
                    _uiState.update {
                        it.copy(
                            currentUserRole = user.role,
                            currentUserId = user.uid,
                            isMember = isMember,
                        )
                    }
                }
            }
        }
    }

    private fun loadCommunity() {
        viewModelScope.launch {
            communityRepository.getCommunity(communityId).fold(
                onSuccess = { community ->
                    _uiState.update { it.copy(community = community, isLoading = false) }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load community.") }
                }
            )
        }
    }

    private fun observePosts() {
        viewModelScope.launch {
            communityRepository.getPosts(communityId).collect { posts ->
                _uiState.update { it.copy(posts = posts) }
            }
        }
    }

    private fun observeResources() {
        viewModelScope.launch {
            communityRepository.getResources(communityId).collect { resources ->
                _uiState.update { it.copy(resources = resources) }
            }
        }
    }

    fun selectTab(index: Int) = _uiState.update { it.copy(selectedTab = index) }

    fun toggleJoinCommunity() {
        val currentUserId = _uiState.value.currentUserId
        val isMember = _uiState.value.isMember
        viewModelScope.launch {
            if (isMember) {
                communityRepository.leaveCommunity(communityId, currentUserId)
                _uiState.update { it.copy(isMember = false) }
            } else {
                communityRepository.joinCommunity(communityId, currentUserId)
                _uiState.update { it.copy(isMember = true) }
            }
            loadCommunity()
        }
    }

    fun toggleHelpful(postId: String) {
        viewModelScope.launch {
            communityRepository.toggleHelpful(communityId, postId, _uiState.value.currentUserId)
        }
    }

    fun pinPost(postId: String, isPinned: Boolean) {
        viewModelScope.launch {
            communityRepository.pinPost(communityId, postId, isPinned)
        }
    }

    fun markPostOfficial(postId: String, isOfficial: Boolean) {
        viewModelScope.launch {
            communityRepository.markPostOfficial(communityId, postId, isOfficial)
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            communityRepository.deletePost(communityId, postId)
        }
    }

    fun deleteResource(resourceId: String) {
        viewModelScope.launch {
            communityRepository.deleteResource(communityId, resourceId)
        }
    }
}

