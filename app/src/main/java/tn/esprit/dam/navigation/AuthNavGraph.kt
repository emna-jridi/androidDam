package tn.esprit.dam.navigation

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import tn.esprit.dam.features.auth.presentation.forgotpassword.ForgotPasswordScreen
import tn.esprit.dam.features.auth.presentation.forgotpassword.ForgotPasswordViewModel
import tn.esprit.dam.features.auth.presentation.forgotpassword.NewPasswordScreen
import tn.esprit.dam.features.auth.presentation.forgotpassword.PasswordResetSuccessScreen
import tn.esprit.dam.features.auth.presentation.forgotpassword.ResetPasswordOTPScreen
import tn.esprit.dam.features.auth.presentation.login.LoginScreen
import tn.esprit.dam.features.auth.presentation.register.RegisterScreen
import tn.esprit.dam.features.auth.presentation.verification.EmailVerificationScreen
import tn.esprit.dam.features.auth.presentation.verification.VerificationSuccessScreen

fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    onLoginSuccess: () -> Unit
) {
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
            onContinue = {
                navController.navigate(Screens.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    composable(Screens.ForgotPassword.route) { backStackEntry ->
        val forgotPasswordViewModel: ForgotPasswordViewModel = hiltViewModel(backStackEntry)
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
        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry(Screens.ForgotPassword.route)
        }
        val forgotPasswordViewModel: ForgotPasswordViewModel = hiltViewModel(parentEntry)

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
        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry(Screens.ForgotPassword.route)
        }
        val forgotPasswordViewModel: ForgotPasswordViewModel = hiltViewModel(parentEntry)

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
