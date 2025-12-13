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

sealed class NavigationScreen(val title: Int) {
    object Home : NavigationScreen(R.string.nav_home)
    object Scan : NavigationScreen(R.string.nav_scan)
    object History : NavigationScreen(R.string.nav_history)
    object Profile : NavigationScreen(R.string.nav_profile)
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
                text = stringResource(id = currentScreen.title),
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
            containerColor = Color(0xFF7C3AED),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}
