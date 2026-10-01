package com.example.askfaculty.presentation.doubts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.domain.model.Doubt
import com.example.askfaculty.domain.repository.AuthRepository
import com.example.askfaculty.domain.repository.DoubtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DoubtsUiState(
    val isLoading: Boolean = true,
    val doubts: List<Doubt> = emptyList(),
    val error: String? = null,
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val selectedSubject: String? = null,
    val selectedPriority: String? = null,
)

val doubtTabs = listOf("All", "Pending", "In Progress", "Answered", "Resolved")

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class DoubtsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val doubtRepository: DoubtRepository,
) : ViewModel() {

    private val _selectedTab     = MutableStateFlow(0)
    private val _searchQuery     = MutableStateFlow("")
    private val _selectedSubject = MutableStateFlow<String?>(null)
    private val _selectedPriority= MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(DoubtsUiState())
    val uiState: StateFlow<DoubtsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.filterNotNull().collect { user ->
                combine(
                    _selectedTab,
                    _searchQuery.debounce(300),
                    _selectedSubject,
                    _selectedPriority,
                ) { tab, query, subject, priority ->
                    val status = when (tab) {
                        1 -> Constants.STATUS_PENDING
                        2 -> Constants.STATUS_IN_PROGRESS
                        3 -> Constants.STATUS_ANSWERED
                        4 -> Constants.STATUS_RESOLVED
                        else -> null
                    }
                    quadruple(status, query, subject, priority)
                }.flatMapLatest { (status, query, subject, priority) ->
                    doubtRepository.getDoubts(
                        facultyId    = user.uid,
                        status       = status,
                        subject      = subject,
                        priority     = priority,
                        searchQuery  = query,
                    )
                }.collect { doubts ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            doubts    = doubts,
                            error     = null,
                        )
                    }
                }
            }
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSubjectFilter(subject: String?) {
        _selectedSubject.value = subject
        _uiState.update { it.copy(selectedSubject = subject) }
    }

    fun setPriorityFilter(priority: String?) {
        _selectedPriority.value = priority
        _uiState.update { it.copy(selectedPriority = priority) }
    }

    fun clearFilters() {
        _selectedSubject.value = null
        _selectedPriority.value = null
        _uiState.update { it.copy(selectedSubject = null, selectedPriority = null) }
    }

    private fun <A,B,C,D> quadruple(a: A, b: B, c: C, d: D) = Quadruple(a, b, c, d)
}

private data class Quadruple<A,B,C,D>(val first: A, val second: B, val third: C, val fourth: D)
