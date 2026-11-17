package com.example.askfaculty.domain.repository

import com.example.askfaculty.domain.model.DashboardStats
import com.example.askfaculty.domain.model.Doubt
import com.example.askfaculty.domain.model.FrequentTopic
import com.example.askfaculty.domain.model.Message
import com.example.askfaculty.domain.model.StudentDashboardStats
import kotlinx.coroutines.flow.Flow

interface DoubtRepository {
    // Faculty queries
    fun getDoubts(
        facultyId: String,
        status: String? = null,
        subject: String? = null,
        priority: String? = null,
        searchQuery: String? = null,
    ): Flow<List<Doubt>>

    fun getPriorityDoubts(facultyId: String, limit: Int = 5): Flow<List<Doubt>>

    // Student queries
    fun getStudentDoubts(
        studentId: String,
        status: String? = null,
        subject: String? = null,
        searchQuery: String? = null,
    ): Flow<List<Doubt>>

    fun getRecentAnsweredDoubts(limit: Int = 5): Flow<List<Doubt>>

    suspend fun getStudentStats(studentId: String): Result<StudentDashboardStats>

    suspend fun submitDoubt(doubt: Doubt): Result<String>

    suspend fun updateDoubt(doubt: Doubt): Result<Unit>

    suspend fun deleteDoubt(doubtId: String, studentId: String): Result<Unit>

    suspend fun reopenDoubt(doubtId: String, reason: String): Result<Unit>

    suspend fun markDoubtResolvedByStudent(doubtId: String): Result<Unit>

    suspend fun toggleHelpful(doubtId: String, messageId: String): Result<Unit>

    // Shared / Detail queries
    suspend fun getDoubt(doubtId: String): Result<Doubt>

    fun getMessages(doubtId: String): Flow<List<Message>>

    suspend fun sendMessage(message: Message): Result<String>

    suspend fun updateDoubtStatus(doubtId: String, status: String): Result<Unit>

    suspend fun updateDoubtPriority(doubtId: String, priority: String): Result<Unit>

    suspend fun markMessageOfficial(doubtId: String, messageId: String, isOfficial: Boolean): Result<Unit>

    suspend fun getDashboardStats(facultyId: String): Result<DashboardStats>

    fun getFrequentTopics(facultyId: String, limit: Int = 5): Flow<List<FrequentTopic>>

    fun searchPublicKnowledgeBase(query: String): Flow<List<Doubt>>
}
