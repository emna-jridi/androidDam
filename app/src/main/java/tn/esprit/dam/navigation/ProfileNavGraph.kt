package tn.esprit.dam.navigation

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kotlinx.coroutines.runBlocking
import tn.esprit.dam.data.SessionManager
import tn.esprit.dam.data.TokenManager
import tn.esprit.dam.features.alert.AlertsScreen
import tn.esprit.dam.features.darkweb.screens.DarkWebScreen
import tn.esprit.dam.features.profile.ProfileScreen
import tn.esprit.dam.features.scan.presentation.HomeScreen

/**
 * Profile and utility feature routes
 * Handles home, profile, alerts, and dark web monitoring
 */
fun NavGraphBuilder.profileNavGraph(
    navController: NavHostController,
    context: Context
) {
    composable(Screens.Home.route) {
        HomeScreen(
            onNavigateToScan = { navController.navigate(Screens.Scan.route) },
            onNavigateToHistory = { navController.navigate(Screens.ScanHistory.route) },
            onNavigateToAppSearch = { navController.navigate(Screens.AppSearch.route) },
            onNavigateToProfile = { navController.navigate(Screens.Profile.route) },
            onNavigateToAppDetails = { /* Handle app details */ },
            onNavigateToVault = { navController.navigate(Screens.Vault.route) },
            onLogout = {
                runBlocking { SessionManager.logout(context) }
                navController.navigate(Screens.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onNavigateToAlerts = { navController.navigate(Screens.AlertsHistory.route) }
        )
    }

    composable(Screens.Profile.route) {
        ProfileScreen(
            onLogout = {
                runBlocking { SessionManager.logout(context) }
                navController.navigate(Screens.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onOpenVault = { navController.navigate(Screens.Vault.route) },
            onCreateVault = { navController.navigate("${Screens.Vault.route}?intent=create") },
            onOpenDarkWeb = { navController.navigate(Screens.DarkWebMonitoring.route) }
        )
    }

    composable(Screens.AlertsHistory.route) {
        AlertsScreen(navController = navController)
    }

    composable(Screens.DarkWebMonitoring.route) {
        DarkWebScreen(
            onNavigateToScanHistory = { navController.navigate(Screens.ScanHistory.route) },
            onNavigateToSearch = { navController.navigate(Screens.AppSearch.route) },
            onNavigateToHistory = { navController.navigate(Screens.ScanHistory.route) },
            onNavigateToAppSearch = { navController.navigate(Screens.AppSearch.route) },
            onNavigateToProfile = { navController.navigate(Screens.Profile.route) },
            onNavigateToAppDetails = { /* Handle app details */ },
            onNavigateToVault = { navController.navigate(Screens.Vault.route) },
            onNavigateToAlerts = { navController.navigate(Screens.AlertsHistory.route) }
        )
    }
}
