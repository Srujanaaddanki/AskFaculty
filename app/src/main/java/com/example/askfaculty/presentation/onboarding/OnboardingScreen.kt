package com.example.askfaculty.presentation.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MarkChatRead
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.askfaculty.core.ui.components.AskFacultyButton
import com.example.askfaculty.ui.theme.AskFacultyDimensions
import com.example.askfaculty.ui.theme.AskFacultyRadius
import com.example.askfaculty.ui.theme.AskFacultySpacing
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Data model
// ─────────────────────────────────────────────────────────────────────────────

private data class OnboardingPageData(
    val title: String,
    val description: String,
    val badge: String,
    val icon: ImageVector,
    val accentColor: Color,
)

// ─────────────────────────────────────────────────────────────────────────────
// Root screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val pages = listOf(
        OnboardingPageData(
            badge       = "CONTINUOUS MENTORSHIP",
            title       = "Stay Connected Beyond the Classroom",
            description = "Give students a structured, dignified channel to reach you when questions arise after class. Keep learning uninterrupted.",
            icon        = Icons.Default.Forum,
            accentColor = MaterialTheme.colorScheme.primary,
        ),
        OnboardingPageData(
            badge       = "PRIORITY WORKFLOW",
            title       = "Manage Every Doubt in One Place",
            description = "Review questions, respond to follow-ups, prioritize urgent inquiries, and keep conversations organized without email clutter.",
            icon        = Icons.Default.MarkChatRead,
            accentColor = MaterialTheme.colorScheme.secondary,
        ),
        OnboardingPageData(
            badge       = "KNOWLEDGE REPOSITORIES",
            title       = "Turn Questions Into Knowledge",
            description = "Create faculty communities, publish official answers, share lecture materials, and build a reusable academic knowledge space.",
            icon        = Icons.AutoMirrored.Filled.MenuBook,
            accentColor = MaterialTheme.colorScheme.tertiary,
        ),
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope      = rememberCoroutineScope()
    val currentPage = pagerState.currentPage
    val currentAccent = pages[currentPage].accentColor

    // Full-screen gradient transitions with the page's accent color
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            currentAccent.copy(alpha = 0.18f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background,
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Full-screen tinted gradient overlay per page
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = AskFacultySpacing.xl, vertical = AskFacultySpacing.lg),
        ) {

            // ── Top bar ────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                // Branding mark
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(AskFacultyRadius.sm))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary,
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector        = Icons.Default.School,
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(20.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(AskFacultySpacing.sm))
                    Text(
                        text       = "AskFaculty",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onBackground,
                    )
                }

                // Skip button — hidden on last page
                AnimatedVisibility(
                    visible = currentPage < pages.size - 1,
                    enter   = fadeIn(tween(300)),
                    exit    = fadeOut(tween(200)),
                ) {
                    TextButton(
                        onClick = { viewModel.completeOnboarding(onFinished) },
                    ) {
                        Text(
                            text  = "Skip",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(AskFacultySpacing.xl))

            // ── Pager ──────────────────────────────────────────────────────
            HorizontalPager(
                state    = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { pageIndex ->
                val page = pages[pageIndex]
                OnboardingPageContent(page = page)
            }

            Spacer(modifier = Modifier.height(AskFacultySpacing.xl))

            // ── Bottom controls ────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                // Animated dot indicators
                Row(horizontalArrangement = Arrangement.spacedBy(AskFacultySpacing.xs + 2.dp)) {
                    repeat(pages.size) { index ->
                        OnboardingDotIndicator(
                            isSelected    = currentPage == index,
                            accentColor   = pages[index].accentColor,
                        )
                    }
                }

                // Next / Get Started
                if (currentPage < pages.size - 1) {
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(currentPage + 1)
                            }
                        },
                        shape  = RoundedCornerShape(AskFacultyRadius.lg),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = pages[currentPage].accentColor,
                            contentColor   = Color.White,
                        ),
                        modifier = Modifier.height(AskFacultyDimensions.buttonHeight),
                    ) {
                        Text(
                            text       = "Next",
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.width(AskFacultySpacing.xs))
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier           = Modifier.size(AskFacultyDimensions.iconSizeSm),
                        )
                    }
                } else {
                    AskFacultyButton(
                        text     = "Get Started",
                        onClick  = { viewModel.completeOnboarding(onFinished) },
                        modifier = Modifier.weight(1f).padding(start = AskFacultySpacing.xl),
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Page content composable
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OnboardingPageContent(page: OnboardingPageData) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(horizontal = AskFacultySpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        // ── Nested gradient ring illustration ──────────────────────────────
        //  Outer ring  200dp  — subtle border + very light accent fill
        //  Middle ring 148dp  — stronger alpha ring (gradient)
        //  Inner ring  100dp  — solid accent tinted fill
        //  Icon        64dp
        Box(
            modifier         = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            page.accentColor.copy(alpha = 0.06f),
                            page.accentColor.copy(alpha = 0.13f),
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            page.accentColor.copy(alpha = 0.35f),
                            page.accentColor.copy(alpha = 0.10f),
                        )
                    ),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Middle gradient ring
            Box(
                modifier         = Modifier
                    .size(148.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                page.accentColor.copy(alpha = 0.18f),
                                page.accentColor.copy(alpha = 0.07f),
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                page.accentColor.copy(alpha = 0.25f),
                                Color.Transparent,
                            )
                        ),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                // Inner solid ring
                Box(
                    modifier         = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    page.accentColor.copy(alpha = 0.28f),
                                    page.accentColor.copy(alpha = 0.16f),
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = page.icon,
                        contentDescription = null,
                        tint               = page.accentColor,
                        modifier           = Modifier.size(52.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AskFacultySpacing.xxxl))

        // ── Badge chip ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(AskFacultyRadius.xs))
                .background(page.accentColor.copy(alpha = 0.13f))
                .border(
                    width = 1.dp,
                    color = page.accentColor.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(AskFacultyRadius.xs),
                )
                .padding(horizontal = AskFacultySpacing.md, vertical = AskFacultySpacing.xs),
        ) {
            Text(
                text          = page.badge,
                style         = MaterialTheme.typography.labelSmall,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                color         = page.accentColor,
            )
        }

        Spacer(modifier = Modifier.height(AskFacultySpacing.lg))

        // ── Title ─────────────────────────────────────────────────────────
        Text(
            text       = page.title,
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
            color      = MaterialTheme.colorScheme.onBackground,
            lineHeight = 32.sp,
        )

        Spacer(modifier = Modifier.height(AskFacultySpacing.md))

        // ── Description ───────────────────────────────────────────────────
        Text(
            text       = page.description,
            style      = MaterialTheme.typography.bodyMedium,
            textAlign  = TextAlign.Center,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Animated dot indicator
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OnboardingDotIndicator(
    isSelected: Boolean,
    accentColor: Color,
) {
    val dotWidth by animateDpAsState(
        targetValue  = if (isSelected) 28.dp else 8.dp,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label        = "dot_width",
    )
    val dotAlpha by animateFloatAsState(
        targetValue  = if (isSelected) 1f else 0.35f,
        animationSpec = tween(durationMillis = 350),
        label        = "dot_alpha",
    )

    Box(
        modifier = Modifier
            .height(8.dp)
            .width(dotWidth)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = dotAlpha)),
    )
}
