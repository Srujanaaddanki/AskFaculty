package com.example.askfaculty.navigation

sealed class Routes(val route: String) {
    // Startup
    object Splash         : Routes("startup/splash")
    object Onboarding     : Routes("startup/onboarding")

    // Auth
    object Login          : Routes("auth/login")
    object Register       : Routes("auth/register")
    object ForgotPassword : Routes("auth/forgot_password")

    // Faculty Bottom Nav Destinations
    object Home           : Routes("main/home")
    object Doubts         : Routes("main/doubts")
    object Communities    : Routes("main/communities")
    object Notifications  : Routes("main/notifications")
    object Profile        : Routes("main/profile")

    // Student Bottom Nav Destinations
    object StudentHome        : Routes("student/home")
    object StudentDoubts      : Routes("student/doubts")
    object StudentCommunities : Routes("student/communities")
    object StudentResources   : Routes("student/resources")
    object StudentSaved       : Routes("student/saved")
    object StudentProfile     : Routes("student/profile")

    // Student Actions
    object AskDoubt           : Routes("student/doubts/ask")
    object AiAssistant        : Routes("student/ai/assistant")

    // Doubt Nested
    object DoubtDetail        : Routes("main/doubts/{doubtId}") {
        fun createRoute(doubtId: String) = "main/doubts/$doubtId"
    }

    // Community Nested
    object CommunityDetail    : Routes("main/communities/{communityId}") {
        fun createRoute(communityId: String) = "main/communities/$communityId"
    }
    object CreateCommunity    : Routes("main/communities/create")
    object CreatePost         : Routes("main/communities/{communityId}/post/create") {
        fun createRoute(communityId: String) = "main/communities/$communityId/post/create"
    }

    // Profile Nested
    object EditProfile          : Routes("main/profile/edit")
    object Availability         : Routes("main/profile/availability")
    object ChangePassword       : Routes("main/profile/change_password")
    object NotificationSettings : Routes("main/profile/notification_settings")
    object Settings             : Routes("main/profile/settings")

    // Global Search
    object Search               : Routes("main/search")
}
