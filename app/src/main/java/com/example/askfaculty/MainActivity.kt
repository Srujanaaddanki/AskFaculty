package com.example.askfaculty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.askfaculty.data.repository.ThemeMode
import com.example.askfaculty.data.repository.UserPreferencesRepository
import com.example.askfaculty.domain.repository.AuthRepository
import com.example.askfaculty.domain.repository.NotificationRepository
import com.example.askfaculty.navigation.FacultyNavGraph
import com.example.askfaculty.navigation.Routes
import com.example.askfaculty.ui.theme.AskFacultyTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var notificationRepository: NotificationRepository
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by userPreferencesRepository.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            AskFacultyTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                // Observe current user auth state for notifications
                val currentUser = FirebaseAuth.getInstance().currentUser
                val unreadCount by if (currentUser != null) {
                    notificationRepository.getUnreadCount(currentUser.uid).collectAsState(initial = 0)
                } else {
                    flowOf(0).collectAsState(initial = 0)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    FacultyNavGraph(
                        navController = navController,
                        startDestination = Routes.Splash.route,
                        unreadNotifications = unreadCount,
                    )
                }
            }
        }
    }
}