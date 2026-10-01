package com.example.askfaculty.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.domain.model.Community
import com.example.askfaculty.domain.model.Doubt
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
            _query.debounce(400)
                .filter { it.length >= 2 }
                .flatMapLatest { query ->
                    _uiState.update { it.copy(isSearching = true) }
                    combine(
                        doubtRepository.getDoubts(facultyId = user.uid, searchQuery = query),
                        communityRepository.getCommunities(user.uid),
                    ) { doubts, communities ->
                        val filteredCommunities = communities.filter {
                            it.name.contains(query, ignoreCase = true) ||
                            it.subject.contains(query, ignoreCase = true) ||
                            it.description.contains(query, ignoreCase = true)
                        }
                        Pair(doubts, filteredCommunities)
                    }
                }
                .collect { (doubts, communities) ->
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            doubts = doubts,
                            communities = communities,
                            hasSearched = true,
                        )
                    }
                }
        }
    }

    fun setQuery(q: String) {
        _query.value = q
        _uiState.update { it.copy(query = q, hasSearched = q.length < 2) }
    }
}
