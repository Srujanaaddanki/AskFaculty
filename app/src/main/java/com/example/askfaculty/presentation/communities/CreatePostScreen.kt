package com.example.askfaculty.presentation.communities

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.askfaculty.core.ui.components.PrimaryButton
import com.example.askfaculty.core.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    communityId: String,
    onBack: () -> Unit,
    onCreated: () -> Unit,
    viewModel: CreatePostViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // File attachment picker (any file type)
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Use last path segment as display name; caller can refine with ContentResolver
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "attachment"
            viewModel.setAttachment(uri, name, Constants.ATTACHMENT_TYPE_FILE)
        }
    }

    // Image picker
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "image.jpg"
            viewModel.setAttachment(uri, name, Constants.ATTACHMENT_TYPE_IMAGE)
        }
    }

    // Navigate away once the post is created
    LaunchedEffect(uiState.created) {
        if (uiState.created) onCreated()
    }

    // Show error in snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Post") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Author row + content field ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Avatar placeholder
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Author avatar",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                // Multi-line, borderless text field
                TextField(
                    value = uiState.content,
                    onValueChange = viewModel::setContent,
                    placeholder = {
                        Text(
                            text = "What would you like to share?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        errorIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    minLines = 4,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    isError = uiState.contentError != null,
                )
            }

            // Content error text
            if (uiState.contentError != null) {
                Text(
                    text = uiState.contentError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 56.dp),
                )
            }

            HorizontalDivider()

            // ── Announcement toggle ────────────────────────────────────────────
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Announcement",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = "Pin this as an announcement",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = uiState.isAnnouncement,
                        onCheckedChange = viewModel::setAnnouncement,
                    )
                }
            }

            // ── Attachment section ─────────────────────────────────────────────
            if (uiState.attachmentName.isNotBlank()) {
                // Show chip with attachment name and remove button
                InputChip(
                    selected = false,
                    onClick = {},
                    label = {
                        Text(
                            text = uiState.attachmentName,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (uiState.attachmentType == Constants.ATTACHMENT_TYPE_IMAGE)
                                Icons.Default.Image
                            else
                                Icons.Default.AttachFile,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = viewModel::clearAttachment,
                            modifier = Modifier.size(18.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove attachment",
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    },
                )
            } else {
                // Show attach file / attach image icon buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Add attachment:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    IconButton(onClick = { fileLauncher.launch("*/*") }) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach file",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(onClick = { imageLauncher.launch("image/*") }) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Attach image",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            // ── Upload progress ────────────────────────────────────────────────
            if (uiState.isUploading) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Uploading… ${(uiState.uploadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LinearProgressIndicator(
                        progress = { uiState.uploadProgress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // ── Post button ────────────────────────────────────────────────────
            PrimaryButton(
                text = "Post",
                onClick = viewModel::createPost,
                isLoading = uiState.isLoading || uiState.isUploading,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
