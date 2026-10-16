package tn.esprit.dam.navigation

import android.content.Context
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import tn.esprit.dam.core.session.rememberTokenRepository
import tn.esprit.dam.features.alert.presentation.AlertsScreen
import tn.esprit.dam.features.darkweb.presentation.BreachDetailScreen
import tn.esprit.dam.features.darkweb.presentation.DarkWebScreen
import tn.esprit.dam.features.profile.presentation.ProfileScreen
import tn.esprit.dam.features.scan.presentation.home.HomeScreen

fun NavGraphBuilder.profileNavGraph(
    navController: NavHostController,
    context: Context
) {
    composable(Screens.Home.route) {
        val coroutineScope = rememberCoroutineScope()
        val tokenRepository = rememberTokenRepository()
        HomeScreen(
            onNavigateToScan = { navController.navigate(Screens.Scan.route) },
            onNavigateToHistory = { navController.navigate(Screens.ScanHistory.route) },
            onNavigateToAppSearch = { navController.navigate(Screens.AppSearch.route) },
            onNavigateToProfile = { navController.navigate(Screens.Profile.route) },
            onNavigateToAppDetails = { packageName ->
                navController.navigate(Screens.AppDetails.createRoute(packageName))
            },
            onNavigateToVault = { navController.navigate(Screens.Vault.route) },
            onLogout = {
                coroutineScope.launch {
                    runCatching { tokenRepository.clearTokens() }
                    navController.navigate(Screens.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            onNavigateToAlerts = { navController.navigate(Screens.AlertsHistory.route) }
        )
    }

    composable(Screens.Profile.route) {
        val coroutineScope = rememberCoroutineScope()
        val tokenRepository = rememberTokenRepository()
        ProfileScreen(
            onLogout = {
                coroutineScope.launch {
                    runCatching { tokenRepository.clearTokens() }
                    navController.navigate(Screens.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
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
            onNavigateToSearch = { navController.navigate(Screens.AppSearch.route) }
        )
    }

    composable(
        route = Screens.BreachDetail.route,
        arguments = listOf(navArgument("breachId") { type = NavType.StringType })
    ) { backStackEntry ->
        val breachId = backStackEntry.arguments?.getString("breachId") ?: ""
        BreachDetailScreen(
            navController = navController,
            breachId = breachId
        )
    }
}
