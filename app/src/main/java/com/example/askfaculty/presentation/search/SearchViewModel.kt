package com.example.askfaculty.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.domain.model.Community
import com.example.askfaculty.domain.model.Doubt
import com.example.askfaculty.domain.model.Resource
import com.example.askfaculty.domain.repository.AuthRepository
import com.example.askfaculty.domain.repository.CommunityRepository
import com.example.askfaculty.domain.repository.DoubtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val doubts: List<Doubt> = emptyList(),
    val communities: List<Community> = emptyList(),
    val resources: List<Resource> = emptyList(),
    val selectedFilter: String = "All",
    val hasSearched: Boolean = false,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val doubtRepository: DoubtRepository,
    private val communityRepository: CommunityRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = authRepository.currentUser.filterNotNull().first()
            _query.debounce(350)
                .filter { it.trim().length >= 2 }
                .flatMapLatest { query ->
                    _uiState.update { it.copy(isSearching = true) }
                    val q = query.trim()
                    val isStudent = user.role == Constants.ROLE_STUDENT

                    val doubtsFlow = if (isStudent) {
                        doubtRepository.searchPublicKnowledgeBase(q)
                    } else {
                        doubtRepository.getDoubts(facultyId = user.uid, searchQuery = q)
                    }

                    val communitiesFlow = if (isStudent) {
                        communityRepository.getAllCommunities()
                    } else {
                        communityRepository.getCommunities(user.uid)
                    }

                    val resourcesFlow = communityRepository.getAllResources()

                    combine(doubtsFlow, communitiesFlow, resourcesFlow) { doubts, communities, resources ->
                        val filteredCommunities = communities.filter {
                            it.name.contains(q, ignoreCase = true) ||
                            it.subject.contains(q, ignoreCase = true) ||
                            it.description.contains(q, ignoreCase = true)
                        }
                        val filteredResources = resources.filter {
                            it.title.contains(q, ignoreCase = true) ||
                            it.subject.contains(q, ignoreCase = true) ||
                            it.fileName.contains(q, ignoreCase = true) ||
                            it.description.contains(q, ignoreCase = true)
                        }
                        Triple(doubts, filteredCommunities, filteredResources)
                    }
                }
                .collect { (doubts, communities, resources) ->
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            doubts = doubts,
                            communities = communities,
                            resources = resources,
                            hasSearched = true,
                        )
                    }
                }
        }
    }

    fun setQuery(q: String) {
        _query.value = q
        _uiState.update { it.copy(query = q, hasSearched = q.trim().length >= 2) }
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }
}
