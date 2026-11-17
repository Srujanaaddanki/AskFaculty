package com.example.askfaculty.presentation.doubts

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.askfaculty.core.ui.components.AskFacultyAvatar
import com.example.askfaculty.core.ui.components.AskFacultyButton
import com.example.askfaculty.core.ui.components.ErrorBanner
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.ui.theme.AskFacultyRadius
import com.example.askfaculty.ui.theme.AskFacultySpacing
import com.example.askfaculty.ui.theme.SuccessColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskDoubtScreen(
    onBack: () -> Unit,
    onDoubtSubmitted: (String) -> Unit,
    viewModel: AskDoubtViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var subjectExpanded by remember { mutableStateOf(false) }
    var facultyExpanded by remember { mutableStateOf(false) }
    var showCodeEditor by remember { mutableStateOf(false) }

    // File pickers
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setAttachment(it, "image.jpg", Constants.ATTACHMENT_IMAGE) }
    }

    val docLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setAttachment(it, "document.pdf", Constants.ATTACHMENT_PDF) }
    }

    // Speech to text voice launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                val current = uiState.question
                val combined = if (current.isBlank()) spokenText else "$current $spokenText"
                viewModel.setQuestion(combined)
            }
        }
    }

    LaunchedEffect(uiState.isSuccess, uiState.createdDoubtId) {
        if (uiState.isSuccess && uiState.createdDoubtId != null) {
            onDoubtSubmitted(uiState.createdDoubtId!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ask a Doubt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.md),
            verticalArrangement = Arrangement.spacedBy(AskFacultySpacing.md),
        ) {
            // ── 1. Select Subject ──────────────────────────────────────
            Text(
                text = "1. Select Academic Subject",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            ExposedDropdownMenuBox(
                expanded = subjectExpanded,
                onExpandedChange = { subjectExpanded = it },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = uiState.selectedSubject,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Course Subject") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(AskFacultyRadius.md),
                )
                ExposedDropdownMenu(
                    expanded = subjectExpanded,
                    onDismissRequest = { subjectExpanded = false },
                ) {
                    uiState.availableSubjects.forEach { sub ->
                        DropdownMenuItem(
                            text = { Text(sub) },
                            onClick = {
                                viewModel.setSubject(sub)
                                subjectExpanded = false
                            }
                        )
                    }
                }
            }

            // ── 2. Select Faculty Member ───────────────────────────────
            Text(
                text = "2. Select Faculty Mentor",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            ExposedDropdownMenuBox(
                expanded = facultyExpanded,
                onExpandedChange = { facultyExpanded = it },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = uiState.selectedFaculty?.name ?: "Select Faculty",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Assigned Professor") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = facultyExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(AskFacultyRadius.md),
                )
                ExposedDropdownMenu(
                    expanded = facultyExpanded,
                    onDismissRequest = { facultyExpanded = false },
                ) {
                    uiState.availableFaculty.forEach { fac ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column {
                                        Text(fac.name, fontWeight = FontWeight.Bold)
                                        Text(fac.designation, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(AskFacultyRadius.xs),
                                        color = if (fac.availability == Constants.AVAILABILITY_AVAILABLE) SuccessColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer,
                                    ) {
                                        Text(
                                            text = fac.availability,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (fac.availability == Constants.AVAILABILITY_AVAILABLE) SuccessColor else MaterialTheme.colorScheme.error,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        )
                                    }
                                }
                            },
                            onClick = {
                                viewModel.setFaculty(fac)
                                facultyExpanded = false
                            }
                        )
                    }
                }
            }

            // ── 3. Topic & Question ────────────────────────────────────
            Text(
                text = "3. Topic & Doubt Details",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            OutlinedTextField(
                value = uiState.topic,
                onValueChange = viewModel::setTopic,
                label = { Text("Specific Topic / Chapter") },
                placeholder = { Text("e.g. Dijkstra Shortest Path or Pointers") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AskFacultyRadius.md),
            )

            // Question body with AI helpers
            OutlinedTextField(
                value = uiState.question,
                onValueChange = viewModel::setQuestion,
                label = { Text("Describe your doubt in detail") },
                placeholder = { Text("Explain clearly where you are stuck, what you tried, and what you expected...") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AskFacultyRadius.md),
            )

            // AI and Voice Helper Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.xs),
            ) {
                OutlinedButton(
                    onClick = viewModel::rephraseQuestionWithAi,
                    enabled = !uiState.isAiProcessing && uiState.question.isNotBlank(),
                    shape = RoundedCornerShape(AskFacultyRadius.pill),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("AI Rephrase", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = viewModel::checkSimilarDoubts,
                    enabled = !uiState.isAiProcessing,
                    shape = RoundedCornerShape(AskFacultyRadius.pill),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Find Similar", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your doubt...")
                            }
                            speechLauncher.launch(intent)
                        } catch (_: Exception) {}
                    },
                    shape = RoundedCornerShape(AskFacultyRadius.pill),
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Input", modifier = Modifier.size(18.dp))
                }
            }

            // AI Rephrase Suggestion Card
            AnimatedVisibility(visible = uiState.aiRephraseSuggestion != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AskFacultyRadius.md),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                ) {
                    Column(modifier = Modifier.padding(AskFacultySpacing.md)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("✨ AI Improved Question Draft:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = viewModel::dismissAiRephrase, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(uiState.aiRephraseSuggestion ?: "", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        AskFacultyButton(
                            text = "Apply Suggestion",
                            onClick = viewModel::applyAiRephrase,
                            modifier = Modifier.height(38.dp),
                        )
                    }
                }
            }

            // Similar doubts notification pill
            AnimatedVisibility(visible = uiState.similarDoubtsFound > 0) {
                Surface(
                    shape = RoundedCornerShape(AskFacultyRadius.sm),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(AskFacultySpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Spacer(Modifier.width(AskFacultySpacing.xs))
                        Text(
                            text = "Found ${uiState.similarDoubtsFound} similar questions resolved in the knowledge base.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                }
            }

            // ── 4. Code Snippet Section ────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(AskFacultySpacing.xs))
                    Text("Attach Code Snippet", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                Switch(checked = showCodeEditor, onCheckedChange = { showCodeEditor = it })
            }

            if (showCodeEditor) {
                // Language selector pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    listOf("Java", "Python", "C++", "Kotlin", "SQL").forEach { lang ->
                        FilterChip(
                            selected = uiState.codeLanguage == lang,
                            onClick = { viewModel.setCodeLanguage(lang) },
                            label = { Text(lang, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }

                OutlinedTextField(
                    value = uiState.codeSnippet,
                    onValueChange = viewModel::setCodeSnippet,
                    placeholder = { Text("// Paste code snippet here...") },
                    minLines = 4,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AskFacultyRadius.md),
                )
            }

            // ── 5. Attachments ─────────────────────────────────────────
            Text("4. Attachments (Images or Documents)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm)) {
                OutlinedButton(
                    onClick = { imageLauncher.launch("image/*") },
                    shape = RoundedCornerShape(AskFacultyRadius.md),
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Attach Image")
                }
                OutlinedButton(
                    onClick = { docLauncher.launch("*/*") },
                    shape = RoundedCornerShape(AskFacultyRadius.md),
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Attach PDF / Doc")
                }
            }

            if (uiState.attachmentName.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(AskFacultyRadius.md),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = AskFacultySpacing.md, vertical = AskFacultySpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(AskFacultySpacing.sm))
                            Text(uiState.attachmentName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        }
                        IconButton(onClick = viewModel::clearAttachment, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            if (uiState.isUploading) {
                LinearProgressIndicator(progress = { uiState.uploadProgress }, modifier = Modifier.fillMaxWidth())
            }

            // ── 6. Visibility & Priority ───────────────────────────────
            Card(
                shape = RoundedCornerShape(AskFacultyRadius.md),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
                ),
            ) {
                Column(modifier = Modifier.padding(AskFacultySpacing.md)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (uiState.isPrivate) Icons.Default.Lock else Icons.Default.Public,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.width(AskFacultySpacing.sm))
                            Column {
                                Text(
                                    text = if (uiState.isPrivate) "Private Doubt (Faculty Only)" else "Community Discussion",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = if (uiState.isPrivate) "Only the assigned professor sees this" else "Visible to classmates in course space",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Switch(checked = uiState.isPrivate, onCheckedChange = viewModel::setPrivate)
                    }

                    Spacer(Modifier.height(AskFacultySpacing.md))

                    Text("Priority Level:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm)) {
                        listOf(Constants.PRIORITY_HIGH to "High", Constants.PRIORITY_MEDIUM to "Medium", Constants.PRIORITY_LOW to "Low").forEach { (pri, label) ->
                            FilterChip(
                                selected = uiState.priority == pri,
                                onClick = { viewModel.setPriority(pri) },
                                label = { Text(label) },
                            )
                        }
                    }
                }
            }

            // Error banner
            AnimatedVisibility(visible = uiState.error != null) {
                ErrorBanner(message = uiState.error ?: "", onDismiss = viewModel::clearError)
            }

            Spacer(Modifier.height(AskFacultySpacing.sm))

            // Submit Button
            AskFacultyButton(
                text = "Submit Doubt to Faculty",
                onClick = viewModel::submitDoubt,
                isLoading = uiState.isSubmitting,
                enabled = uiState.topic.isNotBlank() && uiState.question.isNotBlank() && !uiState.isSubmitting,
            )

            Spacer(Modifier.height(AskFacultySpacing.xl))
        }
    }
}
