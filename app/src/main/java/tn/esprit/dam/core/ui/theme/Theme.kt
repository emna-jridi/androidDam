package tn.esprit.dam.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * The app is dark-only: every screen paints AppColors backgrounds, so Material's colors are
 * derived from the same tokens instead of following the system light/dark setting.
 */
private val ShadowGuardColorScheme = darkColorScheme(
    primary = AppColors.primaryLight,
    onPrimary = AppColors.background,
    primaryContainer = AppColors.primaryDark,
    onPrimaryContainer = AppColors.primaryLight,
    secondary = AppColors.primaryLight,
    onSecondary = AppColors.background,
    secondaryContainer = AppColors.primaryDark,
    onSecondaryContainer = AppColors.primaryLight,
    tertiary = AppColors.info,
    onTertiary = AppColors.background,
    error = AppColors.error,
    onError = AppColors.background,
    errorContainer = AppColors.errorContainer,
    onErrorContainer = AppColors.errorLight,
    background = AppColors.background,
    onBackground = AppColors.textPrimary,
    surface = AppColors.surface,
    onSurface = AppColors.textPrimary,
    surfaceVariant = AppColors.surfaceVariant,
    onSurfaceVariant = AppColors.textTertiary,
    outline = AppColors.textTertiary,
    outlineVariant = AppColors.border
)

@Composable
fun ShadowGuardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ShadowGuardColorScheme,
        typography = Typography,
        content = content
    )
}
