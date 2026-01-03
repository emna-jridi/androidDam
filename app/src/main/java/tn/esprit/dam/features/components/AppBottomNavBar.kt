package tn.esprit.dam.features.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tn.esprit.dam.R

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

    BottomAppBar(
        containerColor = Color(0xFF1A1F3A),
        contentColor = Color.White,
        tonalElevation = 0.dp
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
                        fontSize = MaterialTheme.typography.labelSmall.fontSize
                    )
                },
                selected = currentRoute.contains(item.route, ignoreCase = true),
                onClick = {
                    if (currentRoute != item.route) {
                        onNavigate(item.route)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF7C3AED),
                    selectedTextColor = Color(0xFF7C3AED),
                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                    unselectedTextColor = Color.White.copy(alpha = 0.6f),
                    indicatorColor = Color(0xFF7C3AED).copy(alpha = 0.1f)
                )
            )
        }
    }
}
