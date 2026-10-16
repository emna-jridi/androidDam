package tn.esprit.dam.core.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Deprecated("Use AppColors, AppSpacing, AppCorners instead")
object ScanTheme {
    val DarkBg = AppColors.background
    val CardBg = AppColors.surface
    val TextPrimary = AppColors.textPrimary
    val TextSecondary = AppColors.textSecondary
    val Surface = AppColors.surface
    val SurfaceVariant = AppColors.surfaceVariant
    val Border = AppColors.surfaceVariant
    val DividerColor = AppColors.surfaceVariant.copy(alpha = 0.3f)

    val GradientPrimary = Brush.linearGradient(colors = listOf(AppColors.primary, AppColors.primaryDark))
    val GradientSuccess = Brush.linearGradient(colors = listOf(AppColors.success, Color(0xFF059669)))
    val GradientWarning = Brush.linearGradient(colors = listOf(AppColors.warning, Color(0xFFD97706)))
    val GradientDanger = Brush.linearGradient(colors = listOf(AppColors.error, Color(0xFFDC2626)))

    val Spacing4 = AppSpacing.xs
    val Spacing8 = AppSpacing.sm
    val Spacing12 = 12.dp
    val Spacing16 = AppSpacing.md
    val Spacing20 = 20.dp
    val Spacing24 = AppSpacing.lg
    val Spacing32 = AppSpacing.xl

    val CornerSmall = AppCorners.small
    val CornerMedium = AppCorners.medium
    val CornerLarge = AppCorners.large
    val CornerXLarge = AppCorners.xlarge
}
