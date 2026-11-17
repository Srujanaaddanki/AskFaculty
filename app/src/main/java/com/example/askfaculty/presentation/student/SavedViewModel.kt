package com.example.askfaculty.presentation.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.domain.model.Doubt
import com.example.askfaculty.domain.repository.AuthRepository
import com.example.askfaculty.domain.repository.DoubtRepository
import com.example.askfaculty.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedUiState(
    val isLoading: Boolean = true,
    val bookmarkedDoubts: List<Doubt> = emptyList(),
    val selectedFilter: String = "All",
    val searchQuery: String = "",
    val error: String? = null,
)

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val doubtRepository: DoubtRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedUiState())
    val uiState: StateFlow<SavedUiState> = _uiState.asStateFlow()

    private var currentUserId: String = ""

    init {
        loadBookmarks()
    }

    private fun loadBookmarks() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    currentUserId = user.uid
                    fetchBookmarkedDoubts(user.bookmarks)
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private fun fetchBookmarkedDoubts(bookmarkIds: List<String>) {
        if (bookmarkIds.isEmpty()) {
            _uiState.update { it.copy(bookmarkedDoubts = emptyList(), isLoading = false) }
            return
        }

        viewModelScope.launch {
            val doubts = mutableListOf<Doubt>()
            for (id in bookmarkIds) {
                doubtRepository.getDoubt(id).onSuccess { doubt ->
                    doubts.add(doubt)
                }
            }
            _uiState.update {
                it.copy(
                    bookmarkedDoubts = doubts,
                    isLoading = false,
                    error = null,
                )
            }
        }
    }

    fun setFilter(filter: String) = _uiState.update { it.copy(selectedFilter = filter) }

    fun setSearchQuery(query: String) = _uiState.update { it.copy(searchQuery = query) }

    fun removeBookmark(doubtId: String) {
        viewModelScope.launch {
            if (currentUserId.isNotBlank()) {
                profileRepository.toggleBookmark(currentUserId, doubtId)
                _uiState.update {
                    it.copy(bookmarkedDoubts = it.bookmarkedDoubts.filter { d -> d.id != doubtId })
                }
            }
        }
    }
}
