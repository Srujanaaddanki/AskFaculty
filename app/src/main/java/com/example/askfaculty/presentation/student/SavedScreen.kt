package com.example.askfaculty.presentation.student

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.askfaculty.core.ui.components.*
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.core.util.toRelativeTime
import com.example.askfaculty.domain.model.Doubt
import com.example.askfaculty.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    onDoubtClick: (String) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: SavedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val filteredDoubts = remember(uiState.bookmarkedDoubts, uiState.selectedFilter, uiState.searchQuery) {
        uiState.bookmarkedDoubts.filter { doubt ->
            val matchesFilter = when (uiState.selectedFilter) {
                "Resolved" -> doubt.status == Constants.STATUS_RESOLVED
                "Answered" -> doubt.status == Constants.STATUS_ANSWERED
                "Pending" -> doubt.status == Constants.STATUS_PENDING
                else -> true
            }

            val matchesSearch = if (uiState.searchQuery.isBlank()) true
            else doubt.question.contains(uiState.searchQuery, ignoreCase = true) ||
                    doubt.subject.contains(uiState.searchQuery, ignoreCase = true) ||
                    doubt.topic.contains(uiState.searchQuery, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Saved & Bookmarks",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${uiState.bookmarkedDoubts.size} saved academic discussions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
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
            AskFacultySearchField(
                query = uiState.searchQuery,
                onQueryChange = viewModel::setSearchQuery,
                placeholder = "Search saved discussions…",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.sm),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.xs),
            ) {
                listOf("All", "Answered", "Resolved", "Pending").forEach { filter ->
                    val isSelected = uiState.selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filter, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(AskFacultyRadius.pill),
                    )
                }
            }

            when {
                uiState.isLoading -> LoadingState(modifier = Modifier.fillMaxSize())

                uiState.error != null -> ErrorState(
                    message = uiState.error ?: "Unable to load saved bookmarks",
                    modifier = Modifier.fillMaxSize(),
                )

                filteredDoubts.isEmpty() -> EmptyState(
                    icon = Icons.Default.BookmarkBorder,
                    title = "No saved bookmarks",
                    subtitle = "Tap the bookmark icon on any doubt or discussion thread to save it here for fast offline revision.",
                    modifier = Modifier.fillMaxSize(),
                )

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
                        items(filteredDoubts, key = { it.id }) { doubt ->
                            SavedDoubtCard(
                                doubt = doubt,
                                onRemove = { viewModel.removeBookmark(doubt.id) },
                                onClick = { onDoubtClick(doubt.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedDoubtCard(
    doubt: Doubt,
    onRemove: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AskFacultyRadius.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AskFacultyElevation.low),
    ) {
        Column(modifier = Modifier.padding(AskFacultySpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(AskFacultyRadius.sm),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                ) {
                    Text(
                        text = doubt.subject,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = AskFacultySpacing.sm, vertical = 2.dp),
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(status = doubt.status)
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Remove bookmark",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(AskFacultySpacing.xs))

            Text(
                text = doubt.topic.ifBlank { "Academic Question" },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = doubt.question,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(AskFacultySpacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Faculty: Prof. ${doubt.facultyName.ifBlank { "Assigned" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = doubt.createdAt?.toRelativeTime() ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
