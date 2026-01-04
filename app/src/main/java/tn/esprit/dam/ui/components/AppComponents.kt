package tn.esprit.dam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import tn.esprit.dam.ui.theme.*
import tn.esprit.dam.utils.rememberHapticFeedback
import tn.esprit.dam.utils.rememberShimmerAnimation

/**
 * STANDARD COMPONENTS - Use these throughout the app
 * Ensures consistency across all screens
 */

// ============================================
// BUTTONS
// ============================================

/**
 * Primary Button - 56dp height, full width
 * Use for main actions (Login, Scan, Save, etc.)
 */
@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val haptic = rememberHapticFeedback()
    
    Button(
        onClick = {
            haptic.mediumTap()
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(AppCorners.medium),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.primary,
            disabledContainerColor = AppColors.surfaceVariant
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = AppColors.textPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text,
                style = AppTypography.labelLarge,
                color = AppColors.textPrimary
            )
        }
    }
}

/**
 * Secondary Button - 48dp height, outlined
 * Use for secondary actions (Cancel, Reset, etc.)
 */
@Composable
fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val haptic = rememberHapticFeedback()
    
    OutlinedButton(
        onClick = {
            haptic.lightTap()
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(AppCorners.medium),
        border = BorderStroke(1.dp, AppColors.primary),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = AppColors.primary,
            disabledContentColor = AppColors.textTertiary
        )
    ) {
        Text(text, style = AppTypography.labelLarge)
    }
}

/**
 * Tertiary Button - Text only, no background
 * Use for less important actions (Learn more, Skip, etc.)
 */
@Composable
fun AppTertiaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val haptic = rememberHapticFeedback()
    
    TextButton(
        onClick = {
            haptic.lightTap()
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = AppColors.primary,
            disabledContentColor = AppColors.textTertiary
        )
    ) {
        Text(text, style = AppTypography.labelLarge)
    }
}

// ============================================
// CARDS
// ============================================

/**
 * Standard Card - 16dp corners, consistent styling
 * Use for all card content
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(AppCorners.large),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.medium),
        onClick = { onClick?.invoke() }
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            content = content
        )
    }
}

// ============================================
// LOADING STATE
// ============================================

/**
 * Standard Loading State
 * Use for all loading screens
 */
@Composable
fun AppLoadingState(
    message: String = "Chargement..."
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            CircularProgressIndicator(
                color = AppColors.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = message,
                style = AppTypography.bodyMedium,
                color = AppColors.textSecondary
            )
        }
    }
}

// ============================================
// ERROR STATE
// ============================================

/**
 * Standard Error State
 * Use for all error screens
 */
@Composable
fun AppErrorState(
    message: String,
    icon: ImageVector = Icons.Default.Error,
    onRetry: (() -> Unit)? = null,
    retryText: String = "Réessayer"
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background),
        contentAlignment = Alignment.Center
    ) {
        AppCard(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.error,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = message,
                    style = AppTypography.bodyLarge,
                    color = AppColors.textPrimary,
                    textAlign = TextAlign.Center
                )
                if (onRetry != null) {
                    AppPrimaryButton(
                        text = retryText,
                        onClick = onRetry,
                        modifier = Modifier.padding(top = AppSpacing.sm)
                    )
                }
            }
        }
    }
}

// ============================================
// EMPTY STATE
// ============================================

/**
 * Standard Empty State
 * Use for empty lists, no data screens, etc.
 */
@Composable
fun AppEmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            modifier = Modifier
                .padding(AppSpacing.xl)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.textTertiary,
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = title,
                style = AppTypography.titleLarge,
                color = AppColors.textPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = AppTypography.bodyMedium,
                color = AppColors.textSecondary,
                textAlign = TextAlign.Center
            )
            if (actionText != null && onAction != null) {
                AppPrimaryButton(
                    text = actionText,
                    onClick = onAction,
                    modifier = Modifier.padding(top = AppSpacing.md)
                )
            }
        }
    }
}

// ============================================
// INLINE ERROR ALERT
// ============================================

/**
 * Inline Error Alert - Use for form errors
 */
@Composable
fun AppErrorAlert(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = AppColors.error.copy(alpha = 0.15f),
        shape = RoundedCornerShape(AppCorners.large),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = AppColors.error,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = message,
                color = AppColors.error,
                style = AppTypography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ============================================
// INLINE INFO ALERT
// ============================================

/**
 * Inline Info Alert - Use for informational messages
 */
@Composable
fun AppInfoAlert(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = AppColors.info.copy(alpha = 0.15f),
        shape = RoundedCornerShape(AppCorners.large),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = AppColors.info,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = message,
                color = AppColors.info,
                style = AppTypography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ============================================
// SKELETON LOADING CARD
// ============================================

/**
 * Skeleton Loading Card - Shows placeholder during loading
 * Use instead of showing nothing while data loads
 */
@Composable
fun AppSkeletonCard(
    modifier: Modifier = Modifier
) {
    val shimmerAlpha = rememberShimmerAnimation()
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(AppCorners.large),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        AppColors.surfaceVariant.copy(alpha = shimmerAlpha),
                        RoundedCornerShape(AppCorners.medium)
                    )
            )
            Spacer(Modifier.width(AppSpacing.md))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                // Title placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .background(
                            AppColors.surfaceVariant.copy(alpha = shimmerAlpha),
                            RoundedCornerShape(AppCorners.small)
                        )
                )
                // Subtitle placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                        .background(
                            AppColors.surfaceVariant.copy(alpha = shimmerAlpha),
                            RoundedCornerShape(AppCorners.small)
                        )
                )
            }
        }
    }
}
/**
 * Search Bar Component
 * Reusable search field with icon
 */
@Composable
fun AppSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Rechercher...",
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val haptic = rememberHapticFeedback()
    
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { 
            Text(placeholder, color = AppColors.textTertiary) 
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = AppColors.textSecondary
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { 
                        haptic.lightTap()
                        onQueryChange("")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = AppColors.textSecondary
                    )
                }
            }
        },
        enabled = enabled,
        singleLine = true,
        shape = RoundedCornerShape(AppCorners.large),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppColors.textPrimary,
            unfocusedTextColor = AppColors.textPrimary,
            focusedBorderColor = AppColors.primary,
            unfocusedBorderColor = AppColors.surfaceVariant,
            focusedContainerColor = AppColors.surfaceVariant,
            unfocusedContainerColor = AppColors.surfaceVariant,
            cursorColor = AppColors.primary
        )
    )
}