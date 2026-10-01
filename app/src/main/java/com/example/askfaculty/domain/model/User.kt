package com.example.askfaculty.domain.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val designation: String = "",
    val department: String = "",
    val college: String = "",
    val photoUrl: String = "",
    val subjects: List<String> = emptyList(),
    val fcmToken: String = "",
    val availability: String = "AVAILABLE",
    val officeHours: List<OfficeHour> = emptyList(),
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
)

data class OfficeHour(
    val day: String = "",
    val startTime: String = "",
    val endTime: String = "",
)
