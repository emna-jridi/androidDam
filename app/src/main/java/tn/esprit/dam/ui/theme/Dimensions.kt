package tn.esprit.dam.ui.theme


import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================
// SPACING SYSTEM - Use these EVERYWHERE
// ============================================
object Spacing {
    val none: Dp = 0.dp
    val extraSmall: Dp = 4.dp
    val small: Dp = 8.dp
    val medium: Dp = 16.dp
    val large: Dp = 24.dp
    val extraLarge: Dp = 32.dp
    val huge: Dp = 48.dp
    val massive: Dp = 64.dp
}

// Convenience top-level spacing aliases per spec
val spacing_4 = 4.dp
val spacing_8 = 8.dp
val spacing_12 = 12.dp
val spacing_16 = 16.dp
val spacing_20 = 20.dp
val spacing_24 = 24.dp
val spacing_32 = 32.dp

// ============================================
// COMPONENT SIZES
// ============================================
object ComponentSize {
    val buttonHeight: Dp = 56.dp
    val buttonHeightSmall: Dp = 48.dp
    val buttonHeightMini: Dp = 36.dp

    val iconSize: Dp = 24.dp
    val iconSizeLarge: Dp = 48.dp
    val iconSizeSmall: Dp = 16.dp

    val avatarSize: Dp = 80.dp
    val avatarSizeLarge: Dp = 120.dp
    val avatarSizeSmall: Dp = 40.dp

    val cardElevation: Dp = 4.dp
    val cardElevationHigh: Dp = 8.dp
}

// ============================================
// BORDER RADIUS
// ============================================
object BorderRadius {
    val none: Dp = 0.dp
    val small: Dp = 4.dp
    val medium: Dp = 8.dp
    val large: Dp = 12.dp
    val extraLarge: Dp = 16.dp
    val huge: Dp = 24.dp
    val massive: Dp = 32.dp
    val round: Dp = 999.dp  // Fully rounded
}

// Convenience top-level corner radius aliases per spec
val cornerSmall = 8.dp
val cornerMedium = 12.dp
val cornerLarge = 16.dp

// ============================================
// BORDER WIDTH
// ============================================
object BorderWidth {
    val none: Dp = 0.dp
    val thin: Dp = 1.dp
    val medium: Dp = 2.dp
    val thick: Dp = 4.dp
}
