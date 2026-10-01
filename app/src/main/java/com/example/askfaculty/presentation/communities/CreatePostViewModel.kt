package com.example.askfaculty.presentation.communities

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.data.remote.StorageService
import com.example.askfaculty.domain.model.Post
import com.example.askfaculty.domain.repository.AuthRepository
import com.example.askfaculty.domain.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreatePostUiState(
    val content: String = "",
    val isAnnouncement: Boolean = false,
    val attachmentUri: Uri? = null,
    val attachmentName: String = "",
    val attachmentType: String = "",
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val error: String? = null,
    val created: Boolean = false,
    val contentError: String? = null,
)

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val communityRepository: CommunityRepository,
    private val storageService: StorageService,
) : ViewModel() {

    private val communityId: String = checkNotNull(savedStateHandle["communityId"])
    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    fun setContent(v: String) = _uiState.update { it.copy(content = v, contentError = null) }
    fun setAnnouncement(v: Boolean) = _uiState.update { it.copy(isAnnouncement = v) }
    fun setAttachment(uri: Uri?, name: String, type: String) = _uiState.update { it.copy(attachmentUri = uri, attachmentName = name, attachmentType = type) }
    fun clearAttachment() = _uiState.update { it.copy(attachmentUri = null, attachmentName = "", attachmentType = "") }

    fun createPost() {
        val state = _uiState.value
        if (state.content.isBlank() && state.attachmentUri == null) {
            _uiState.update { it.copy(contentError = "Post content is required") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val user = authRepository.currentUser.firstOrNull()
            if (user == null) { _uiState.update { it.copy(isLoading = false, error = "Not authenticated") }; return@launch }

            var attachmentUrl = ""
            if (state.attachmentUri != null) {
                _uiState.update { it.copy(isUploading = true) }
                storageService.uploadFile(state.attachmentUri, "${Constants.STORAGE_ATTACHMENTS}/posts/$communityId", "${System.currentTimeMillis()}_${state.attachmentName}")
                    .collect { progress ->
                        _uiState.update { it.copy(uploadProgress = progress.progress) }
                        if (progress.downloadUrl != null) attachmentUrl = progress.downloadUrl
                    }
                _uiState.update { it.copy(isUploading = false) }
            }

            communityRepository.createPost(
                Post(
                    communityId = communityId,
                    authorId = user.uid,
                    authorName = user.name,
                    authorRole = user.role,
                    authorPhotoUrl = user.photoUrl,
                    content = state.content.trim(),
                    attachmentUrl = attachmentUrl,
                    attachmentType = state.attachmentType,
                    attachmentName = state.attachmentName,
                    isAnnouncement = state.isAnnouncement,
                )
            ).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, created = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = "Failed to create post.") } }
            )
        }
    }
}
