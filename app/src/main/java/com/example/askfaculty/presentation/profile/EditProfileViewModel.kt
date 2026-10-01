package com.example.askfaculty.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.domain.model.User
import com.example.askfaculty.domain.repository.AuthRepository
import com.example.askfaculty.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val name: String = "",
    val designation: String = "",
    val department: String = "",
    val college: String = "",
    val subjectInput: String = "",
    val subjects: List<String> = emptyList(),
    val saved: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()
    private var currentUser: User? = null

    init {
        viewModelScope.launch {
            authRepository.currentUser.filterNotNull().first().let { user ->
                currentUser = user
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        name = user.name,
                        designation = user.designation,
                        department = user.department,
                        college = user.college,
                        subjects = user.subjects,
                    )
                }
            }
        }
    }

    fun setName(v: String) = _uiState.update { it.copy(name = v) }
    fun setDesignation(v: String) = _uiState.update { it.copy(designation = v) }
    fun setDepartment(v: String) = _uiState.update { it.copy(department = v) }
    fun setCollege(v: String) = _uiState.update { it.copy(college = v) }
    fun setSubjectInput(v: String) = _uiState.update { it.copy(subjectInput = v) }

    fun addSubject() {
        val s = _uiState.value.subjectInput.trim()
        if (s.isNotBlank() && !_uiState.value.subjects.contains(s)) {
            _uiState.update { it.copy(subjects = it.subjects + s, subjectInput = "") }
        }
    }

    fun removeSubject(subject: String) = _uiState.update { it.copy(subjects = it.subjects - subject) }

    fun save() {
        val state = _uiState.value
        val user = currentUser ?: return
        if (state.name.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            profileRepository.updateProfile(
                user.copy(
                    name = state.name.trim(),
                    designation = state.designation.trim(),
                    department = state.department.trim(),
                    college = state.college.trim(),
                    subjects = state.subjects,
                )
            ).fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, saved = true) } },
                onFailure = { e -> _uiState.update { it.copy(isSaving = false, error = "Save failed: ${e.message}") } }
            )
        }
    }
}
