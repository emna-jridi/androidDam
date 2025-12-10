package tn.esprit.dam.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF6366F1)
val PrimaryDark = Color(0xFF4F46E5)
val PrimaryLight = Color(0xFF818CF8)
val PrimaryVariant = Color(0xFF3730A3)

val Secondary = Color(0xFF8B5CF6)
val SecondaryLight = Color(0xFFA78BFA)


val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF6366F1),
        Color(0xFF8B5CF6)
    )
)

val SuccessGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF10B981),
        Color(0xFF34D399)
    )
)

val WarningGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFF59E0B),
        Color(0xFFFBBF24)
    )
)

val ErrorGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFEF4444),
        Color(0xFFF87171)
    )
)

// ============================================
// BACKGROUND & SURFACE COLORS
// ============================================
val Background = Color(0xFFF8FAFC)
val CardBg = Color(0xFFFFFFFF)
val SurfaceVariant = Color(0xFFF1F5F9)

// ============================================
// TEXT COLORS
// ============================================
val TextPrimary = Color(0xFF1E293B)
val TextSecondary = Color(0xFF64748B)
val TextTertiary = Color(0xFF94A3B8)
val TextOnPrimary = Color(0xFFFFFFFF)

// ============================================
// STATUS COLORS
// ============================================
val CriticalBg = Color(0xFFFEE2E2)
val CriticalText = Color(0xFFEF4444)

val WarningBg = Color(0xFFFEF3C7)
val WarningText = Color(0xFFF59E0B)

val SuccessBg = Color(0xFFDCFCE7)
val SuccessText = Color(0xFF10B981)

val InfoBg = Color(0xFFDBEAFE)
val InfoText = Color(0xFF3B82F6)
val MediumText = Color(0xFFFBBF24)

val LowText = Color(0xFF10B981)
// ============================================
// RISK LEVEL COLORS
// ============================================
val RiskLow = Color(0xFF10B981)
val RiskMedium = Color(0xFFF59E0B)
val RiskHigh = Color(0xFFEF4444)
val RiskCritical = Color(0xFFDC2626)
val LowBg = Color(0x1A10B981)
val MediumBg = Color(0x1AFBBF24)
val CriticalBg1 = Color(0x1AEF4444)
val DarkBackground = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val DarkCardBg = Color(0xFF334155)
val DarkTextPrimary = Color(0xFFF1F5F9)
val DarkTextSecondary = Color(0xFF94A3B8)
