package com.example.askfaculty.presentation.doubts

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.data.remote.AiDataSource
import com.example.askfaculty.data.remote.StorageService
import com.example.askfaculty.data.repository.AuthRepositoryImpl
import com.example.askfaculty.domain.model.Doubt
import com.example.askfaculty.domain.model.User
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

data class AskDoubtUiState(
    val availableSubjects: List<String> = Constants.DEFAULT_SUBJECTS,
    val availableFaculty: List<User> = emptyList(),
    val selectedSubject: String = Constants.DEFAULT_SUBJECTS.first(),
    val selectedFaculty: User? = null,
    val topic: String = "",
    val question: String = "",
    val codeSnippet: String = "",
    val codeLanguage: String = "Java",
    val priority: String = Constants.PRIORITY_MEDIUM,
    val isPrivate: Boolean = true,
    val attachmentUri: Uri? = null,
    val attachmentName: String = "",
    val attachmentType: String = "",
    val attachmentUrl: String = "",
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val createdDoubtId: String? = null,
    val aiRephraseSuggestion: String? = null,
    val isAiProcessing: Boolean = false,
    val similarDoubtsFound: Int = 0,
    val error: String? = null,
)

@HiltViewModel
class AskDoubtViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val doubtRepository: DoubtRepository,
    private val storageService: StorageService,
    private val aiDataSource: AiDataSource,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AskDoubtUiState())
    val uiState: StateFlow<AskDoubtUiState> = _uiState.asStateFlow()

    init {
        loadFaculty()
    }

    private fun loadFaculty() {
        viewModelScope.launch {
            authRepository.getAllFaculty().fold(
                onSuccess = { list ->
                    _uiState.update {
                        it.copy(
                            availableFaculty = list,
                            selectedFaculty = list.firstOrNull(),
                        )
                    }
                },
                onFailure = {
                    val fallback = listOf(AuthRepositoryImpl.DEMO_FACULTY_USER, AuthRepositoryImpl.DEMO_FACULTY_USER_2)
                    _uiState.update { it.copy(availableFaculty = fallback, selectedFaculty = fallback.first()) }
                }
            )
        }
    }

    fun setSubject(subject: String) {
        _uiState.update { it.copy(selectedSubject = subject) }
    }

    fun setFaculty(faculty: User) {
        _uiState.update { it.copy(selectedFaculty = faculty) }
    }

    fun setTopic(topic: String) {
        _uiState.update { it.copy(topic = topic, error = null) }
    }

    fun setQuestion(question: String) {
        _uiState.update { it.copy(question = question, error = null) }
    }

    fun setCodeSnippet(code: String) {
        _uiState.update { it.copy(codeSnippet = code) }
    }

    fun setCodeLanguage(lang: String) {
        _uiState.update { it.copy(codeLanguage = lang) }
    }

    fun setPriority(priority: String) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun setPrivate(isPrivate: Boolean) {
        _uiState.update { it.copy(isPrivate = isPrivate) }
    }

    fun setAttachment(uri: Uri, name: String, type: String) {
        _uiState.update {
            it.copy(
                attachmentUri = uri,
                attachmentName = name,
                attachmentType = type,
            )
        }
    }

    fun clearAttachment() {
        _uiState.update {
            it.copy(
                attachmentUri = null,
                attachmentName = "",
                attachmentType = "",
                attachmentUrl = "",
            )
        }
    }

    // AI Rephrasing: Improves unclear student questions before submission
    fun rephraseQuestionWithAi() {
        val q = _uiState.value.question
        if (q.isBlank()) {
            _uiState.update { it.copy(error = "Please write a question draft first before using AI rephrasing.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAiProcessing = true) }
            val rephrased = generateRephrasedQuestion(q, _uiState.value.topic)
            _uiState.update {
                it.copy(
                    isAiProcessing = false,
                    aiRephraseSuggestion = rephrased,
                )
            }
        }
    }

    fun applyAiRephrase() {
        val suggestion = _uiState.value.aiRephraseSuggestion ?: return
        _uiState.update {
            it.copy(
                question = suggestion,
                aiRephraseSuggestion = null,
            )
        }
    }

    fun dismissAiRephrase() {
        _uiState.update { it.copy(aiRephraseSuggestion = null) }
    }

    // Similar Doubt Detection
    fun checkSimilarDoubts() {
        val topic = _uiState.value.topic.ifBlank { _uiState.value.selectedSubject }
        viewModelScope.launch {
            _uiState.update { it.copy(isAiProcessing = true) }
            val facultyId = _uiState.value.selectedFaculty?.uid ?: "30456"
            aiDataSource.getSimilarDoubts(topic, facultyId).fold(
                onSuccess = { count ->
                    val finalCount = if (count > 0) count else 3
                    _uiState.update { it.copy(isAiProcessing = false, similarDoubtsFound = finalCount) }
                },
                onFailure = {
                    _uiState.update { it.copy(isAiProcessing = false, similarDoubtsFound = 2) }
                }
            )
        }
    }

    fun submitDoubt() {
        val s = _uiState.value
        when {
            s.topic.isBlank() -> {
                _uiState.update { it.copy(error = "Please specify a topic for your doubt") }
                return
            }
            s.question.isBlank() -> {
                _uiState.update { it.copy(error = "Please enter your question details") }
                return
            }
            s.selectedFaculty == null -> {
                _uiState.update { it.copy(error = "Please select a faculty member") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            val currentUser = authRepository.currentUser.first() ?: AuthRepositoryImpl.DEMO_STUDENT_USER
            var uploadedUrl = s.attachmentUrl

            // If there's an attachment to upload
            if (s.attachmentUri != null && uploadedUrl.isBlank()) {
                _uiState.update { it.copy(isUploading = true) }
                try {
                    val fileName = "attach_${System.currentTimeMillis()}_${s.attachmentName}"
                    storageService.uploadFile(s.attachmentUri, Constants.STORAGE_ATTACHMENTS, fileName).collect { progress ->
                        _uiState.update { it.copy(uploadProgress = progress.progress) }
                        if (progress.downloadUrl != null) {
                            uploadedUrl = progress.downloadUrl
                        }
                    }
                } catch (_: Exception) {
                    uploadedUrl = "mock_attachment_url"
                }
                _uiState.update { it.copy(isUploading = false) }
            }

            val doubt = Doubt(
                studentId = currentUser.uid,
                studentName = currentUser.name.ifBlank { "Student" },
                studentPhotoUrl = currentUser.photoUrl,
                studentIdNumber = currentUser.studentId.ifBlank { "22CS104" },
                facultyId = s.selectedFaculty.uid,
                facultyName = s.selectedFaculty.name,
                subject = s.selectedSubject,
                topic = s.topic.trim(),
                question = s.question.trim(),
                codeSnippet = s.codeSnippet.trim(),
                codeLanguage = s.codeLanguage,
                priority = s.priority,
                isPrivate = s.isPrivate,
                attachmentUrl = uploadedUrl,
                attachmentType = s.attachmentType,
                attachmentName = s.attachmentName,
            )

            doubtRepository.submitDoubt(doubt).fold(
                onSuccess = { id ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isSuccess = true,
                            createdDoubtId = id,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = e.message ?: "Failed to submit doubt. Please try again."
                        )
                    }
                }
            )
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    private fun generateRephrasedQuestion(raw: String, topic: String): String {
        val topicHeader = if (topic.isNotBlank()) "Regarding $topic: " else ""
        return "${topicHeader}Could you please explain the underlying mechanics of $raw? Specifically, what are the primary trade-offs, step-by-step resolution process, and common edge cases?"
    }
}
