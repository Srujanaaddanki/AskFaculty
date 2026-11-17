package com.example.askfaculty.domain.model

import com.google.firebase.Timestamp

data class Notification(
    val id: String = "",
    val recipientId: String = "",
    val type: String = "",
    val title: String = "",
    val body: String = "",
    val doubtId: String = "",
    val communityId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val isRead: Boolean = false,
    val createdAt: Timestamp? = null,
)

data class DashboardStats(
    val pendingDoubts: Int = 0,
    val unansweredDoubts: Int = 0,
    val answeredToday: Int = 0,
    val activeCommunities: Int = 0,
)

data class StudentDashboardStats(
    val pendingDoubts: Int = 0,
    val answeredDoubts: Int = 0,
    val resolvedDoubts: Int = 0,
    val joinedCommunities: Int = 0,
)

data class FrequentTopic(
    val topic: String = "",
    val doubtCount: Int = 0,
)
