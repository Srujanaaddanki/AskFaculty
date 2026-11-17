package com.example.askfaculty.domain.model

import com.google.firebase.Timestamp

data class Resource(
    val id: String = "",
    val communityId: String = "",
    val subject: String = "",
    val uploaderId: String = "",
    val uploaderName: String = "",
    val uploaderRole: String = "FACULTY",
    val title: String = "",
    val description: String = "",
    val fileUrl: String = "",
    val fileType: String = "",
    val fileName: String = "",
    val fileSizeBytes: Long = 0L,
    val isLink: Boolean = false,
    val linkUrl: String = "",
    val isPinned: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
)
