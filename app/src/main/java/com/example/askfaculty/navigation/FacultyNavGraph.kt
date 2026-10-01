package com.example.askfaculty.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.askfaculty.presentation.auth.ForgotPasswordScreen
import com.example.askfaculty.presentation.auth.LoginScreen
import com.example.askfaculty.presentation.communities.CommunitiesScreen
import com.example.askfaculty.presentation.communities.CommunityDetailScreen
import com.example.askfaculty.presentation.communities.CreateCommunityScreen
import com.example.askfaculty.presentation.communities.CreatePostScreen
import com.example.askfaculty.presentation.doubts.DoubtDetailScreen
import com.example.askfaculty.presentation.doubts.DoubtsScreen
import com.example.askfaculty.presentation.home.HomeScreen
import com.example.askfaculty.presentation.notifications.NotificationsScreen
import com.example.askfaculty.presentation.onboarding.OnboardingScreen
import com.example.askfaculty.presentation.profile.AvailabilityScreen
import com.example.askfaculty.presentation.profile.EditProfileScreen
import com.example.askfaculty.presentation.profile.ProfileScreen
import com.example.askfaculty.presentation.search.SearchScreen
import com.example.askfaculty.presentation.settings.SettingsScreen
import com.example.askfaculty.presentation.splash.SplashScreen

val bottomNavRoutes = setOf(
    Routes.Home.route,
    Routes.Doubts.route,
    Routes.Communities.route,
    Routes.Notifications.route,
    Routes.Profile.route,
)

@Composable
fun FacultyNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.Splash.route,
    unreadNotifications: Int,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    unreadNotifications = unreadNotifications,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { fadeOut() },
        ) {
            // Startup
            composable(Routes.Splash.route) {
                SplashScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(Routes.Onboarding.route) {
                            popUpTo(Routes.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Routes.Login.route) {
                            popUpTo(Routes.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Splash.route) { inclusive = true }
                        }
                    },
                )
            }

            composable(Routes.Onboarding.route) {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(Routes.Login.route) {
                            popUpTo(Routes.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Auth
            composable(Routes.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Login.route) { inclusive = true }
                        }
                    },
                    onForgotPassword = { navController.navigate(Routes.ForgotPassword.route) },
                )
            }
            composable(Routes.ForgotPassword.route) {
                ForgotPasswordScreen(onBack = { navController.popBackStack() })
            }

            // Main bottom nav destinations
            composable(Routes.Home.route) {
                HomeScreen(
                    onDoubtClick = { doubtId -> navController.navigate(Routes.DoubtDetail.createRoute(doubtId)) },
                    onSearchClick = { navController.navigate(Routes.Search.route) },
                    onNavigateToDoubts = { navController.navigate(Routes.Doubts.route) },
                    onCreateCommunityClick = { navController.navigate(Routes.CreateCommunity.route) },
                )
            }
            composable(Routes.Doubts.route) {
                DoubtsScreen(
                    onDoubtClick = { doubtId -> navController.navigate(Routes.DoubtDetail.createRoute(doubtId)) },
                )
            }
            composable(Routes.Communities.route) {
                CommunitiesScreen(
                    onCommunityClick = { communityId -> navController.navigate(Routes.CommunityDetail.createRoute(communityId)) },
                    onCreateCommunity = { navController.navigate(Routes.CreateCommunity.route) },
                )
            }
            composable(Routes.Notifications.route) {
                NotificationsScreen(
                    onDoubtClick = { doubtId -> navController.navigate(Routes.DoubtDetail.createRoute(doubtId)) },
                )
            }
            composable(Routes.Profile.route) {
                ProfileScreen(
                    onEditProfile = { navController.navigate(Routes.EditProfile.route) },
                    onAvailability = { navController.navigate(Routes.Availability.route) },
                    onSettings = { navController.navigate(Routes.Settings.route) },
                    onLogout = {
                        navController.navigate(Routes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }

            // Nested
            composable(Routes.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.DoubtDetail.route) { backStackEntry ->
                val doubtId = backStackEntry.arguments?.getString("doubtId") ?: ""
                DoubtDetailScreen(
                    doubtId = doubtId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.CommunityDetail.route) { backStackEntry ->
                val communityId = backStackEntry.arguments?.getString("communityId") ?: ""
                CommunityDetailScreen(
                    communityId = communityId,
                    onBack = { navController.popBackStack() },
                    onCreatePost = { navController.navigate(Routes.CreatePost.createRoute(communityId)) },
                )
            }
            composable(Routes.CreateCommunity.route) {
                CreateCommunityScreen(onBack = { navController.popBackStack() }, onCreated = { navController.popBackStack() })
            }
            composable(Routes.CreatePost.route) { backStackEntry ->
                val communityId = backStackEntry.arguments?.getString("communityId") ?: ""
                CreatePostScreen(
                    communityId = communityId,
                    onBack = { navController.popBackStack() },
                    onCreated = { navController.popBackStack() },
                )
            }
            composable(Routes.EditProfile.route) {
                EditProfileScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.Availability.route) {
                AvailabilityScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.Search.route) {
                SearchScreen(onBack = { navController.popBackStack() }, onDoubtClick = { doubtId -> navController.navigate(Routes.DoubtDetail.createRoute(doubtId)) })
            }
        }
    }
}
