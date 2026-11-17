package com.example.askfaculty.presentation.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.domain.model.Doubt
import com.example.askfaculty.domain.repository.AuthRepository
import com.example.askfaculty.domain.repository.DoubtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentDoubtsUiState(
    val isLoading: Boolean = true,
    val selectedTab: Int = 0,
    val searchQuery: String = "",
    val selectedSubject: String? = null,
    val selectedPriority: String? = null,
    val doubts: List<Doubt> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class StudentDoubtsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val doubtRepository: DoubtRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentDoubtsUiState())
    val uiState: StateFlow<StudentDoubtsUiState> = _uiState.asStateFlow()

    init {
        loadDoubts()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
        loadDoubts()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadDoubts()
    }

    fun setSubjectFilter(subject: String?) {
        _uiState.update { it.copy(selectedSubject = subject) }
        loadDoubts()
    }

    fun setPriorityFilter(priority: String?) {
        _uiState.update { it.copy(selectedPriority = priority) }
        loadDoubts()
    }

    fun clearFilters() {
        _uiState.update { it.copy(selectedSubject = null, selectedPriority = null, searchQuery = "") }
        loadDoubts()
    }

    fun loadDoubts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val currentUser = authRepository.currentUser.first()
            val studentId = currentUser?.uid ?: "std_101"

            val statusFilter = when (_uiState.value.selectedTab) {
                1 -> Constants.STATUS_PENDING
                2 -> Constants.STATUS_ANSWERED
                3 -> Constants.STATUS_RESOLVED
                else -> null
            }

            doubtRepository.getStudentDoubts(
                studentId = studentId,
                status = statusFilter,
                subject = _uiState.value.selectedSubject,
                searchQuery = _uiState.value.searchQuery,
            ).collect { list ->
                val filtered = if (_uiState.value.selectedPriority != null) {
                    list.filter { it.priority == _uiState.value.selectedPriority }
                } else {
                    list
                }
                _uiState.update { it.copy(doubts = filtered, isLoading = false) }
            }
        }
    }
}
