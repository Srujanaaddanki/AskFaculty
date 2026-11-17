package com.example.askfaculty.presentation.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.data.repository.AuthRepositoryImpl
import com.example.askfaculty.data.repository.CommunityRepositoryImpl
import com.example.askfaculty.domain.model.Community
import com.example.askfaculty.domain.model.Doubt
import com.example.askfaculty.domain.model.Post
import com.example.askfaculty.domain.model.StudentDashboardStats
import com.example.askfaculty.domain.model.User
import com.example.askfaculty.domain.repository.AuthRepository
import com.example.askfaculty.domain.repository.CommunityRepository
import com.example.askfaculty.domain.repository.DoubtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentHomeUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val stats: StudentDashboardStats = StudentDashboardStats(),
    val mySubjects: List<String> = emptyList(),
    val recentAnsweredDoubts: List<Doubt> = emptyList(),
    val joinedCommunities: List<Community> = emptyList(),
    val facultyAnnouncements: List<Post> = emptyList(),
    val trendingDiscussions: List<Post> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class StudentHomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val doubtRepository: DoubtRepository,
    private val communityRepository: CommunityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentHomeUiState())
    val uiState: StateFlow<StudentHomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.currentUser.collect { currentUser ->
                val user = currentUser ?: AuthRepositoryImpl.DEMO_STUDENT_USER
                val subjects = if (user.subjects.isNotEmpty()) user.subjects else Constants.DEFAULT_SUBJECTS.take(4)

                _uiState.update { it.copy(user = user, mySubjects = subjects) }

                // Load stats
                val statsResult = doubtRepository.getStudentStats(user.uid)
                statsResult.onSuccess { s ->
                    _uiState.update { it.copy(stats = s) }
                }

                // Load recent answered doubts
                launch {
                    doubtRepository.getRecentAnsweredDoubts(5).collect { doubts ->
                        _uiState.update { it.copy(recentAnsweredDoubts = doubts) }
                    }
                }

                // Load joined communities
                launch {
                    communityRepository.getJoinedCommunities(user.uid).collect { comms ->
                        val activeComms = if (comms.isNotEmpty()) comms else CommunityRepositoryImpl.IN_MEMORY_COMMUNITIES.take(2)
                        _uiState.update { it.copy(joinedCommunities = activeComms) }

                        // Extract announcements & trending discussions from communities
                        val allPosts = mutableListOf<Post>()
                        activeComms.forEach { comm ->
                            communityRepository.getPosts(comm.id).collect { posts ->
                                allPosts.addAll(posts)
                                val announcements = allPosts.filter { it.isAnnouncement || it.isPinned }.distinctBy { it.id }
                                val trending = allPosts.filter { !it.isAnnouncement }.sortedByDescending { it.helpfulCount }.distinctBy { it.id }
                                _uiState.update { current ->
                                    current.copy(
                                        facultyAnnouncements = announcements,
                                        trendingDiscussions = trending.take(5),
                                        isLoading = false,
                                    )
                                }
                            }
                        }
                    }
                }

                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun refresh() = loadData()
}
