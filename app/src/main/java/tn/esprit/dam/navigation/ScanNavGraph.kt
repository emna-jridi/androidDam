package tn.esprit.dam.navigation

import android.provider.Settings
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import tn.esprit.dam.features.scan.presentation.detail.AppDetailScreen
import tn.esprit.dam.features.scan.presentation.history.ScanHistoryScreen
import tn.esprit.dam.features.scan.presentation.scan.ScanScreen
import tn.esprit.dam.features.scan.presentation.search.SearchAppScreen

fun NavGraphBuilder.scanNavGraph(navController: NavHostController) {
    composable(Screens.Scan.route) {
        val context = LocalContext.current
        val deviceId = remember {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
        }

        ScanScreen(
            deviceId = deviceId,
            onNavigateToHome = {
                navController.navigate(Screens.Home.route) {
                    popUpTo(Screens.Home.route) { inclusive = true }
                }
            },
            onNavigateToAppDetails = { packageName ->
                navController.navigate(Screens.AppDetails.createRoute(packageName))
            }
        )
    }

    composable(
        route = Screens.AppDetails.route,
        arguments = listOf(navArgument("packageName") { type = NavType.StringType })
    ) { backStackEntry ->
        val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
        AppDetailScreen(
            packageName = packageName,
            onBackClick = { navController.popBackStack() }
        )
    }

    composable(Screens.ScanHistory.route) {
        ScanHistoryScreen(
            onBackClick = { navController.popBackStack() }
        )
    }

    composable(Screens.AppSearch.route) {
        SearchAppScreen(
            onAppClick = { packageName ->
                navController.navigate(Screens.AppDetails.createRoute(packageName))
            },
            onBackClick = { navController.popBackStack() }
        )
    }
}
