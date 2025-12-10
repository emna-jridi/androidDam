package tn.esprit.dam.screens.scan

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color



object ScanTheme {
    val DarkBg = Color(0xFF0F172A)
    val CardBg = Color(0xFF1E293B)
    val CardHover = Color(0xFF334155)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF94A3B8)
    val BorderColor = Color(0xFF334155)


    val CriticalText = Color(0xFFEF4444)
    val HighBg = Color(0x1AFB923C)
    val HighText = Color(0xFFFB923C)
    val MediumText = Color(0xFFFBBF24)

    val LowText = Color(0xFF10B981)

    val PrimaryGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF6366F1), Color(0xFF9333EA))
    )
}