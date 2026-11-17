package com.example.askfaculty.presentation.resources

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.askfaculty.core.ui.components.*
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.domain.model.Resource
import com.example.askfaculty.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcesScreen(
    onNavigateBack: (() -> Unit)? = null,
    viewModel: ResourcesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val subjects = remember(uiState.resources) {
        listOf("All") + Constants.DEFAULT_SUBJECTS
    }

    val typeFilters = listOf("All", "PDF & Documents", "Slides & PPT", "Links & Code")

    val filteredResources = remember(
        uiState.resources,
        uiState.selectedSubject,
        uiState.selectedType,
        uiState.searchQuery,
    ) {
        uiState.resources.filter { resource ->
            val matchesSubject = if (uiState.selectedSubject == "All") true
            else resource.subject.equals(uiState.selectedSubject, ignoreCase = true)

            val matchesType = when (uiState.selectedType) {
                "PDF & Documents" -> resource.fileType.contains("pdf", ignoreCase = true) || resource.fileName.endsWith(".pdf", ignoreCase = true)
                "Slides & PPT" -> resource.fileType.contains("ppt", ignoreCase = true) || resource.fileName.endsWith(".pptx", ignoreCase = true)
                "Links & Code" -> resource.isLink || resource.fileType.contains("code", ignoreCase = true)
                else -> true
            }

            val matchesSearch = if (uiState.searchQuery.isBlank()) true
            else resource.title.contains(uiState.searchQuery, ignoreCase = true) ||
                    resource.description.contains(uiState.searchQuery, ignoreCase = true) ||
                    resource.subject.contains(uiState.searchQuery, ignoreCase = true) ||
                    resource.fileName.contains(uiState.searchQuery, ignoreCase = true)

            matchesSubject && matchesType && matchesSearch
        }
    }

    val openResourceUrl = { url: String ->
        if (url.isNotBlank()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open document: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Resource link is not available", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Academic Resources",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Faculty notes, slide decks & syllabus guides",
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
            // Search field
            AskFacultySearchField(
                query = uiState.searchQuery,
                onQueryChange = viewModel::setSearchQuery,
                placeholder = "Search resources by title, topic, or file name…",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.sm),
            )

            // Subject filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.xs),
            ) {
                items(subjects) { subject ->
                    val isSelected = uiState.selectedSubject == subject
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectSubject(subject) },
                        label = { Text(subject, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(AskFacultyRadius.pill),
                    )
                }
            }

            // Type filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.xs),
            ) {
                items(typeFilters) { type ->
                    val isSelected = uiState.selectedType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectType(type) },
                        label = { Text(type, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(AskFacultyRadius.pill),
                    )
                }
            }

            Spacer(Modifier.height(AskFacultySpacing.xs))

            when {
                uiState.isLoading -> LoadingState(modifier = Modifier.fillMaxSize())

                uiState.error != null -> ErrorState(
                    message = uiState.error ?: "Unable to load resources",
                    modifier = Modifier.fillMaxSize(),
                )

                filteredResources.isEmpty() -> EmptyState(
                    icon = Icons.Default.FolderOpen,
                    title = "No resources found",
                    subtitle = "Try adjusting your subject or type filters.",
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
                        // Pinned Section if any
                        val pinned = filteredResources.filter { it.isPinned }
                        if (pinned.isNotEmpty()) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.xs),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = "Important Faculty Materials",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                            items(pinned, key = { "pinned_${it.id}" }) { res ->
                                ResourceItemCard(
                                    resource = res,
                                    onOpen = { openResourceUrl(if (res.isLink) res.linkUrl else res.fileUrl) },
                                )
                            }
                            item {
                                Spacer(Modifier.height(AskFacultySpacing.xs))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(Modifier.height(AskFacultySpacing.xs))
                            }
                        }

                        val nonPinned = filteredResources.filter { !it.isPinned }
                        items(nonPinned, key = { it.id }) { res ->
                            ResourceItemCard(
                                resource = res,
                                onOpen = { openResourceUrl(if (res.isLink) res.linkUrl else res.fileUrl) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResourceItemCard(
    resource: Resource,
    onOpen: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(AskFacultyRadius.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AskFacultyElevation.low),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AskFacultySpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.md),
        ) {
            // Resource type icon container
            val iconBg = if (resource.isLink) Color(0xFF3B82F6)
            else if (resource.fileType.contains("pdf", true) || resource.fileName.endsWith(".pdf", true)) Color(0xFFEF4444)
            else Color(0xFFF59E0B)

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(AskFacultyRadius.md))
                    .background(iconBg.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (resource.isLink) Icons.Default.Link
                    else if (resource.fileType.contains("pdf", true) || resource.fileName.endsWith(".pdf", true)) Icons.Default.PictureAsPdf
                    else Icons.Default.Description,
                    contentDescription = null,
                    tint = iconBg,
                    modifier = Modifier.size(24.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resource.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (resource.description.isNotBlank()) {
                    Text(
                        text = resource.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
                ) {
                    Text(
                        text = resource.subject,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Text(
                        text = "Prof. ${resource.uploaderName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            IconButton(onClick = onOpen) {
                Icon(
                    imageVector = if (resource.isLink) Icons.Default.OpenInNew else Icons.Default.Download,
                    contentDescription = "Open Resource",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
