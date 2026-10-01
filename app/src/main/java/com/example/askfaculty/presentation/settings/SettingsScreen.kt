package com.example.askfaculty.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.askfaculty.data.repository.ThemeMode
import com.example.askfaculty.ui.theme.AskFacultyRadius
import com.example.askfaculty.ui.theme.AskFacultySpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings & Preferences",
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .verticalScroll(rememberScrollState())
                .padding(AskFacultySpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AskFacultySpacing.xl),
        ) {
            // ── Appearance ────────────────────────────────────────────
            Text(
                "APPEARANCE",
                style         = MaterialTheme.typography.labelMedium,
                fontWeight    = FontWeight.Bold,
                color         = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
            )

            Card(
                shape  = RoundedCornerShape(AskFacultyRadius.lg),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant),
                ),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                ListItem(
                    headlineContent = { Text("Theme Mode", fontWeight = FontWeight.SemiBold) },
                    supportingContent = {
                        Text(
                            when (uiState.themeMode) {
                                ThemeMode.SYSTEM -> "System Default"
                                ThemeMode.LIGHT  -> "Light Theme"
                                ThemeMode.DARK   -> "Dark Theme"
                            }
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = when (uiState.themeMode) {
                                ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                ThemeMode.LIGHT  -> Icons.Default.LightMode
                                ThemeMode.DARK   -> Icons.Default.DarkMode
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    trailingContent = {
                        Icon(Icons.Default.ChevronRight, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    modifier = Modifier.clickable { showThemeDialog = true },
                    colors   = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }

            // ── Notifications ─────────────────────────────────────────
            Text(
                "NOTIFICATIONS & ALERTS",
                style         = MaterialTheme.typography.labelMedium,
                fontWeight    = FontWeight.Bold,
                color         = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
            )

            Card(
                shape  = RoundedCornerShape(AskFacultyRadius.lg),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant),
                ),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Column {
                    ListItem(
                        headlineContent   = { Text("Push Notifications", fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Receive instant alerts for student doubts") },
                        leadingContent    = {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Switch(
                                checked         = uiState.notificationsEnabled,
                                onCheckedChange = viewModel::toggleNotifications,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor      = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor      = MaterialTheme.colorScheme.primary,
                                    uncheckedTrackColor    = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = AskFacultySpacing.lg))
                    ListItem(
                        headlineContent   = { Text("Sound & Vibration", fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Play acoustic tone on incoming doubts") },
                        leadingContent    = {
                            Icon(Icons.Default.VolumeUp, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Switch(
                                checked         = uiState.soundEnabled,
                                onCheckedChange = viewModel::toggleSound,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor   = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor   = MaterialTheme.colorScheme.primary,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }

            // ── About & Security ──────────────────────────────────────
            Text(
                "ABOUT & SECURITY",
                style         = MaterialTheme.typography.labelMedium,
                fontWeight    = FontWeight.Bold,
                color         = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
            )

            Card(
                shape  = RoundedCornerShape(AskFacultyRadius.lg),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant),
                ),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Column {
                    ListItem(
                        headlineContent   = { Text("Application Version", fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("v2.1.0 (Production Academic Edition)") },
                        leadingContent    = {
                            Icon(Icons.Default.Info, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = AskFacultySpacing.lg))
                    ListItem(
                        headlineContent   = { Text("Security Architecture", fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Role-based institutional access with 5-digit Faculty ID") },
                        leadingContent    = {
                            Icon(Icons.Default.Security, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }
        }
    }

    // ── Theme Dialog ────────────────────────────────────────────────────────
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title            = { Text("Select Appearance Theme", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    ThemeMode.values().forEach { mode ->
                        val isSelected = uiState.themeMode == mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = AskFacultySpacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick  = {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                },
                            )
                            Spacer(Modifier.width(AskFacultySpacing.md))
                            Text(
                                text = when (mode) {
                                    ThemeMode.SYSTEM -> "System Default (Auto)"
                                    ThemeMode.LIGHT  -> "Light Mode"
                                    ThemeMode.DARK   -> "Dark Mode"
                                },
                                style      = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Done") }
            },
            shape = RoundedCornerShape(AskFacultyRadius.xl),
        )
    }
}
