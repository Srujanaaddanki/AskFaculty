package com.example.askfaculty.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════════════════
// AskFaculty Premium Color System
// Primary: Indigo #4F46E5  |  Secondary: Violet #7C3AED  |  Accent: Teal #0EA5E9
// ═══════════════════════════════════════════════════════════════════════════

// ── Primary (Indigo) ────────────────────────────────────────────────────────
val Primary              = Color(0xFF4F46E5)   // Indigo 600
val PrimaryDark          = Color(0xFF3730A3)   // Indigo 800
val PrimaryLight         = Color(0xFF818CF8)   // Indigo 400
val PrimaryContainer     = Color(0xFFE0E7FF)   // Indigo 100
val OnPrimary            = Color(0xFFFFFFFF)
val OnPrimaryContainer   = Color(0xFF3730A3)

val PrimaryDarkTheme           = Color(0xFF818CF8) // Indigo 400
val PrimaryDarkThemeDark       = Color(0xFF4F46E5)
val PrimaryContainerDarkTheme  = Color(0xFF3730A3)
val OnPrimaryDarkTheme         = Color(0xFF1E1B4B)
val OnPrimaryContainerDarkTheme = Color(0xFFE0E7FF)

// ── Secondary (Violet) ───────────────────────────────────────────────────────
val Secondary              = Color(0xFF7C3AED)   // Violet 600
val SecondaryLight         = Color(0xFFA78BFA)   // Violet 400
val SecondaryContainer     = Color(0xFFEDE9FE)   // Violet 100
val OnSecondary            = Color(0xFFFFFFFF)
val OnSecondaryContainer   = Color(0xFF5B21B6)

val SecondaryDarkTheme           = Color(0xFFA78BFA)
val SecondaryContainerDarkTheme  = Color(0xFF5B21B6)
val OnSecondaryDarkTheme         = Color(0xFF2D1B69)
val OnSecondaryContainerDarkTheme = Color(0xFFEDE9FE)

// ── Tertiary / Accent (Sky-Teal) ─────────────────────────────────────────────
val Tertiary              = Color(0xFF0EA5E9)   // Sky 500
val TertiaryContainer     = Color(0xFFE0F2FE)   // Sky 100
val OnTertiary            = Color(0xFFFFFFFF)
val OnTertiaryContainer   = Color(0xFF0369A1)

val TertiaryDarkTheme           = Color(0xFF38BDF8)
val TertiaryContainerDarkTheme  = Color(0xFF0369A1)
val OnTertiaryDarkTheme         = Color(0xFF082F49)
val OnTertiaryContainerDarkTheme = Color(0xFFE0F2FE)

// ── Error ──────────────────────────────────────────────────────────────────
val ErrorColor          = Color(0xFFDC2626)   // Red 600
val OnErrorColor        = Color(0xFFFFFFFF)
val ErrorContainer      = Color(0xFFFEE2E2)   // Red 100
val OnErrorContainer    = Color(0xFF991B1B)

val ErrorColorDark         = Color(0xFFFCA5A5)
val OnErrorColorDark       = Color(0xFF7F1D1D)
val ErrorContainerDark     = Color(0xFF991B1B)
val OnErrorContainerDark   = Color(0xFFFEE2E2)

// ── Neutral Light (Background & Surface) ──────────────────────────────────
val BackgroundLight       = Color(0xFFF8FAFC)   // Slate 50
val OnBackgroundLight     = Color(0xFF172033)   // Rich Navy
val SurfaceLight          = Color(0xFFFFFFFF)
val OnSurfaceLight        = Color(0xFF172033)
val SurfaceVariantLight   = Color(0xFFF1F5F9)   // Slate 100
val OnSurfaceVariantLight = Color(0xFF64748B)   // Slate 500
val OutlineLight          = Color(0xFFCBD5E1)   // Slate 300
val OutlineVariantLight   = Color(0xFFE2E8F0)   // Slate 200

// ── Neutral Dark (Deliberate dark theme — NOT inverted) ───────────────────
val BackgroundDark        = Color(0xFF0B1020)   // Deep Midnight
val OnBackgroundDark      = Color(0xFFF1F5F9)
val SurfaceDark           = Color(0xFF121A2A)   // Deep navy surface
val OnSurfaceDark         = Color(0xFFE2E8F0)
val SurfaceElevatedDark   = Color(0xFF182235)   // Slightly elevated surface
val SurfaceVariantDark    = Color(0xFF1E2D42)   // Card background
val OnSurfaceVariantDark  = Color(0xFF94A3B8)   // Slate 400
val OutlineDark           = Color(0xFF2D3E54)
val OutlineVariantDark    = Color(0xFF1E2D42)

// ── Semantic — Status Colors ──────────────────────────────────────────────
val StatusPending    = Color(0xFFD97706)   // Amber 600
val StatusProgress   = Color(0xFF4F46E5)   // Indigo (= Primary)
val StatusAnswered   = Color(0xFF16A34A)   // Green 600
val StatusResolved   = Color(0xFF7C3AED)   // Violet (= Secondary)

// ── Semantic — Priority Colors ────────────────────────────────────────────
val PriorityHigh     = Color(0xFFDC2626)   // Red 600
val PriorityMedium   = Color(0xFFD97706)   // Amber 600
val PriorityLow      = Color(0xFF16A34A)   // Green 600

// ── Semantic — Additional ─────────────────────────────────────────────────
val SuccessColor     = Color(0xFF16A34A)
val WarningColor     = Color(0xFFD97706)

val OfficialAnswer                   = Color(0xFF059669)
val OfficialAnswerBackgroundLight    = Color(0xFFD1FAE5)
val OfficialAnswerBackgroundDark     = Color(0xFF064E3B)
val VerifiedBadgeColor               = Color(0xFF4F46E5)

// ── Gradient Helpers ──────────────────────────────────────────────────────
val GradientStart    = Color(0xFF4F46E5)
val GradientMid      = Color(0xFF7C3AED)
val GradientEnd      = Color(0xFF0EA5E9)