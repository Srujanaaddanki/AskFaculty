package com.example.askfaculty.presentation.profile

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.askfaculty.core.ui.components.*
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.domain.model.OfficeHour
import com.example.askfaculty.domain.repository.AuthRepository
import com.example.askfaculty.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── AvailabilityUiState ───────────────────────────────────────────────────────

data class AvailabilityUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val availability: String = Constants.AVAILABILITY_AVAILABLE,
    val officeHours: List<OfficeHour> = emptyList(),
    val saved: Boolean = false,
    val error: String? = null,
)

// ── AvailabilityViewModel ─────────────────────────────────────────────────────

@HiltViewModel
class AvailabilityViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AvailabilityUiState())
    val uiState: StateFlow<AvailabilityUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.filterNotNull().first().let { user ->
                profileRepository.getUserProfile(user.uid).fold(
                    onSuccess = { profile ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                availability = profile.availability.ifBlank { Constants.AVAILABILITY_AVAILABLE },
                                officeHours = profile.officeHours,
                            )
                        }
                    },
                    onFailure = {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                availability = user.availability.ifBlank { Constants.AVAILABILITY_AVAILABLE },
                                officeHours = user.officeHours,
                            )
                        }
                    },
                )
            }
        }
    }

    fun setAvailability(status: String) {
        _uiState.update { it.copy(availability = status) }
    }

    fun addOfficeHour(officeHour: OfficeHour) {
        _uiState.update { it.copy(officeHours = it.officeHours + officeHour) }
    }

    fun removeOfficeHour(officeHour: OfficeHour) {
        _uiState.update { it.copy(officeHours = it.officeHours - officeHour) }
    }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            val user = authRepository.currentUser.filterNotNull().first()
            _uiState.update { it.copy(isSaving = true, error = null) }
            profileRepository.updateAvailability(
                uid = user.uid,
                availability = state.availability,
                officeHours = state.officeHours,
            ).fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, saved = true) } },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isSaving = false, error = "Save failed: ${e.message}")
                    }
                },
            )
        }
    }
}

// ── AvailabilityScreen ────────────────────────────────────────────────────────

private val DAYS_OF_WEEK = listOf(
    "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityScreen(
    onBack: () -> Unit,
    viewModel: AvailabilityViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onBack()
    }

    if (showAddDialog) {
        AddOfficeHourDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { officeHour ->
                viewModel.addOfficeHour(officeHour)
                showAddDialog = false
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Availability") },
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 32.dp),
                ) {
                    // ── Current Status section ───────────────────────────────
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                        ) {
                            Text(
                                text = "Current Status",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 12.dp),
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                AvailabilityChip(
                                    label = "Available",
                                    color = Color(0xFF43A047),
                                    selected = uiState.availability == Constants.AVAILABILITY_AVAILABLE,
                                    onClick = { viewModel.setAvailability(Constants.AVAILABILITY_AVAILABLE) },
                                )
                                AvailabilityChip(
                                    label = "Busy",
                                    color = Color(0xFFE53935),
                                    selected = uiState.availability == Constants.AVAILABILITY_BUSY,
                                    onClick = { viewModel.setAvailability(Constants.AVAILABILITY_BUSY) },
                                )
                                AvailabilityChip(
                                    label = "Away",
                                    color = Color(0xFFFB8C00),
                                    selected = uiState.availability == Constants.AVAILABILITY_AWAY,
                                    onClick = { viewModel.setAvailability(Constants.AVAILABILITY_AWAY) },
                                )
                            }
                        }
                    }

                    item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

                    // ── Office Hours section header ───────────────────────────
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Office Hours",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            TextButton(onClick = { showAddDialog = true }) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add")
                            }
                        }
                    }

                    // ── Existing office hours list ───────────────────────────
                    if (uiState.officeHours.isEmpty()) {
                        item {
                            Text(
                                text = "No office hours added yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    } else {
                        items(uiState.officeHours) { officeHour ->
                            OfficeHourRow(
                                officeHour = officeHour,
                                onDelete = { viewModel.removeOfficeHour(officeHour) },
                            )
                        }
                    }

                    item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }

                    // ── Error message ────────────────────────────────────────
                    if (uiState.error != null) {
                        item {
                            Text(
                                text = uiState.error ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }

                    // ── Save button ──────────────────────────────────────────
                    item {
                        PrimaryButton(
                            text = "Save",
                            isLoading = uiState.isSaving,
                            enabled = !uiState.isSaving,
                            onClick = { viewModel.save() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

// ── AvailabilityChip ──────────────────────────────────────────────────────────

@Composable
private fun AvailabilityChip(
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Circle,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(10.dp),
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.15f),
            selectedLabelColor = color,
            selectedLeadingIconColor = color,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = color,
        ),
    )
}

// ── OfficeHourRow ─────────────────────────────────────────────────────────────

@Composable
private fun OfficeHourRow(
    officeHour: OfficeHour,
    onDelete: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(officeHour.day, fontWeight = FontWeight.Medium) },
        supportingContent = {
            Text(
                "${officeHour.startTime} – ${officeHour.endTime}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove office hour",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        },
    )
}

// ── AddOfficeHourDialog ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddOfficeHourDialog(
    onDismiss: () -> Unit,
    onConfirm: (OfficeHour) -> Unit,
) {
    var selectedDay by remember { mutableStateOf(DAYS_OF_WEEK.first()) }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var dayDropdownExpanded by remember { mutableStateOf(false) }

    val isValid = startTime.isNotBlank() && endTime.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Office Hours") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Day selector
                ExposedDropdownMenuBox(
                    expanded = dayDropdownExpanded,
                    onExpandedChange = { dayDropdownExpanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedDay,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Day") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayDropdownExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = dayDropdownExpanded,
                        onDismissRequest = { dayDropdownExpanded = false },
                    ) {
                        DAYS_OF_WEEK.forEach { day ->
                            DropdownMenuItem(
                                text = { Text(day) },
                                onClick = {
                                    selectedDay = day
                                    dayDropdownExpanded = false
                                },
                            )
                        }
                    }
                }

                // Start time field
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Start Time") },
                    placeholder = { Text("e.g. 09:00 AM") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                // End time field
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("End Time") },
                    placeholder = { Text("e.g. 11:00 AM") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        OfficeHour(
                            day = selectedDay,
                            startTime = startTime.trim(),
                            endTime = endTime.trim(),
                        )
                    )
                },
                enabled = isValid,
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
