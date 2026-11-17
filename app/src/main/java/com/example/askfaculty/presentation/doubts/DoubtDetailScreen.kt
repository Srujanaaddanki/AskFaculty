package com.example.askfaculty.presentation.doubts

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.askfaculty.core.ui.components.*
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.core.util.toDateTimeString
import com.example.askfaculty.core.util.toRelativeTime
import com.example.askfaculty.domain.model.Message
import com.example.askfaculty.ui.theme.AskFacultyDimensions
import com.example.askfaculty.ui.theme.AskFacultyElevation
import com.example.askfaculty.ui.theme.AskFacultyRadius
import com.example.askfaculty.ui.theme.AskFacultySpacing
import com.example.askfaculty.ui.theme.GradientEnd
import com.example.askfaculty.ui.theme.GradientMid
import com.example.askfaculty.ui.theme.GradientStart
import com.example.askfaculty.ui.theme.OfficialAnswer
import com.example.askfaculty.ui.theme.Primary
import com.example.askfaculty.ui.theme.StatusAnswered

// ─────────────────────────────────────────────────────────────────────────────
// Root screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoubtDetailScreen(
    doubtId: String,
    onBack: () -> Unit,
    viewModel: DoubtDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var showActionBottomSheet by remember { mutableStateOf(false) }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setAttachment(it, "document.pdf", Constants.ATTACHMENT_OTHER) }
    }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setAttachment(it, "image.jpg", Constants.ATTACHMENT_IMAGE) }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            // Custom two-line top bar: can't use AskFacultyTopBar (needs two-line title)
            Surface(
                shadowElevation = AskFacultyElevation.low,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(AskFacultyDimensions.topBarHeight)
                        .padding(horizontal = AskFacultySpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = uiState.doubt?.topic ?: "Doubt Discussion",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        uiState.doubt?.subject?.let { subject ->
                            Text(
                                text = subject,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    // Bookmark toggle icon
                    IconButton(onClick = viewModel::toggleBookmark) {
                        Icon(
                            imageVector = if (uiState.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save Bookmark",
                            tint = if (uiState.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(AskFacultyDimensions.iconSize),
                        )
                    }
                    // AI toggle icon
                    IconButton(onClick = viewModel::toggleAiPanel) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Teaching Assistant",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(AskFacultyDimensions.iconSize),
                        )
                    }
                    // More options icon
                    IconButton(onClick = { showActionBottomSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Doubt actions",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        bottomBar = {
            ReplyComposer(
                text = uiState.replyText,
                onTextChange = viewModel::setReplyText,
                onSend = viewModel::sendReply,
                onAttachFile = { fileLauncher.launch("*/*") },
                onAttachImage = { imageLauncher.launch("image/*") },
                isSending = uiState.isReplying,
                isUploading = uiState.isUploading,
                uploadProgress = uiState.uploadProgress,
                attachmentName = uiState.attachmentName,
                onClearAttachment = viewModel::clearAttachment,
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
                message = uiState.error ?: "Unable to load discussion",
                onRetry = {},
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )

            uiState.doubt == null -> EmptyState(
                icon = Icons.Default.QuestionAnswer,
                title = "Doubt record not found",
                subtitle = "This doubt may have been removed.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )

            else -> {
                val doubt = uiState.doubt!!
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(
                        start = AskFacultySpacing.lg,
                        end = AskFacultySpacing.lg,
                        top = AskFacultySpacing.lg,
                        bottom = AskFacultySpacing.xl,
                    ),
                    verticalArrangement = Arrangement.spacedBy(AskFacultySpacing.md),
                ) {
                    // ── 1. Gradient Question Card ────────────────────────────
                    item {
                        QuestionCard(
                            doubt = doubt,
                        )
                    }

                    // ── 2. Status action chips / Student Resolution ─────────
                    item {
                        if (uiState.currentUserRole == Constants.ROLE_STUDENT) {
                            StudentResolutionBanner(
                                status = doubt.status,
                                onMarkResolved = viewModel::markResolvedByStudent,
                                onReopen = { viewModel.setShowReopenDialog(true) },
                            )
                        } else {
                            StatusActionRow(
                                currentStatus = doubt.status,
                                onStatusChange = {
                                    viewModel.updateStatus(it)
                                },
                            )
                        }
                    }

                    // ── 3. AI Suggestion Panel ───────────────────────────────
                    item {
                        AnimatedVisibility(
                            visible = uiState.showAiPanel,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut(),
                        ) {
                            AiPanel(
                                summary = uiState.aiSummary,
                                suggestedResponse = uiState.aiSuggestedResponse,
                                isLoading = uiState.isAiLoading,
                                onSummarize = viewModel::generateAiSummary,
                                onSuggestResponse = viewModel::generateSuggestedResponse,
                                onUseResponse = viewModel::useAiSuggestion,
                                onClose = viewModel::toggleAiPanel,
                            )
                        }
                    }

                    // ── 4. Thread divider ────────────────────────────────────
                    item {
                        ThreadDivider()
                    }

                    // ── 5. Message bubbles ───────────────────────────────────
                    items(uiState.messages, key = { it.id }) { message ->
                        MessageBubble(
                            message = message,
                            isFaculty = message.senderRole == Constants.SENDER_FACULTY,
                            currentUserRole = uiState.currentUserRole,
                            onMarkOfficial = { isOfficial ->
                                viewModel.markOfficial(message.id, isOfficial)
                            },
                            onToggleHelpful = {
                                viewModel.toggleHelpful(message.id)
                            },
                        )
                    }
                }
            }
        }
    }

    // ── Action Bottom Sheet ──────────────────────────────────────────────────
    if (showActionBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showActionBottomSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(
                topStart = AskFacultyRadius.xxl,
                topEnd = AskFacultyRadius.xxl,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AskFacultySpacing.xl)
                    .padding(bottom = AskFacultySpacing.xxxl),
            ) {
                // Handle indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(AskFacultyRadius.pill))
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
                Spacer(Modifier.height(AskFacultySpacing.xl))

                Text(
                    "Manage Doubt Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(AskFacultySpacing.md))

                listOf(
                    Constants.STATUS_PENDING to Triple(Icons.Default.HourglassEmpty, "Pending Review", MaterialTheme.colorScheme.onSurfaceVariant),
                    Constants.STATUS_IN_PROGRESS to Triple(Icons.Default.Sync, "In Progress", MaterialTheme.colorScheme.primary),
                    Constants.STATUS_ANSWERED to Triple(Icons.Default.CheckCircle, "Answered", StatusAnswered),
                    Constants.STATUS_RESOLVED to Triple(Icons.Default.Verified, "Resolved (Closed)", Color(0xFF7C3AED)),
                ).forEach { (status, meta) ->
                    val (icon, label, accentColor) = meta
                    val isCurrent = uiState.doubt?.status == status
                    StatusSheetItem(
                        icon = icon,
                        label = label,
                        accentColor = accentColor,
                        isSelected = isCurrent,
                        onClick = {
                            viewModel.updateStatus(status)
                            showActionBottomSheet = false
                        },
                    )
                    Spacer(Modifier.height(AskFacultySpacing.xs))
                }

                Spacer(Modifier.height(AskFacultySpacing.lg))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(AskFacultySpacing.lg))

                Text(
                    "Adjust Priority",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(AskFacultySpacing.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
                ) {
                    listOf(
                        Constants.PRIORITY_HIGH to "🔴 High",
                        Constants.PRIORITY_MEDIUM to "🟡 Medium",
                        Constants.PRIORITY_LOW to "🟢 Low",
                    ).forEach { (priority, label) ->
                        val isSelected = uiState.doubt?.priority == priority
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updatePriority(priority)
                                showActionBottomSheet = false
                            },
                            label = {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(AskFacultyRadius.md),
                        )
                    }
                }
                Spacer(Modifier.height(AskFacultySpacing.lg))
            }
        }
    }

    if (uiState.showReopenDialog) {
        ReopenDoubtDialog(
            onDismiss = { viewModel.setShowReopenDialog(false) },
            onConfirm = viewModel::reopenDoubt,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Question Card — gradient header, metadata row, question body
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuestionCard(doubt: com.example.askfaculty.domain.model.Doubt) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AskFacultyRadius.xl),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = AskFacultyElevation.medium),
    ) {
        Column {
            // ── Gradient header ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientStart, GradientMid, GradientEnd),
                        ),
                        shape = RoundedCornerShape(
                            topStart = AskFacultyRadius.xl,
                            topEnd = AskFacultyRadius.xl,
                        ),
                    )
                    .padding(AskFacultySpacing.lg),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.md),
                ) {
                    AvatarImage(
                        url = doubt.studentPhotoUrl,
                        size = AskFacultyDimensions.avatarMedium,
                        placeholder = doubt.studentName,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = doubt.studentName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        if (doubt.studentIdNumber.isNotBlank()) {
                            Text(
                                text = "ID: ${doubt.studentIdNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.75f),
                            )
                        }
                    }
                    StatusChip(status = doubt.status)
                }
            }

            // ── Metadata row ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AskFacultySpacing.lg)
                    .padding(top = AskFacultySpacing.md),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PriorityChip(priority = doubt.priority)
                    // Subject badge
                    Surface(
                        shape = RoundedCornerShape(AskFacultyRadius.sm),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = AskFacultySpacing.sm,
                                vertical = AskFacultySpacing.xs,
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.xs),
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(AskFacultyDimensions.iconSizeXs),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                text = doubt.subject,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(AskFacultySpacing.md))

                // ── Question body ────────────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AskFacultyRadius.md),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                ) {
                    Text(
                        text = doubt.question,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(AskFacultySpacing.md),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                if (doubt.codeSnippet.isNotBlank()) {
                    Spacer(Modifier.height(AskFacultySpacing.sm))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AskFacultyRadius.md),
                        color = Color(0xFF1E1E2E),
                    ) {
                        Column(modifier = Modifier.padding(AskFacultySpacing.md)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = doubt.codeLanguage.ifBlank { "Code Snippet" }.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA6E3A1),
                                )
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = Color(0xFFA6E3A1),
                                    modifier = Modifier.size(AskFacultyDimensions.iconSizeXs),
                                )
                            }
                            Spacer(Modifier.height(AskFacultySpacing.xs))
                            Text(
                                text = doubt.codeSnippet,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                color = Color(0xFFCDD6F4),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(AskFacultySpacing.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.xs),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(AskFacultyDimensions.iconSizeXs),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = doubt.createdAt?.toDateTimeString() ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (doubt.attachmentUrl.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.xs),
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = null,
                                modifier = Modifier.size(AskFacultyDimensions.iconSizeXs),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "Attachment",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(AskFacultySpacing.md))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Status Action Chips Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatusActionRow(
    currentStatus: String,
    onStatusChange: (String) -> Unit,
) {
    val statuses = listOf(
        Constants.STATUS_PENDING to "Pending",
        Constants.STATUS_IN_PROGRESS to "In Progress",
        Constants.STATUS_ANSWERED to "Answered",
        Constants.STATUS_RESOLVED to "Resolved",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.xs),
    ) {
        statuses.forEach { (status, label) ->
            val isSelected = currentStatus == status
            FilterChip(
                selected = isSelected,
                onClick = { if (!isSelected) onStatusChange(status) },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(AskFacultyRadius.sm),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Thread divider
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ThreadDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AskFacultySpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Text(
            text = "  OFFICIAL DISCUSSION THREAD  ",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.outline,
            letterSpacing = androidx.compose.ui.unit.TextUnit(0.8f, androidx.compose.ui.unit.TextUnitType.Sp),
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Student Resolution Banner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StudentResolutionBanner(
    status: String,
    onMarkResolved: () -> Unit,
    onReopen: () -> Unit,
) {
    when (status) {
        Constants.STATUS_ANSWERED -> {
            Card(
                shape = RoundedCornerShape(AskFacultyRadius.lg),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AskFacultySpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.md),
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Faculty Responded",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = "Did this answer resolve your question completely?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        )
                    }
                    Button(
                        onClick = onMarkResolved,
                        shape = RoundedCornerShape(AskFacultyRadius.md),
                        contentPadding = PaddingValues(horizontal = AskFacultySpacing.md, vertical = AskFacultySpacing.xs),
                    ) {
                        Text("Resolved", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        Constants.STATUS_RESOLVED -> {
            Card(
                shape = RoundedCornerShape(AskFacultyRadius.lg),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF7C3AED).copy(alpha = 0.12f),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AskFacultySpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.md),
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(28.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Doubt Resolved",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Need further clarification on this topic?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    OutlinedButton(
                        onClick = onReopen,
                        shape = RoundedCornerShape(AskFacultyRadius.md),
                        contentPadding = PaddingValues(horizontal = AskFacultySpacing.md, vertical = AskFacultySpacing.xs),
                    ) {
                        Text("Reopen", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        else -> {
            Surface(
                shape = RoundedCornerShape(AskFacultyRadius.md),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AskFacultySpacing.md, vertical = AskFacultySpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(AskFacultyDimensions.iconSizeSm),
                    )
                    Text(
                        text = "Pending faculty review. You will be notified when an answer is posted.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reopen Doubt Dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ReopenDoubtDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reopen Doubt") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm)) {
                Text(
                    "Please describe what further clarification or guidance you need from faculty:",
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = { Text("Reason for reopening...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(AskFacultyRadius.md),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isNotBlank()) onConfirm(reason)
                },
                enabled = reason.isNotBlank(),
            ) {
                Text("Reopen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Message Bubble
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MessageBubble(
    message: Message,
    isFaculty: Boolean,
    currentUserRole: String,
    onMarkOfficial: (Boolean) -> Unit,
    onToggleHelpful: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AskFacultySpacing.xs),
        horizontalAlignment = if (isFaculty) Alignment.End else Alignment.Start,
    ) {
        // ── Official answer badge ────────────────────────────────────────────
        if (message.isOfficial) {
            Surface(
                shape = RoundedCornerShape(AskFacultyRadius.sm),
                color = OfficialAnswer.copy(alpha = 0.12f),
                modifier = Modifier.padding(bottom = AskFacultySpacing.xs),
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = AskFacultySpacing.sm,
                        vertical = AskFacultySpacing.xs,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.xs),
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        modifier = Modifier.size(AskFacultyDimensions.iconSizeXs),
                        tint = OfficialAnswer,
                    )
                    Text(
                        text = "Official Faculty Answer",
                        style = MaterialTheme.typography.labelSmall,
                        color = OfficialAnswer,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // ── Bubble card ──────────────────────────────────────────────────────
        val bubbleModifier = if (message.isOfficial && !isFaculty) {
            Modifier
                .widthIn(max = 300.dp)
                .border(
                    width = 3.dp,
                    brush = SolidColor(OfficialAnswer),
                    shape = RoundedCornerShape(
                        topStart = AskFacultyRadius.xl,
                        topEnd = AskFacultyRadius.xl,
                        bottomStart = AskFacultyRadius.xs,
                        bottomEnd = AskFacultyRadius.xl,
                    ),
                )
        } else {
            Modifier.widthIn(max = 300.dp)
        }

        Card(
            shape = RoundedCornerShape(
                topStart = AskFacultyRadius.xl,
                topEnd = AskFacultyRadius.xl,
                bottomStart = if (isFaculty) AskFacultyRadius.xl else AskFacultyRadius.xs,
                bottomEnd = if (isFaculty) AskFacultyRadius.xs else AskFacultyRadius.xl,
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isFaculty)
                    Primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = AskFacultyElevation.low),
            modifier = bubbleModifier,
        ) {
            Column(modifier = Modifier.padding(AskFacultySpacing.md)) {
                // Sender name
                Text(
                    text = if (isFaculty) "Prof. ${message.senderName}" else message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isFaculty)
                        Color.White.copy(alpha = 0.85f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(AskFacultySpacing.xs))

                // Message text
                if (message.message.isNotBlank()) {
                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isFaculty)
                            Color.White
                        else
                            MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Code snippet if attached to message
                if (message.codeSnippet.isNotBlank()) {
                    Spacer(Modifier.height(AskFacultySpacing.xs))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AskFacultyRadius.sm),
                        color = Color(0xFF1E1E2E),
                    ) {
                        Text(
                            text = message.codeSnippet,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                            color = Color(0xFFCDD6F4),
                            modifier = Modifier.padding(AskFacultySpacing.sm),
                        )
                    }
                }

                // Attachment pill
                if (message.attachmentUrl.isNotBlank()) {
                    Spacer(Modifier.height(AskFacultySpacing.sm))
                    Surface(
                        shape = RoundedCornerShape(AskFacultyRadius.sm),
                        color = if (isFaculty)
                            Color.White.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = AskFacultySpacing.sm,
                                vertical = AskFacultySpacing.xs,
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.xs),
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = null,
                                modifier = Modifier.size(AskFacultyDimensions.iconSizeXs),
                                tint = if (isFaculty) Color.White else MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = message.attachmentName.ifBlank { "Attachment" },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = if (isFaculty) Color.White else MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(AskFacultySpacing.xs))

                // Timestamp + actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = message.createdAt?.toRelativeTime() ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isFaculty)
                            Color.White.copy(alpha = 0.65f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Helpful button for faculty responses
                        if (isFaculty) {
                            IconButton(
                                onClick = onToggleHelpful,
                                modifier = Modifier.size(28.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (message.isHelpful) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                        contentDescription = "Helpful",
                                        tint = if (message.isHelpful) Color(0xFFFDE047) else Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(AskFacultyDimensions.iconSizeXs),
                                    )
                                    if (message.helpfulCount > 0) {
                                        Spacer(Modifier.width(2.dp))
                                        Text(
                                            text = "${message.helpfulCount}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                        )
                                    }
                                }
                            }
                        }

                        if (isFaculty && currentUserRole == Constants.ROLE_FACULTY) {
                            TextButton(
                                onClick = { onMarkOfficial(!message.isOfficial) },
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Icon(
                                    imageVector = if (message.isOfficial) Icons.Default.StarBorder else Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(AskFacultyDimensions.iconSizeXs),
                                    tint = if (message.isOfficial) OfficialAnswer else Color.White.copy(alpha = 0.75f),
                                )
                                Spacer(Modifier.width(AskFacultySpacing.xs))
                                Text(
                                    text = if (message.isOfficial) "Remove" else "Mark Official",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (message.isOfficial) OfficialAnswer else Color.White.copy(alpha = 0.75f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reply Composer
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ReplyComposer(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachFile: () -> Unit,
    onAttachImage: () -> Unit,
    isSending: Boolean,
    isUploading: Boolean,
    uploadProgress: Float,
    attachmentName: String,
    onClearAttachment: () -> Unit,
) {
    Surface(
        shadowElevation = AskFacultyElevation.floating,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = AskFacultyElevation.low,
    ) {
        Column {
            // Upload progress bar
            if (isUploading) {
                LinearProgressIndicator(
                    progress = { uploadProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Attachment preview strip
            AnimatedVisibility(
                visible = attachmentName.isNotBlank(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = AskFacultySpacing.lg,
                                vertical = AskFacultySpacing.sm,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = null,
                            modifier = Modifier.size(AskFacultyDimensions.iconSizeXs),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Text(
                            text = attachmentName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        IconButton(
                            onClick = onClearAttachment,
                            modifier = Modifier.size(AskFacultyDimensions.minTouchTarget),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove attachment",
                                modifier = Modifier.size(AskFacultyDimensions.iconSizeXs),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }
            }

            // Composer row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = AskFacultySpacing.sm,
                        vertical = AskFacultySpacing.sm,
                    )
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.xs),
            ) {
                // Attach file button
                IconButton(onClick = onAttachFile) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach Document",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AskFacultyDimensions.iconSize),
                    )
                }

                // Attach image button
                IconButton(onClick = onAttachImage) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Attach Image",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AskFacultyDimensions.iconSize),
                    )
                }

                // Multi-line text field
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    placeholder = {
                        Text(
                            text = "Write a verified response…",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    modifier = Modifier.weight(1f),
                    maxLines = 5,
                    shape = RoundedCornerShape(AskFacultyRadius.xl),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium,
                )

                Spacer(Modifier.width(AskFacultySpacing.xs))

                // Send FAB
                FloatingActionButton(
                    onClick = onSend,
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = AskFacultyElevation.medium,
                        pressedElevation = AskFacultyElevation.high,
                    ),
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(AskFacultyDimensions.iconSizeSm),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send reply",
                            modifier = Modifier.size(AskFacultyDimensions.iconSizeSm),
                            tint = Color.White,
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AI Suggestion Panel — collapsible, indigo tinted, lightbulb header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AiPanel(
    summary: String,
    suggestedResponse: String,
    isLoading: Boolean,
    onSummarize: () -> Unit,
    onSuggestResponse: () -> Unit,
    onUseResponse: () -> Unit,
    onClose: () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(true) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 0f else 180f,
        animationSpec = tween(250),
        label = "chevron_rotate",
    )

    Card(
        shape = RoundedCornerShape(AskFacultyRadius.xl),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1456), // deep indigo tint
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(GradientStart.copy(alpha = 0.5f), GradientMid.copy(alpha = 0.5f)))
        ),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = AskFacultyElevation.medium),
    ) {
        Column(modifier = Modifier.padding(AskFacultySpacing.lg)) {
            // ── Panel header ─────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { isExpanded = !isExpanded },
            ) {
                // Lightbulb glow badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(listOf(Color(0xFFFACC15), Color(0xFFF59E0B)))
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(AskFacultyDimensions.iconSize),
                    )
                }
                Spacer(Modifier.width(AskFacultySpacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Teaching Assistant",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = "Powered by AskFaculty AI",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                }
                // Chevron expand/collapse
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(AskFacultyDimensions.iconSize)
                        .rotate(chevronRotation),
                )
                Spacer(Modifier.width(AskFacultySpacing.xs))
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(AskFacultyDimensions.minTouchTarget),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close AI panel",
                        modifier = Modifier.size(AskFacultyDimensions.iconSizeSm),
                        tint = Color.White.copy(alpha = 0.7f),
                    )
                }
            }

            // ── Expanded content ─────────────────────────────────────────────
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column {
                    Spacer(Modifier.height(AskFacultySpacing.md))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                    Spacer(Modifier.height(AskFacultySpacing.md))

                    if (isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFA78BFA),
                            trackColor = Color.White.copy(alpha = 0.1f),
                        )
                        Spacer(Modifier.height(AskFacultySpacing.sm))
                        Text(
                            text = "AI is analyzing the question…",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                    } else {
                        // Summary section
                        if (summary.isNotBlank()) {
                            AiSection(
                                label = "DOUBT SYNOPSIS",
                                content = summary,
                            )
                            Spacer(Modifier.height(AskFacultySpacing.md))
                        }

                        // Suggested response section
                        if (suggestedResponse.isNotBlank()) {
                            AiSection(
                                label = "SUGGESTED EXPLANATION DRAFT",
                                content = suggestedResponse,
                            )
                            Spacer(Modifier.height(AskFacultySpacing.md))
                            AskFacultyButton(
                                text = "Insert Draft into Reply",
                                onClick = onUseResponse,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = Icons.Default.Edit,
                            )
                            Spacer(Modifier.height(AskFacultySpacing.sm))
                        }

                        // Action buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedButton(
                                onClick = onSummarize,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(AskFacultyRadius.md),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White,
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color.White.copy(alpha = 0.35f),
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Summarize,
                                    contentDescription = null,
                                    modifier = Modifier.size(AskFacultyDimensions.iconSizeXs),
                                )
                                Spacer(Modifier.width(AskFacultySpacing.xs))
                                Text("Summarize", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = onSuggestResponse,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(AskFacultyRadius.md),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White,
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    Color.White.copy(alpha = 0.35f),
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(AskFacultyDimensions.iconSizeXs),
                                )
                                Spacer(Modifier.width(AskFacultySpacing.xs))
                                Text("Suggest Draft", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AI content section (label + body)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AiSection(label: String, content: String) {
    Surface(
        shape = RoundedCornerShape(AskFacultyRadius.md),
        color = Color.White.copy(alpha = 0.07f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(AskFacultySpacing.md)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA78BFA),
                letterSpacing = androidx.compose.ui.unit.TextUnit(0.6f, androidx.compose.ui.unit.TextUnitType.Sp),
            )
            Spacer(Modifier.height(AskFacultySpacing.xs))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Status sheet list item (used inside bottom sheet)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatusSheetItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(AskFacultyRadius.md),
        color = if (isSelected)
            accentColor.copy(alpha = 0.12f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.md),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AskFacultyDimensions.iconSize),
                tint = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(AskFacultyDimensions.iconSize),
                    tint = accentColor,
                )
            }
        }
    }
}
