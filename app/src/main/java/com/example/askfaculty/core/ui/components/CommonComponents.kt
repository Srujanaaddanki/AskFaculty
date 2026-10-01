package com.example.askfaculty.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.askfaculty.core.util.toRelativeTime
import com.example.askfaculty.domain.model.Doubt
import com.example.askfaculty.ui.theme.AskFacultyDimensions
import com.example.askfaculty.ui.theme.AskFacultyRadius
import com.example.askfaculty.ui.theme.AskFacultySpacing
import com.example.askfaculty.ui.theme.PriorityHigh
import com.example.askfaculty.ui.theme.PriorityLow
import com.example.askfaculty.ui.theme.PriorityMedium
import com.example.askfaculty.ui.theme.StatusAnswered
import com.example.askfaculty.ui.theme.StatusPending
import com.example.askfaculty.ui.theme.StatusProgress
import com.example.askfaculty.ui.theme.StatusResolved

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 1: BUTTONS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Primary action button — filled, indigo gradient, 52dp height, 16dp radius.
 * Supports loading spinner and disabled state with proper opacity.
 */
@Composable
fun AskFacultyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    contentDescription: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(100),
        label = "button_scale",
    )

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .height(AskFacultyDimensions.buttonHeight)
            .semantics { if (contentDescription != null) this.contentDescription = contentDescription },
        shape = RoundedCornerShape(AskFacultyRadius.lg),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor   = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            disabledContentColor   = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
        ),
        contentPadding = PaddingValues(horizontal = AskFacultySpacing.xl),
        interactionSource = interactionSource,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier  = Modifier.size(18.dp),
                color     = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
            )
            Spacer(modifier = Modifier.width(AskFacultySpacing.sm))
            Text(
                text  = "Please wait...",
                style = MaterialTheme.typography.labelLarge,
            )
        } else {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(AskFacultyDimensions.iconSizeSm),
                )
                Spacer(modifier = Modifier.width(AskFacultySpacing.sm))
            }
            Text(
                text  = text,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

// Alias for backward compatibility
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) = AskFacultyButton(text = text, onClick = onClick, isLoading = isLoading, enabled = enabled, modifier = modifier)

/**
 * Secondary outlined button — same height/radius as primary, outlined style.
 */
@Composable
fun AskFacultyOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    OutlinedButton(
        onClick  = onClick,
        enabled  = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(AskFacultyDimensions.buttonHeight),
        shape  = RoundedCornerShape(AskFacultyRadius.lg),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 1f else 0.38f),
        ),
        contentPadding = PaddingValues(horizontal = AskFacultySpacing.xl),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(AskFacultyDimensions.iconSizeSm),
            )
            Spacer(modifier = Modifier.width(AskFacultySpacing.sm))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

// Alias for backward compatibility
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) = AskFacultyOutlinedButton(text = text, onClick = onClick, modifier = modifier)

/**
 * Danger button — filled red, same dimensions.
 */
@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    Button(
        onClick  = onClick,
        enabled  = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(AskFacultyDimensions.buttonHeight),
        shape  = RoundedCornerShape(AskFacultyRadius.lg),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor   = MaterialTheme.colorScheme.onError,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onError, strokeWidth = 2.dp)
            Spacer(Modifier.width(AskFacultySpacing.sm))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 2: TEXT FIELDS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Standardized text field — rounded, outlined, with leading/trailing icon support.
 */
@Composable
fun AskFacultyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    supportingText: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            label         = { Text(label) },
            placeholder   = if (placeholder.isNotBlank()) ({ Text(placeholder, style = MaterialTheme.typography.bodyMedium) }) else null,
            leadingIcon   = if (leadingIcon != null) ({
                Icon(
                    imageVector        = leadingIcon,
                    contentDescription = null,
                    modifier           = Modifier.size(AskFacultyDimensions.iconSize),
                    tint               = if (isError) MaterialTheme.colorScheme.error
                                         else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }) else null,
            trailingIcon = trailingIcon,
            isError      = isError,
            enabled      = enabled,
            singleLine   = singleLine,
            maxLines     = maxLines,
            keyboardOptions    = keyboardOptions,
            keyboardActions    = keyboardActions,
            visualTransformation = visualTransformation,
            modifier      = Modifier
                .fillMaxWidth()
                .heightIn(min = AskFacultyDimensions.textFieldHeight),
            shape  = RoundedCornerShape(AskFacultyRadius.md),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor    = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor  = MaterialTheme.colorScheme.outline,
                errorBorderColor      = MaterialTheme.colorScheme.error,
                focusedLabelColor     = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor   = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor           = MaterialTheme.colorScheme.primary,
            ),
        )
        if (isError && !errorMessage.isNullOrBlank()) {
            Spacer(Modifier.height(AskFacultySpacing.xs))
            Text(
                text  = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = AskFacultySpacing.md),
            )
        } else if (!supportingText.isNullOrBlank()) {
            Spacer(Modifier.height(AskFacultySpacing.xs))
            Text(
                text  = supportingText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = AskFacultySpacing.md),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 3: SEARCH BAR
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun AskFacultySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
) {
    OutlinedTextField(
        value         = query,
        onValueChange = onQueryChange,
        modifier      = modifier.fillMaxWidth(),
        placeholder   = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
        leadingIcon   = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(AskFacultyDimensions.iconSize),
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        singleLine     = true,
        shape          = RoundedCornerShape(AskFacultyRadius.xxl),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
    )
}

// Alias
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier,
) = AskFacultySearchBar(query, onQueryChange, onSearch, modifier, placeholder)

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 4: TOP APP BAR
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskFacultyTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.surface,
) {
    TopAppBar(
        title = {
            Text(
                text     = title,
                style    = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            }
        },
        actions = actions,
        colors  = TopAppBarDefaults.topAppBarColors(
            containerColor       = containerColor,
            titleContentColor    = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor     = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

// Alias
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
) = AskFacultyTopBar(title = title, onBack = onBack, actions = actions)

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 5: CHIPS
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun AskFacultyStatusChip(status: String, modifier: Modifier = Modifier) {
    val (color, label) = when (status.uppercase()) {
        "PENDING"     -> StatusPending  to "Pending"
        "IN_PROGRESS" -> StatusProgress to "In Progress"
        "ANSWERED"    -> StatusAnswered to "Answered"
        "RESOLVED"    -> StatusResolved to "Resolved"
        else          -> MaterialTheme.colorScheme.onSurfaceVariant to status
    }
    Surface(
        modifier  = modifier,
        shape     = RoundedCornerShape(AskFacultyRadius.pill),
        color     = color.copy(alpha = 0.12f),
        tonalElevation = 0.dp,
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            color    = color,
            modifier = Modifier.padding(horizontal = AskFacultySpacing.sm, vertical = AskFacultySpacing.xs),
        )
    }
}

// Alias
@Composable
fun StatusChip(status: String) = AskFacultyStatusChip(status)

@Composable
fun AskFacultyPriorityChip(priority: String, modifier: Modifier = Modifier) {
    val (color, label) = when (priority.uppercase()) {
        "HIGH"   -> PriorityHigh   to "High"
        "MEDIUM" -> PriorityMedium to "Medium"
        "LOW"    -> PriorityLow    to "Low"
        else     -> MaterialTheme.colorScheme.onSurfaceVariant to priority
    }
    Surface(
        modifier  = modifier,
        shape     = RoundedCornerShape(AskFacultyRadius.pill),
        color     = color.copy(alpha = 0.12f),
        tonalElevation = 0.dp,
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            color    = color,
            modifier = Modifier.padding(horizontal = AskFacultySpacing.sm, vertical = AskFacultySpacing.xs),
        )
    }
}

// Alias
@Composable
fun PriorityChip(priority: String) = AskFacultyPriorityChip(priority)

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 6: AVATAR
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun AskFacultyAvatar(
    photoUrl: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = AskFacultyDimensions.avatarSmall,
    availabilityColor: Color? = null,
    onClick: (() -> Unit)? = null,
) {
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Box(
        modifier = modifier
            .size(size)
            .then(clickModifier),
        contentAlignment = Alignment.BottomEnd,
    ) {
        AvatarImage(url = photoUrl ?: "", placeholder = name, size = size)
        if (availabilityColor != null) {
            Box(
                modifier = Modifier
                    .size(size * 0.30f)
                    .clip(CircleShape)
                    .background(availabilityColor)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
            )
        }
    }
}

// Alias for backward compat
@Composable
fun FacultyAvatar(
    photoUrl: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = AskFacultyDimensions.avatarSmall,
    availabilityColor: Color? = null,
    onClick: (() -> Unit)? = null,
) = AskFacultyAvatar(photoUrl, name, modifier, size, availabilityColor, onClick)

@Composable
fun AvatarImage(
    url: String,
    size: Dp = AskFacultyDimensions.avatarSmall,
    placeholder: String = "",
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (url.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
            )
        } else {
            val initials = placeholder
                .split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercaseChar().toString() }
                .ifEmpty { "?" }
            Text(
                text       = initials,
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 7: SKELETON LOADER
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun AskFacultySkeleton(modifier: Modifier = Modifier, cornerRadius: Dp = AskFacultyRadius.sm) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue  = 0.4f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeleton_alpha",
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)),
    )
}

// Alias
@Composable
fun SkeletonLoader(modifier: Modifier = Modifier) = AskFacultySkeleton(modifier)

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 8: EMPTY STATE
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun AskFacultyEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String = "",
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AskFacultySpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(AskFacultyRadius.xxl))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            )
        }
        Spacer(Modifier.height(AskFacultySpacing.lg))
        Text(
            text      = title,
            style     = MaterialTheme.typography.titleMedium,
            color     = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(AskFacultySpacing.sm))
            Text(
                text      = subtitle,
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(AskFacultySpacing.xl))
            AskFacultyButton(
                text     = actionLabel,
                onClick  = onAction,
                modifier = Modifier.width(200.dp),
            )
        }
    }
}

// Alias
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String = "",
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) = AskFacultyEmptyState(icon, title, subtitle, actionLabel, onAction, modifier)

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 9: ERROR STATE
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun AskFacultyErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AskFacultySpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(AskFacultyRadius.xxl))
                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(AskFacultySpacing.lg))
        Text(
            text      = "Something went wrong",
            style     = MaterialTheme.typography.titleMedium,
            color     = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(AskFacultySpacing.sm))
        Text(
            text      = message,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (onRetry != null) {
            Spacer(Modifier.height(AskFacultySpacing.xl))
            AskFacultyButton(
                text     = "Try Again",
                onClick  = onRetry,
                modifier = Modifier.width(200.dp),
            )
        }
    }
}

// Alias
@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) = AskFacultyErrorState(message, onRetry, modifier)

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 10: LOADING STATE
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(AskFacultySpacing.xxxl),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 11: ERROR BANNER
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun ErrorBanner(
    message: String?,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = !message.isNullOrBlank(),
        enter   = expandVertically() + fadeIn(),
        exit    = shrinkVertically() + fadeOut(),
    ) {
        if (message.isNullOrBlank()) return@AnimatedVisibility
        Card(
            colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            shape    = RoundedCornerShape(AskFacultyRadius.md),
            modifier = modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AskFacultySpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector        = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.error,
                    modifier           = Modifier.size(AskFacultyDimensions.iconSize),
                )
                Spacer(Modifier.width(AskFacultySpacing.sm))
                Text(
                    text     = message,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f),
                )
                if (onDismiss != null) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(AskFacultyDimensions.minTouchTarget)) {
                        Icon(
                            imageVector        = Icons.Default.Clear,
                            contentDescription = "Dismiss",
                            tint               = MaterialTheme.colorScheme.onErrorContainer,
                            modifier           = Modifier.size(AskFacultyDimensions.iconSizeSm),
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 12: OFFLINE BANNER
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun OfflineBanner(isVisible: Boolean) {
    AnimatedVisibility(
        visible = isVisible,
        enter   = expandVertically(animationSpec = tween(300)),
        exit    = shrinkVertically(animationSpec = tween(300)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.tertiary)
                .padding(horizontal = AskFacultySpacing.lg, vertical = AskFacultySpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.size(AskFacultyDimensions.iconSizeSm),
            )
            Spacer(Modifier.width(AskFacultySpacing.sm))
            Text(
                text  = "You're offline. Changes will sync when reconnected.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiary,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 13: SECTION HEADER
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun AskFacultySectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text  = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(0.dp)) {
                Text(
                    text  = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 14: DOUBT CARD
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun DoubtCard(
    doubt: Doubt,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val priorityColor = when (doubt.priority.uppercase()) {
        "HIGH"   -> PriorityHigh
        "MEDIUM" -> PriorityMedium
        else     -> PriorityLow
    }

    Card(
        modifier  = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(AskFacultyRadius.lg),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant),
        ),
    ) {
        Row(modifier = Modifier.padding(AskFacultySpacing.md)) {
            // Priority stripe
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .width(3.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(AskFacultyRadius.pill))
                    .background(priorityColor),
            )
            Spacer(Modifier.width(AskFacultySpacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        text     = doubt.studentName,
                        style    = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color    = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text  = doubt.createdAt.toRelativeTime(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text     = "${doubt.subject} · ${doubt.topic}",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(AskFacultySpacing.xs))
                Text(
                    text     = doubt.question,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(AskFacultySpacing.sm))
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    AskFacultyStatusChip(status = doubt.status)
                    if (!doubt.attachmentUrl.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector        = Icons.Default.AttachFile,
                                contentDescription = "Attachment",
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier           = Modifier.size(AskFacultyDimensions.iconSizeSm),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 15: STAT CARD
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(AskFacultyRadius.lg),
        colors    = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(AskFacultySpacing.lg),
            horizontalAlignment = Alignment.Start,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(AskFacultyRadius.md))
                    .background(contentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    tint               = contentColor,
                    modifier           = Modifier.size(AskFacultyDimensions.iconSize),
                )
            }
            Spacer(Modifier.height(AskFacultySpacing.md))
            Text(
                text       = value,
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = contentColor,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = title,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.75f),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 16: QUICK ACTION CARD
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
) {
    Card(
        onClick   = onClick,
        modifier  = modifier,
        shape     = RoundedCornerShape(AskFacultyRadius.lg),
        colors    = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AskFacultySpacing.md),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(AskFacultyRadius.md))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = icon,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(AskFacultyDimensions.iconSize),
                    )
                }
                Spacer(Modifier.width(AskFacultySpacing.md))
                Column {
                    Text(
                        text       = title,
                        style      = MaterialTheme.typography.titleSmall,
                        color      = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text  = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(
                imageVector        = Icons.Default.MoreVert,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(AskFacultyDimensions.iconSize),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 17: ASK FACULTY LOGO
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun AskFacultyLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 72.dp,
    showSubtitle: Boolean = true,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .clip(RoundedCornerShape(iconSize * 0.28f))
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
                contentDescription = "AskFaculty",
                tint               = Color.White,
                modifier           = Modifier.size(iconSize * 0.55f),
            )
        }
        Spacer(Modifier.height(AskFacultySpacing.md))
        Text(
            text       = "AskFaculty",
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground,
        )
        if (showSubtitle) {
            Spacer(Modifier.height(AskFacultySpacing.xs))
            Surface(
                shape = RoundedCornerShape(AskFacultyRadius.sm),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            ) {
                Text(
                    text       = "FACULTY PORTAL",
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    color      = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier   = Modifier.padding(horizontal = AskFacultySpacing.sm, vertical = AskFacultySpacing.xs),
                )
            }
        }
    }
}
