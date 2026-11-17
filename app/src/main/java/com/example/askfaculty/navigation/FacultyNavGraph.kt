package com.example.askfaculty.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.presentation.ai.AiAssistantScreen
import com.example.askfaculty.presentation.auth.ForgotPasswordScreen
import com.example.askfaculty.presentation.auth.LoginScreen
import com.example.askfaculty.presentation.auth.RegisterScreen
import com.example.askfaculty.presentation.communities.CommunitiesScreen
import com.example.askfaculty.presentation.communities.CommunityDetailScreen
import com.example.askfaculty.presentation.communities.CreateCommunityScreen
import com.example.askfaculty.presentation.communities.CreatePostScreen
import com.example.askfaculty.presentation.doubts.AskDoubtScreen
import com.example.askfaculty.presentation.doubts.DoubtDetailScreen
import com.example.askfaculty.presentation.doubts.DoubtsScreen
import com.example.askfaculty.presentation.home.HomeScreen
import com.example.askfaculty.presentation.notifications.NotificationsScreen
import com.example.askfaculty.presentation.onboarding.OnboardingScreen
import com.example.askfaculty.presentation.profile.AvailabilityScreen
import com.example.askfaculty.presentation.profile.EditProfileScreen
import com.example.askfaculty.presentation.profile.ProfileScreen
import com.example.askfaculty.presentation.resources.ResourcesScreen
import com.example.askfaculty.presentation.search.SearchScreen
import com.example.askfaculty.presentation.settings.SettingsScreen
import com.example.askfaculty.presentation.splash.SplashScreen
import com.example.askfaculty.presentation.student.SavedScreen
import com.example.askfaculty.presentation.student.StudentCommunitiesScreen
import com.example.askfaculty.presentation.student.StudentDoubtsScreen
import com.example.askfaculty.presentation.student.StudentHomeScreen
import com.example.askfaculty.presentation.student.StudentProfileScreen

val facultyBottomNavRoutes = setOf(
    Routes.Home.route,
    Routes.Doubts.route,
    Routes.Communities.route,
    Routes.Notifications.route,
    Routes.Profile.route,
)

val studentBottomNavRoutes = setOf(
    Routes.StudentHome.route,
    Routes.StudentDoubts.route,
    Routes.StudentCommunities.route,
    Routes.StudentResources.route,
    Routes.StudentProfile.route,
)

@Composable
fun FacultyNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.Splash.route,
    unreadNotifications: Int,
    isStudent: Boolean = false,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = if (isStudent) {
        currentRoute in studentBottomNavRoutes
    } else {
        currentRoute in facultyBottomNavRoutes
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    isStudent = isStudent,
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
                    onNavigateToFacultyHome = {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToStudentHome = {
                        navController.navigate(Routes.StudentHome.route) {
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
                    onLoginSuccess = { user ->
                        val targetRoute = if (user.role == Constants.ROLE_STUDENT) {
                            Routes.StudentHome.route
                        } else {
                            Routes.Home.route
                        }
                        navController.navigate(targetRoute) {
                            popUpTo(Routes.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Routes.Register.route)
                    },
                    onForgotPassword = { _ ->
                        navController.navigate(Routes.ForgotPassword.route)
                    },
                )
            }

            composable(Routes.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = { _ ->
                        navController.navigate(Routes.StudentHome.route) {
                            popUpTo(Routes.Register.route) { inclusive = true }
                        }
                    },
                    onBackToLogin = {
                        navController.popBackStack()
                    },
                )
            }

            composable(Routes.ForgotPassword.route) {
                ForgotPasswordScreen(onBack = { navController.popBackStack() })
            }

            // ── Faculty Main bottom nav destinations ─────────────────────────
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

            // ── Student Main bottom nav destinations ─────────────────────────
            composable(Routes.StudentHome.route) {
                StudentHomeScreen(
                    onAskDoubtClick = { navController.navigate(Routes.AskDoubt.route) },
                    onDoubtClick = { doubtId -> navController.navigate(Routes.DoubtDetail.createRoute(doubtId)) },
                    onCommunityClick = { communityId -> navController.navigate(Routes.CommunityDetail.createRoute(communityId)) },
                    onSearchClick = { navController.navigate(Routes.Search.route) },
                    onNotificationsClick = { navController.navigate(Routes.Notifications.route) },
                    onViewAllDoubts = { navController.navigate(Routes.StudentDoubts.route) },
                    onViewAllCommunities = { navController.navigate(Routes.StudentCommunities.route) },
                    onViewResources = { navController.navigate(Routes.StudentResources.route) },
                )
            }

            composable(Routes.StudentDoubts.route) {
                StudentDoubtsScreen(
                    onDoubtClick = { doubtId -> navController.navigate(Routes.DoubtDetail.createRoute(doubtId)) },
                    onAskDoubtClick = { navController.navigate(Routes.AskDoubt.route) },
                )
            }

            composable(Routes.StudentCommunities.route) {
                StudentCommunitiesScreen(
                    onCommunityClick = { communityId -> navController.navigate(Routes.CommunityDetail.createRoute(communityId)) },
                )
            }

            composable(Routes.StudentResources.route) {
                ResourcesScreen()
            }

            composable(Routes.StudentSaved.route) {
                SavedScreen(
                    onDoubtClick = { doubtId -> navController.navigate(Routes.DoubtDetail.createRoute(doubtId)) },
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(Routes.StudentProfile.route) {
                StudentProfileScreen(
                    onNavigateToSaved = { navController.navigate(Routes.StudentSaved.route) },
                    onNavigateToAi = { navController.navigate(Routes.AiAssistant.route) },
                    onNavigateToResources = { navController.navigate(Routes.StudentResources.route) },
                    onLogout = {
                        navController.navigate(Routes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }

            // ── Student Action destinations ──────────────────────────────────
            composable(Routes.AskDoubt.route) {
                AskDoubtScreen(
                    onBack = { navController.popBackStack() },
                    onDoubtSubmitted = { doubtId ->
                        navController.navigate(Routes.DoubtDetail.createRoute(doubtId)) {
                            popUpTo(Routes.AskDoubt.route) { inclusive = true }
                        }
                    },
                )
            }

            composable(Routes.AiAssistant.route) {
                AiAssistantScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onEscalateToFaculty = { _ ->
                        navController.navigate(Routes.AskDoubt.route)
                    },
                )
            }

            // ── Shared Nested Destinations ───────────────────────────────────
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
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onDoubtClick = { doubtId -> navController.navigate(Routes.DoubtDetail.createRoute(doubtId)) },
                    onCommunityClick = { communityId -> navController.navigate(Routes.CommunityDetail.createRoute(communityId)) },
                )
            }
        }
    }
}
