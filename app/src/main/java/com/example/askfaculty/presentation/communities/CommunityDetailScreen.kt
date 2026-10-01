package com.example.askfaculty.presentation.communities

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.askfaculty.core.ui.components.*
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.core.util.toRelativeTime
import com.example.askfaculty.domain.model.Post
import com.example.askfaculty.domain.model.Resource
import com.example.askfaculty.ui.theme.OfficialAnswer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityDetailScreen(
    communityId: String,
    onBack: () -> Unit,
    onCreatePost: () -> Unit,
    viewModel: CommunityDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tabs = listOf("Posts", "Resources")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.community?.name ?: "Community",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        floatingActionButton = {
            if (uiState.selectedTab == 0) {
                FloatingActionButton(onClick = onCreatePost) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Post",
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Tab row
            TabRow(selectedTabIndex = uiState.selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { Text(text = title) },
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingState()
                    }
                }

                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        ErrorState(message = uiState.error!!)
                    }
                }

                else -> {
                    when (uiState.selectedTab) {
                        0 -> PostsTab(
                            posts = uiState.posts,
                            onPinPost = { postId, isPinned -> viewModel.pinPost(postId, isPinned) },
                            onMarkOfficial = { postId, isOfficial ->
                                viewModel.markPostOfficial(postId, isOfficial)
                            },
                            onDeletePost = { postId -> viewModel.deletePost(postId) },
                        )

                        1 -> ResourcesTab(
                            resources = uiState.resources,
                            onDeleteResource = { resourceId -> viewModel.deleteResource(resourceId) },
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Posts Tab
// ---------------------------------------------------------------------------

@Composable
private fun PostsTab(
    posts: List<Post>,
    onPinPost: (String, Boolean) -> Unit,
    onMarkOfficial: (String, Boolean) -> Unit,
    onDeletePost: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (posts.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                icon = Icons.Default.Forum,
                title = "No posts yet",
                subtitle = "Be the first to start a discussion in this community.",
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(
                items = posts,
                key = { post -> post.id },
            ) { post ->
                PostCard(
                    post = post,
                    onPinPost = { onPinPost(post.id, !post.isPinned) },
                    onMarkOfficial = { onMarkOfficial(post.id, !post.isOfficialAnswer) },
                    onDeletePost = { onDeletePost(post.id) },
                )
            }
        }
    }
}

@Composable
private fun PostCard(
    post: Post,
    onPinPost: () -> Unit,
    onMarkOfficial: () -> Unit,
    onDeletePost: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (post.isOfficialAnswer)
                OfficialAnswer.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header row: author + role badge + time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Author name
                Text(
                    text = post.authorName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )

                // Faculty badge
                if (post.authorRole == Constants.ROLE_FACULTY) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ) {
                        Text(
                            text = "Faculty",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }

                // Pinned indicator
                if (post.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Relative time
                Text(
                    text = post.createdAt.toRelativeTime(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Post content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stats + actions row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Helpful count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${post.helpfulCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Reply count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${post.replyCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Pin / Unpin action
                IconButton(
                    onClick = onPinPost,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = if (post.isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                        contentDescription = if (post.isPinned) "Unpin post" else "Pin post",
                        modifier = Modifier.size(18.dp),
                        tint = if (post.isPinned)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Mark official / remove official
                IconButton(
                    onClick = onMarkOfficial,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = if (post.isOfficialAnswer) Icons.Default.Verified else Icons.Default.VerifiedUser,
                        contentDescription = if (post.isOfficialAnswer) "Remove official mark" else "Mark as official",
                        modifier = Modifier.size(18.dp),
                        tint = if (post.isOfficialAnswer) OfficialAnswer else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Delete action (only for faculty role)
                if (post.authorRole == Constants.ROLE_FACULTY) {
                    IconButton(
                        onClick = onDeletePost,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete post",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Resources Tab
// ---------------------------------------------------------------------------

@Composable
private fun ResourcesTab(
    resources: List<Resource>,
    onDeleteResource: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (resources.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                icon = Icons.Default.FolderOpen,
                title = "No resources yet",
                subtitle = "Upload study materials, notes, or links to share with the community.",
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(
                items = resources,
                key = { resource -> resource.id },
            ) { resource ->
                ResourceItem(
                    resource = resource,
                    onDelete = { onDeleteResource(resource.id) },
                )
            }
        }
    }
}

@Composable
private fun ResourceItem(
    resource: Resource,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fileIcon: ImageVector = when (resource.fileType.lowercase()) {
        "pdf" -> Icons.Default.PictureAsPdf
        "image", "jpg", "jpeg", "png", "gif", "webp" -> Icons.Default.Image
        "link", "url" -> Icons.Default.Link
        else -> Icons.Default.InsertDriveFile
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // File type icon
            Icon(
                imageVector = fileIcon,
                contentDescription = resource.fileType,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            // Title + uploader + date
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resource.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${resource.uploaderName} · ${resource.createdAt.toRelativeTime()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete resource",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
