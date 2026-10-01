package com.example.askfaculty.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.askfaculty.ui.theme.AskFacultyRadius
import com.example.askfaculty.ui.theme.AskFacultySpacing

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onForgotPassword: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var facultyId by remember { mutableStateOf("30456") }
    var password by remember { mutableStateOf("Gow@1415") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.user) {
        if (uiState.user != null) onLoginSuccess()
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
            // Subtle background gradient at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
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
                Spacer(Modifier.height(AskFacultySpacing.xl))

                // ── Brand Logo ─────────────────────────────────────────
                AskFacultyLogo(iconSize = 72.dp, showSubtitle = true)

                Spacer(Modifier.height(AskFacultySpacing.xl))

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
                        // Card header
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

                        Spacer(Modifier.height(AskFacultySpacing.xl))

                        // Faculty ID field
                        OutlinedTextField(
                            value = facultyId,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() }.take(5)
                                facultyId = filtered
                                viewModel.clearError()
                            },
                            label       = { Text("Faculty ID") },
                            placeholder = { Text("5-digit ID  e.g. 10245") },
                            leadingIcon = {
                                Icon(
                                    imageVector        = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.primary,
                                    modifier           = Modifier.size(24.dp),
                                )
                            },
                            supportingText = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text  = if (facultyId.isNotEmpty() && facultyId.length < 5) "Must be 5 digits" else "",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                    Text(
                                        text  = "${facultyId.length}/5",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction    = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            ),
                            shape      = RoundedCornerShape(AskFacultyRadius.md),
                            singleLine = true,
                            modifier   = Modifier.fillMaxWidth(),
                            isError    = uiState.error != null,
                            colors     = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor    = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor  = MaterialTheme.colorScheme.outline,
                                errorBorderColor      = MaterialTheme.colorScheme.error,
                                focusedLabelColor     = MaterialTheme.colorScheme.primary,
                                cursorColor           = MaterialTheme.colorScheme.primary,
                            ),
                        )

                        Spacer(Modifier.height(AskFacultySpacing.md))

                        // Password field
                        OutlinedTextField(
                            value         = password,
                            onValueChange = { password = it; viewModel.clearError() },
                            label         = { Text("Password") },
                            leadingIcon   = {
                                Icon(
                                    imageVector        = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.primary,
                                    modifier           = Modifier.size(24.dp),
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction    = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (facultyId.length == 5 && password.isNotBlank()) {
                                        viewModel.loginWithFacultyId(facultyId, password)
                                    }
                                },
                            ),
                            shape      = RoundedCornerShape(AskFacultyRadius.md),
                            singleLine = true,
                            modifier   = Modifier.fillMaxWidth(),
                            isError    = uiState.error != null,
                            colors     = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor    = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor  = MaterialTheme.colorScheme.outline,
                                errorBorderColor      = MaterialTheme.colorScheme.error,
                                focusedLabelColor     = MaterialTheme.colorScheme.primary,
                                cursorColor           = MaterialTheme.colorScheme.primary,
                            ),
                        )

                        // Error banner
                        AnimatedVisibility(
                            visible = uiState.error != null,
                            enter   = fadeIn(),
                            exit    = fadeOut(),
                        ) {
                            Column {
                                Spacer(Modifier.height(AskFacultySpacing.md))
                                ErrorBanner(
                                    message   = uiState.error ?: "",
                                    onDismiss = viewModel::clearError,
                                )
                            }
                        }

                        // Forgot password
                        Box(
                            modifier         = Modifier
                                .fillMaxWidth()
                                .padding(top = AskFacultySpacing.xs),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            TextButton(
                                onClick        = onForgotPassword,
                                contentPadding = PaddingValues(horizontal = AskFacultySpacing.xs, vertical = 2.dp),
                            ) {
                                Text(
                                    text       = "Forgot Password?",
                                    style      = MaterialTheme.typography.labelLarge,
                                    color      = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }

                        Spacer(Modifier.height(AskFacultySpacing.lg))

                        // Sign In button
                        AskFacultyButton(
                            text      = "Sign In as Faculty",
                            onClick   = {
                                focusManager.clearFocus()
                                viewModel.loginWithFacultyId(facultyId, password)
                            },
                            isLoading = uiState.isLoading,
                            enabled   = facultyId.length == 5 && password.isNotBlank(),
                        )
                    }
                }

                Spacer(Modifier.height(AskFacultySpacing.xl))

                // Security notice
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(AskFacultyRadius.md),
                    color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AskFacultySpacing.md, vertical = AskFacultySpacing.sm),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector        = Icons.Default.Security,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(AskFacultySpacing.sm))
                        Text(
                            text       = "Restricted to authorized faculty & staff",
                            style      = MaterialTheme.typography.labelSmall,
                            color      = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                Spacer(Modifier.height(AskFacultySpacing.lg))
            }
        }
    }
}
