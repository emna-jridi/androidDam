package tn.esprit.dam.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AppColors {
    val background = Color(0xFF0F172A)
    val surface = Color(0xFF1E293B)
    val surfaceVariant = Color(0xFF334155)

    val textPrimary = Color(0xFFF1F5F9)
    val textSecondary = Color(0xFFCBD5E1)
    val textTertiary = Color(0xFF94A3B8)

    val primary = Color(0xFF6366F1)
    val primaryLight = Color(0xFF818CF8)
    val primaryDark = Color(0xFF4F46E5)

    val success = Color(0xFF10B981)
    val successDark = Color(0xFF059669)
    val warning = Color(0xFFF59E0B)
    val error = Color(0xFFEF4444)
    val info = Color(0xFF3B82F6)
    val infoDark = Color(0xFF2563EB)
    val teal = Color(0xFF14B8A6)

    val riskLow = success
    val riskMedium = warning
    val riskHigh = Color(0xFFFF6B6B)
    val riskCritical = error
}

object AppTypography {
    val displayLarge = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 40.sp
    )

    val titleLarge = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp
    )

    val titleMedium = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp
    )

    val bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp
    )

    val bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
    )

    val labelLarge = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp
    )

    val labelMedium = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp
    )
}

object AppSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

object AppCorners {
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val xlarge = 20.dp
}

object AppElevation {
    val none = 0.dp
    val low = 2.dp
    val medium = 4.dp
    val high = 8.dp
    val highest = 16.dp
}
