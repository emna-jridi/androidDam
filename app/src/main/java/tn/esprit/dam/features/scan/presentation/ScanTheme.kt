package tn.esprit.dam.features.scan.presentation

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object ScanTheme {
    val DarkBg = Color(0xFF0F172A)
    val CardBg = Color(0xFF1E293B)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF94A3B8)
    val Surface = Color(0xFF1E293B)
    val SurfaceVariant = Color(0xFF334155)
    val Border = Color(0xFF334155)
    val DividerColor = Color(0xFF334155).copy(alpha = 0.3f)
    val GradientPrimary = Brush.linearGradient(colors = listOf(Color(0xFF6366F1), Color(0xFF9333EA)))
    val GradientSuccess = Brush.linearGradient(colors = listOf(Color(0xFF10B981), Color(0xFF059669)))
    val GradientWarning = Brush.linearGradient(colors = listOf(Color(0xFFFB923C), Color(0xFFD97706)))
    val GradientDanger = Brush.linearGradient(colors = listOf(Color(0xFFEF4444), Color(0xFFDC2626)))
    val Spacing4 = 4.dp
    val Spacing8 = 8.dp
    val Spacing12 = 12.dp
    val Spacing16 = 16.dp
    val Spacing20 = 20.dp
    val Spacing24 = 24.dp
    val Spacing32 = 32.dp
    val CornerSmall = 8.dp
    val CornerMedium = 12.dp
    val CornerLarge = 16.dp
    val CornerXLarge = 20.dp
}
