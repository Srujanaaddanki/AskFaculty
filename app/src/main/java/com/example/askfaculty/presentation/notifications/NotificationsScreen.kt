package com.example.askfaculty.presentation.notifications

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.askfaculty.core.ui.components.*
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.core.util.toRelativeTime
import com.example.askfaculty.domain.model.Notification
import com.example.askfaculty.ui.theme.AskFacultyDimensions
import com.example.askfaculty.ui.theme.AskFacultyElevation
import com.example.askfaculty.ui.theme.AskFacultyRadius
import com.example.askfaculty.ui.theme.AskFacultySpacing

// ─────────────────────────────────────────────────────────────────────────────
// Screen entry point
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onDoubtClick: (String) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val allNotifs = uiState.todayNotifs + uiState.yesterdayNotifs + uiState.olderNotifs
    val totalUnread = allNotifs.count { !it.isRead }
    val isEmpty = allNotifs.isEmpty()

    Scaffold(
        topBar = {
            NotificationsTopBar(
                totalUnread = totalUnread,
                onMarkAllRead = { viewModel.markAllAsRead() },
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
                uiState.isLoading -> NotificationsLoadingSkeleton()

                isEmpty -> {
                    EmptyState(
                        icon = Icons.Default.NotificationsNone,
                        title = "All Caught Up!",
                        subtitle = "When students ask questions, follow up on answers, or mention you, notifications will appear here.",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    NotificationsList(
                        uiState = uiState,
                        onNotificationClick = { notif ->
                            viewModel.markAsRead(notif.id)
                            if (notif.doubtId.isNotBlank()) {
                                onDoubtClick(notif.doubtId)
                            }
                        },
                        onDismiss = { notif -> viewModel.markAsRead(notif.id) },
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsTopBar(
    totalUnread: Int,
    onMarkAllRead: () -> Unit,
) {
    AskFacultyTopBar(
        title = "Notifications",
        actions = {
            // Unread badge pill
            if (totalUnread > 0) {
                Surface(
                    shape = RoundedCornerShape(AskFacultyRadius.pill),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = AskFacultySpacing.xs),
                ) {
                    Text(
                        text = "$totalUnread new",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(
                            horizontal = AskFacultySpacing.sm,
                            vertical = AskFacultySpacing.xs,
                        ),
                    )
                }

                // Mark All Read action
                TextButton(
                    onClick = onMarkAllRead,
                    contentPadding = PaddingValues(
                        horizontal = AskFacultySpacing.sm,
                        vertical = AskFacultySpacing.xs,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = null,
                        modifier = Modifier.size(AskFacultyDimensions.iconSizeSm),
                    )
                    Spacer(Modifier.width(AskFacultySpacing.xs))
                    Text(
                        text = "Mark all read",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Skeleton loading state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NotificationsLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AskFacultySpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
    ) {
        Spacer(Modifier.height(AskFacultySpacing.md))
        // Section label skeleton
        SkeletonLoader(
            modifier = Modifier
                .width(80.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(AskFacultyRadius.xs)),
        )
        Spacer(Modifier.height(AskFacultySpacing.xs))
        repeat(6) { index ->
            SkeletonLoader(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (index == 2) 88.dp else 76.dp)
                    .clip(RoundedCornerShape(AskFacultyRadius.lg)),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Grouped list
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsList(
    uiState: NotificationsUiState,
    onNotificationClick: (Notification) -> Unit,
    onDismiss: (Notification) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = AskFacultySpacing.sm,
            bottom = AskFacultySpacing.xxxl,
        ),
    ) {
        notificationSection(
            label = "TODAY",
            items = uiState.todayNotifs,
            onNotificationClick = onNotificationClick,
            onDismiss = onDismiss,
        )
        notificationSection(
            label = "YESTERDAY",
            items = uiState.yesterdayNotifs,
            onNotificationClick = onNotificationClick,
            onDismiss = onDismiss,
        )
        notificationSection(
            label = "EARLIER",
            items = uiState.olderNotifs,
            onNotificationClick = onNotificationClick,
            onDismiss = onDismiss,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun LazyListScope.notificationSection(
    label: String,
    items: List<Notification>,
    onNotificationClick: (Notification) -> Unit,
    onDismiss: (Notification) -> Unit,
) {
    if (items.isEmpty()) return

    item(key = "header_$label") {
        NotificationSectionHeader(label = label)
    }

    items(items = items, key = { it.id }) { notif ->
        SwipeToDismissNotificationItem(
            notification = notif,
            onClick = { onNotificationClick(notif) },
            onDismiss = { onDismiss(notif) },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NotificationSectionHeader(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = AskFacultySpacing.lg,
                end = AskFacultySpacing.lg,
                top = AskFacultySpacing.lg,
                bottom = AskFacultySpacing.sm,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Swipe-to-dismiss wrapper
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissNotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd ||
                value == SwipeToDismissBoxValue.EndToStart
            ) {
                onDismiss()
                true
            } else false
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.4f },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = { SwipeDismissBackground(dismissState) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = AskFacultySpacing.lg,
                vertical = AskFacultySpacing.xs,
            )
            .clip(RoundedCornerShape(AskFacultyRadius.lg)),
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
    ) {
        NotificationItem(
            notification = notification,
            onClick = onClick,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeDismissBackground(state: SwipeToDismissBoxState) {
    val direction = state.dismissDirection
    val isActive = state.targetValue != SwipeToDismissBoxValue.Settled

    val bgColor by animateColorAsState(
        targetValue = if (isActive)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(200),
        label = "swipe_bg",
    )

    val scale by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.75f,
        animationSpec = tween(200),
        label = "swipe_icon_scale",
    )

    val alignment = when (direction) {
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        else -> Alignment.CenterStart
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = bgColor,
                shape = RoundedCornerShape(AskFacultyRadius.lg),
            )
            .padding(horizontal = AskFacultySpacing.xl),
        contentAlignment = alignment,
    ) {
        Icon(
            imageVector = Icons.Default.MarkEmailRead,
            contentDescription = "Mark as read",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .scale(scale)
                .size(AskFacultyDimensions.iconSizeLg),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Notification card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val unread = !notification.isRead
    val iconMeta = notification.type.toNotifIconMeta()

    val cardContainerColor = if (unread)
        MaterialTheme.colorScheme.surface
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AskFacultyRadius.lg),
        color = cardContainerColor,
        tonalElevation = if (unread) AskFacultyElevation.medium else AskFacultyElevation.none,
        shadowElevation = if (unread) AskFacultyElevation.low else AskFacultyElevation.none,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ── Colored left-border unread indicator ──────────────────────
            if (unread) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    iconMeta.tint,
                                    iconMeta.tint.copy(alpha = 0.4f),
                                ),
                            ),
                            shape = RoundedCornerShape(
                                topStart = AskFacultyRadius.lg,
                                bottomStart = AskFacultyRadius.lg,
                            ),
                        ),
                )
            } else {
                Spacer(Modifier.width(AskFacultySpacing.xs))
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        top = AskFacultySpacing.md,
                        bottom = AskFacultySpacing.md,
                        start = AskFacultySpacing.md,
                        end = AskFacultySpacing.md,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // ── Category icon bubble ───────────────────────────────────
                Surface(
                    shape = RoundedCornerShape(AskFacultyRadius.md),
                    color = iconMeta.tint.copy(alpha = if (unread) 0.18f else 0.08f),
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = iconMeta.icon,
                            contentDescription = null,
                            tint = if (unread) iconMeta.tint else iconMeta.tint.copy(alpha = 0.6f),
                            modifier = Modifier.size(AskFacultyDimensions.iconSize),
                        )
                    }
                }

                Spacer(Modifier.width(AskFacultySpacing.md))

                // ── Text content ───────────────────────────────────────────
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (unread) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(AskFacultySpacing.xs))
                    Text(
                        text = notification.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (unread)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    notification.createdAt?.let { date ->
                        Spacer(Modifier.height(AskFacultySpacing.xs))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.xs),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(10.dp),
                            )
                            Text(
                                text = date.toRelativeTime(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                }

                // ── Unread dot indicator ───────────────────────────────────
                if (unread) {
                    Spacer(Modifier.width(AskFacultySpacing.sm))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(iconMeta.tint),
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Notification icon meta — maps type → icon + tint
// ─────────────────────────────────────────────────────────────────────────────

private data class NotifIconMeta(val icon: ImageVector, val tint: Color)

@Composable
private fun String.toNotifIconMeta(): NotifIconMeta {
    val primary   = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary  = MaterialTheme.colorScheme.tertiary
    val error     = MaterialTheme.colorScheme.error

    return when (this) {
        Constants.NOTIF_NEW_DOUBT     -> NotifIconMeta(Icons.Default.QuestionAnswer, primary)
        Constants.NOTIF_FOLLOW_UP     -> NotifIconMeta(Icons.Default.Refresh, tertiary)
        Constants.NOTIF_STUDENT_REPLY -> NotifIconMeta(Icons.AutoMirrored.Filled.Reply, secondary)
        Constants.NOTIF_COMMUNITY     -> NotifIconMeta(Icons.Default.Groups, error)
        Constants.NOTIF_MENTION       -> NotifIconMeta(Icons.Default.AlternateEmail, tertiary)
        else                          -> NotifIconMeta(Icons.Default.Notifications, primary)
    }
}
