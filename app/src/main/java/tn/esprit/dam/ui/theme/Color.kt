package tn.esprit.dam.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// MD3 Dark Mode Palette per spec
val PrimaryPurple = Color(0xFF6366F1)      // Indigo 500
val PrimaryDark = Color(0xFF4F46E5)        // Indigo 600
val PrimaryLight = Color(0xFF818CF8)       // Indigo 400

val SuccessGreen = Color(0xFF10B981)       // Green 500
val WarningOrange = Color(0xFFF59E0B)      // Amber 500
val DangerRed = Color(0xFFEF4444)          // Red 500
val CriticalRed = Color(0xFFDC2626)        // Red 600

val Surface = Color(0xFF111827)            // Gray 900
val SurfaceVariant = Color(0xFF1F2937)     // Gray 800
val OnSurface = Color(0xFFFFFFFF)          // White
val OnSurfaceSecondary = Color(0xFF9CA3AF) // Gray 400

// Gradients
val GradientPurple = Brush.linearGradient(colors = listOf(PrimaryPurple, PrimaryDark))
val GradientSuccess = Brush.linearGradient(colors = listOf(SuccessGreen, Color(0xFF059669)))
val GradientWarning = Brush.linearGradient(colors = listOf(WarningOrange, Color(0xFFD97706)))
val GradientDanger = Brush.linearGradient(colors = listOf(DangerRed, CriticalRed))

// Backward-compatible aliases (old names used across the app)
val Primary = PrimaryPurple
val Secondary = PrimaryLight
val SecondaryLight = PrimaryLight
val PrimaryVariant = PrimaryDark
val PrimaryGradient = GradientPurple
val SuccessGradient = GradientSuccess
val WarningGradient = GradientWarning
val ErrorGradient = GradientDanger

// ============================================
// BACKGROUND & SURFACE COLORS
// ============================================
// Legacy aliases (kept for compatibility)
val Background = Surface
val CardBg = SurfaceVariant
val DarkBackground = Surface
val DarkSurface = SurfaceVariant
val DarkCardBg = SurfaceVariant

// ============================================
// TEXT COLORS
// ============================================
val TextPrimary = OnSurface
val TextSecondary = OnSurfaceSecondary
val TextTertiary = Color(0xFF94A3B8)
val TextOnPrimary = OnSurface

// ============================================
// STATUS COLORS (Softer, less alarming)
// ============================================
val CriticalBg = Color(0xFFFEE2E2)
val CriticalText = Color(0xFFE74C3C)        // Softer red

val WarningBg = Color(0xFFFEF3C7)
val WarningText = Color(0xFFF39C12)         // Warmer orange

val SuccessBg = Color(0xFFDCFCE7)
val SuccessText = Color(0xFF27AE60)         // Calming green

val InfoBg = Color(0xFFDBEAFE)
val InfoText = Color(0xFF5DADE2)            // Softer blue
val MediumText = Color(0xFFF39C12)

val LowText = Color(0xFF27AE60)

// ============================================
// RISK LEVEL COLORS (Subtle indicators, not dominant)
// ============================================
val RiskLow = Color(0xFF27AE60)             // Calm green
val RiskMedium = Color(0xFFF39C12)          // Warm amber
val RiskHigh = Color(0xFFE74C3C)            // Softer red
val RiskCritical = Color(0xFFCC3333)        // Muted red
val LowBg = Color(0x1A27AE60)
val MediumBg = Color(0x1AF39C12)
val CriticalBg1 = Color(0x1AE74C3C)
val DarkTextPrimary = Color(0xFFF1F5F9)
val DarkTextSecondary = Color(0xFF94A3B8)

// Semantic aliases per spec
// Remove duplicate aliases to avoid conflicts (already defined above)
