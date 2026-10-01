package com.example.askfaculty.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.askfaculty.ui.theme.AskFacultyDimensions

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0,
)

val bottomNavItems = listOf(
    BottomNavItem("Home",          Routes.Home.route,          Icons.Filled.Home,           Icons.Outlined.Home),
    BottomNavItem("Doubts",        Routes.Doubts.route,        Icons.Filled.QuestionAnswer, Icons.Outlined.QuestionAnswer),
    BottomNavItem("Communities",   Routes.Communities.route,   Icons.Filled.Groups,         Icons.Outlined.Groups),
    BottomNavItem("Alerts",        Routes.Notifications.route, Icons.Filled.Notifications,  Icons.Outlined.Notifications),
    BottomNavItem("Profile",       Routes.Profile.route,       Icons.Filled.Person,         Icons.Outlined.Person),
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    unreadNotifications: Int,
    onNavigate: (String) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier       = Modifier.height(AskFacultyDimensions.bottomNavHeight),
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute?.startsWith(item.route) == true
            val badge = if (item.route == Routes.Notifications.route) unreadNotifications else item.badgeCount

            NavigationBarItem(
                selected = isSelected,
                onClick  = { onNavigate(item.route) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (badge > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor   = MaterialTheme.colorScheme.onError,
                                ) {
                                    Text(
                                        text       = if (badge > 99) "99+" else badge.toString(),
                                        style      = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector        = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                        )
                    }
                },
                label = {
                    Text(
                        text       = item.label,
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor   = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor      = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    }
}
