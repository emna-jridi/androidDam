package tn.esprit.dam.features.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tn.esprit.dam.R
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import tn.esprit.dam.ui.theme.AppColors

sealed class BottomNavItem(
    val route: String,
    val label: Int,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", R.string.nav_home, Icons.Default.Home)
    object Scan : BottomNavItem("scan", R.string.nav_scan, Icons.Default.Security)
    object History : BottomNavItem("scan_history", R.string.nav_history, Icons.Default.History)
    object Profile : BottomNavItem("profile", R.string.nav_profile, Icons.Default.Person)

    companion object {
        fun items() = listOf(Home, Scan, History, Profile)
    }
}

@Composable
fun AppBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    isLoggedIn: Boolean = true
) {
    if (!isLoggedIn) return

    NavigationBar(
        containerColor = AppColors.surface,
        tonalElevation = 10.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .navigationBarsPadding(),
        windowInsets = WindowInsets.navigationBars
    ) {
        BottomNavItem.items().forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(id = item.label),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = item.label),
                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                selected = currentRoute.startsWith(item.route) && 
                           (currentRoute.length == item.route.length || currentRoute[item.route.length] == '/' || currentRoute[item.route.length] == '?'),
                onClick = {
                    val isCurrentlySelected = currentRoute.startsWith(item.route) && 
                           (currentRoute.length == item.route.length || currentRoute[item.route.length] == '/' || currentRoute[item.route.length] == '?')
                    if (!isCurrentlySelected) {
                        onNavigate(item.route)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.primary,
                    selectedTextColor = AppColors.primary,
                    unselectedIconColor = AppColors.textSecondary,
                    unselectedTextColor = AppColors.textSecondary,
                    indicatorColor = AppColors.primary.copy(alpha = 0.18f)
                ),
                modifier = Modifier
            )
        }
    }
}
