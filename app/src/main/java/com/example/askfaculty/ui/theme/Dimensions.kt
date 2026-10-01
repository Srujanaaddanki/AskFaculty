package com.example.askfaculty.ui.theme

import androidx.compose.ui.unit.dp

/**
 * AskFaculty Dimension System
 * Centralized dimension tokens — heights, icon sizes, avatar sizes, radii, elevation.
 */
object AskFacultyDimensions {
    // Buttons
    val buttonHeight         = 52.dp
    val buttonHeightSmall    = 40.dp

    // Text Fields
    val textFieldHeight      = 56.dp

    // Icons
    val iconSizeXs           = 16.dp
    val iconSizeSm           = 18.dp
    val iconSize             = 24.dp
    val iconSizeLg           = 28.dp
    val iconSizeXl           = 32.dp

    // Avatars
    val avatarXs             = 32.dp
    val avatarSmall          = 40.dp
    val avatarMedium         = 48.dp
    val avatarLarge          = 64.dp
    val avatarXl             = 80.dp
    val avatarXxl            = 96.dp

    // Cards
    val cardElevation        = 0.dp
    val cardElevationHovered = 4.dp

    // Bottom Nav
    val bottomNavHeight      = 72.dp

    // Touch targets (accessibility)
    val minTouchTarget       = 48.dp

    // Top Bar
    val topBarHeight         = 56.dp

    // Elevation aliases
    val elevationNone        = 0.dp
    val elevationLow         = 1.dp
    val elevationMedium      = 2.dp
    val elevationHigh        = 4.dp
    val elevationFloating    = 8.dp
}

/**
 * AskFaculty Shape / Corner Radius System
 */
object AskFacultyRadius {
    val xs   = 4.dp
    val sm   = 8.dp
    val md   = 12.dp
    val lg   = 16.dp
    val xl   = 20.dp
    val xxl  = 24.dp
    val pill = 50.dp
}

/**
 * AskFaculty Elevation System
 */
object AskFacultyElevation {
    val none     = 0.dp
    val low      = 1.dp
    val medium   = 2.dp
    val high     = 4.dp
    val floating = 8.dp
}
