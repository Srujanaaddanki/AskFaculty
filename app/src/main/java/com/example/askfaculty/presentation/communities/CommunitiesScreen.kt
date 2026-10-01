package com.example.askfaculty.presentation.communities

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.askfaculty.core.ui.components.*
import com.example.askfaculty.domain.model.Community
import com.example.askfaculty.ui.theme.AskFacultyDimensions
import com.example.askfaculty.ui.theme.AskFacultyElevation
import com.example.askfaculty.ui.theme.AskFacultyRadius
import com.example.askfaculty.ui.theme.AskFacultySpacing

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunitiesScreen(
    onCommunityClick: (String) -> Unit,
    onCreateCommunity: () -> Unit,
    viewModel: CommunitiesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Local search query — pure UI concern, no ViewModel change needed
    var searchQuery by remember { mutableStateOf("") }

    val filteredCommunities = remember(uiState.communities, searchQuery) {
        if (searchQuery.isBlank()) uiState.communities
        else uiState.communities.filter { community ->
            community.name.contains(searchQuery, ignoreCase = true) ||
                community.subject.contains(searchQuery, ignoreCase = true) ||
                community.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Faculty Spaces",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        if (!uiState.isLoading && uiState.communities.isNotEmpty()) {
                            Spacer(Modifier.width(AskFacultySpacing.sm))
                            Surface(
                                shape = RoundedCornerShape(AskFacultyRadius.md),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Text(
                                    text = "${uiState.communities.size}",
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
                onClick = onCreateCommunity,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                    )
                },
                text = {
                    Text(
                        text = "Create Space",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = AskFacultyElevation.floating,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                // ── Loading ────────────────────────────────────────────────
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = AskFacultySpacing.lg)
                            .padding(top = AskFacultySpacing.lg),
                        verticalArrangement = Arrangement.spacedBy(AskFacultySpacing.lg),
                    ) {
                        // Search bar skeleton
                        AskFacultySkeleton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(AskFacultyDimensions.textFieldHeight),
                            cornerRadius = AskFacultyRadius.xxl,
                        )
                        repeat(3) {
                            AskFacultySkeleton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(CommunityCardDefaults.CARD_HEIGHT),
                                cornerRadius = AskFacultyRadius.lg,
                            )
                        }
                    }
                }

                // ── Error ──────────────────────────────────────────────────
                uiState.error != null -> {
                    ErrorState(
                        message = uiState.error!!,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                // ── Content (possibly empty after search) ──────────────────
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = AskFacultySpacing.lg,
                            end = AskFacultySpacing.lg,
                            top = AskFacultySpacing.md,
                            bottom = AskFacultySpacing.xxxl + AskFacultySpacing.xl, // FAB clearance
                        ),
                        verticalArrangement = Arrangement.spacedBy(AskFacultySpacing.md),
                    ) {
                        // ── Search bar ─────────────────────────────────────
                        item(key = "search_bar") {
                            AskFacultySearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                onSearch = {},
                                placeholder = "Search spaces, subjects…",
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        // ── Empty state (no results) ───────────────────────
                        if (filteredCommunities.isEmpty()) {
                            item(key = "empty_state") {
                                if (uiState.communities.isEmpty()) {
                                    // No communities at all
                                    AskFacultyEmptyState(
                                        icon = Icons.Default.Groups,
                                        title = "No Faculty Spaces Yet",
                                        subtitle = "Establish course discussion groups, resource libraries, and announcement channels for your student batches.",
                                        actionLabel = "Create First Space",
                                        onAction = onCreateCommunity,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = AskFacultySpacing.xxxl),
                                    )
                                } else {
                                    // Has communities but search returned nothing
                                    AskFacultyEmptyState(
                                        icon = Icons.Default.SearchOff,
                                        title = "No Spaces Found",
                                        subtitle = "Try a different keyword or clear the search.",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = AskFacultySpacing.xxxl),
                                    )
                                }
                            }
                        }

                        // ── Community cards ────────────────────────────────
                        itemsIndexed(
                            items = filteredCommunities,
                            key = { _, community -> community.id },
                        ) { index, community ->
                            CommunityCard(
                                community = community,
                                cardIndex = index,
                                onClick = { onCommunityClick(community.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Card defaults
// ─────────────────────────────────────────────────────────────────────────────

private object CommunityCardDefaults {
    val CARD_HEIGHT = 220.dp       // approximate skeleton height
    val BANNER_HEIGHT = 120.dp     // gradient/image header height
    val ICON_BANNER_SIZE = 52.dp   // centered icon inside gradient banner
}

// ─────────────────────────────────────────────────────────────────────────────
// Community Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CommunityCard(
    community: Community,
    cardIndex: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Cycle through primary / secondary / tertiary gradient pairs
    val gradientColors = communityGradientFor(cardIndex)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AskFacultyRadius.lg),
        elevation = CardDefaults.cardElevation(
            defaultElevation = AskFacultyElevation.low,
            pressedElevation = AskFacultyElevation.medium,
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = androidx.compose.ui.unit.Dp(1f),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
        ),
    ) {
        Column {
            // ── Banner / header ───────────────────────────────────────────
            if (community.coverImageUrl.isNotBlank()) {
                AsyncImage(
                    model = community.coverImageUrl,
                    contentDescription = "Cover for ${community.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CommunityCardDefaults.BANNER_HEIGHT)
                        .clip(
                            RoundedCornerShape(
                                topStart = AskFacultyRadius.lg,
                                topEnd = AskFacultyRadius.lg,
                            )
                        ),
                    contentScale = ContentScale.Crop,
                )
            } else {
                GradientBanner(
                    gradientColors = gradientColors,
                    communityName = community.name,
                )
            }

            // ── Body ──────────────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(
                    horizontal = AskFacultySpacing.lg,
                    vertical = AskFacultySpacing.md,
                ),
            ) {
                // Name row + subject badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = community.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    if (community.subject.isNotBlank()) {
                        Spacer(Modifier.width(AskFacultySpacing.sm))
                        SubjectBadge(subject = community.subject)
                    }
                }

                // Description
                if (community.description.isNotBlank()) {
                    Spacer(Modifier.height(AskFacultySpacing.xs))
                    Text(
                        text = community.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(AskFacultySpacing.md))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                )
                Spacer(Modifier.height(AskFacultySpacing.sm))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.lg),
                    ) {
                        StatChip(
                            icon = Icons.Default.Person,
                            label = "${community.memberCount} enrolled",
                        )
                        StatChip(
                            icon = Icons.Default.Forum,
                            label = "${community.postCount} posts",
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open space",
                        modifier = Modifier.size(AskFacultyDimensions.iconSize),
                        tint = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Gradient Banner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GradientBanner(
    gradientColors: List<Color>,
    communityName: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(CommunityCardDefaults.BANNER_HEIGHT)
            .clip(
                RoundedCornerShape(
                    topStart = AskFacultyRadius.lg,
                    topEnd = AskFacultyRadius.lg,
                )
            )
            .background(
                Brush.linearGradient(gradientColors)
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Subtle scrim at the bottom for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.18f),
                        ),
                    )
                ),
        )

        // Monogram icon
        Surface(
            modifier = Modifier.size(CommunityCardDefaults.ICON_BANNER_SIZE),
            shape = RoundedCornerShape(AskFacultyRadius.md),
            color = Color.White.copy(alpha = 0.20f),
            tonalElevation = AskFacultyElevation.none,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = communityName
                        .split(" ")
                        .filter { it.isNotBlank() }
                        .take(2)
                        .joinToString("") { it.first().uppercaseChar().toString() }
                        .ifEmpty { "S" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Subject Badge
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SubjectBadge(subject: String) {
    Surface(
        shape = RoundedCornerShape(AskFacultyRadius.sm),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
        tonalElevation = AskFacultyElevation.none,
    ) {
        Text(
            text = subject,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                horizontal = AskFacultySpacing.sm,
                vertical = AskFacultySpacing.xs,
            ),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Stat Chip (icon + label)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.xs),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(AskFacultyDimensions.iconSizeXs),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Gradient palette helper
// Cycles: primary → secondary → tertiary → secondary+primary → …
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun communityGradientFor(index: Int): List<Color> {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary

    return when (index % 3) {
        0 -> listOf(primary, secondary.copy(alpha = 0.80f))
        1 -> listOf(secondary, tertiary.copy(alpha = 0.80f))
        else -> listOf(tertiary, primary.copy(alpha = 0.80f))
    }
}
