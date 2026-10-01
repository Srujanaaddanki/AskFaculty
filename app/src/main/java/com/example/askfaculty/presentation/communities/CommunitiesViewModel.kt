package com.example.askfaculty.presentation.communities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.domain.model.Community
import com.example.askfaculty.domain.repository.AuthRepository
import com.example.askfaculty.domain.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunitiesUiState(
    val isLoading: Boolean = true,
    val communities: List<Community> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class CommunitiesViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val communityRepository: CommunityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunitiesUiState())
    val uiState: StateFlow<CommunitiesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.filterNotNull().collect { user ->
                communityRepository.getCommunities(user.uid).collect { communities ->
                    _uiState.update {
                        it.copy(isLoading = false, communities = communities, error = null)
                    }
                }
            }
        }
    }
}
