package tn.esprit.dam.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tn.esprit.dam.Screens.HistoryScreen
import tn.esprit.dam.Screens.HomeScreen
import tn.esprit.dam.Screens.ScanScreen
import tn.esprit.dam.Screens.SearchScreen
import tn.esprit.dam.Screens.TopAppsScreen
import tn.esprit.dam.data.api.ShadowGuardApi


@Composable
fun ShadowGuardNavigation(api: ShadowGuardApi) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToScan = { navController.navigate("scan") },
                onNavigateToSearch = { navController.navigate("search") },
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToTopApps = { navController.navigate("topApps") }
            )
        }

        composable("scan") {
            ScanScreen(
                onBack = { navController.popBackStack() },
                onAppDetails = { packageName ->
                    navController.navigate("appDetails/$packageName")
                }
            )
        }

        composable("search") {
            SearchScreen(
                api = api,
                onBack = { navController.popBackStack() },
                onAppDetails = { packageName ->
                    navController.navigate("appDetails/$packageName")
                }
            )
        }

        composable("appDetails/{packageName}") { backStackEntry ->
            val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
            AppDetailsScreen(
                packageName = packageName,
                api = api,
                onBack = { navController.popBackStack() }
            )
        }

        composable("history") {
            HistoryScreen(
                api = api,
                onBack = { navController.popBackStack() }
            )
        }

        composable("topApps") {
            TopAppsScreen(
                api = api,
                onBack = { navController.popBackStack() },
                onAppDetails = { packageName ->
                    navController.navigate("appDetails/$packageName")
                }
            )
        }
    }
}

@Composable
fun AppDetailsScreen(packageName: String, api: ShadowGuardApi, onBack: () -> Boolean) {
    TODO("Not yet implemented")
}