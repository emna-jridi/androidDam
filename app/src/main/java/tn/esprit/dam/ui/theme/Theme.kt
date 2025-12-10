package tn.esprit.dam.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class ShadowGuardColors(
    val riskLow: Color,
    val riskMedium: Color,
    val riskHigh: Color,
    val riskCritical: Color,
    val successBg: Color,
    val successText: Color,
    val warningBg: Color,
    val warningText: Color,
    val criticalBg: Color,
    val criticalText: Color,
    val infoBg: Color,
    val infoText: Color,
    val primaryGradient: Brush,
    val successGradient: Brush,
    val warningGradient: Brush,
    val errorGradient: Brush
)

private val LightShadowGuardColors = ShadowGuardColors(
    riskLow = RiskLow,
    riskMedium = RiskMedium,
    riskHigh = RiskHigh,
    riskCritical = RiskCritical,
    successBg = SuccessBg,
    successText = SuccessText,
    warningBg = WarningBg,
    warningText = WarningText,
    criticalBg = CriticalBg,
    criticalText = CriticalText,
    infoBg = InfoBg,
    infoText = InfoText,
    primaryGradient = PrimaryGradient,
    successGradient = SuccessGradient,
    warningGradient = WarningGradient,
    errorGradient = ErrorGradient
)

private val DarkShadowGuardColors = ShadowGuardColors(
    riskLow = RiskLow,
    riskMedium = RiskMedium,
    riskHigh = RiskHigh,
    riskCritical = RiskCritical,
    successBg = SuccessBg.copy(alpha = 0.3f),
    successText = SuccessText,
    warningBg = WarningBg.copy(alpha = 0.3f),
    warningText = WarningText,
    criticalBg = CriticalBg.copy(alpha = 0.3f),
    criticalText = CriticalText,
    infoBg = InfoBg.copy(alpha = 0.3f),
    infoText = InfoText,
    primaryGradient = PrimaryGradient,
    successGradient = SuccessGradient,
    warningGradient = WarningGradient,
    errorGradient = ErrorGradient
)


private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = TextOnPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryVariant,

    secondary = Secondary,
    onSecondary = TextOnPrimary,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = PrimaryVariant,

    tertiary = InfoText,
    onTertiary = TextOnPrimary,

    error = CriticalText,
    onError = TextOnPrimary,
    errorContainer = CriticalBg,
    onErrorContainer = CriticalText,

    background = Background,
    onBackground = TextPrimary,

    surface = CardBg,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,

    outline = TextTertiary,
    outlineVariant = Color(0xFFE2E8F0)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = DarkBackground,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = PrimaryLight,

    secondary = SecondaryLight,
    onSecondary = DarkBackground,
    secondaryContainer = Secondary,
    onSecondaryContainer = SecondaryLight,

    tertiary = InfoText,
    onTertiary = DarkBackground,

    error = CriticalText,
    onError = DarkBackground,
    errorContainer = CriticalBg.copy(alpha = 0.3f),
    onErrorContainer = CriticalText,

    background = DarkBackground,
    onBackground = DarkTextPrimary,

    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCardBg,
    onSurfaceVariant = DarkTextSecondary,

    outline = DarkTextSecondary,
    outlineVariant = Color(0xFF475569)
)


val LocalShadowGuardColors = staticCompositionLocalOf { LightShadowGuardColors }

@Composable
fun ShadowGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val shadowGuardColors = if (darkTheme) DarkShadowGuardColors else LightShadowGuardColors

    CompositionLocalProvider(LocalShadowGuardColors provides shadowGuardColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}


object ShadowGuardTheme {
    val colors: ShadowGuardColors
        @Composable
        get() = LocalShadowGuardColors.current
}
