package tn.esprit.dam.features.darkweb.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun DarkWebScreen(
    onNavigateToScanHistory: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToAppSearch: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAppDetails: (String) -> Unit = {},
    onNavigateToVault: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Dark Web Monitoring Screen")
    }
}
