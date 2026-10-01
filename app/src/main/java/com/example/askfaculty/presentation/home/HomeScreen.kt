package com.example.askfaculty.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.askfaculty.core.ui.components.AskFacultyAvatar
import com.example.askfaculty.core.ui.components.DoubtCard
import com.example.askfaculty.core.ui.components.ErrorState
import com.example.askfaculty.core.ui.components.LoadingState
import com.example.askfaculty.core.ui.components.QuickActionCard
import com.example.askfaculty.core.ui.components.StatCard
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.core.util.greetingForTime
import com.example.askfaculty.ui.theme.AskFacultyRadius
import com.example.askfaculty.ui.theme.AskFacultySpacing
import com.example.askfaculty.ui.theme.StatusAnswered
import com.example.askfaculty.ui.theme.StatusPending
import com.example.askfaculty.ui.theme.SuccessColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onDoubtClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onNavigateToDoubts: () -> Unit = {},
    onCreateCommunityClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(AskFacultyRadius.sm))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary,
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(Modifier.width(AskFacultySpacing.sm))
                        Column {
                            Text(
                                "AskFaculty",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                "FACULTY PORTAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.8.sp,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = onNotificationsClick) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    AskFacultyAvatar(
                        photoUrl = uiState.user?.photoUrl,
                        name     = uiState.user?.name ?: "Professor",
                        size     = 34.dp,
                        availabilityColor = if (uiState.user?.availability == Constants.AVAILABILITY_AVAILABLE)
                            StatusAnswered else StatusPending,
                        modifier = Modifier.padding(end = AskFacultySpacing.md),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        when {
            uiState.isLoading -> LoadingState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
            uiState.error != null -> ErrorState(
                message = uiState.error ?: "Unable to load faculty dashboard",
                onRetry = viewModel::refresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
            else -> {
                LazyColumn(
                    modifier       = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = AskFacultySpacing.xl),
                ) {
                    // ── Faculty Profile Banner ──────────────────────────
                    item {
                        val facultyName = uiState.user?.name?.ifBlank { "Professor" } ?: "Professor"
                        val department  = uiState.user?.department?.ifBlank { "Academic Faculty" } ?: "Academic Faculty"
                        val designation = uiState.user?.designation?.ifBlank { "Faculty Member" } ?: "Faculty Member"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.md),
                            shape  = RoundedCornerShape(AskFacultyRadius.xl),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            elevation = CardDefaults.cardElevation(0.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                                            )
                                        )
                                    )
                                    .padding(AskFacultySpacing.xl),
                            ) {
                                Row(
                                    modifier          = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        // Designation badge
                                        Surface(
                                            shape = RoundedCornerShape(AskFacultyRadius.xs),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        ) {
                                            Text(
                                                text          = designation.uppercase(),
                                                style         = MaterialTheme.typography.labelSmall,
                                                fontWeight    = FontWeight.Bold,
                                                color         = MaterialTheme.colorScheme.primary,
                                                letterSpacing = 0.8.sp,
                                                modifier      = Modifier.padding(horizontal = AskFacultySpacing.sm, vertical = 3.dp),
                                            )
                                        }
                                        Spacer(Modifier.height(AskFacultySpacing.sm))
                                        Text(
                                            text       = "${greetingForTime()}, $facultyName",
                                            style      = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color      = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Spacer(Modifier.height(AskFacultySpacing.xs))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.MenuBook,
                                                contentDescription = null,
                                                tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(14.dp),
                                            )
                                            Spacer(Modifier.width(AskFacultySpacing.xs))
                                            Text(
                                                text  = department,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }

                                    // Availability indicator
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        AskFacultyAvatar(
                                            photoUrl = uiState.user?.photoUrl,
                                            name     = facultyName,
                                            size     = 52.dp,
                                            availabilityColor = if (uiState.user?.availability == Constants.AVAILABILITY_AVAILABLE)
                                                SuccessColor else StatusPending,
                                        )
                                        Spacer(Modifier.height(AskFacultySpacing.xs))
                                        Text(
                                            text  = if (uiState.user?.availability == Constants.AVAILABILITY_AVAILABLE) "Available" else "Busy",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (uiState.user?.availability == Constants.AVAILABILITY_AVAILABLE)
                                                SuccessColor else StatusPending,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── Stats Grid ─────────────────────────────────────
                    item {
                        Column(modifier = Modifier.padding(horizontal = AskFacultySpacing.lg)) {
                            Text(
                                text       = "Dashboard Overview",
                                style      = MaterialTheme.typography.titleMedium,
                                color      = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(AskFacultySpacing.md))
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.md),
                            ) {
                                StatCard(
                                    title          = "Pending",
                                    value          = uiState.stats.pendingDoubts.toString(),
                                    icon           = Icons.Default.HourglassEmpty,
                                    modifier       = Modifier.weight(1f),
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                                    contentColor   = MaterialTheme.colorScheme.onErrorContainer,
                                )
                                StatCard(
                                    title          = "Unanswered",
                                    value          = uiState.stats.unansweredDoubts.toString(),
                                    icon           = Icons.Outlined.QuestionAnswer,
                                    modifier       = Modifier.weight(1f),
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                                    contentColor   = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                            }
                            Spacer(Modifier.height(AskFacultySpacing.md))
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.md),
                            ) {
                                StatCard(
                                    title          = "Answered Today",
                                    value          = uiState.stats.answeredToday.toString(),
                                    icon           = Icons.Default.CheckCircle,
                                    modifier       = Modifier.weight(1f),
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                    contentColor   = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                                StatCard(
                                    title          = "Communities",
                                    value          = uiState.stats.activeCommunities.toString(),
                                    icon           = Icons.Default.Groups,
                                    modifier       = Modifier.weight(1f),
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    contentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }

                    item { Spacer(Modifier.height(AskFacultySpacing.xl)) }

                    // ── Quick Actions ──────────────────────────────────
                    item {
                        Column(modifier = Modifier.padding(horizontal = AskFacultySpacing.lg)) {
                            Text(
                                text  = "Quick Actions",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(AskFacultySpacing.md))
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
                            ) {
                                QuickActionCard(
                                    title    = "Answer Doubts",
                                    subtitle = "Review queue",
                                    icon     = Icons.Outlined.QuestionAnswer,
                                    onClick  = onNavigateToDoubts,
                                    modifier = Modifier.weight(1f),
                                )
                                QuickActionCard(
                                    title    = "New Space",
                                    subtitle = "Create group",
                                    icon     = Icons.Outlined.GroupAdd,
                                    onClick  = onCreateCommunityClick,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }

                    item { Spacer(Modifier.height(AskFacultySpacing.xl)) }

                    // ── Weekly Activity Chart ─────────────────────────
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AskFacultySpacing.lg),
                            shape     = RoundedCornerShape(AskFacultyRadius.lg),
                            colors    = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            ),
                            elevation = CardDefaults.cardElevation(0.dp),
                            border    = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    MaterialTheme.colorScheme.outlineVariant,
                                )
                            ),
                        ) {
                            Column(modifier = Modifier.padding(AskFacultySpacing.lg)) {
                                Row(
                                    modifier              = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.TrendingUp,
                                            contentDescription = null,
                                            tint     = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Spacer(Modifier.width(AskFacultySpacing.sm))
                                        Text(
                                            text       = "Weekly Doubt Resolution",
                                            style      = MaterialTheme.typography.titleSmall,
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(AskFacultyRadius.xs),
                                        color = SuccessColor.copy(alpha = 0.12f),
                                    ) {
                                        Text(
                                            text       = "94% Resolved",
                                            style      = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color      = SuccessColor,
                                            modifier   = Modifier.padding(horizontal = AskFacultySpacing.sm, vertical = 3.dp),
                                        )
                                    }
                                }

                                Spacer(Modifier.height(AskFacultySpacing.lg))

                                val days    = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                                val heights = listOf(0.6f, 0.85f, 0.45f, 0.95f, 0.7f, 0.3f, 0.4f)

                                Row(
                                    modifier              = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.Bottom,
                                ) {
                                    days.forEachIndexed { index, day ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier            = Modifier.weight(1f),
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(16.dp)
                                                    .fillMaxHeight(heights[index])
                                                    .clip(RoundedCornerShape(topStart = AskFacultyRadius.xs, topEnd = AskFacultyRadius.xs))
                                                    .background(
                                                        if (index == 3)
                                                            Brush.verticalGradient(
                                                                listOf(
                                                                    MaterialTheme.colorScheme.primary,
                                                                    MaterialTheme.colorScheme.secondary,
                                                                )
                                                            )
                                                        else
                                                            Brush.verticalGradient(
                                                                listOf(
                                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                                )
                                                            )
                                                    ),
                                            )
                                            Spacer(Modifier.height(AskFacultySpacing.xs))
                                            Text(
                                                text  = day,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (index == 3)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = if (index == 3) FontWeight.Bold else FontWeight.Normal,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(AskFacultySpacing.xl)) }

                    // ── Priority Doubts ────────────────────────────────
                    item {
                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AskFacultySpacing.lg),
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text(
                                text     = "Needs Your Attention",
                                style    = MaterialTheme.typography.titleMedium,
                                color    = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            if (uiState.priorityDoubts.isNotEmpty()) {
                                TextButton(onClick = onNavigateToDoubts) {
                                    Text(
                                        text  = "View All",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(AskFacultySpacing.sm))
                    }

                    if (uiState.priorityDoubts.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AskFacultySpacing.lg),
                                shape     = RoundedCornerShape(AskFacultyRadius.lg),
                                colors    = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                ),
                                elevation = CardDefaults.cardElevation(0.dp),
                            ) {
                                Row(
                                    modifier          = Modifier.padding(AskFacultySpacing.xl),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(SuccessColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint     = SuccessColor,
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }
                                    Spacer(Modifier.width(AskFacultySpacing.lg))
                                    Column {
                                        Text(
                                            text       = "All Caught Up!",
                                            style      = MaterialTheme.typography.titleSmall,
                                        )
                                        Text(
                                            text  = "No urgent doubts are waiting right now.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        items(uiState.priorityDoubts) { doubt ->
                            DoubtCard(
                                doubt    = doubt,
                                onClick  = { onDoubtClick(doubt.id) },
                                modifier = Modifier.padding(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.xs),
                            )
                        }
                    }

                    item { Spacer(Modifier.height(AskFacultySpacing.xl)) }

                    // ── Frequent Topics ─────────────────────────────────
                    if (uiState.frequentTopics.isNotEmpty()) {
                        item {
                            Text(
                                text     = "Frequently Asked Topics",
                                style    = MaterialTheme.typography.titleMedium,
                                color    = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = AskFacultySpacing.lg),
                            )
                            Spacer(Modifier.height(AskFacultySpacing.sm))
                        }
                        items(uiState.frequentTopics) { topic ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.xs)
                                    .clickable {},
                                shape     = RoundedCornerShape(AskFacultyRadius.md),
                                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(0.dp),
                                border    = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        MaterialTheme.colorScheme.outlineVariant,
                                    )
                                ),
                            ) {
                                Row(
                                    modifier              = Modifier
                                        .fillMaxWidth()
                                        .padding(AskFacultySpacing.md),
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier          = Modifier.weight(1f),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(AskFacultyRadius.sm))
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.TrendingUp,
                                                contentDescription = null,
                                                tint     = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                        Spacer(Modifier.width(AskFacultySpacing.md))
                                        Text(
                                            text       = topic.topic,
                                            style      = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color      = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(AskFacultyRadius.sm),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                    ) {
                                        Text(
                                            text       = "${topic.doubtCount} doubts",
                                            style      = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color      = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier   = Modifier.padding(horizontal = AskFacultySpacing.sm, vertical = AskFacultySpacing.xs),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
