package com.example.askfaculty.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.askfaculty.core.ui.components.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val subjectFocusRequester = remember { FocusRequester() }

    // Navigate back after successful save
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.save()
                        },
                        enabled = !uiState.isSaving,
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingState()
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // ── Basic info fields ────────────────────────────────────
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::setName,
                        label = { Text("Full Name *") },
                        singleLine = true,
                        isError = uiState.name.isBlank(),
                        supportingText = if (uiState.name.isBlank()) {
                            { Text("Name is required") }
                        } else null,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = uiState.designation,
                        onValueChange = viewModel::setDesignation,
                        label = { Text("Designation") },
                        placeholder = { Text("e.g. Associate Professor") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = uiState.department,
                        onValueChange = viewModel::setDepartment,
                        label = { Text("Department") },
                        placeholder = { Text("e.g. Computer Science") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = uiState.college,
                        onValueChange = viewModel::setCollege,
                        label = { Text("College / Institution") },
                        placeholder = { Text("e.g. MIT College of Engineering") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // ── Subjects taught section ──────────────────────────────
                    HorizontalDivider()
                    Text(
                        text = "Subjects Taught",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    // Subject input with trailing "Add" icon
                    OutlinedTextField(
                        value = uiState.subjectInput,
                        onValueChange = viewModel::setSubjectInput,
                        label = { Text("Add a subject") },
                        placeholder = { Text("e.g. Data Structures") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            viewModel.addSubject()
                            // Keep focus on field so user can add more subjects
                        }),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    viewModel.addSubject()
                                    focusManager.clearFocus()
                                },
                                enabled = uiState.subjectInput.isNotBlank(),
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add subject")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(subjectFocusRequester),
                    )

                    // Chips for existing subjects
                    if (uiState.subjects.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            uiState.subjects.forEach { subject ->
                                InputChip(
                                    selected = false,
                                    onClick = {},
                                    label = { Text(subject) },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { viewModel.removeSubject(subject) },
                                            modifier = Modifier.size(18.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove $subject",
                                                modifier = Modifier.size(14.dp),
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }

                    // ── Error message ────────────────────────────────────────
                    if (uiState.error != null) {
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── Save button ──────────────────────────────────────────
                    PrimaryButton(
                        text = "Save Changes",
                        isLoading = uiState.isSaving,
                        enabled = uiState.name.isNotBlank() && !uiState.isSaving,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.save()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
