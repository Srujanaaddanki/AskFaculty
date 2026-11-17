package com.example.askfaculty.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.askfaculty.core.ui.components.AskFacultyAvatar
import com.example.askfaculty.core.ui.components.AskFacultyButton
import com.example.askfaculty.core.ui.components.DoubtCard
import com.example.askfaculty.core.ui.components.ErrorState
import com.example.askfaculty.core.ui.components.LoadingState
import com.example.askfaculty.core.ui.components.StatCard
import com.example.askfaculty.core.util.greetingForTime
import com.example.askfaculty.core.util.toRelativeTime
import com.example.askfaculty.domain.model.Community
import com.example.askfaculty.domain.model.Post
import com.example.askfaculty.ui.theme.AskFacultyRadius
import com.example.askfaculty.ui.theme.AskFacultySpacing
import com.example.askfaculty.ui.theme.StatusAnswered
import com.example.askfaculty.ui.theme.StatusPending
import com.example.askfaculty.ui.theme.StatusResolved
import com.example.askfaculty.ui.theme.SuccessColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    onAskDoubtClick: () -> Unit,
    onDoubtClick: (String) -> Unit,
    onCommunityClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onViewAllDoubts: () -> Unit,
    onViewAllCommunities: () -> Unit,
    onViewResources: () -> Unit,
    viewModel: StudentHomeViewModel = hiltViewModel(),
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
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "STUDENT PORTAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.8.sp,
                                fontWeight = FontWeight.SemiBold,
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
                        name = uiState.user?.name ?: "Student",
                        size = 34.dp,
                        modifier = Modifier.padding(end = AskFacultySpacing.md),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAskDoubtClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Ask a Doubt", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
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
                message = uiState.error ?: "Unable to load student dashboard",
                onRetry = viewModel::refresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = 88.dp),
                ) {
                    // ── 1. Personalized Student Welcome Banner ──────────
                    item {
                        val studentName = uiState.user?.name?.ifBlank { "Student" } ?: "Student"
                        val rollNo = uiState.user?.studentId?.ifBlank { "22CS104" } ?: "22CS104"
                        val department = uiState.user?.department?.ifBlank { "Computer Science & Engineering" } ?: "Computer Science & Engineering"
                        val year = uiState.user?.academicYear?.ifBlank { "3rd Year" } ?: "3rd Year"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.md),
                            shape = RoundedCornerShape(AskFacultyRadius.xl),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
                                            )
                                        )
                                    )
                                    .padding(AskFacultySpacing.xl),
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(AskFacultyRadius.xs),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        ) {
                                            Text(
                                                text = "ROLL NO: $rollNo • $year",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            )
                                        }

                                        AskFacultyAvatar(
                                            photoUrl = uiState.user?.photoUrl,
                                            name = studentName,
                                            size = 44.dp,
                                        )
                                    }

                                    Spacer(Modifier.height(AskFacultySpacing.sm))

                                    Text(
                                        text = "${greetingForTime()}, $studentName",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )

                                    Spacer(Modifier.height(AskFacultySpacing.xs))

                                    Text(
                                        text = department,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )

                                    Spacer(Modifier.height(AskFacultySpacing.md))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
                                    ) {
                                        AskFacultyButton(
                                            text = "Ask a Doubt Now",
                                            onClick = onAskDoubtClick,
                                            leadingIcon = Icons.Default.Add,
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── 2. Real-Time Stats Overview ────────────────────
                    item {
                        Column(modifier = Modifier.padding(horizontal = AskFacultySpacing.lg)) {
                            Text(
                                text = "My Doubt Resolution Stats",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(AskFacultySpacing.md))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.md),
                            ) {
                                StatCard(
                                    title = "Pending",
                                    value = uiState.stats.pendingDoubts.toString(),
                                    icon = Icons.Default.HourglassEmpty,
                                    modifier = Modifier.weight(1f),
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                )
                                StatCard(
                                    title = "Answered",
                                    value = uiState.stats.answeredDoubts.toString(),
                                    icon = Icons.Default.CheckCircle,
                                    modifier = Modifier.weight(1f),
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                            Spacer(Modifier.height(AskFacultySpacing.md))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.md),
                            ) {
                                StatCard(
                                    title = "Resolved",
                                    value = uiState.stats.resolvedDoubts.toString(),
                                    icon = Icons.Default.Verified,
                                    modifier = Modifier.weight(1f),
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                                StatCard(
                                    title = "Joined Spaces",
                                    value = uiState.stats.joinedCommunities.toString(),
                                    icon = Icons.Default.Groups,
                                    modifier = Modifier.weight(1f),
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                            }
                        }
                    }

                    item { Spacer(Modifier.height(AskFacultySpacing.xl)) }

                    // ── 3. My Subjects Horizontal Carousel ─────────────
                    item {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AskFacultySpacing.lg),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "My Enrolled Subjects",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                TextButton(onClick = onViewResources) {
                                    Text("View Notes")
                                }
                            }
                            Spacer(Modifier.height(AskFacultySpacing.xs))
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = AskFacultySpacing.lg),
                                horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.md),
                            ) {
                                items(uiState.mySubjects) { subject ->
                                    SubjectPillCard(
                                        subject = subject,
                                        onClick = { onAskDoubtClick() },
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(AskFacultySpacing.xl)) }

                    // ── 4. Faculty Announcements ───────────────────────
                    if (uiState.facultyAnnouncements.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = AskFacultySpacing.lg)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Campaign,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                        Spacer(Modifier.width(AskFacultySpacing.xs))
                                        Text(
                                            text = "Faculty Announcements",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                                Spacer(Modifier.height(AskFacultySpacing.sm))
                            }
                        }
                        items(uiState.facultyAnnouncements) { post ->
                            AnnouncementCard(
                                post = post,
                                onClick = { onCommunityClick(post.communityId) },
                                modifier = Modifier.padding(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.xs),
                            )
                        }
                        item { Spacer(Modifier.height(AskFacultySpacing.xl)) }
                    }

                    // ── 5. Recently Answered Questions ────────────────
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AskFacultySpacing.lg),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Recently Answered Doubts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            TextButton(onClick = onViewAllDoubts) {
                                Text("My Doubts")
                            }
                        }
                        Spacer(Modifier.height(AskFacultySpacing.xs))
                    }

                    if (uiState.recentAnsweredDoubts.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AskFacultySpacing.lg),
                                shape = RoundedCornerShape(AskFacultyRadius.lg),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            ) {
                                Column(
                                    modifier = Modifier.padding(AskFacultySpacing.lg),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text("No answered doubts yet.", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(AskFacultySpacing.xs))
                                    Text(
                                        "When faculty replies to questions, they will appear here.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    } else {
                        items(uiState.recentAnsweredDoubts) { doubt ->
                            DoubtCard(
                                doubt = doubt,
                                onClick = { onDoubtClick(doubt.id) },
                                modifier = Modifier.padding(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.xs),
                            )
                        }
                    }

                    item { Spacer(Modifier.height(AskFacultySpacing.xl)) }

                    // ── 6. Joined Communities & Spaces ────────────────
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AskFacultySpacing.lg),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Joined Academic Spaces",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            TextButton(onClick = onViewAllCommunities) {
                                Text("Browse All")
                            }
                        }
                        Spacer(Modifier.height(AskFacultySpacing.xs))
                    }

                    items(uiState.joinedCommunities) { comm ->
                        CommunityPreviewCard(
                            community = comm,
                            onClick = { onCommunityClick(comm.id) },
                            modifier = Modifier.padding(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.xs),
                        )
                    }

                    item { Spacer(Modifier.height(AskFacultySpacing.xl)) }

                    // ── 7. Trending Discussions ────────────────────────
                    if (uiState.trendingDiscussions.isNotEmpty()) {
                        item {
                            Text(
                                text = "Trending Discussions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = AskFacultySpacing.lg),
                            )
                            Spacer(Modifier.height(AskFacultySpacing.sm))
                        }
                        items(uiState.trendingDiscussions) { post ->
                            TrendingDiscussionCard(
                                post = post,
                                onClick = { onCommunityClick(post.communityId) },
                                modifier = Modifier.padding(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.xs),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectPillCard(
    subject: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AskFacultyRadius.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        ),
    ) {
        Column(modifier = Modifier.padding(AskFacultySpacing.md)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(AskFacultyRadius.sm))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.height(AskFacultySpacing.sm))
            Text(
                text = subject,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Ask question →",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun AnnouncementCard(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AskFacultyRadius.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        ),
    ) {
        Column(modifier = Modifier.padding(AskFacultySpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(AskFacultyRadius.xs),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = "PINNED ANNOUNCEMENT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                Text(
                    text = post.createdAt?.toRelativeTime() ?: "Recent",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(AskFacultySpacing.sm))
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(AskFacultySpacing.xs))
            Text(
                text = "By ${post.authorName}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun CommunityPreviewCard(
    community: Community,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AskFacultyRadius.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AskFacultySpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(AskFacultyRadius.md))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Groups,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Spacer(Modifier.width(AskFacultySpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = community.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${community.facultyName} • ${community.memberCount} members",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TrendingDiscussionCard(
    post: Post,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AskFacultyRadius.md),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AskFacultySpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${post.authorName} • ${post.replyCount} replies",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Surface(
                shape = RoundedCornerShape(AskFacultyRadius.sm),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = "👍 ${post.helpfulCount}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
    }
}
