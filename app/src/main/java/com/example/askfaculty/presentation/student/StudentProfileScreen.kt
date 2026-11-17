package com.example.askfaculty.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.askfaculty.core.ui.components.*
import com.example.askfaculty.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    onNavigateToSaved: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToResources: () -> Unit,
    onLogout: () -> Unit,
    viewModel: StudentProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Student Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (uiState.isLoading) {
            LoadingState(modifier = Modifier.fillMaxSize())
        } else {
            val user = uiState.user
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            ) {
                // ── Student Header Card ───────────────────────────────────────
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AskFacultySpacing.lg),
                    shape = RoundedCornerShape(AskFacultyRadius.xl),
                    elevation = CardDefaults.cardElevation(defaultElevation = AskFacultyElevation.medium),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(listOf(GradientStart, GradientMid, GradientEnd))
                            )
                            .padding(AskFacultySpacing.xl),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            AvatarImage(
                                url = user?.photoUrl ?: "",
                                size = 80.dp,
                                placeholder = user?.displayName ?: "Student",
                            )

                            Spacer(Modifier.height(AskFacultySpacing.md))

                            Text(
                                text = user?.displayName ?: "Student",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )

                            Text(
                                text = "College ID: ${user?.studentId?.ifBlank { "22CS104" } ?: "22CS104"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f),
                            )

                            Spacer(Modifier.height(AskFacultySpacing.sm))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.xs),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(AskFacultyRadius.pill),
                                    color = Color.White.copy(alpha = 0.2f),
                                ) {
                                    Text(
                                        text = user?.department?.ifBlank { "Computer Science & Eng." } ?: "Computer Science & Eng.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = AskFacultySpacing.sm, vertical = 2.dp),
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(AskFacultyRadius.pill),
                                    color = Color.White.copy(alpha = 0.2f),
                                ) {
                                    Text(
                                        text = "Year ${user?.academicYear ?: 3} • Sem ${user?.semester ?: 6}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = AskFacultySpacing.sm, vertical = 2.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Learning Statistics Grid ──────────────────────────────────
                Text(
                    text = "ACADEMIC ACTIVITY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = AskFacultySpacing.xl, vertical = AskFacultySpacing.xs),
                    letterSpacing = androidx.compose.ui.unit.TextUnit(0.6f, androidx.compose.ui.unit.TextUnitType.Sp),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AskFacultySpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
                ) {
                    MetricBox(
                        title = "Doubts Asked",
                        count = "${uiState.stats.pendingDoubts + uiState.stats.answeredDoubts}",
                        icon = Icons.Default.QuestionAnswer,
                        modifier = Modifier.weight(1f),
                    )
                    MetricBox(
                        title = "Resolved",
                        count = "${uiState.stats.answeredDoubts}",
                        icon = Icons.Default.Verified,
                        modifier = Modifier.weight(1f),
                    )
                    MetricBox(
                        title = "Spaces Joined",
                        count = "${user?.joinedCommunities?.size ?: 3}",
                        icon = Icons.Default.Groups,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(AskFacultySpacing.lg))

                // ── Quick Navigation Section ──────────────────────────────────
                Text(
                    text = "LEARNING TOOLS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = AskFacultySpacing.xl, vertical = AskFacultySpacing.xs),
                    letterSpacing = androidx.compose.ui.unit.TextUnit(0.6f, androidx.compose.ui.unit.TextUnitType.Sp),
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AskFacultySpacing.lg),
                    shape = RoundedCornerShape(AskFacultyRadius.lg),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = AskFacultyElevation.low),
                ) {
                    Column {
                        ProfileNavRow(
                            icon = Icons.Default.AutoAwesome,
                            title = "AskFaculty AI Assistant",
                            subtitle = "Concept explainer, study notes & MCQs",
                            onClick = onNavigateToAi,
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = AskFacultySpacing.md))
                        ProfileNavRow(
                            icon = Icons.Outlined.BookmarkBorder,
                            title = "Saved & Bookmarked Doubts",
                            subtitle = "Quick offline access to verified answers",
                            onClick = onNavigateToSaved,
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = AskFacultySpacing.md))
                        ProfileNavRow(
                            icon = Icons.Default.FolderOpen,
                            title = "Academic Resources & Slides",
                            subtitle = "Download faculty notes and syllabus",
                            onClick = onNavigateToResources,
                        )
                    }
                }

                Spacer(Modifier.height(AskFacultySpacing.lg))

                // ── Notification Preferences ──────────────────────────────────
                Text(
                    text = "NOTIFICATION PREFERENCES",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = AskFacultySpacing.xl, vertical = AskFacultySpacing.xs),
                    letterSpacing = androidx.compose.ui.unit.TextUnit(0.6f, androidx.compose.ui.unit.TextUnitType.Sp),
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AskFacultySpacing.lg),
                    shape = RoundedCornerShape(AskFacultyRadius.lg),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = AskFacultyElevation.low),
                ) {
                    Column(modifier = Modifier.padding(AskFacultySpacing.md)) {
                        NotificationToggleRow(
                            title = "Faculty Answers & Follow-ups",
                            subtitle = "Instant alert when faculty replies to your doubt",
                            checked = uiState.notificationPrefs.doubtReplies,
                            onCheckedChange = { viewModel.toggleNotificationPref("doubtReplies", it) },
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = AskFacultySpacing.xs))
                        NotificationToggleRow(
                            title = "Space Announcements",
                            subtitle = "Notifications from professors in joined spaces",
                            checked = uiState.notificationPrefs.communityAnnouncements,
                            onCheckedChange = { viewModel.toggleNotificationPref("communityAnnouncements", it) },
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = AskFacultySpacing.xs))
                        NotificationToggleRow(
                            title = "New Subject Materials",
                            subtitle = "Alerts when study notes or slides are uploaded",
                            checked = uiState.notificationPrefs.newResources,
                            onCheckedChange = { viewModel.toggleNotificationPref("newResources", it) },
                        )
                    }
                }

                Spacer(Modifier.height(AskFacultySpacing.xl))

                // ── Logout Action ─────────────────────────────────────────────
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AskFacultySpacing.lg),
                    shape = RoundedCornerShape(AskFacultyRadius.md),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(AskFacultyDimensions.iconSizeSm),
                    )
                    Spacer(Modifier.width(AskFacultySpacing.sm))
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(AskFacultySpacing.xxxl))
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out of your student account?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(onLogout)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun MetricBox(
    title: String,
    count: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(AskFacultyRadius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AskFacultyElevation.low),
    ) {
        Column(
            modifier = Modifier.padding(AskFacultySpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.height(AskFacultySpacing.xs))
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProfileNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(AskFacultySpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(AskFacultyRadius.sm))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
