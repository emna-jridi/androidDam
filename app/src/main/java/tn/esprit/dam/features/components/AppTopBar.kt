package tn.esprit.dam.features.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import tn.esprit.dam.R

sealed class NavigationScreen(val titleRes: Int = 0, val titleString: String? = null) {
    object Home : NavigationScreen(R.string.nav_home)
    object Scan : NavigationScreen(R.string.nav_scan)
    object History : NavigationScreen(R.string.nav_history)
    object Profile : NavigationScreen(R.string.nav_profile)
    object Vault : NavigationScreen(titleString = "ShadowVault")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentScreen: NavigationScreen,
    onBackClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(
                text = currentScreen.titleString ?: if (currentScreen.titleRes != 0) stringResource(id = currentScreen.titleRes) else "",
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                color = Color.White
            )
        },
        navigationIcon = {
            if (onBackClick != null && currentScreen != NavigationScreen.Home) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color.White
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF1F2937), // Neutral Dark Slate for security feel
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}
