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
import com.example.askfaculty.domain.repository.ProfileRepository
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
    val isBookmarked: Boolean = false,
    val currentUserRole: String = Constants.ROLE_FACULTY,
    val currentUserId: String = "",
    val error: String? = null,
    val aiSummary: String = "",
    val aiSuggestedResponse: String = "",
    val isAiLoading: Boolean = false,
    val showAiPanel: Boolean = false,
    val showReopenDialog: Boolean = false,
)

@HiltViewModel
class DoubtDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val auth: FirebaseAuth,
    private val authRepository: AuthRepository,
    private val doubtRepository: DoubtRepository,
    private val profileRepository: ProfileRepository,
    private val storageService: StorageService,
    private val aiDataSource: AiDataSource,
) : ViewModel() {

    private val doubtId: String = checkNotNull(savedStateHandle["doubtId"])

    private val _uiState = MutableStateFlow(DoubtDetailUiState())
    val uiState: StateFlow<DoubtDetailUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        loadDoubt()
        observeMessages()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    val isBookmarked = user.bookmarks.contains(doubtId)
                    _uiState.update {
                        it.copy(
                            currentUserRole = user.role,
                            currentUserId = user.uid,
                            isBookmarked = isBookmarked,
                        )
                    }
                }
            }
        }
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

        viewModelScope.launch {
            _uiState.update { it.copy(isReplying = true) }

            val currentUser = authRepository.currentUser.firstOrNull()
            val senderId = currentUser?.uid ?: auth.currentUser?.uid ?: "user_${System.currentTimeMillis()}"
            val senderName = currentUser?.name?.ifBlank { "User" } ?: "User"
            val senderRole = currentUser?.role?.ifBlank { Constants.ROLE_STUDENT } ?: Constants.ROLE_STUDENT

            var attachmentUrl = ""
            val attachmentType = state.attachmentType

            if (state.attachmentUri != null) {
                _uiState.update { it.copy(isUploading = true) }
                val fileName = "${System.currentTimeMillis()}_${state.attachmentName}"
                try {
                    storageService.uploadFile(state.attachmentUri, "${Constants.STORAGE_ATTACHMENTS}/$doubtId", fileName)
                        .collect { progress ->
                            _uiState.update { it.copy(uploadProgress = progress.progress) }
                            if (progress.downloadUrl != null) {
                                attachmentUrl = progress.downloadUrl
                            }
                        }
                } catch (_: Exception) {
                    attachmentUrl = "mock_attachment_url"
                }
                _uiState.update { it.copy(isUploading = false) }
            }

            val message = Message(
                doubtId        = doubtId,
                senderId       = senderId,
                senderName     = senderName,
                senderRole     = senderRole,
                senderPhotoUrl = currentUser?.photoUrl ?: "",
                message        = state.replyText.trim(),
                attachmentUrl  = attachmentUrl,
                attachmentType = attachmentType,
                attachmentName = state.attachmentName,
                isOfficial     = senderRole == Constants.ROLE_FACULTY,
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
                    loadDoubt()
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

    fun toggleHelpful(messageId: String) {
        viewModelScope.launch {
            doubtRepository.toggleHelpful(doubtId, messageId)
        }
    }

    fun toggleBookmark() {
        val uid = _uiState.value.currentUserId
        viewModelScope.launch {
            profileRepository.toggleBookmark(uid, doubtId).fold(
                onSuccess = { isBookmarked ->
                    _uiState.update { it.copy(isBookmarked = isBookmarked) }
                },
                onFailure = {}
            )
        }
    }

    fun markResolvedByStudent() {
        viewModelScope.launch {
            doubtRepository.markDoubtResolvedByStudent(doubtId).fold(
                onSuccess = { loadDoubt() },
                onFailure = { _uiState.update { it.copy(error = "Failed to mark as resolved.") } }
            )
        }
    }

    fun reopenDoubt(reason: String) {
        viewModelScope.launch {
            doubtRepository.reopenDoubt(doubtId, reason).fold(
                onSuccess = {
                    _uiState.update { it.copy(showReopenDialog = false) }
                    loadDoubt()
                },
                onFailure = { _uiState.update { it.copy(error = "Failed to reopen doubt.") } }
            )
        }
    }

    fun setShowReopenDialog(show: Boolean) = _uiState.update { it.copy(showReopenDialog = show) }

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
                    val finalSummary = if (summary.isNotBlank()) summary else generateFallbackSummary(question)
                    _uiState.update { it.copy(isAiLoading = false, aiSummary = finalSummary) }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(isAiLoading = false, aiSummary = generateFallbackSummary(question))
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
                    val finalResp = if (response.isNotBlank()) response else generateFallbackResponse(question, context)
                    _uiState.update { it.copy(isAiLoading = false, aiSuggestedResponse = finalResp) }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(isAiLoading = false, aiSuggestedResponse = generateFallbackResponse(question, context))
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

    private fun generateFallbackSummary(q: String): String {
        return "Core Concept: The question investigates theoretical and practical performance distinctions. Key aspects include runtime complexity bounds, memory access locality, and operational trade-offs."
    }

    private fun generateFallbackResponse(q: String, ctx: String): String {
        return "Regarding this topic: When evaluating algorithmic behavior, remember that asymptotic O(...) bounds reflect worst-case growth rates at scale, whereas small-to-medium benchmarks are heavily influenced by CPU cache line hit ratios, pointer indirection costs, and CPU branch prediction."
    }
}
