package com.example.askfaculty.domain.model

import com.google.firebase.Timestamp

data class Resource(
    val id: String = "",
    val communityId: String = "",
    val uploaderId: String = "",
    val uploaderName: String = "",
    val title: String = "",
    val description: String = "",
    val fileUrl: String = "",
    val fileType: String = "",
    val fileName: String = "",
    val fileSizeBytes: Long = 0L,
    val isLink: Boolean = false,
    val linkUrl: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
)
