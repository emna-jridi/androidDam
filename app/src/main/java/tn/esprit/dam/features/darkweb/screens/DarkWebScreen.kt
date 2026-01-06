package tn.esprit.dam.features.darkweb.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tn.esprit.dam.features.darkweb.DarkWebViewModel

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
    val navController = rememberNavController()
    val viewModel: DarkWebViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = "dark_web_monitoring"
    ) {
        composable("dark_web_monitoring") {
            DarkWebMonitoringScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable("breach_detail/{breachId}") { backStackEntry ->
            val breachId = backStackEntry.arguments?.getString("breachId") ?: ""
            BreachDetailScreen(
                navController = navController,
                breachId = breachId,
                viewModel = viewModel
            )
        }
    }
}
