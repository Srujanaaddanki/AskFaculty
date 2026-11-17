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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    initialIsStudent: Boolean = false,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(if (initialIsStudent) 0 else 1) }
    var accountId by remember { mutableStateOf(if (initialIsStudent) "22CS104" else "30456") }

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
            // Hero section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .scale(iconScale)
                        .size(AskFacultyDimensions.avatarXl)
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AskFacultySpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Role selector
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
                            onClick = {
                                selectedTab = 0
                                accountId = "22CS104"
                                viewModel.clearError()
                            },
                            text = { Text("Student", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                accountId = "30456"
                                viewModel.clearError()
                            },
                            text = { Text("Faculty", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(Modifier.height(AskFacultySpacing.lg))

                AnimatedContent(
                    targetState = uiState.passwordResetSent,
                    transitionSpec = {
                        (fadeIn(tween(400)) + scaleIn(tween(400), initialScale = 0.92f))
                            .togetherWith(fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.92f))
                    },
                    label = "reset_content_transition",
                ) { isSuccess ->
                    if (isSuccess) {
                        SuccessPanel(
                            accountId = accountId,
                            isStudent = selectedTab == 0,
                            onBackToSignIn = {
                                viewModel.clearPasswordResetFlag()
                                onBack()
                            },
                        )
                    } else {
                        FormPanel(
                            isStudent = selectedTab == 0,
                            accountId = accountId,
                            onAccountIdChange = { input ->
                                accountId = input
                                viewModel.clearError()
                            },
                            isLoading = uiState.isLoading,
                            errorMessage = uiState.error,
                            onDismissError = viewModel::clearError,
                            onSendReset = {
                                if (selectedTab == 0) {
                                    viewModel.sendPasswordResetForStudentId(accountId)
                                } else {
                                    viewModel.sendPasswordResetForFacultyId(accountId)
                                }
                            },
                            onBack = onBack,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormPanel(
    isStudent: Boolean,
    accountId: String,
    onAccountIdChange: (String) -> Unit,
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
        Text(
            text = if (isStudent) "Recover Student Account" else "Recover Faculty Account",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(AskFacultySpacing.xs))

        Text(
            text = if (isStudent)
                "Enter your Student ID, Roll No, or registered college email address to receive password recovery instructions."
            else
                "Enter your 5-digit institutional Faculty ID below to receive password recovery instructions.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(AskFacultySpacing.md))

        AnimatedVisibility(visible = errorMessage != null) {
            Column {
                ErrorBanner(
                    message = errorMessage ?: "",
                    onDismiss = onDismissError,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(AskFacultySpacing.md))
            }
        }

        AskFacultyTextField(
            value = accountId,
            onValueChange = onAccountIdChange,
            label = if (isStudent) "Student ID / Roll No / Email" else "Faculty ID",
            placeholder = if (isStudent) "e.g. 22CS104 or email" else "e.g. 30456",
            leadingIcon = if (isStudent) Icons.Default.School else Icons.Default.Badge,
            keyboardOptions = KeyboardOptions(keyboardType = if (isStudent) KeyboardType.Text else KeyboardType.Number),
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(AskFacultySpacing.lg))

        AskFacultyButton(
            text = "Send Reset Link",
            onClick = onSendReset,
            isLoading = isLoading,
            enabled = accountId.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

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

@Composable
private fun SuccessPanel(
    accountId: String,
    isStudent: Boolean,
    onBackToSignIn: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AskFacultySpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(AskFacultyDimensions.avatarXxl)
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
                modifier = Modifier.size(AskFacultyDimensions.avatarMedium),
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
            text = "Instructions to reset your password have been sent to the registered email for ID $accountId.\n\nPlease check your inbox and follow the link.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(AskFacultySpacing.xl))

        AskFacultyButton(
            text = "Back to Sign In",
            onClick = onBackToSignIn,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(AskFacultySpacing.xl))
    }
}
