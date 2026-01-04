package tn.esprit.dam.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * SINGLE SOURCE OF TRUTH FOR ALL DESIGN TOKENS
 * Use these in every composable - NO inline colors/spacing/typography
 */

// ============================================
// COLORS (WCAG AA Compliant)
// ============================================
object AppColors {
    // Backgrounds
    val background = Color(0xFF0F172A)       // Deep navy
    val surface = Color(0xFF1E293B)          // Slate
    val surfaceVariant = Color(0xFF334155)   // Lighter slate
    
    // Text (High contrast for accessibility)
    val textPrimary = Color(0xFFF1F5F9)      // Contrast: 15:1 ✓
    val textSecondary = Color(0xFFCBD5E1)    // Contrast: 9:1 ✓
    val textTertiary = Color(0xFF94A3B8)     // Contrast: 5.2:1 ✓
    
    // Brand & Primary
    val primary = Color(0xFF6366F1)          // Indigo
    val primaryLight = Color(0xFF818CF8)     // Indigo Light
    val primaryDark = Color(0xFF4F46E5)      // Indigo Dark
    
    // Status Colors
    val success = Color(0xFF10B981)          // Green
    val warning = Color(0xFFF59E0B)          // Amber
    val error = Color(0xFFEF4444)            // Red
    val info = Color(0xFF3B82F6)             // Blue
    
    // Risk Levels
    val riskLow = success
    val riskMedium = warning
    val riskHigh = Color(0xFFFF6B6B)         // Coral
    val riskCritical = error
}

// ============================================
// TYPOGRAPHY (Material3 Type Scale)
// ============================================
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

// ============================================
// SPACING (8dp Grid System)
// ============================================
object AppSpacing {
    val xs = 4.dp      // Icon padding, very tight
    val sm = 8.dp      // Related elements
    val md = 16.dp     // Card padding, standard
    val lg = 24.dp     // Section spacing
    val xl = 32.dp     // Screen padding
    val xxl = 48.dp    // Major sections, large gaps
}

// ============================================
// CORNER RADIUS
// ============================================
object AppCorners {
    val small = 8.dp    // Chips, small elements
    val medium = 12.dp  // Buttons, fields
    val large = 16.dp   // Cards (DEFAULT)
    val xlarge = 20.dp  // Hero cards, modals
}

// ============================================
// ELEVATION (Shadow Depth)
// ============================================
object AppElevation {
    val none = 0.dp
    val low = 2.dp      // Subtle lift
    val medium = 4.dp   // Standard cards
    val high = 8.dp     // Modals, FABs
    val highest = 16.dp // Dialogs, popups
}
