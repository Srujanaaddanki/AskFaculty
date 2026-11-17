package com.example.askfaculty.presentation.student

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.askfaculty.core.ui.components.AskFacultyButton
import com.example.askfaculty.core.ui.components.AskFacultySearchBar
import com.example.askfaculty.core.ui.components.AskFacultySkeleton
import com.example.askfaculty.core.ui.components.DoubtCard
import com.example.askfaculty.core.ui.components.EmptyState
import com.example.askfaculty.core.ui.components.ErrorState
import com.example.askfaculty.ui.theme.AskFacultyRadius
import com.example.askfaculty.ui.theme.AskFacultySpacing

private val studentTabLabels = listOf("All", "Pending", "Answered", "Resolved")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDoubtsScreen(
    onDoubtClick: (String) -> Unit,
    onAskDoubtClick: () -> Unit,
    viewModel: StudentDoubtsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }

    val hasActiveFilters = uiState.selectedPriority != null || uiState.selectedSubject != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "My Doubts",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        if (!uiState.isLoading && uiState.doubts.isNotEmpty()) {
                            Spacer(Modifier.width(AskFacultySpacing.sm))
                            Surface(
                                shape = RoundedCornerShape(AskFacultyRadius.pill),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Text(
                                    text = "${uiState.doubts.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = AskFacultySpacing.sm, vertical = AskFacultySpacing.xs),
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAskDoubtClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Ask Doubt", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Search Bar & Filter Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AskFacultySearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::setSearchQuery,
                    onSearch = {},
                    placeholder = "Search my doubts by topic or subject...",
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(AskFacultySpacing.sm))
                IconButton(onClick = { showFilterSheet = true }) {
                    BadgedBox(
                        badge = {
                            if (hasActiveFilters) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary)
                            }
                        }
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            }

            // Status Tabs
            PrimaryScrollableTabRow(
                selectedTabIndex = uiState.selectedTab,
                edgePadding = AskFacultySpacing.lg,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) },
            ) {
                studentTabLabels.forEachIndexed { index, label ->
                    val isSelected = uiState.selectedTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(index) },
                        text = {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    )
                }
            }

            // Active Filter Chips
            AnimatedVisibility(visible = hasActiveFilters) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    uiState.selectedSubject?.let { sub ->
                        item {
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.setSubjectFilter(null) },
                                label = { Text("Subject: $sub") },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            )
                        }
                    }
                    uiState.selectedPriority?.let { pri ->
                        item {
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.setPriorityFilter(null) },
                                label = { Text("Priority: $pri") },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            )
                        }
                    }
                    item {
                        TextButton(onClick = viewModel::clearFilters) {
                            Text("Clear all", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Main List Content
            AnimatedContent(
                targetState = Triple(uiState.isLoading, uiState.error, uiState.doubts.isEmpty()),
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                label = "student_doubts_content",
                modifier = Modifier.fillMaxSize(),
            ) { (isLoading, error, isEmpty) ->
                when {
                    isLoading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.md),
                            verticalArrangement = Arrangement.spacedBy(AskFacultySpacing.md),
                        ) {
                            repeat(3) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    shape = RoundedCornerShape(AskFacultyRadius.lg),
                                    color = MaterialTheme.colorScheme.surface,
                                ) {
                                    AskFacultySkeleton(modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                    error != null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            ErrorState(message = error, onRetry = viewModel::loadDoubts)
                        }
                    }
                    isEmpty -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyState(
                                icon = Icons.Default.QuestionAnswer,
                                title = when (uiState.selectedTab) {
                                    1 -> "No Pending Doubts"
                                    2 -> "No Answered Doubts Yet"
                                    3 -> "No Resolved Doubts"
                                    else -> "No Doubts Found"
                                },
                                subtitle = when (uiState.selectedTab) {
                                    1 -> "You don't have any doubts awaiting faculty responses."
                                    2 -> "Answers from professors will show up here."
                                    3 -> "Doubts you mark as resolved will be saved here."
                                    else -> "Submit your first academic doubt to connect with professors."
                                },
                                actionLabel = "Ask a Doubt Now",
                                onAction = onAskDoubtClick,
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = AskFacultySpacing.lg,
                                end = AskFacultySpacing.lg,
                                top = AskFacultySpacing.sm,
                                bottom = 88.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
                        ) {
                            items(uiState.doubts, key = { it.id }) { doubt ->
                                DoubtCard(
                                    doubt = doubt,
                                    onClick = { onDoubtClick(doubt.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AskFacultySpacing.xl, vertical = AskFacultySpacing.lg),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Filter Doubts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { viewModel.clearFilters(); showFilterSheet = false }) {
                        Text("Reset", color = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(Modifier.height(AskFacultySpacing.md))
                Text("Priority Level", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm)) {
                    listOf("HIGH", "MEDIUM", "LOW").forEach { pri ->
                        FilterChip(
                            selected = uiState.selectedPriority == pri,
                            onClick = {
                                viewModel.setPriorityFilter(if (uiState.selectedPriority == pri) null else pri)
                            },
                            label = { Text(pri) },
                        )
                    }
                }

                Spacer(Modifier.height(AskFacultySpacing.lg))
                AskFacultyButton(
                    text = "Apply Filters",
                    onClick = { showFilterSheet = false },
                )
                Spacer(Modifier.height(AskFacultySpacing.xl))
            }
        }
    }
}
