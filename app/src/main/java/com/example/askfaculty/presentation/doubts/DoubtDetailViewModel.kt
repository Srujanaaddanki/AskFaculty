package com.example.askfaculty.presentation.doubts

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.data.remote.AiDataSource
import com.example.askfaculty.data.remote.StorageService
import com.example.askfaculty.domain.model.Doubt
import com.example.askfaculty.domain.model.Message
import com.example.askfaculty.domain.repository.AuthRepository
import com.example.askfaculty.domain.repository.DoubtRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DoubtDetailUiState(
    val isLoading: Boolean = true,
    val doubt: Doubt? = null,
    val messages: List<Message> = emptyList(),
    val replyText: String = "",
    val isReplying: Boolean = false,
    val uploadProgress: Float = 0f,
    val isUploading: Boolean = false,
    val attachmentUri: Uri? = null,
    val attachmentName: String = "",
    val attachmentType: String = "",
    val error: String? = null,
    val aiSummary: String = "",
    val aiSuggestedResponse: String = "",
    val isAiLoading: Boolean = false,
    val showAiPanel: Boolean = false,
)

@HiltViewModel
class DoubtDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val auth: FirebaseAuth,
    private val authRepository: AuthRepository,
    private val doubtRepository: DoubtRepository,
    private val storageService: StorageService,
    private val aiDataSource: AiDataSource,
) : ViewModel() {

    private val doubtId: String = checkNotNull(savedStateHandle["doubtId"])

    private val _uiState = MutableStateFlow(DoubtDetailUiState())
    val uiState: StateFlow<DoubtDetailUiState> = _uiState.asStateFlow()

    init {
        loadDoubt()
        observeMessages()
    }

    private fun loadDoubt() {
        viewModelScope.launch {
            doubtRepository.getDoubt(doubtId).fold(
                onSuccess = { doubt ->
                    _uiState.update { it.copy(doubt = doubt, isLoading = false) }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Failed to load doubt. Please try again.")
                    }
                }
            )
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            doubtRepository.getMessages(doubtId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    fun setReplyText(text: String) = _uiState.update { it.copy(replyText = text) }

    fun setAttachment(uri: Uri?, name: String, type: String) {
        _uiState.update { it.copy(attachmentUri = uri, attachmentName = name, attachmentType = type) }
    }

    fun clearAttachment() = _uiState.update { it.copy(attachmentUri = null, attachmentName = "", attachmentType = "") }

    fun sendReply() {
        val state = _uiState.value
        if (state.replyText.isBlank() && state.attachmentUri == null) return
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isReplying = true) }

            var attachmentUrl = ""
            var attachmentType = state.attachmentType

            // Upload attachment if present
            if (state.attachmentUri != null) {
                _uiState.update { it.copy(isUploading = true) }
                val fileName = "${System.currentTimeMillis()}_${state.attachmentName}"
                storageService.uploadFile(state.attachmentUri, "${Constants.STORAGE_ATTACHMENTS}/$doubtId", fileName)
                    .collect { progress ->
                        _uiState.update { it.copy(uploadProgress = progress.progress) }
                        if (progress.downloadUrl != null) {
                            attachmentUrl = progress.downloadUrl
                        }
                    }
                _uiState.update { it.copy(isUploading = false) }
            }

            // Fetch current user profile for name
            val facultyName = authRepository.currentUser.firstOrNull()?.name ?: ""

            val message = Message(
                doubtId        = doubtId,
                senderId       = currentUser.uid,
                senderName     = facultyName,
                senderRole     = Constants.SENDER_FACULTY,
                senderPhotoUrl = authRepository.currentUser.firstOrNull()?.photoUrl ?: "",
                message        = state.replyText.trim(),
                attachmentUrl  = attachmentUrl,
                attachmentType = attachmentType,
                attachmentName = state.attachmentName,
                isOfficial     = false,
            )

            doubtRepository.sendMessage(message).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isReplying = false,
                            replyText = "",
                            attachmentUri = null,
                            attachmentName = "",
                            attachmentType = "",
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(isReplying = false, error = "Failed to send reply. Please try again.")
                    }
                }
            )
        }
    }

    fun markOfficial(messageId: String, isOfficial: Boolean) {
        viewModelScope.launch {
            doubtRepository.markMessageOfficial(doubtId, messageId, isOfficial)
        }
    }

    fun updateStatus(status: String) {
        viewModelScope.launch {
            doubtRepository.updateDoubtStatus(doubtId, status).fold(
                onSuccess = { loadDoubt() },
                onFailure = { _uiState.update { it.copy(error = "Failed to update status.") } }
            )
        }
    }

    fun updatePriority(priority: String) {
        viewModelScope.launch {
            doubtRepository.updateDoubtPriority(doubtId, priority).fold(
                onSuccess = { loadDoubt() },
                onFailure = { _uiState.update { it.copy(error = "Failed to update priority.") } }
            )
        }
    }

    fun generateAiSummary() {
        val question = _uiState.value.doubt?.question ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true, showAiPanel = true) }
            aiDataSource.getQuestionSummary(question).fold(
                onSuccess = { summary ->
                    _uiState.update { it.copy(isAiLoading = false, aiSummary = summary) }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(isAiLoading = false, aiSummary = "AI summary unavailable. Please check your connection.")
                    }
                }
            )
        }
    }

    fun generateSuggestedResponse() {
        val question = _uiState.value.doubt?.question ?: return
        val context = _uiState.value.doubt?.let { "${it.subject} - ${it.topic}" } ?: ""
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true) }
            aiDataSource.getSuggestedResponse(question, context).fold(
                onSuccess = { response ->
                    _uiState.update { it.copy(isAiLoading = false, aiSuggestedResponse = response) }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(isAiLoading = false, aiSuggestedResponse = "AI response unavailable at this time.")
                    }
                }
            )
        }
    }

    fun useAiSuggestion() {
        _uiState.update { it.copy(replyText = it.aiSuggestedResponse, showAiPanel = false) }
    }

    fun toggleAiPanel() = _uiState.update { it.copy(showAiPanel = !it.showAiPanel) }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
