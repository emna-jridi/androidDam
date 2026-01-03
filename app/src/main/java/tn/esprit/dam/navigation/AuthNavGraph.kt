package tn.esprit.dam.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import tn.esprit.dam.features.auth.forgotpassword.ForgotPasswordScreen
import tn.esprit.dam.features.auth.forgotpassword.ForgotPasswordViewModel
import tn.esprit.dam.features.auth.forgotpassword.NewPasswordScreen
import tn.esprit.dam.features.auth.forgotpassword.PasswordResetSuccessScreen
import tn.esprit.dam.features.auth.forgotpassword.ResetPasswordOTPScreen
import tn.esprit.dam.features.auth.login.LoginScreen
import tn.esprit.dam.features.auth.register.RegisterScreen
import tn.esprit.dam.features.auth.verification.EmailVerificationScreen
import tn.esprit.dam.features.auth.verification.VerificationSuccessScreen

/**
 * Authentication feature routes
 */
@androidx.compose.runtime.Composable
fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    onLoginSuccess: () -> Unit
) {
    val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel()

    composable(Screens.Login.route) {
        LoginScreen(
            onLoginSuccess = onLoginSuccess,
            onNavigateToRegister = { navController.navigate(Screens.Register.route) },
            onNavigateToForgotPassword = { navController.navigate(Screens.ForgotPassword.route) }
        )
    }

    composable(Screens.Register.route) {
        RegisterScreen(
            onRegisterSuccess = { email ->
                navController.navigate(Screens.EmailVerification.createRoute(email)) {
                    popUpTo(Screens.Register.route) { inclusive = true }
                }
            },
            onNavigateToLogin = { navController.popBackStack() }
        )
    }

    composable(
        route = Screens.EmailVerification.route,
        arguments = listOf(navArgument("email") { type = NavType.StringType })
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

    composable(Screens.VerificationSuccess.route) {
        VerificationSuccessScreen(
            userName = "Utilisateur",
            onContinue = {
                navController.navigate(Screens.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    composable(Screens.ForgotPassword.route) {
        ForgotPasswordScreen(
            onNavigateToOTP = {
                val email = forgotPasswordViewModel.uiState.value.email
                navController.navigate(Screens.ResetPasswordOTP.createRoute(email))
            },
            onNavigateBack = { navController.popBackStack() },
            viewModel = forgotPasswordViewModel
        )
    }

    composable(
        route = Screens.ResetPasswordOTP.route,
        arguments = listOf(navArgument("email") { type = NavType.StringType })
    ) { backStackEntry ->
        val email = backStackEntry.arguments?.getString("email") ?: ""

        ResetPasswordOTPScreen(
            email = email,
            onOTPVerified = {
                navController.navigate(Screens.NewPassword.createRoute(email))
            },
            onNavigateBack = { navController.popBackStack() },
            viewModel = forgotPasswordViewModel
        )
    }

    composable(
        route = Screens.NewPassword.route,
        arguments = listOf(navArgument("email") { type = NavType.StringType })
    ) { backStackEntry ->
        val email = backStackEntry.arguments?.getString("email") ?: ""

        NewPasswordScreen(
            onPasswordResetSuccess = {
                navController.navigate(Screens.PasswordResetSuccess.route) {
                    popUpTo(Screens.ForgotPassword.route) { inclusive = true }
                }
            },
            onNavigateBack = { navController.popBackStack() },
            viewModel = forgotPasswordViewModel
        )
    }

    composable(Screens.PasswordResetSuccess.route) {
        PasswordResetSuccessScreen(
            onNavigateToLogin = {
                navController.navigate(Screens.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}
