package tn.esprit.dam.navigation


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

/**
 * Routes de navigation
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")

    // Email Verification Flow
    object EmailVerification : Screen("email_verification/{email}") {
        fun createRoute(email: String) = "email_verification/$email"
    }
    object VerificationSuccess : Screen("verification_success")

    // Forgot Password Flow
    object ForgotPassword : Screen("forgot_password")
    object ResetPasswordOTP : Screen("reset_password_otp/{email}") {
        fun createRoute(email: String) = "reset_password_otp/$email"
    }
    object NewPassword : Screen("new_password")
    object PasswordResetSuccess : Screen("password_reset_success")

    object Home : Screen("home")
}

/**
 * Graph de navigation principal
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    // ViewModel partagé pour le flow forgot password
    val forgotPasswordViewModel: ForgotPasswordViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ==================== LOGIN ====================
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        // ==================== REGISTER ====================
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { email ->
                    navController.navigate(Screen.EmailVerification.createRoute(email)) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // ==================== EMAIL VERIFICATION ====================
        composable(
            route = Screen.EmailVerification.route,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""

            EmailVerificationScreen(
                email = email,
                onVerificationSuccess = {
                    navController.navigate(Screen.VerificationSuccess.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ==================== VERIFICATION SUCCESS ====================
        composable(Screen.VerificationSuccess.route) {
            val userName = "Utilisateur" // TODO: Récupérer depuis TokenManager

            VerificationSuccessScreen(
                userName = userName,
                onContinue = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ==================== FORGOT PASSWORD - PAGE 1 (Email) ====================
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateToOTP = {
                    // Récupérer l'email depuis le ViewModel
                    val email = forgotPasswordViewModel.uiState.value.email
                    navController.navigate(Screen.ResetPasswordOTP.createRoute(email))
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = forgotPasswordViewModel
            )
        }

        // ==================== FORGOT PASSWORD - PAGE 2 (OTP) ====================
        composable(
            route = Screen.ResetPasswordOTP.route,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""

            ResetPasswordOTPScreen(
                email = email,
                onOTPVerified = {
                    navController.navigate(Screen.NewPassword.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = forgotPasswordViewModel
            )
        }

        // ==================== FORGOT PASSWORD - PAGE 3 (Nouveau Password) ====================
        composable(Screen.NewPassword.route) {
            NewPasswordScreen(
                onPasswordResetSuccess = {
                    navController.navigate(Screen.PasswordResetSuccess.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = forgotPasswordViewModel
            )
        }

        // ==================== FORGOT PASSWORD - PAGE 4 (Succès) ====================
        composable(Screen.PasswordResetSuccess.route) {
            PasswordResetSuccessScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ========== MAIN ROUTES ==========

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToScan = {
                    navController.navigate(Screens.Scan.route)
                },
                onNavigateToSearch = {
                    navController.navigate(Screens.Search.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screens.History.route)
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
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
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
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToSecurity = {
                    // TODO: Implement security screen
                }
            )
        }

        // ========== SCAN ROUTE ✅ NOUVEAU ==========

        composable(Screens.Scan.route) {
            // Récupérer le userHash depuis TokenManager
            var userHash = ""
            runBlocking {
                val user = TokenManager.getUser(context)
                userHash = user?.userHash ?:  user?.id ?: ""
            }

            ScanScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAppDetails = { packageName ->
                    navController.navigate(Screens.AppDetails.createRoute(packageName))
                },
                userHash = userHash
            )
        }

        // ========== AUTRES ROUTES (À IMPLÉMENTER) ==========

        composable(Screens.Search.route) {
            // TODO: SearchScreen
            EmptyComingSoonScreen(
                title = "Recherche",
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screens.History.route) {
            // TODO: HistoryScreen
            EmptyComingSoonScreen(
                title = "Historique",
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screens.TopApps.route) {
            // TODO: TopAppsScreen
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
            // TODO: AppDetailsScreen
            EmptyComingSoonScreen(
                title = "Détails: $packageName",
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// ========== ÉCRAN TEMPORAIRE POUR LES ROUTES NON IMPLÉMENTÉES ==========

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
                            androidx.compose.material.icons.Icons.Default.ArrowBack,
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
                    androidx.compose.material.icons.Icons.Default.Construction,
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
