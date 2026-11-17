package com.example.askfaculty.core.util

object Constants {
    // Firestore Collections
    const val COLLECTION_USERS          = "users"
    const val COLLECTION_SUBJECTS       = "subjects"
    const val COLLECTION_DOUBTS         = "doubts"
    const val COLLECTION_MESSAGES       = "messages"
    const val COLLECTION_COMMUNITIES    = "communities"
    const val COLLECTION_POSTS          = "posts"
    const val COLLECTION_COMMENTS       = "comments"
    const val COLLECTION_POST_REPLIES   = "replies"
    const val COLLECTION_RESOURCES      = "resources"
    const val COLLECTION_NOTIFICATIONS  = "notifications"

    // User Roles
    const val ROLE_FACULTY  = "FACULTY"
    const val ROLE_STUDENT  = "STUDENT"

    // Doubt Status
    const val STATUS_PENDING    = "PENDING"
    const val STATUS_IN_PROGRESS = "IN_PROGRESS"
    const val STATUS_ANSWERED   = "ANSWERED"
    const val STATUS_RESOLVED   = "RESOLVED"

    // Priority
    const val PRIORITY_HIGH   = "HIGH"
    const val PRIORITY_MEDIUM = "MEDIUM"
    const val PRIORITY_LOW    = "LOW"

    // Message sender role
    const val SENDER_FACULTY  = "FACULTY"
    const val SENDER_STUDENT  = "STUDENT"

    // Attachment types
    const val ATTACHMENT_IMAGE    = "IMAGE"
    const val ATTACHMENT_PDF      = "PDF"
    const val ATTACHMENT_DOC      = "DOC"
    const val ATTACHMENT_LINK     = "LINK"
    const val ATTACHMENT_OTHER    = "OTHER"
    const val ATTACHMENT_TYPE_FILE  = "FILE"
    const val ATTACHMENT_TYPE_IMAGE = "IMAGE"

    // Firebase Storage paths
    const val STORAGE_AVATARS       = "avatars"
    const val STORAGE_ATTACHMENTS   = "attachments"
    const val STORAGE_RESOURCES     = "resources"
    const val STORAGE_COVERS        = "community_covers"

    // Pagination
    const val PAGE_SIZE = 20L

    // Faculty availability
    const val AVAILABILITY_AVAILABLE = "AVAILABLE"
    const val AVAILABILITY_BUSY      = "BUSY"
    const val AVAILABILITY_AWAY      = "AWAY"

    // Notification types
    const val NOTIF_NEW_DOUBT       = "NEW_DOUBT"
    const val NOTIF_FACULTY_ANSWER  = "FACULTY_ANSWER"
    const val NOTIF_FOLLOW_UP       = "FOLLOW_UP"
    const val NOTIF_MENTION         = "MENTION"
    const val NOTIF_COMMUNITY       = "COMMUNITY"
    const val NOTIF_STUDENT_REPLY   = "STUDENT_REPLY"
    const val NOTIF_DOUBT_RESOLVED  = "DOUBT_RESOLVED"
    const val NOTIF_NEW_RESOURCE    = "NEW_RESOURCE"
    const val NOTIF_ANNOUNCEMENT    = "ANNOUNCEMENT"

    object NotificationType {
        const val NEW_DOUBT      = NOTIF_NEW_DOUBT
        const val FACULTY_ANSWER = NOTIF_FACULTY_ANSWER
        const val FOLLOW_UP      = NOTIF_FOLLOW_UP
        const val MENTION        = NOTIF_MENTION
        const val COMMUNITY      = NOTIF_COMMUNITY
        const val STUDENT_REPLY  = NOTIF_STUDENT_REPLY
        const val DOUBT_RESOLVED = NOTIF_DOUBT_RESOLVED
        const val NEW_RESOURCE   = NOTIF_NEW_RESOURCE
        const val ANNOUNCEMENT   = NOTIF_ANNOUNCEMENT
    }

    // AI Cloud Function endpoint (replace with your deployed URL)
    const val AI_FUNCTION_BASE_URL = "https://YOUR_REGION-YOUR_PROJECT.cloudfunctions.net"

    // Standard Academic Subjects
    val DEFAULT_SUBJECTS = listOf(
        "Data Structures",
        "Design & Analysis of Algorithms",
        "Operating Systems",
        "Database Management Systems",
        "Computer Networks",
        "Artificial Intelligence",
        "Software Engineering",
        "Cloud Computing",
    )
}
