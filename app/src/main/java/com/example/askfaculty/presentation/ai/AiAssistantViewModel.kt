package com.example.askfaculty.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.data.remote.AiDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AiToolMode {
    CONCEPT_EXPLAINER,
    QUESTION_REPHRASER,
    STUDY_NOTES,
    PRACTICE_QUESTIONS,
}

data class AiAssistantUiState(
    val selectedMode: AiToolMode = AiToolMode.CONCEPT_EXPLAINER,
    val selectedSubject: String = Constants.DEFAULT_SUBJECTS.first(),
    val inputTopic: String = "",
    val inputQuestion: String = "",
    val isLoading: Boolean = false,
    val resultText: String = "",
    val practiceQuestions: List<String> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val aiDataSource: AiDataSource,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiAssistantUiState())
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    fun setMode(mode: AiToolMode) {
        _uiState.update {
            it.copy(
                selectedMode = mode,
                resultText = "",
                practiceQuestions = emptyList(),
                error = null,
            )
        }
    }

    fun setSubject(subject: String) = _uiState.update { it.copy(selectedSubject = subject) }
    fun setInputTopic(topic: String) = _uiState.update { it.copy(inputTopic = topic) }
    fun setInputQuestion(q: String) = _uiState.update { it.copy(inputQuestion = q) }

    fun processAiAction() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (state.selectedMode) {
                AiToolMode.CONCEPT_EXPLAINER -> {
                    val topic = state.inputTopic.ifBlank { "Binary Search Trees" }
                    aiDataSource.explainConcept(topic, state.selectedSubject).fold(
                        onSuccess = { res -> _uiState.update { it.copy(isLoading = false, resultText = res) } },
                        onFailure = { err -> _uiState.update { it.copy(isLoading = false, error = err.localizedMessage) } }
                    )
                }
                AiToolMode.QUESTION_REPHRASER -> {
                    val q = state.inputQuestion.ifBlank { "What is difference between TCP and UDP?" }
                    aiDataSource.rephraseQuestion(q).fold(
                        onSuccess = { res -> _uiState.update { it.copy(isLoading = false, resultText = res) } },
                        onFailure = { err -> _uiState.update { it.copy(isLoading = false, error = err.localizedMessage) } }
                    )
                }
                AiToolMode.STUDY_NOTES -> {
                    val topic = state.inputTopic.ifBlank { "Deadlocks and Prevention" }
                    aiDataSource.generateStudyNotes(state.selectedSubject, topic).fold(
                        onSuccess = { res -> _uiState.update { it.copy(isLoading = false, resultText = res) } },
                        onFailure = { err -> _uiState.update { it.copy(isLoading = false, error = err.localizedMessage) } }
                    )
                }
                AiToolMode.PRACTICE_QUESTIONS -> {
                    val topic = state.inputTopic.ifBlank { "B-Trees and Indexing" }
                    aiDataSource.generatePracticeQuestions(state.selectedSubject, topic).fold(
                        onSuccess = { res -> _uiState.update { it.copy(isLoading = false, practiceQuestions = res) } },
                        onFailure = { err -> _uiState.update { it.copy(isLoading = false, error = err.localizedMessage) } }
                    )
                }
            }
        }
    }
}
