package com.example.askfaculty.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.askfaculty.core.ui.components.AskFacultyButton
import com.example.askfaculty.core.ui.components.AskFacultyLogo
import com.example.askfaculty.core.ui.components.ErrorBanner
import com.example.askfaculty.core.util.Constants
import com.example.askfaculty.domain.model.User
import com.example.askfaculty.ui.theme.AskFacultyRadius
import com.example.askfaculty.ui.theme.AskFacultySpacing

@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit,
    onNavigateToRegister: () -> Unit,
    onForgotPassword: (isStudent: Boolean) -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    // Tab: 0 -> Student, 1 -> Faculty
    var selectedTab by remember { mutableIntStateOf(0) }

    // Student fields
    var studentId by remember { mutableStateOf("22CS104") }
    var studentPassword by remember { mutableStateOf("Student@123") }
    var studentPasswordVisible by remember { mutableStateOf(false) }

    // Faculty fields
    var facultyId by remember { mutableStateOf("30456") }
    var facultyPassword by remember { mutableStateOf("Gow@1415") }
    var facultyPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.user) {
        uiState.user?.let { onLoginSuccess(it) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(),
            contentAlignment = Alignment.Center,
        ) {
            // Subtle top background gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                Color.Transparent,
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AskFacultySpacing.xl, vertical = AskFacultySpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Spacer(Modifier.height(AskFacultySpacing.md))

                // ── Brand Logo ─────────────────────────────────────────
                AskFacultyLogo(iconSize = 64.dp, showSubtitle = true)

                Spacer(Modifier.height(AskFacultySpacing.lg))

                // ── Role Tab Selector (Student vs Faculty) ─────────────
                Surface(
                    shape = RoundedCornerShape(AskFacultyRadius.lg),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier
                                    .tabIndicatorOffset(tabPositions[selectedTab])
                                    .clip(RoundedCornerShape(AskFacultyRadius.pill)),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        },
                        divider = {},
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0; viewModel.clearError() },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.School,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(AskFacultySpacing.xs))
                                    Text("Student Portal", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1; viewModel.clearError() },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Badge,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(AskFacultySpacing.xs))
                                    Text("Faculty Portal", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(AskFacultySpacing.md))

                // ── Sign-In Card ───────────────────────────────────────
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(AskFacultyRadius.xxl),
                    colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border    = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                        )
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AskFacultySpacing.xl),
                    ) {
                        if (selectedTab == 0) {
                            // ── STUDENT LOGIN ──
                            Text(
                                text       = "Student Sign In",
                                style      = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(AskFacultySpacing.xs))
                            Text(
                                text  = "Enter your College ID / Roll No and password",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Spacer(Modifier.height(AskFacultySpacing.lg))

                            OutlinedTextField(
                                value = studentId,
                                onValueChange = { studentId = it; viewModel.clearError() },
                                label = { Text("Student ID / Roll No") },
                                placeholder = { Text("e.g. 22CS104 or email") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.School,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp),
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next,
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                ),
                                shape = RoundedCornerShape(AskFacultyRadius.md),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Spacer(Modifier.height(AskFacultySpacing.md))

                            OutlinedTextField(
                                value = studentPassword,
                                onValueChange = { studentPassword = it; viewModel.clearError() },
                                label = { Text("Password") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp),
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { studentPasswordVisible = !studentPasswordVisible }) {
                                        Icon(
                                            imageVector = if (studentPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (studentPasswordVisible) "Hide password" else "Show password",
                                        )
                                    }
                                },
                                visualTransformation = if (studentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (studentId.isNotBlank() && studentPassword.isNotBlank()) {
                                            viewModel.loginWithStudentId(studentId, studentPassword)
                                        }
                                    },
                                ),
                                shape = RoundedCornerShape(AskFacultyRadius.md),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            // Forgot password row
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = AskFacultySpacing.xs),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                TextButton(
                                    onClick = { onForgotPassword(true) },
                                    contentPadding = PaddingValues(horizontal = AskFacultySpacing.xs, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = "Forgot Password?",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }

                            // Error banner
                            AnimatedVisibility(
                                visible = uiState.error != null,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                Column {
                                    Spacer(Modifier.height(AskFacultySpacing.sm))
                                    ErrorBanner(message = uiState.error ?: "", onDismiss = viewModel::clearError)
                                }
                            }

                            Spacer(Modifier.height(AskFacultySpacing.md))

                            AskFacultyButton(
                                text = "Sign In as Student",
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.loginWithStudentId(studentId, studentPassword)
                                },
                                isLoading = uiState.isLoading,
                                enabled = studentId.isNotBlank() && studentPassword.isNotBlank(),
                            )

                            Spacer(Modifier.height(AskFacultySpacing.md))

                            // Register navigation link
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Don't have an account?",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                TextButton(onClick = onNavigateToRegister) {
                                    Text(
                                        text = "Register as Student",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }

                        } else {
                            // ── FACULTY LOGIN ──
                            Text(
                                text       = "Faculty Sign In",
                                style      = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(AskFacultySpacing.xs))
                            Text(
                                text  = "Enter your 5-digit institutional ID and password",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Spacer(Modifier.height(AskFacultySpacing.lg))

                            OutlinedTextField(
                                value = facultyId,
                                onValueChange = { input ->
                                    val filtered = input.filter { it.isDigit() }.take(5)
                                    facultyId = filtered
                                    viewModel.clearError()
                                },
                                label = { Text("Faculty ID") },
                                placeholder = { Text("5-digit ID  e.g. 30456") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp),
                                    )
                                },
                                supportingText = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = if (facultyId.isNotEmpty() && facultyId.length < 5) "Must be 5 digits" else "",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                        Text(
                                            text = "${facultyId.length}/5",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next,
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                ),
                                shape = RoundedCornerShape(AskFacultyRadius.md),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Spacer(Modifier.height(AskFacultySpacing.sm))

                            OutlinedTextField(
                                value = facultyPassword,
                                onValueChange = { facultyPassword = it; viewModel.clearError() },
                                label = { Text("Password") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp),
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { facultyPasswordVisible = !facultyPasswordVisible }) {
                                        Icon(
                                            imageVector = if (facultyPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (facultyPasswordVisible) "Hide password" else "Show password",
                                        )
                                    }
                                },
                                visualTransformation = if (facultyPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (facultyId.length == 5 && facultyPassword.isNotBlank()) {
                                            viewModel.loginWithFacultyId(facultyId, facultyPassword)
                                        }
                                    },
                                ),
                                shape = RoundedCornerShape(AskFacultyRadius.md),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = AskFacultySpacing.xs),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                TextButton(
                                    onClick = { onForgotPassword(false) },
                                    contentPadding = PaddingValues(horizontal = AskFacultySpacing.xs, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = "Forgot Password?",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = uiState.error != null,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                Column {
                                    Spacer(Modifier.height(AskFacultySpacing.sm))
                                    ErrorBanner(message = uiState.error ?: "", onDismiss = viewModel::clearError)
                                }
                            }

                            Spacer(Modifier.height(AskFacultySpacing.md))

                            AskFacultyButton(
                                text = "Sign In as Faculty",
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.loginWithFacultyId(facultyId, facultyPassword)
                                },
                                isLoading = uiState.isLoading,
                                enabled = facultyId.length == 5 && facultyPassword.isNotBlank(),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(AskFacultySpacing.lg))

                // Demo quick login pills for evaluation
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AskFacultyRadius.lg),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ) {
                    Column(
                        modifier = Modifier.padding(AskFacultySpacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "💡 Quick Test Credentials",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(AskFacultySpacing.xs))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            Surface(
                                shape = RoundedCornerShape(AskFacultyRadius.sm),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.clickable {
                                    selectedTab = 0
                                    studentId = "22CS104"
                                    studentPassword = "Student@123"
                                }
                            ) {
                                Text(
                                    text = "Student: 22CS104",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(AskFacultyRadius.sm),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.clickable {
                                    selectedTab = 1
                                    facultyId = "30456"
                                    facultyPassword = "Gow@1415"
                                }
                            ) {
                                Text(
                                    text = "Faculty: 30456",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(AskFacultySpacing.lg))
            }
        }
    }
}
