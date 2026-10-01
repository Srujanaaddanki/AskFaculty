package com.example.askfaculty.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.askfaculty.ui.theme.AskFacultySpacing
import com.example.askfaculty.ui.theme.GradientEnd
import com.example.askfaculty.ui.theme.GradientMid
import com.example.askfaculty.ui.theme.GradientStart

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()

    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        )
    }
    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600),
        )
    }
    LaunchedEffect(destination) {
        when (destination) {
            is SplashDestination.Onboarding -> onNavigateToOnboarding()
            is SplashDestination.Login      -> onNavigateToLogin()
            is SplashDestination.Home       -> onNavigateToHome()
            null -> Unit
        }
    }

    // Pulsing ring animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue  = 0.35f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ring_alpha",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                    )
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
                .padding(AskFacultySpacing.xxl),
        ) {
            // Outer pulsing ring
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = ringAlpha),
                                    Color.Transparent,
                                )
                            )
                        ),
                )
                // Logo icon box with gradient
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(GradientStart, GradientMid)
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = Icons.Default.School,
                        contentDescription = "AskFaculty",
                        tint               = Color.White,
                        modifier           = Modifier.size(48.dp),
                    )
                }
            }

            Spacer(Modifier.height(AskFacultySpacing.xl))

            Text(
                text       = "AskFaculty",
                style      = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color      = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(AskFacultySpacing.xs))

            Text(
                text          = "FACULTY PORTAL",
                style         = MaterialTheme.typography.labelMedium,
                fontWeight    = FontWeight.SemiBold,
                color         = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp,
            )

            Spacer(Modifier.height(AskFacultySpacing.xxl))

            // Shimmer loading indicator
            Box(
                modifier = Modifier
                    .size(width = 48.dp, height = 3.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            )
                        )
                    ),
            )
        }

        // Footer tagline
        Text(
            text          = "Academic Knowledge Network",
            style         = MaterialTheme.typography.labelSmall,
            color         = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            letterSpacing = 1.sp,
            fontWeight    = FontWeight.Medium,
            modifier      = Modifier
                .align(Alignment.BottomCenter)
                .alpha(alpha.value)
                .padding(bottom = 40.dp),
        )
    }
}
