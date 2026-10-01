package com.example.askfaculty.presentation.communities

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.data.remote.StorageService
import com.example.askfaculty.domain.model.Community
import com.example.askfaculty.domain.repository.AuthRepository
import com.example.askfaculty.domain.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateCommunityUiState(
    val name: String = "",
    val description: String = "",
    val subject: String = "",
    val coverUri: Uri? = null,
    val isPublic: Boolean = true,
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val error: String? = null,
    val created: Boolean = false,
    val nameError: String? = null,
    val descriptionError: String? = null,
    val subjectError: String? = null,
)

@HiltViewModel
class CreateCommunityViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val communityRepository: CommunityRepository,
    private val storageService: StorageService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCommunityUiState())
    val uiState: StateFlow<CreateCommunityUiState> = _uiState.asStateFlow()

    fun setName(v: String) = _uiState.update { it.copy(name = v, nameError = null) }
    fun setDescription(v: String) = _uiState.update { it.copy(description = v, descriptionError = null) }
    fun setSubject(v: String) = _uiState.update { it.copy(subject = v, subjectError = null) }
    fun setCoverUri(uri: Uri?) = _uiState.update { it.copy(coverUri = uri) }
    fun setPublic(v: Boolean) = _uiState.update { it.copy(isPublic = v) }

    fun createCommunity() {
        val state = _uiState.value
        var hasError = false
        if (state.name.isBlank()) { _uiState.update { it.copy(nameError = "Name is required") }; hasError = true }
        if (state.description.isBlank()) { _uiState.update { it.copy(descriptionError = "Description is required") }; hasError = true }
        if (state.subject.isBlank()) { _uiState.update { it.copy(subjectError = "Subject is required") }; hasError = true }
        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val user = authRepository.currentUser.firstOrNull()
            if (user == null) { _uiState.update { it.copy(isLoading = false, error = "Not authenticated") }; return@launch }

            var coverUrl = ""
            if (state.coverUri != null) {
                _uiState.update { it.copy(isUploading = true) }
                storageService.uploadFile(state.coverUri, Constants.STORAGE_COVERS, "${System.currentTimeMillis()}.jpg")
                    .collect { progress ->
                        _uiState.update { it.copy(uploadProgress = progress.progress) }
                        if (progress.downloadUrl != null) coverUrl = progress.downloadUrl
                    }
                _uiState.update { it.copy(isUploading = false) }
            }

            communityRepository.createCommunity(
                Community(
                    name = state.name.trim(),
                    description = state.description.trim(),
                    subject = state.subject.trim(),
                    facultyId = user.uid,
                    facultyName = user.name,
                    coverImageUrl = coverUrl,
                    isPublic = state.isPublic,
                )
            ).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, created = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = "Failed to create community: ${e.message}") } }
            )
        }
    }
}
