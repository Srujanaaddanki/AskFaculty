package com.example.askfaculty.domain.repository

import com.example.askfaculty.domain.model.DashboardStats
import com.example.askfaculty.domain.model.Doubt
import com.example.askfaculty.domain.model.FrequentTopic
import com.example.askfaculty.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface DoubtRepository {
    fun getDoubts(
        facultyId: String,
        status: String? = null,
        subject: String? = null,
        priority: String? = null,
        searchQuery: String? = null,
    ): Flow<List<Doubt>>

    fun getPriorityDoubts(facultyId: String, limit: Int = 5): Flow<List<Doubt>>

    suspend fun getDoubt(doubtId: String): Result<Doubt>

    fun getMessages(doubtId: String): Flow<List<Message>>

    suspend fun sendMessage(message: Message): Result<String>

    suspend fun updateDoubtStatus(doubtId: String, status: String): Result<Unit>

    suspend fun updateDoubtPriority(doubtId: String, priority: String): Result<Unit>

    suspend fun markMessageOfficial(doubtId: String, messageId: String, isOfficial: Boolean): Result<Unit>

    suspend fun getDashboardStats(facultyId: String): Result<DashboardStats>

    fun getFrequentTopics(facultyId: String, limit: Int = 5): Flow<List<FrequentTopic>>
}
