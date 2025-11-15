package tn.esprit.dam.navigation


import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
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

        // ==================== HOME (Placeholder) ====================
        composable("home") {
            HomeScreen(
                onNavigateToScan = { navController.navigate("scan") },
                onNavigateToSearch = { navController.navigate("search") },
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToTopApps = { navController.navigate("top_apps") },
                onNavigateToProfile = { navController.navigate("profile") },
                onLogout = {
                    // Ici tu peux par exemple naviguer vers le login et vider le backstack
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                currentRoute = navController.currentBackStackEntry?.destination?.route ?: "home"
            )
        }

    }
}

