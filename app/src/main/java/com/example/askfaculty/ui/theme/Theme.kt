package com.example.askfaculty.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Light Color Scheme ─────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary              = Primary,
    onPrimary            = OnPrimary,
    primaryContainer     = PrimaryContainer,
    onPrimaryContainer   = OnPrimaryContainer,

    secondary            = Secondary,
    onSecondary          = OnSecondary,
    secondaryContainer   = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    tertiary             = Tertiary,
    onTertiary           = OnTertiary,
    tertiaryContainer    = TertiaryContainer,
    onTertiaryContainer  = OnTertiaryContainer,

    error                = ErrorColor,
    onError              = OnErrorColor,
    errorContainer       = ErrorContainer,
    onErrorContainer     = OnErrorContainer,

    background           = BackgroundLight,
    onBackground         = OnBackgroundLight,
    surface              = SurfaceLight,
    onSurface            = OnSurfaceLight,
    surfaceVariant       = SurfaceVariantLight,
    onSurfaceVariant     = OnSurfaceVariantLight,
    outline              = OutlineLight,
    outlineVariant       = OutlineVariantLight,
    inverseSurface       = SurfaceDark,
    inverseOnSurface     = OnSurfaceDark,
    inversePrimary       = PrimaryDarkTheme,
)

// ── Dark Color Scheme (Deliberate — not simply inverted) ───────────────────
private val DarkColorScheme = darkColorScheme(
    primary              = PrimaryDarkTheme,
    onPrimary            = OnPrimaryDarkTheme,
    primaryContainer     = PrimaryContainerDarkTheme,
    onPrimaryContainer   = OnPrimaryContainerDarkTheme,

    secondary            = SecondaryDarkTheme,
    onSecondary          = OnSecondaryDarkTheme,
    secondaryContainer   = SecondaryContainerDarkTheme,
    onSecondaryContainer = OnSecondaryContainerDarkTheme,

    tertiary             = TertiaryDarkTheme,
    onTertiary           = OnTertiaryDarkTheme,
    tertiaryContainer    = TertiaryContainerDarkTheme,
    onTertiaryContainer  = OnTertiaryContainerDarkTheme,

    error                = ErrorColorDark,
    onError              = OnErrorColorDark,
    errorContainer       = ErrorContainerDark,
    onErrorContainer     = OnErrorContainerDark,

    background           = BackgroundDark,
    onBackground         = OnBackgroundDark,
    surface              = SurfaceDark,
    onSurface            = OnSurfaceDark,
    surfaceVariant       = SurfaceVariantDark,
    onSurfaceVariant     = OnSurfaceVariantDark,
    outline              = OutlineDark,
    outlineVariant       = OutlineVariantDark,
    inverseSurface       = SurfaceLight,
    inverseOnSurface     = OnSurfaceLight,
    inversePrimary       = Primary,
)

@Composable
fun AskFacultyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content,
    )
}