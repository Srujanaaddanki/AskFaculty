package com.example.askfaculty.domain.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val studentId: String = "",
    val academicYear: String = "",
    val semester: String = "",
    val designation: String = "",
    val department: String = "",
    val college: String = "",
    val photoUrl: String = "",
    val subjects: List<String> = emptyList(),
    val joinedCommunities: List<String> = emptyList(),
    val bookmarks: List<String> = emptyList(),
    val fcmToken: String = "",
    val availability: String = "AVAILABLE",
    val officeHours: List<OfficeHour> = emptyList(),
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
) {
    val displayName: String get() = name.ifBlank { "User" }
}

data class NotificationPreferences(
    val doubtReplies: Boolean = true,
    val communityAnnouncements: Boolean = true,
    val newResources: Boolean = true,
    val emailDigest: Boolean = false,
)

data class OfficeHour(
    val day: String = "",
    val startTime: String = "",
    val endTime: String = "",
)
