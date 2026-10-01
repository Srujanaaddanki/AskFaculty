package com.example.askfaculty.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.ui.theme.AskFacultyDimensions
import com.example.askfaculty.ui.theme.AskFacultyElevation
import com.example.askfaculty.ui.theme.AskFacultyRadius
import com.example.askfaculty.ui.theme.AskFacultySpacing

// ─── Availability colour helpers ─────────────────────────────────────────────

private data class AvailabilityStyle(val color: Color, val label: String, val icon: ImageVector)

@Composable
private fun resolveAvailabilityStyle(availability: String): AvailabilityStyle = when (availability) {
    Constants.AVAILABILITY_AVAILABLE -> AvailabilityStyle(
        color = Color(0xFF22C55E),
        label = "Available for Doubts",
        icon  = Icons.Default.CheckCircle,
    )
    Constants.AVAILABILITY_BUSY -> AvailabilityStyle(
        color = Color(0xFFF59E0B),
        label = "Busy in Class / Meetings",
        icon  = Icons.Default.Schedule,
    )
    Constants.AVAILABILITY_AWAY -> AvailabilityStyle(
        color = Color(0xFFEF4444),
        label = "Away / Outside Hours",
        icon  = Icons.Default.DoNotDisturbOn,
    )
    else -> AvailabilityStyle(
        color = MaterialTheme.colorScheme.outline,
        label = "Status Unknown",
        icon  = Icons.Default.HelpOutline,
    )
}

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onAvailability: () -> Unit,
    onSettings: () -> Unit = {},
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // ── Navigate away on successful logout ───────────────────────────────────
    LaunchedEffect(uiState.loggedOut) {
        if (uiState.loggedOut) onLogout()
    }

    // ── Logout confirmation dialog ────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector        = Icons.Default.Logout,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.error,
                )
            },
            title = {
                Text(
                    text       = "Sign Out of Portal",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text("Are you sure you want to log out of your faculty account on this device?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    },
                ) {
                    Text(
                        text       = "Sign Out",
                        color      = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    // ── Scaffold ──────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            AskFacultyTopBar(
                title = "Faculty Profile",
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(
                            imageVector        = Icons.Default.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->

        when {
            uiState.isLoading -> {
                Box(
                    modifier           = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment   = Alignment.Center,
                ) {
                    LoadingState()
                }
            }

            else -> {
                val user         = uiState.user
                val availability = user?.availability ?: Constants.AVAILABILITY_AVAILABLE
                val availStyle   = resolveAvailabilityStyle(availability)

                LazyColumn(
                    modifier       = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = AskFacultySpacing.xxxl),
                ) {

                    // ── 1. Gradient Hero Card ─────────────────────────────────
                    item {
                        ProfileHeroCard(
                            name         = user?.name ?: "Faculty Member",
                            designation  = user?.designation ?: "",
                            department   = user?.department ?: "",
                            college      = user?.college ?: "",
                            email        = user?.email ?: "",
                            photoUrl     = user?.photoUrl ?: "",
                            availStyle   = availStyle,
                        )
                    }

                    // ── 2. Stats row ──────────────────────────────────────────
                    item {
                        Spacer(Modifier.height(AskFacultySpacing.md))
                        ProfileStatsRow(
                            subjectsCount = user?.subjects?.size ?: 0,
                            officeSlots   = user?.officeHours?.size ?: 0,
                        )
                    }

                    // ── 3. Subject chips ──────────────────────────────────────
                    if (!user?.subjects.isNullOrEmpty()) {
                        item {
                            Spacer(Modifier.height(AskFacultySpacing.md))
                            SubjectsSection(subjects = user?.subjects ?: emptyList())
                        }
                    }

                    // ── 4. Availability banner card ───────────────────────────
                    item {
                        Spacer(Modifier.height(AskFacultySpacing.md))
                        AvailabilityCard(
                            availStyle  = availStyle,
                            onClick     = onAvailability,
                        )
                    }

                    // ── 5. Office Hours card ──────────────────────────────────
                    if (!user?.officeHours.isNullOrEmpty()) {
                        item {
                            Spacer(Modifier.height(AskFacultySpacing.md))
                            OfficeHoursCard(
                                officeHours = user?.officeHours ?: emptyList(),
                            )
                        }
                    }

                    // ── 6. Account & Settings action list ─────────────────────
                    item {
                        Spacer(Modifier.height(AskFacultySpacing.md))
                        AccountSettingsCard(
                            onEditProfile    = onEditProfile,
                            onAvailability   = onAvailability,
                            onSettings       = onSettings,
                            onLogoutRequest  = { showLogoutDialog = true },
                        )
                    }

                    // ── 7. Error banner ───────────────────────────────────────
                    if (uiState.error != null) {
                        item {
                            Spacer(Modifier.height(AskFacultySpacing.sm))
                            ErrorBanner(
                                message   = uiState.error,
                                onDismiss = viewModel::clearError,
                                modifier  = Modifier.padding(horizontal = AskFacultySpacing.lg),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Hero Card ────────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeroCard(
    name:        String,
    designation: String,
    department:  String,
    college:     String,
    email:       String,
    photoUrl:    String,
    availStyle:  AvailabilityStyle,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AskFacultySpacing.lg)
            .padding(top = AskFacultySpacing.lg),
    ) {
        // Gradient background card
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(AskFacultyRadius.xxl),
            elevation = CardDefaults.cardElevation(defaultElevation = AskFacultyDimensions.elevationMedium),
            colors    = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF4F46E5),
                                Color(0xFF7C3AED),
                                Color(0xFF0EA5E9),
                            ),
                        ),
                    )
                    .padding(AskFacultySpacing.xl),
            ) {
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    // Avatar with availability ring
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            AvatarImage(
                                url         = photoUrl,
                                size        = 76.dp,
                                placeholder = name,
                            )
                        }
                        // Availability dot
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(availStyle.color)
                                .then(
                                    Modifier
                                        .clip(CircleShape)
                                        .background(availStyle.color),
                                ),
                        )
                    }

                    Spacer(Modifier.height(AskFacultySpacing.md))

                    // Name
                    Text(
                        text       = name,
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White,
                    )

                    Spacer(Modifier.height(AskFacultySpacing.xs))

                    // Designation badge
                    if (designation.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(AskFacultyRadius.pill),
                            color = Color.White.copy(alpha = 0.20f),
                        ) {
                            Text(
                                text     = designation.uppercase(),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(
                                    horizontal = AskFacultySpacing.md,
                                    vertical   = AskFacultySpacing.xs,
                                ),
                            )
                        }
                        Spacer(Modifier.height(AskFacultySpacing.sm))
                    }

                    // Department
                    if (department.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector        = Icons.Default.School,
                                contentDescription = null,
                                tint               = Color.White.copy(alpha = 0.80f),
                                modifier           = Modifier.size(AskFacultyDimensions.iconSizeSm),
                            )
                            Spacer(Modifier.width(AskFacultySpacing.xs))
                            Text(
                                text  = department,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.90f),
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        Spacer(Modifier.height(AskFacultySpacing.xs))
                    }

                    // College
                    if (college.isNotBlank()) {
                        Text(
                            text  = college,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f),
                        )
                        Spacer(Modifier.height(AskFacultySpacing.xs))
                    }

                    // Email
                    if (email.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Email,
                                contentDescription = null,
                                tint               = Color.White.copy(alpha = 0.65f),
                                modifier           = Modifier.size(AskFacultyDimensions.iconSizeSm),
                            )
                            Spacer(Modifier.width(AskFacultySpacing.xs))
                            Text(
                                text  = email,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.70f),
                            )
                        }
                        Spacer(Modifier.height(AskFacultySpacing.sm))
                    }

                    // Availability pill
                    Surface(
                        shape = RoundedCornerShape(AskFacultyRadius.pill),
                        color = availStyle.color.copy(alpha = 0.20f),
                    ) {
                        Row(
                            modifier          = Modifier.padding(
                                horizontal = AskFacultySpacing.md,
                                vertical   = AskFacultySpacing.xs,
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(availStyle.color),
                            )
                            Spacer(Modifier.width(AskFacultySpacing.xs))
                            Text(
                                text       = availStyle.label,
                                style      = MaterialTheme.typography.labelSmall,
                                color      = Color.White,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Stats Row ────────────────────────────────────────────────────────────────

@Composable
private fun ProfileStatsRow(
    subjectsCount: Int,
    officeSlots:   Int,
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = AskFacultySpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
    ) {
        ProfileStatCard(
            modifier = Modifier.weight(1f),
            value    = "AskFaculty",
            label    = "Platform",
            icon     = Icons.Default.Verified,
        )
        ProfileStatCard(
            modifier = Modifier.weight(1f),
            value    = "$subjectsCount",
            label    = "Subjects",
            icon     = Icons.Default.MenuBook,
        )
        ProfileStatCard(
            modifier = Modifier.weight(1f),
            value    = "$officeSlots",
            label    = "Time Slots",
            icon     = Icons.Default.AccessTime,
        )
    }
}

@Composable
private fun ProfileStatCard(
    modifier: Modifier,
    value:    String,
    label:    String,
    icon:     ImageVector,
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(AskFacultyRadius.lg),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = AskFacultyDimensions.elevationLow,
        ),
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(AskFacultySpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(AskFacultyDimensions.iconSize),
            )
            Spacer(Modifier.height(AskFacultySpacing.xs))
            Text(
                text       = value,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─── Subjects Section ─────────────────────────────────────────────────────────

@Composable
private fun SubjectsSection(subjects: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AskFacultySpacing.lg),
    ) {
        AskFacultySectionHeader(title = "Subjects & Courses")
        Spacer(Modifier.height(AskFacultySpacing.sm))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
        ) {
            items(subjects) { subject ->
                Surface(
                    shape = RoundedCornerShape(AskFacultyRadius.pill),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Row(
                        modifier          = Modifier.padding(
                            horizontal = AskFacultySpacing.md,
                            vertical   = AskFacultySpacing.sm,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Book,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(AskFacultySpacing.xs))
                        Text(
                            text       = subject,
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }
    }
}

// ─── Availability Card ────────────────────────────────────────────────────────

@Composable
private fun AvailabilityCard(
    availStyle: AvailabilityStyle,
    onClick:    () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AskFacultySpacing.lg),
    ) {
        AskFacultySectionHeader(title = "Availability & Status")
        Spacer(Modifier.height(AskFacultySpacing.sm))
        Card(
            onClick   = onClick,
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(AskFacultyRadius.lg),
            colors    = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = AskFacultyDimensions.elevationLow,
            ),
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(AskFacultySpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Coloured icon container
                Box(
                    modifier         = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(AskFacultyRadius.md))
                        .background(availStyle.color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = availStyle.icon,
                        contentDescription = null,
                        tint               = availStyle.color,
                        modifier           = Modifier.size(AskFacultyDimensions.iconSize),
                    )
                }

                Spacer(Modifier.width(AskFacultySpacing.md))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = "Current Availability",
                        style      = MaterialTheme.typography.labelMedium,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(AskFacultySpacing.xs))
                    Text(
                        text       = availStyle.label,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = availStyle.color,
                    )
                }

                Icon(
                    imageVector        = Icons.Default.ChevronRight,
                    contentDescription = "Edit availability",
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ─── Office Hours Card ────────────────────────────────────────────────────────

@Composable
private fun OfficeHoursCard(
    officeHours: List<com.example.askfaculty.domain.model.OfficeHour>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AskFacultySpacing.lg),
    ) {
        AskFacultySectionHeader(title = "Scheduled Office Hours")
        Spacer(Modifier.height(AskFacultySpacing.sm))
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(AskFacultyRadius.lg),
            colors    = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = AskFacultyDimensions.elevationLow,
            ),
        ) {
            Column(modifier = Modifier.padding(vertical = AskFacultySpacing.xs)) {
                officeHours.forEachIndexed { index, slot ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = AskFacultySpacing.lg),
                            color    = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = AskFacultySpacing.lg,
                                vertical   = AskFacultySpacing.md,
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector        = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(AskFacultyDimensions.iconSizeSm),
                            )
                            Spacer(Modifier.width(AskFacultySpacing.sm))
                            Text(
                                text       = slot.day,
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(AskFacultyRadius.sm),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                text     = "${slot.startTime} – ${slot.endTime}",
                                style    = MaterialTheme.typography.labelMedium,
                                color    = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(
                                    horizontal = AskFacultySpacing.sm,
                                    vertical   = AskFacultySpacing.xs,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Account & Settings Card ──────────────────────────────────────────────────

@Composable
private fun AccountSettingsCard(
    onEditProfile:   () -> Unit,
    onAvailability:  () -> Unit,
    onSettings:      () -> Unit,
    onLogoutRequest: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AskFacultySpacing.lg),
    ) {
        AskFacultySectionHeader(title = "Account & App Preferences")
        Spacer(Modifier.height(AskFacultySpacing.sm))
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(AskFacultyRadius.lg),
            colors    = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = AskFacultyDimensions.elevationLow,
            ),
        ) {
            Column {
                SettingsItem(
                    icon        = Icons.Default.Person,
                    iconTint    = MaterialTheme.colorScheme.primary,
                    label       = "Edit Faculty Profile",
                    onClick     = onEditProfile,
                )
                SettingsDivider()
                SettingsItem(
                    icon        = Icons.Default.Schedule,
                    iconTint    = MaterialTheme.colorScheme.primary,
                    label       = "Availability & Office Hours",
                    onClick     = onAvailability,
                )
                SettingsDivider()
                SettingsItem(
                    icon        = Icons.Default.Palette,
                    iconTint    = MaterialTheme.colorScheme.primary,
                    label       = "App Preferences & Appearance",
                    subtitle    = "Dark mode, alerts & notifications",
                    onClick     = onSettings,
                )
                SettingsDivider()
                SettingsItem(
                    icon        = Icons.Default.Logout,
                    iconTint    = MaterialTheme.colorScheme.error,
                    label       = "Sign Out",
                    labelColor  = MaterialTheme.colorScheme.error,
                    showChevron = false,
                    onClick     = onLogoutRequest,
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon:        ImageVector,
    iconTint:    Color,
    label:       String,
    onClick:     () -> Unit,
    modifier:    Modifier = Modifier,
    subtitle:    String? = null,
    labelColor:  Color = MaterialTheme.colorScheme.onSurface,
    showChevron: Boolean = true,
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = AskFacultySpacing.lg,
                vertical   = AskFacultySpacing.md,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon container
        Box(
            modifier         = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(AskFacultyRadius.md))
                .background(iconTint.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = iconTint,
                modifier           = Modifier.size(AskFacultyDimensions.iconSize),
            )
        }

        Spacer(Modifier.width(AskFacultySpacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = label,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = labelColor,
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(AskFacultySpacing.xs))
                Text(
                    text  = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (showChevron) {
            Icon(
                imageVector        = Icons.Default.ChevronRight,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(start = AskFacultySpacing.lg + 40.dp + AskFacultySpacing.md),
        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.60f),
    )
}
