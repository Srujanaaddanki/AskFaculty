package com.example.askfaculty.presentation.doubts

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.askfaculty.core.ui.components.AskFacultySearchBar
import com.example.askfaculty.core.ui.components.AskFacultySkeleton
import com.example.askfaculty.core.ui.components.DoubtCard
import com.example.askfaculty.core.ui.components.EmptyState
import com.example.askfaculty.core.ui.components.ErrorState
import com.example.askfaculty.core.ui.components.PrimaryButton
import com.example.askfaculty.ui.theme.AskFacultyRadius
import com.example.askfaculty.ui.theme.AskFacultySpacing

// ─────────────────────────────────────────────────────────────────────────────
// Tab definitions (index-aligned with ViewModel's selectTab() mapping)
// ─────────────────────────────────────────────────────────────────────────────
private val kTabLabels = listOf("All", "Pending", "In Progress", "Answered", "Resolved")

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoubtsScreen(
    onDoubtClick: (String) -> Unit,
    viewModel: DoubtsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }

    val hasActiveFilters = uiState.selectedPriority != null || uiState.selectedSubject != null

    // Per-tab badge counts derived from the full list (not filtered list so badges stay
    // meaningful when filters are active).
    val tabBadgeCounts = remember(uiState.doubts, uiState.selectedTab) {
        // Counts are only shown for the "All" tab to give a global overview; individual
        // status tabs already show exactly those doubts, so a badge would be redundant.
        listOf(uiState.doubts.size, 0, 0, 0, 0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Doubts Repository",
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
                                    modifier = Modifier.padding(
                                        horizontal = AskFacultySpacing.sm,
                                        vertical = AskFacultySpacing.xs,
                                    ),
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showFilterSheet = true },
                icon = {
                    BadgedBox(
                        badge = {
                            if (hasActiveFilters) {
                                Badge(containerColor = MaterialTheme.colorScheme.error)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter doubts",
                        )
                    }
                },
                text = {
                    Text(
                        text = if (hasActiveFilters) "Filters Active" else "Filter",
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                containerColor = if (hasActiveFilters)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surface,
                contentColor = if (hasActiveFilters)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.primary,
            )
        },
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {

            // ── Search bar ────────────────────────────────────────────────────
            AskFacultySearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::setSearchQuery,
                onSearch = {},
                placeholder = "Search by student, subject, topic…",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = AskFacultySpacing.lg,
                        vertical = AskFacultySpacing.sm,
                    ),
            )

            // ── Scrollable tab row with badge counts ──────────────────────────
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTab,
                edgePadding = AskFacultySpacing.lg,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    if (uiState.selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[uiState.selectedTab])
                                .clip(
                                    RoundedCornerShape(
                                        topStart = AskFacultyRadius.xs,
                                        topEnd = AskFacultyRadius.xs,
                                    )
                                ),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                divider = {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                },
            ) {
                kTabLabels.forEachIndexed { index, label ->
                    val isSelected = uiState.selectedTab == index
                    val badgeCount = tabBadgeCounts[index]

                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(index) },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(
                                horizontal = AskFacultySpacing.sm,
                                vertical = AskFacultySpacing.md,
                            ),
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            )
                            if (badgeCount > 0 && !uiState.isLoading) {
                                Spacer(Modifier.width(AskFacultySpacing.xs))
                                Surface(
                                    shape = RoundedCornerShape(AskFacultyRadius.pill),
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                ) {
                                    Text(
                                        text = "$badgeCount",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(
                                            horizontal = AskFacultySpacing.xs,
                                            vertical = 2.dp,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Active filter pills ───────────────────────────────────────────
            AnimatedVisibility(
                visible = uiState.selectedSubject != null || uiState.selectedPriority != null,
                enter = expandVertically(tween(200)) + fadeIn(tween(200)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(200)),
            ) {
                LazyRow(
                    contentPadding = PaddingValues(
                        horizontal = AskFacultySpacing.lg,
                        vertical = AskFacultySpacing.sm,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    uiState.selectedSubject?.let { subject ->
                        item {
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.setSubjectFilter(null) },
                                label = { Text("Subject: $subject") },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove subject filter",
                                        modifier = Modifier.size(14.dp),
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                            )
                        }
                    }
                    uiState.selectedPriority?.let { priority ->
                        item {
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.setPriorityFilter(null) },
                                label = { Text("Priority: ${priority.lowercase().replaceFirstChar { it.uppercaseChar() }}") },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove priority filter",
                                        modifier = Modifier.size(14.dp),
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    selectedTrailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                ),
                            )
                        }
                    }
                    item {
                        TextButton(
                            onClick = viewModel::clearFilters,
                            contentPadding = PaddingValues(horizontal = AskFacultySpacing.sm),
                        ) {
                            Text(
                                text = "Clear all",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }

            // ── Main content — loading / error / empty / list ─────────────────
            AnimatedContent(
                targetState = Triple(uiState.isLoading, uiState.error, uiState.doubts.isEmpty()),
                transitionSpec = {
                    fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                },
                label = "doubts_content",
                modifier = Modifier.fillMaxSize(),
            ) { (isLoading, error, isEmpty) ->
                when {
                    // Loading — 3 skeleton cards
                    isLoading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    horizontal = AskFacultySpacing.lg,
                                    vertical = AskFacultySpacing.md,
                                ),
                            verticalArrangement = Arrangement.spacedBy(AskFacultySpacing.md),
                        ) {
                            repeat(3) {
                                SkeletonDoubtCard()
                            }
                        }
                    }

                    // Error state
                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            ErrorState(
                                message = error,
                                onRetry = { viewModel.selectTab(uiState.selectedTab) },
                            )
                        }
                    }

                    // Empty state
                    isEmpty -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            EmptyState(
                                icon = Icons.Default.QuestionAnswer,
                                title = when (uiState.selectedTab) {
                                    1 -> "No Pending Doubts"
                                    2 -> "No Doubts In Progress"
                                    3 -> "No Answered Doubts Yet"
                                    4 -> "No Resolved Doubts"
                                    else -> "No Doubts Found"
                                },
                                subtitle = when (uiState.selectedTab) {
                                    1 -> "All caught up! There are no pending questions from students."
                                    2 -> "No doubts are currently marked as in progress."
                                    3 -> "Doubts you respond to will appear here."
                                    4 -> "Completed and resolved doubts are archived here."
                                    else -> "Try adjusting your search keywords or active filters."
                                },
                                actionLabel = if (uiState.selectedSubject != null ||
                                    uiState.selectedPriority != null ||
                                    uiState.searchQuery.isNotBlank()
                                ) "Clear Filters" else null,
                                onAction = {
                                    viewModel.clearFilters()
                                    viewModel.setSearchQuery("")
                                },
                            )
                        }
                    }

                    // List
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = AskFacultySpacing.lg,
                                end = AskFacultySpacing.lg,
                                top = AskFacultySpacing.sm,
                                bottom = AskFacultySpacing.xxxl, // space for FAB
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

    // ── Filter bottom sheet ───────────────────────────────────────────────────
    if (showFilterSheet) {
        FilterBottomSheet(
            selectedPriority = uiState.selectedPriority,
            selectedSubject = uiState.selectedSubject,
            onPrioritySelected = { viewModel.setPriorityFilter(it) },
            onSubjectSelected = { viewModel.setSubjectFilter(it) },
            onResetAll = { viewModel.clearFilters() },
            onApply = { showFilterSheet = false },
            onDismiss = { showFilterSheet = false },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Skeleton card  (3 shimmer blocks matching DoubtCard proportions)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SkeletonDoubtCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AskFacultyRadius.lg),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(AskFacultySpacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            // Priority stripe
            AskFacultySkeleton(
                modifier = Modifier
                    .width(3.dp)
                    .height(56.dp),
                cornerRadius = AskFacultyRadius.pill,
            )
            Spacer(Modifier.width(AskFacultySpacing.md))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
            ) {
                // Name + timestamp row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    AskFacultySkeleton(modifier = Modifier.width(120.dp).height(14.dp))
                    AskFacultySkeleton(modifier = Modifier.width(52.dp).height(12.dp))
                }
                // Subject line
                AskFacultySkeleton(modifier = Modifier.width(180.dp).height(12.dp))
                // Question lines
                AskFacultySkeleton(modifier = Modifier.fillMaxWidth().height(14.dp))
                AskFacultySkeleton(modifier = Modifier.fillMaxWidth(0.75f).height(14.dp))
                // Chips row
                Row(horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm)) {
                    AskFacultySkeleton(modifier = Modifier.width(72.dp).height(22.dp), cornerRadius = AskFacultyRadius.pill)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Filter bottom sheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    selectedPriority: String?,
    selectedSubject: String?,
    onPrioritySelected: (String?) -> Unit,
    onSubjectSelected: (String?) -> Unit,
    onResetAll: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = AskFacultyRadius.xl,
            topEnd = AskFacultyRadius.xl,
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = AskFacultySpacing.md)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(AskFacultyRadius.pill))
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = AskFacultySpacing.xl)
                .padding(bottom = AskFacultySpacing.xl),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Filter & Sort Doubts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                TextButton(onClick = onResetAll) {
                    Text(
                        text = "Reset All",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Spacer(Modifier.height(AskFacultySpacing.lg))

            // ── Priority section ──────────────────────────────────────────────
            Text(
                text = "Priority Level",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(AskFacultySpacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
            ) {
                listOf("HIGH", "MEDIUM", "LOW").forEach { priority ->
                    val isSelected = selectedPriority == priority
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPrioritySelected(if (isSelected) null else priority) },
                        label = {
                            Text(
                                text = priority.lowercase().replaceFirstChar { it.uppercaseChar() },
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(AskFacultySpacing.xl))

            // ── Subject section ───────────────────────────────────────────────
            Text(
                text = "Common Subjects",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(AskFacultySpacing.sm))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
                contentPadding = PaddingValues(vertical = AskFacultySpacing.xs),
            ) {
                val subjects = listOf(
                    "Data Structures",
                    "Algorithms",
                    "Operating Systems",
                    "Computer Networks",
                    "Database Systems",
                    "Machine Learning",
                )
                items(subjects) { subject ->
                    val isSelected = selectedSubject == subject
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSubjectSelected(if (isSelected) null else subject) },
                        label = { Text(subject) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(AskFacultySpacing.xxl))

            // ── Apply button ──────────────────────────────────────────────────
            PrimaryButton(
                text = "Apply Filters",
                onClick = onApply,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
