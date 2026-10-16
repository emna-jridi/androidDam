package tn.esprit.dam.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import tn.esprit.dam.core.ui.components.AppBottomNavBar
import tn.esprit.dam.core.ui.components.AppTopBar
import tn.esprit.dam.core.ui.components.NavigationScreen
import tn.esprit.dam.features.vault.presentation.passwords.PasswordViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screens.Login.route,
) {
    val context = LocalContext.current
    val passwordViewModel: PasswordViewModel = hiltViewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    val isLoggedIn = currentRoute != Screens.Login.route &&
                     currentRoute != Screens.Register.route &&
                     !currentRoute.contains("email_verification") &&
                     !currentRoute.contains("verification_success") &&
                     !currentRoute.contains("forgot_password") &&
                     !currentRoute.contains("reset_password_otp") &&
                     !currentRoute.contains("new_password") &&
                     !currentRoute.contains("password_reset_success")

    val currentScreen = when {
        currentRoute.startsWith(Screens.Vault.route) && !currentRoute.contains(Screens.VaultDetail.route) -> NavigationScreen.Vault
        currentRoute.startsWith(Screens.VaultDetail.route) -> NavigationScreen.Vault
        currentRoute.startsWith(Screens.VaultAddPassword.route) -> NavigationScreen.Vault
        currentRoute.startsWith(Screens.Home.route) -> NavigationScreen.Home
        currentRoute.startsWith(Screens.Scan.route) -> NavigationScreen.Scan
        currentRoute.startsWith(Screens.ScanHistory.route) -> NavigationScreen.History
        currentRoute.startsWith(Screens.Profile.route) -> NavigationScreen.Profile
        else -> NavigationScreen.Home
    }

    val bottomBarRoutes = setOf(
        Screens.Home.route,
        Screens.ScanHistory.route,
        Screens.Profile.route,
        Screens.Vault.route
    )
    val showBottomBar = isLoggedIn &&
        !currentRoute.startsWith(Screens.Scan.route) &&
        bottomBarRoutes.any { route -> currentRoute.startsWith(route) }

    val hiddenTopBarRoutes = listOf("darkweb_monitoring", "breach_detail", "alerts_history")
    val showTopBar = isLoggedIn && !hiddenTopBarRoutes.any { currentRoute.startsWith(it) }

    Scaffold(
        topBar = {
            if (showTopBar) {
                AppTopBar(
                    currentScreen = currentScreen,
                    onBackClick = if (currentScreen != NavigationScreen.Home) {
                        { navController.popBackStack() }
                    } else null
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screens.Home.route) { saveState = true }
                            restoreState = true
                        }
                    },
                    isLoggedIn = isLoggedIn
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            authNavGraph(
                navController = navController,
                onLoginSuccess = {
                    navController.navigate(Screens.Home.route) {
                        popUpTo(Screens.Login.route) { inclusive = true }
                    }
                }
            )

            profileNavGraph(
                navController = navController,
                context = context
            )

            scanNavGraph(
                navController = navController
            )

            vaultNavGraph(
                navController = navController,
                passwordViewModel = passwordViewModel
            )
        }
    }
}
