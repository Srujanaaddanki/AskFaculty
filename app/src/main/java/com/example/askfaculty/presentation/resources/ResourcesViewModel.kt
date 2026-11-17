package com.example.askfaculty.presentation.resources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.domain.model.Resource
import com.example.askfaculty.domain.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResourcesUiState(
    val isLoading: Boolean = true,
    val resources: List<Resource> = emptyList(),
    val selectedSubject: String = "All",
    val selectedType: String = "All",
    val searchQuery: String = "",
    val error: String? = null,
)

@HiltViewModel
class ResourcesViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResourcesUiState())
    val uiState: StateFlow<ResourcesUiState> = _uiState.asStateFlow()

    init {
        loadResources()
    }

    private fun loadResources() {
        viewModelScope.launch {
            communityRepository.getAllResources().collect { list ->
                _uiState.update {
                    it.copy(
                        resources = list,
                        isLoading = false,
                        error = null,
                    )
                }
            }
        }
    }

    fun selectSubject(subject: String) = _uiState.update { it.copy(selectedSubject = subject) }

    fun selectType(type: String) = _uiState.update { it.copy(selectedType = type) }

    fun setSearchQuery(query: String) = _uiState.update { it.copy(searchQuery = query) }
}
