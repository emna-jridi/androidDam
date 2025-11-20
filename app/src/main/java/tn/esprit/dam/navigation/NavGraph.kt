package tn.esprit.dam.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import kotlinx.coroutines.runBlocking
import tn.esprit.dam.data.TokenManager
import tn.esprit.dam.screens.HomeScreen
import tn.esprit.dam.screens.auth.forgotpassword.ForgotPasswordScreen
import tn.esprit.dam.screens.auth.forgotpassword.NewPasswordScreen
import tn.esprit.dam.screens.auth.forgotpassword.PasswordResetSuccessScreen
import tn.esprit.dam.screens.auth.forgotpassword.ResetPasswordOTPScreen
import tn.esprit.dam.screens.auth.verification.VerificationSuccessScreen
import tn.esprit.dam.screens.auth.forgotpassword.ForgotPasswordViewModel
import tn.esprit.dam.screens.auth.login.LoginScreen
import tn.esprit.dam.screens.auth.register.RegisterScreen
import tn.esprit.dam.screens.auth.verification.EmailVerificationScreen
import tn.esprit.dam.screens.profile.ProfileScreen
import tn.esprit.dam.screens.scan.ScanScreen
import tn.esprit.dam.screens.history.ScanHistoryScreen
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.screens.SearchScreen
import tn.esprit.dam.screens.history.ScanDetailScreen
import tn.esprit.dam.screens.history.ScanDetailViewModel
import tn.esprit.dam.screens.comparison.ComparisonScreen
import tn.esprit.dam.screens.comparison.ComparisonViewModel
import tn.esprit.dam.screens.history.ScanHistoryViewModel
import tn.esprit.dam.data.remote.api.ScanApiService


@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screens.Login.route,
    api: ScanApiService,

) {
    val forgotPasswordViewModel: ForgotPasswordViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ==================== LOGIN ====================
        composable(Screens.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screens.Home.route) {
                        popUpTo(Screens.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screens.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screens.ForgotPassword.route)
                }
            )
        }

        // ==================== REGISTER ====================
        composable(Screens.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { email ->
                    navController.navigate(Screens.EmailVerification.createRoute(email)) {
                        popUpTo(Screens.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // ==================== EMAIL VERIFICATION ====================
        composable(
            route = Screens.EmailVerification.route,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""

            EmailVerificationScreen(
                email = email,
                onVerificationSuccess = {
                    navController.navigate(Screens.VerificationSuccess.route) {
                        popUpTo(Screens.Register.route) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.navigate(Screens.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ==================== VERIFICATION SUCCESS ====================
        composable(Screens.VerificationSuccess.route) {
            val userName = "Utilisateur"

            VerificationSuccessScreen(
                userName = userName,
                onContinue = {
                    navController.navigate(Screens.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ==================== FORGOT PASSWORD - PAGE 1 (Email) ====================
        composable(Screens.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateToOTP = {
                    val email = forgotPasswordViewModel.uiState.value.email
                    navController.navigate(Screens.ResetPasswordOTP.createRoute(email))
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = forgotPasswordViewModel
            )
        }

        // ==================== FORGOT PASSWORD - PAGE 2 (OTP) ====================
        composable(
            route = Screens.ResetPasswordOTP.route,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""

            ResetPasswordOTPScreen(
                email = email,
                onOTPVerified = {
                    navController.navigate(Screens.NewPassword.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = forgotPasswordViewModel
            )
        }

        // ==================== FORGOT PASSWORD - PAGE 3 (Nouveau Password) ====================
        composable(Screens.NewPassword.route) {
            NewPasswordScreen(
                onPasswordResetSuccess = {
                    navController.navigate(Screens.PasswordResetSuccess.route) {
                        popUpTo(Screens.ForgotPassword.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = forgotPasswordViewModel
            )
        }

        // ==================== FORGOT PASSWORD - PAGE 4 (Succès) ====================
        composable(Screens.PasswordResetSuccess.route) {
            PasswordResetSuccessScreen(
                onNavigateToLogin = {
                    navController.navigate(Screens.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ========== MAIN ROUTES ==========

        composable(Screens.Home.route) {
            HomeScreen(
                onNavigateToScan = {
                    navController.navigate(Screens.Scan.route)
                },
                onNavigateToSearch = {
                    navController.navigate(Screens.Search.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screens.ScanHistory.route)
                },
                onNavigateToTopApps = {
                    navController.navigate(Screens.TopApps.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screens.Profile.route)
                },
                onLogout = {
                    runBlocking {
                        TokenManager.clearAll(context)
                    }
                    navController.navigate(Screens.Login.route) {
                        popUpTo(Screens.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screens.Profile.route) {
            ProfileScreen(
                onLogout = {
                    runBlocking {
                        TokenManager.clearAll(context)
                    }
                    navController.navigate(Screens.Login.route) {
                        popUpTo(Screens.Home.route) { inclusive = true }
                    }
                },
                onNavigateToSecurity = {
                    // TODO: Implement security screen
                }
            )
        }

        // ========== SCAN ROUTE ==========

        composable(Screens.Scan.route) {
            var userHash = ""
            runBlocking {
                val user = TokenManager.getUser(context)
                userHash = user?.userHash ?: user?.id ?: ""
            }
            ScanScreen(
                userHash = userHash,
                onNavigateToAppDetail = { packageName ->
                    navController.navigate(Screens.AppDetails.createRoute(packageName))
                },
                onNavigateToHistory = {
                    navController.navigate(Screens.ScanHistory.route)
                }
            )
        }

        // ========== SCAN HISTORY ROUTES ==========

        // ✅ SCAN HISTORY SCREEN
        composable(Screens.ScanHistory.route) {

            var token = ""
            var userHash = ""

            runBlocking {
                token = TokenManager.getAccessToken(context) ?: ""
                val user = TokenManager.getUser(context)
                userHash = user?.userHash ?: user?.id ?: ""
            }

            val viewModel: ScanHistoryViewModel = hiltViewModel()

            ScanHistoryScreen(
                viewModel = viewModel,
                token = token,
                userHash = userHash,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDetail = { scanId ->
                    navController.navigate(
                        Screens.ScanDetail.createRoute(scanId)
                    )
                }
            )
        }



        // ✅ 2. SCAN DETAIL SCREEN
        composable(
            route = Screens.ScanDetail.route,
            arguments = listOf(
                navArgument("scanId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val scanId = backStackEntry.arguments?.getString("scanId") ?: ""

            var token = ""
            runBlocking {
                token = TokenManager.getAccessToken(context) ?: ""
            }

            // ✅ UTILISER: hiltViewModel() au lieu du paramètre
            val viewModel: ScanDetailViewModel = hiltViewModel()

            ScanDetailScreen(
                viewModel = viewModel,
                token = token,
                scanId = scanId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ✅ 3. SCAN COMPARISON SCREEN
        composable(
            route = Screens.ScanComparison.route,
            arguments = listOf(
                navArgument("scanId1") { type = NavType.StringType },
                navArgument("scanId2") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val scanId1 = backStackEntry.arguments?.getString("scanId1") ?: ""
            val scanId2 = backStackEntry.arguments?.getString("scanId2") ?: ""

            var token = ""
            var userHash = ""

            runBlocking {
                token = TokenManager.getAccessToken(context) ?: ""
                val user = TokenManager.getUser(context)
                userHash = user?.userHash ?: user?.id ?: ""
            }

            // ✅ UTILISER: hiltViewModel() au lieu du paramètre
            val viewModel: ComparisonViewModel = hiltViewModel()

            ComparisonScreen(
                viewModel = viewModel,
                token = token,
                userHash = userHash,
                scanId1 = scanId1,
                scanId2 = scanId2,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ========== AUTRES ROUTES ==========
        composable(Screens.Search.route) {
            SearchScreen(
                api = api,
                onBack = { navController.popBackStack() },
                onAppDetails = { packageName ->
                    navController.navigate("appDetails/$packageName")
                }
            )
        }


        composable(Screens.History.route) {
            EmptyComingSoonScreen(
                title = "Historique",
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screens.TopApps.route) {
            EmptyComingSoonScreen(
                title = "Top Apps",
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screens.AppDetails.route,
            arguments = listOf(
                navArgument("packageName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
            EmptyComingSoonScreen(
                title = "Détails: $packageName",
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// ========== ÉCRAN TEMPORAIRE ==========

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmptyComingSoonScreen(
    title: String,
    onBack: () -> Unit
) {
    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { androidx.compose.material3.Text(title) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Icon(
                    Icons.Default.Construction,
                    contentDescription = null,
                    modifier = androidx.compose.ui.Modifier.size(64.dp),
                    tint = androidx.compose.ui.graphics.Color(0xFF7C3AED)
                )
                androidx.compose.foundation.layout.Spacer(
                    modifier = androidx.compose.ui.Modifier.height(16.dp)
                )
                androidx.compose.material3.Text(
                    text = "Bientôt disponible",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}