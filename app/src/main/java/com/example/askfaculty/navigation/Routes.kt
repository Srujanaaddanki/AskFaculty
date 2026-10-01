package com.example.askfaculty.navigation

sealed class Routes(val route: String) {
    // Startup
    object Splash         : Routes("startup/splash")
    object Onboarding     : Routes("startup/onboarding")

    // Auth
    object Login          : Routes("auth/login")
    object ForgotPassword : Routes("auth/forgot_password")

    // Main
    object Home           : Routes("main/home")
    object Doubts         : Routes("main/doubts")
    object Communities    : Routes("main/communities")
    object Notifications  : Routes("main/notifications")
    object Profile        : Routes("main/profile")

    // Doubt nested
    object DoubtDetail    : Routes("main/doubts/{doubtId}") {
        fun createRoute(doubtId: String) = "main/doubts/$doubtId"
    }

    // Community nested
    object CommunityDetail : Routes("main/communities/{communityId}") {
        fun createRoute(communityId: String) = "main/communities/$communityId"
    }
    object CreateCommunity : Routes("main/communities/create")
    object CreatePost      : Routes("main/communities/{communityId}/post/create") {
        fun createRoute(communityId: String) = "main/communities/$communityId/post/create"
    }

    // Profile nested
    object EditProfile      : Routes("main/profile/edit")
    object Availability     : Routes("main/profile/availability")
    object ChangePassword   : Routes("main/profile/change_password")
    object NotificationSettings : Routes("main/profile/notification_settings")
    object Settings         : Routes("main/profile/settings")

    // Search
    object Search : Routes("main/search")
}
