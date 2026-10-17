package tn.esprit.dam.navigation

import android.content.Context
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import tn.esprit.dam.features.alert.presentation.AlertsScreen
import tn.esprit.dam.features.darkweb.presentation.BreachDetailScreen
import tn.esprit.dam.features.darkweb.presentation.DarkWebMonitoringScreen
import tn.esprit.dam.features.darkweb.presentation.DarkWebViewModel
import tn.esprit.dam.features.profile.presentation.ProfileScreen
import tn.esprit.dam.features.scan.presentation.home.HomeScreen

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
            onNavigateToAppDetails = { packageName ->
                navController.navigate(Screens.AppDetails.createRoute(packageName))
            },
            onNavigateToVault = { navController.navigate(Screens.Vault.route) },
            onNavigateToAlerts = { navController.navigate(Screens.AlertsHistory.route) }
        )
    }

    composable(Screens.Profile.route) {
        ProfileScreen(
            // ProfileViewModel.logout ends the session before calling this
            onLogout = {
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
        DarkWebMonitoringScreen(
            onBreachClick = { breachId -> navController.navigate(Screens.BreachDetail.createRoute(breachId)) }
        )
    }

    composable(
        route = Screens.BreachDetail.route,
        arguments = listOf(navArgument("breachId") { type = NavType.StringType })
    ) { backStackEntry ->
        val breachId = backStackEntry.arguments?.getString("breachId") ?: ""
        // Share the list screen's ViewModel: the detail reads the breach from its already-loaded list
        val listEntry = remember(backStackEntry) {
            navController.getBackStackEntry(Screens.DarkWebMonitoring.route)
        }
        BreachDetailScreen(
            breachId = breachId,
            onBackClick = { navController.popBackStack() },
            viewModel = hiltViewModel<DarkWebViewModel>(listEntry)
        )
    }
}
