package tn.esprit.dam.features.scan.presentation

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tn.esprit.dam.ui.theme.AppColors
import tn.esprit.dam.ui.theme.AppSpacing
import tn.esprit.dam.ui.theme.AppCorners

/**
 * DEPRECATED: Use AppColors, AppSpacing, AppCorners from ui.theme package instead.
 * This object exists only for backward compatibility during migration.
 * All new code should use AppColors/AppSpacing/AppCorners directly.
 */
@Deprecated("Use AppColors, AppSpacing, AppCorners instead")
object ScanTheme {
    // Colors - delegate to AppColors
    val DarkBg = AppColors.background
    val CardBg = AppColors.surface
    val TextPrimary = AppColors.textPrimary
    val TextSecondary = AppColors.textSecondary
    val Surface = AppColors.surface
    val SurfaceVariant = AppColors.surfaceVariant
    val Border = AppColors.surfaceVariant
    val DividerColor = AppColors.surfaceVariant.copy(alpha = 0.3f)
    
    // Gradients - keep as-is (will be moved to AppColors later)
    val GradientPrimary = Brush.linearGradient(colors = listOf(AppColors.primary, AppColors.primaryDark))
    val GradientSuccess = Brush.linearGradient(colors = listOf(AppColors.success, Color(0xFF059669)))
    val GradientWarning = Brush.linearGradient(colors = listOf(AppColors.warning, Color(0xFFD97706)))
    val GradientDanger = Brush.linearGradient(colors = listOf(AppColors.error, Color(0xFFDC2626)))
    
    // Spacing - delegate to AppSpacing
    val Spacing4 = AppSpacing.xs
    val Spacing8 = AppSpacing.sm
    val Spacing12 = 12.dp  // AppSpacing doesn't have 12dp, keep as-is
    val Spacing16 = AppSpacing.md
    val Spacing20 = 20.dp  // AppSpacing doesn't have 20dp, keep as-is
    val Spacing24 = AppSpacing.lg
    val Spacing32 = AppSpacing.xl
    
    // Corners - delegate to AppCorners
    val CornerSmall = AppCorners.small
    val CornerMedium = AppCorners.medium
    val CornerLarge = AppCorners.large
    val CornerXLarge = AppCorners.xlarge
}
