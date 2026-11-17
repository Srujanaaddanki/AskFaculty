package com.example.askfaculty.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.askfaculty.core.ui.components.*
import com.example.askfaculty.domain.model.Community

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onDoubtClick: (String) -> Unit,
    onCommunityClick: ((String) -> Unit)? = null,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        // ── Top bar: back button + search field ───────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }

            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text("Search doubts, spaces, topics, notes…") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                ),
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                            )
                        }
                    }
                },
            )

            Spacer(modifier = Modifier.width(4.dp))
        }

        // Category filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("All", "Doubts", "Spaces", "Resources").forEach { filter ->
                val isSelected = uiState.selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setFilter(filter) },
                    label = { Text(filter, style = MaterialTheme.typography.labelSmall) },
                )
            }
        }

        HorizontalDivider()

        // ── Content area ──────────────────────────────────────────────────
        when {
            !uiState.hasSearched -> {
                EmptyState(
                    icon = Icons.Default.Search,
                    title = "Search AskFaculty",
                    subtitle = "Search across doubts, faculty answers, academic spaces, and lecture notes.",
                )
            }

            uiState.isSearching -> {
                LoadingState()
            }

            uiState.doubts.isEmpty() && uiState.communities.isEmpty() && uiState.resources.isEmpty() -> {
                EmptyState(
                    icon = Icons.Default.SearchOff,
                    title = "No results found",
                    subtitle = "Try a different search term or subject.",
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    // ── Doubts section ────────────────────────────────────
                    if ((uiState.selectedFilter == "All" || uiState.selectedFilter == "Doubts") && uiState.doubts.isNotEmpty()) {
                        item {
                            SearchResultHeader(
                                title = "Doubts & Answers",
                                count = uiState.doubts.size,
                            )
                        }
                        items(
                            items = uiState.doubts,
                            key = { "doubt_${it.id}" },
                        ) { doubt ->
                            DoubtCard(
                                doubt = doubt,
                                onClick = { onDoubtClick(doubt.id) },
                            )
                        }
                    }

                    // ── Communities section ───────────────────────────────
                    if ((uiState.selectedFilter == "All" || uiState.selectedFilter == "Spaces") && uiState.communities.isNotEmpty()) {
                        item {
                            SearchResultHeader(
                                title = "Academic Spaces",
                                count = uiState.communities.size,
                            )
                        }
                        items(
                            items = uiState.communities,
                            key = { "space_${it.id}" },
                        ) { community ->
                            CommunitySearchListItem(
                                community = community,
                                onClick = { onCommunityClick?.invoke(community.id) },
                            )
                        }
                    }

                    // ── Resources section ─────────────────────────────────
                    if ((uiState.selectedFilter == "All" || uiState.selectedFilter == "Resources") && uiState.resources.isNotEmpty()) {
                        item {
                            SearchResultHeader(
                                title = "Study Materials & Notes",
                                count = uiState.resources.size,
                            )
                        }
                        items(
                            items = uiState.resources,
                            key = { "res_${it.id}" },
                        ) { res ->
                            ListItem(
                                headlineContent = { Text(res.title, fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text("${res.subject} • Prof. ${res.uploaderName}") },
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                },
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Private composables ───────────────────────────────────────────────────────

@Composable
private fun SearchResultHeader(title: String, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun CommunitySearchListItem(
    community: Community,
    onClick: () -> Unit = {},
) {
    ListItem(
        headlineContent = {
            Text(
                text = community.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        },
        supportingContent = {
            Text(
                text = community.subject,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Text(
                text = "${community.memberCount} members",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingContent = {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}
