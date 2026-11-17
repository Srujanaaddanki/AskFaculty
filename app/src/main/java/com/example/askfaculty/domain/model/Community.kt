package com.example.askfaculty.domain.model

import com.google.firebase.Timestamp

data class Community(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val subject: String = "",
    val facultyId: String = "",
    val facultyName: String = "",
    val coverImageUrl: String = "",
    val memberCount: Int = 0,
    val postCount: Int = 0,
    val members: List<String> = emptyList(),
    val isActive: Boolean = true,
    val isPublic: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
)

data class Post(
    val id: String = "",
    val communityId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: String = "",
    val authorPhotoUrl: String = "",
    val content: String = "",
    val codeSnippet: String = "",
    val attachmentUrl: String = "",
    val attachmentType: String = "",
    val attachmentName: String = "",
    val isAnnouncement: Boolean = false,
    val isPinned: Boolean = false,
    val isOfficialAnswer: Boolean = false,
    val helpfulCount: Int = 0,
    val replyCount: Int = 0,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
)

data class PostReply(
    val id: String = "",
    val postId: String = "",
    val communityId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorRole: String = "",
    val authorPhotoUrl: String = "",
    val content: String = "",
    val isOfficial: Boolean = false,
    val createdAt: Timestamp? = null,
)
