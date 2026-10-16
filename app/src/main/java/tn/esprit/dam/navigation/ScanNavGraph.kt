package tn.esprit.dam.navigation

import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import tn.esprit.dam.core.session.rememberTokenRepository
import tn.esprit.dam.features.scan.presentation.detail.AppDetailScreen
import tn.esprit.dam.features.scan.presentation.history.ScanHistoryScreen
import tn.esprit.dam.features.scan.presentation.scan.ScanScreen
import tn.esprit.dam.features.scan.presentation.search.SearchAppScreen

fun NavGraphBuilder.scanNavGraph(navController: NavHostController) {
    composable(Screens.Scan.route) {
        val context = LocalContext.current
        val tokenRepository = rememberTokenRepository()
        var userId by remember { mutableStateOf<String?>(null) }
        var deviceId by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            val user = tokenRepository.getUser()
            val token = tokenRepository.getAccessToken()
            val decodedId = token?.let { decodeUserIdFromToken(it) }
            userId = user?.id ?: decodedId ?: "unknown"

            deviceId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: "unknown"
        }

        if (userId != null && deviceId != null) {
            ScanScreen(
                userId = userId!!,
                deviceId = deviceId!!,
                onNavigateToHome = {
                    navController.navigate(Screens.Home.route) {
                        popUpTo(Screens.Home.route) { inclusive = true }
                    }
                },
                onNavigateToAppDetails = { packageName ->
                    navController.navigate(Screens.AppDetails.createRoute(packageName))
                }
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    composable(
        route = Screens.AppDetails.route,
        arguments = listOf(navArgument("packageName") { type = NavType.StringType })
    ) { backStackEntry ->
        val context = LocalContext.current
        val tokenRepository = rememberTokenRepository()
        val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
        var userId by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            val user = tokenRepository.getUser()
            val token = tokenRepository.getAccessToken()
            val decodedId = token?.let { decodeUserIdFromToken(it) }
            userId = user?.id ?: decodedId ?: "unknown"
        }

        if (userId != null) {
            AppDetailScreen(
                packageName = packageName,
                userId = userId!!,
                onBackClick = { navController.popBackStack() }
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
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

private fun decodeUserIdFromToken(token: String): String? {
    return try {
        val parts = token.split(".")
        if (parts.size >= 2) {
            val payload = java.util.Base64.getUrlDecoder().decode(parts[1])
            val json = String(payload)
            json.substringAfter("\"sub\":\"")
                .substringBefore("\"")
        } else null
    } catch (e: Exception) {
        null
    }
}
