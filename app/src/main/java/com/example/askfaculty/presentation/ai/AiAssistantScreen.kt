package com.example.askfaculty.presentation.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.askfaculty.core.ui.components.*
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    onNavigateBack: () -> Unit,
    onEscalateToFaculty: (String) -> Unit,
    viewModel: AiAssistantViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AskFaculty AI Assistant",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Concept explanations, study notes & MCQs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Disclaimer Banner ─────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AskFacultySpacing.md),
                shape = RoundedCornerShape(AskFacultyRadius.md),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
            ) {
                Row(
                    modifier = Modifier.padding(AskFacultySpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "AI outputs assist with study preparation and question formulation. They do not replace authoritative faculty answers.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }

            // ── Mode Switcher TabRow ──────────────────────────────────────────
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedMode.ordinal,
                edgePadding = AskFacultySpacing.md,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                Tab(
                    selected = uiState.selectedMode == AiToolMode.CONCEPT_EXPLAINER,
                    onClick = { viewModel.setMode(AiToolMode.CONCEPT_EXPLAINER) },
                    text = { Text("Concept Explainer") },
                )
                Tab(
                    selected = uiState.selectedMode == AiToolMode.QUESTION_REPHRASER,
                    onClick = { viewModel.setMode(AiToolMode.QUESTION_REPHRASER) },
                    text = { Text("Rephrase Question") },
                )
                Tab(
                    selected = uiState.selectedMode == AiToolMode.STUDY_NOTES,
                    onClick = { viewModel.setMode(AiToolMode.STUDY_NOTES) },
                    text = { Text("Study Notes") },
                )
                Tab(
                    selected = uiState.selectedMode == AiToolMode.PRACTICE_QUESTIONS,
                    onClick = { viewModel.setMode(AiToolMode.PRACTICE_QUESTIONS) },
                    text = { Text("Practice MCQs") },
                )
            }

            Spacer(Modifier.height(AskFacultySpacing.md))

            // ── Input Controls ────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AskFacultySpacing.lg),
                shape = RoundedCornerShape(AskFacultyRadius.xl),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = AskFacultyElevation.low),
            ) {
                Column(modifier = Modifier.padding(AskFacultySpacing.lg)) {
                    // Subject selection
                    Text(
                        text = "Select Academic Subject",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(AskFacultySpacing.xs))
                    var expandedSubject by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedSubject,
                        onExpandedChange = { expandedSubject = it },
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedSubject,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubject) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(AskFacultyRadius.md),
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSubject,
                            onDismissRequest = { expandedSubject = false },
                        ) {
                            Constants.DEFAULT_SUBJECTS.forEach { subj ->
                                DropdownMenuItem(
                                    text = { Text(subj) },
                                    onClick = {
                                        viewModel.setSubject(subj)
                                        expandedSubject = false
                                    },
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(AskFacultySpacing.md))

                    // Input field based on mode
                    if (uiState.selectedMode == AiToolMode.QUESTION_REPHRASER) {
                        Text(
                            text = "Draft Student Question",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(AskFacultySpacing.xs))
                        OutlinedTextField(
                            value = uiState.inputQuestion,
                            onValueChange = viewModel::setInputQuestion,
                            placeholder = { Text("Enter your rough or unclear doubt to improve clarity and structure…") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(AskFacultyRadius.md),
                        )
                    } else {
                        Text(
                            text = "Academic Concept or Topic",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(AskFacultySpacing.xs))
                        OutlinedTextField(
                            value = uiState.inputTopic,
                            onValueChange = viewModel::setInputTopic,
                            placeholder = { Text("e.g. Dijkstra's Algorithm, B-Trees, Normalization…") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(AskFacultyRadius.md),
                        )
                    }

                    Spacer(Modifier.height(AskFacultySpacing.lg))

                    // Action button
                    Button(
                        onClick = viewModel::processAiAction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(AskFacultyRadius.md),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(AskFacultySpacing.sm))
                            Text("Analyzing with AI…")
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(AskFacultySpacing.sm))
                            Text(
                                text = when (uiState.selectedMode) {
                                    AiToolMode.CONCEPT_EXPLAINER -> "Explain Concept"
                                    AiToolMode.QUESTION_REPHRASER -> "Rephrase & Polish"
                                    AiToolMode.STUDY_NOTES -> "Generate Revision Notes"
                                    AiToolMode.PRACTICE_QUESTIONS -> "Generate Practice MCQs"
                                },
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(AskFacultySpacing.lg))

            // ── AI Result Card ────────────────────────────────────────────────
            if (uiState.resultText.isNotBlank() || uiState.practiceQuestions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AskFacultySpacing.lg),
                    shape = RoundedCornerShape(AskFacultyRadius.xl),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1456), // deep indigo
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = AskFacultyElevation.medium),
                ) {
                    Column(modifier = Modifier.padding(AskFacultySpacing.lg)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Text(
                                text = "AI Generated Response",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }

                        Spacer(Modifier.height(AskFacultySpacing.md))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                        Spacer(Modifier.height(AskFacultySpacing.md))

                        if (uiState.practiceQuestions.isNotEmpty()) {
                            uiState.practiceQuestions.forEachIndexed { idx, q ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = AskFacultySpacing.xs),
                                    shape = RoundedCornerShape(AskFacultyRadius.md),
                                    color = Color.White.copy(alpha = 0.08f),
                                ) {
                                    Text(
                                        text = q,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        modifier = Modifier.padding(AskFacultySpacing.md),
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = uiState.resultText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.95f),
                            )
                        }

                        Spacer(Modifier.height(AskFacultySpacing.lg))

                        // Escalate button
                        AskFacultyButton(
                            text = "Escalate / Ask Faculty Directly",
                            onClick = {
                                val text = if (uiState.selectedMode == AiToolMode.QUESTION_REPHRASER) {
                                    uiState.resultText
                                } else {
                                    "I have a doubt regarding ${uiState.inputTopic.ifBlank { uiState.selectedSubject }}: "
                                }
                                onEscalateToFaculty(text)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = Icons.Default.School,
                        )
                    }
                }
            }

            Spacer(Modifier.height(AskFacultySpacing.xxxl))
        }
    }
}
