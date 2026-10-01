package com.example.askfaculty.presentation.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.askfaculty.core.ui.components.AskFacultyButton
import com.example.askfaculty.core.ui.components.AskFacultyTextField
import com.example.askfaculty.core.ui.components.AskFacultyTopBar
import com.example.askfaculty.core.ui.components.ErrorBanner
import com.example.askfaculty.ui.theme.AskFacultyDimensions
import com.example.askfaculty.ui.theme.AskFacultyRadius
import com.example.askfaculty.ui.theme.AskFacultySpacing
import com.example.askfaculty.ui.theme.GradientEnd
import com.example.askfaculty.ui.theme.GradientMid
import com.example.askfaculty.ui.theme.GradientStart
import com.example.askfaculty.ui.theme.SuccessColor

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var facultyId by remember { mutableStateOf("") }

    // Animate the icon scale in on entry
    val iconScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "icon_scale",
    )

    Scaffold(
        topBar = {
            AskFacultyTopBar(
                title = "Reset Password",
                onBack = onBack,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {

            // ── Gradient Hero Section ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AskFacultySpacing.xxxxl + AskFacultySpacing.xxxxl), // 128dp hero
                contentAlignment = Alignment.Center,
            ) {
                // Ambient gradient splash behind the icon box
                Box(
                    modifier = Modifier
                        .size(AskFacultySpacing.xxxxl + AskFacultySpacing.xxxxl) // 128dp circle
                        .clip(RoundedCornerShape(AskFacultyRadius.pill))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    GradientStart.copy(alpha = 0.18f),
                                    GradientMid.copy(alpha = 0.10f),
                                    GradientEnd.copy(alpha = 0.0f),
                                ),
                            ),
                        ),
                )

                // Gradient icon container
                Box(
                    modifier = Modifier
                        .scale(iconScale)
                        .size(AskFacultyDimensions.avatarXl)          // 80dp
                        .clip(RoundedCornerShape(AskFacultyRadius.xxl))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(GradientStart, GradientMid),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(AskFacultyDimensions.iconSizeXl),
                    )
                }
            }

            // ── Main Content Card ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AskFacultySpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                AnimatedContent(
                    targetState = uiState.passwordResetSent,
                    transitionSpec = {
                        (fadeIn(tween(400)) + scaleIn(tween(400), initialScale = 0.92f))
                            .togetherWith(fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.92f))
                    },
                    label = "reset_content_transition",
                ) { isSuccess ->

                    if (isSuccess) {
                        // ── Success State ──────────────────────────────────────
                        SuccessPanel(
                            facultyId = facultyId,
                            onBackToSignIn = {
                                viewModel.clearPasswordResetFlag()
                                onBack()
                            },
                        )
                    } else {
                        // ── Form State ─────────────────────────────────────────
                        FormPanel(
                            facultyId = facultyId,
                            onFacultyIdChange = { input ->
                                if (input.length <= 5 && input.all { it.isDigit() }) {
                                    facultyId = input
                                    viewModel.clearError()
                                }
                            },
                            isLoading = uiState.isLoading,
                            errorMessage = uiState.error,
                            onDismissError = viewModel::clearError,
                            onSendReset = { viewModel.sendPasswordResetForFacultyId(facultyId) },
                            onBack = onBack,
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Form Panel — Faculty ID input + Send Reset button
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FormPanel(
    facultyId: String,
    onFacultyIdChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onDismissError: () -> Unit,
    onSendReset: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
    ) {

        // Title
        Text(
            text = "Recover Faculty Account",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(AskFacultySpacing.xs))

        // Subtitle / instructions
        Text(
            text = "Enter your 5-digit institutional Faculty ID below. " +
                "A secure password reset link will be dispatched to your " +
                "linked university email address.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(AskFacultySpacing.lg))

        // ── Step indicator pills ──────────────────────────────────────────
        Surface(
            shape = RoundedCornerShape(AskFacultyRadius.pill),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = AskFacultySpacing.lg,
                    vertical = AskFacultySpacing.md,
                ),
                verticalArrangement = Arrangement.spacedBy(AskFacultySpacing.xs),
            ) {
                StepHint(number = "1", label = "Enter your Faculty ID")
                StepHint(number = "2", label = "Receive the reset link via email")
                StepHint(number = "3", label = "Follow the link to set a new password")
            }
        }

        Spacer(Modifier.height(AskFacultySpacing.lg))

        // ── Error banner ──────────────────────────────────────────────────
        AnimatedVisibility(visible = errorMessage != null) {
            Column {
                ErrorBanner(
                    message = errorMessage,
                    onDismiss = onDismissError,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(AskFacultySpacing.md))
            }
        }

        // ── Faculty ID text field ─────────────────────────────────────────
        AskFacultyTextField(
            value = facultyId,
            onValueChange = onFacultyIdChange,
            label = "Faculty ID",
            placeholder = "e.g. 30456",
            leadingIcon = Icons.Default.Badge,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = errorMessage != null,
            supportingText = "${facultyId.length}/5 digits",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(AskFacultySpacing.xl))

        // ── Primary action ────────────────────────────────────────────────
        AskFacultyButton(
            text = "Send Reset Link",
            onClick = onSendReset,
            isLoading = isLoading,
            enabled = facultyId.length == 5 && !isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        // ── Secondary link ────────────────────────────────────────────────
        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Return to Sign In",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.height(AskFacultySpacing.xl))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Success Panel — shown after reset link is dispatched
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SuccessPanel(
    facultyId: String,
    onBackToSignIn: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
    ) {

        // Large success icon with gradient ring
        Box(
            modifier = Modifier
                .size(AskFacultyDimensions.avatarXxl)       // 96dp
                .clip(RoundedCornerShape(AskFacultyRadius.pill))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            SuccessColor.copy(alpha = 0.20f),
                            SuccessColor.copy(alpha = 0.08f),
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.MarkEmailRead,
                contentDescription = null,
                tint = SuccessColor,
                modifier = Modifier.size(AskFacultyDimensions.avatarMedium), // 48dp
            )
        }

        Spacer(Modifier.height(AskFacultySpacing.md))

        Text(
            text = "Reset Link Dispatched!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(AskFacultySpacing.xs))

        Text(
            text = "Instructions to reset your password have been sent to " +
                "the official email registered with Faculty ID $facultyId.\n\n" +
                "Please check your institutional inbox or spam folder.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(AskFacultySpacing.xl))

        // Confirmation checklist
        Surface(
            shape = RoundedCornerShape(AskFacultyRadius.md),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(AskFacultySpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
            ) {
                ConfirmCheck(label = "Check your university email inbox")
                ConfirmCheck(label = "Look in spam / promotions if not found")
                ConfirmCheck(label = "Link expires in 30 minutes")
            }
        }

        Spacer(Modifier.height(AskFacultySpacing.xl))

        AskFacultyButton(
            text = "Back to Sign In",
            onClick = onBackToSignIn,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(AskFacultySpacing.xl))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Small helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepHint(number: String, label: String) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
    ) {
        Surface(
            shape = RoundedCornerShape(AskFacultyRadius.pill),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(AskFacultySpacing.xl),  // 24dp badge
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConfirmCheck(label: String) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = SuccessColor,
            modifier = Modifier.size(AskFacultyDimensions.iconSizeSm),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
