package com.example.askfaculty.presentation.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.askfaculty.core.ui.components.*
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.domain.model.Community
import com.example.askfaculty.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCommunitiesScreen(
    onCommunityClick: (String) -> Unit,
    viewModel: StudentCommunitiesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val subjects = remember(uiState.communities) {
        listOf("All") + Constants.DEFAULT_SUBJECTS
    }

    val filteredCommunities = remember(
        uiState.communities,
        uiState.joinedCommunityIds,
        uiState.selectedTab,
        uiState.selectedSubject,
        uiState.searchQuery,
    ) {
        uiState.communities.filter { community ->
            val matchesTab = if (uiState.selectedTab == 1) {
                uiState.joinedCommunityIds.contains(community.id)
            } else true

            val matchesSubject = if (uiState.selectedSubject == "All") true
            else community.subject.equals(uiState.selectedSubject, ignoreCase = true)

            val matchesSearch = if (uiState.searchQuery.isBlank()) true
            else community.name.contains(uiState.searchQuery, ignoreCase = true) ||
                    community.description.contains(uiState.searchQuery, ignoreCase = true) ||
                    community.subject.contains(uiState.searchQuery, ignoreCase = true)

            matchesTab && matchesSubject && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Academic Spaces",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Collaborate with professors & peers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Search Bar
            AskFacultySearchField(
                query = uiState.searchQuery,
                onQueryChange = viewModel::setSearchQuery,
                placeholder = "Search spaces by name, subject, or topic…",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.sm),
            )

            // Tabs: Explore vs Joined
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = {
                        Text(
                            text = "Explore Spaces (${uiState.communities.size})",
                            fontWeight = if (uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = {
                        Text(
                            text = "Joined Spaces (${uiState.joinedCommunityIds.size})",
                            fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                )
            }

            // Subject Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.xs),
            ) {
                items(subjects) { subject ->
                    val isSelected = uiState.selectedSubject == subject
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectSubject(subject) },
                        label = { Text(subject, style = MaterialTheme.typography.labelMedium) },
                        shape = RoundedCornerShape(AskFacultyRadius.pill),
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    LoadingState(modifier = Modifier.fillMaxSize())
                }

                uiState.error != null -> {
                    ErrorState(
                        message = uiState.error ?: "Unable to load communities",
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                filteredCommunities.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.Groups,
                        title = if (uiState.selectedTab == 1) "No joined spaces yet" else "No spaces found",
                        subtitle = if (uiState.selectedTab == 1) "Explore academic communities and join to ask questions and share resources."
                        else "Try a different search query or subject filter.",
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = AskFacultySpacing.lg,
                            end = AskFacultySpacing.lg,
                            top = AskFacultySpacing.sm,
                            bottom = AskFacultySpacing.xxl,
                        ),
                        verticalArrangement = Arrangement.spacedBy(AskFacultySpacing.md),
                    ) {
                        items(filteredCommunities, key = { it.id }) { community ->
                            val isJoined = uiState.joinedCommunityIds.contains(community.id)
                            StudentCommunityCard(
                                community = community,
                                isJoined = isJoined,
                                onToggleJoin = { viewModel.toggleJoinCommunity(community) },
                                onClick = { onCommunityClick(community.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentCommunityCard(
    community: Community,
    isJoined: Boolean,
    onToggleJoin: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AskFacultyRadius.xl),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AskFacultyElevation.low),
    ) {
        Column(modifier = Modifier.padding(AskFacultySpacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.md),
            ) {
                // Space icon banner
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(AskFacultyRadius.md))
                        .background(
                            Brush.linearGradient(listOf(GradientStart, GradientEnd))
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = community.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = community.subject,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                // Join / Leave Button
                Button(
                    onClick = onToggleJoin,
                    colors = if (isJoined) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        )
                    },
                    shape = RoundedCornerShape(AskFacultyRadius.pill),
                    contentPadding = PaddingValues(horizontal = AskFacultySpacing.md, vertical = 4.dp),
                ) {
                    Text(
                        text = if (isJoined) "Joined ✓" else "Join Space",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            if (community.description.isNotBlank()) {
                Spacer(Modifier.height(AskFacultySpacing.sm))
                Text(
                    text = community.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(AskFacultySpacing.md))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(AskFacultySpacing.sm))

            // Footer stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.xs),
                    ) {
                        Icon(
                            imageVector = Icons.Default.PeopleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "${community.memberCount} members",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.xs),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "${community.postCount} posts",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Text(
                    text = "Lead: Prof. ${community.facultyName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
