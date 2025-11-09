package tn.esprit.dam

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tn.esprit.dam.Screens.ForgotPasswordScreen
import tn.esprit.dam.Screens.LoginScreen
import tn.esprit.dam.Screens.ProfileScreen
import tn.esprit.dam.Screens.RegisterScreen
import tn.esprit.dam.Screens.ResetPasswordScreen

class MainActivity : ComponentActivity() {

    private var pendingDeepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Gérer le deep link au démarrage
        handleDeepLink(intent)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(initialDeepLinkToken = pendingDeepLink)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Gérer le deep link quand l'app est déjà ouverte
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data: Uri? = intent?.data

        if (data != null) {
            val scheme = data.scheme // "myapp" ou "http/https"
            val host = data.host // "reset-password" ou "your-domain.com"
            val path = data.path // "/reset-password" pour http/https
            val token = data.getQueryParameter("token")

            Log.d("DeepLink", "Reçu: $scheme://$host$path?token=$token")

            // Vérifier si c'est un lien de réinitialisation
            if ((host == "reset-password" || path == "/reset-password") && token != null) {
                pendingDeepLink = token
                Log.d("DeepLink", "Token extrait: $token")
            }
        }
    }
}

@Composable
fun AppNavigation(initialDeepLinkToken: String?) {
    val navController = rememberNavController()
    var hasNavigated by remember { mutableStateOf(false) }

    // Gérer la navigation automatique vers reset-password si un token existe
    LaunchedEffect(initialDeepLinkToken) {
        if (initialDeepLinkToken != null && !hasNavigated) {
            Log.d("Navigation", "Navigation vers reset-password avec token")
            navController.navigate("reset-password/$initialDeepLinkToken") {
                // Optionnel: éviter de s'empiler sur login
                popUpTo("login") { inclusive = false }
            }
            hasNavigated = true
        }
    }

    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("profile") },
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("forgot") }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.popBackStack() },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable("profile") {
            ProfileScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("forgot") {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Route corrigée pour le reset password avec paramètre
        composable("reset-password/{token}") { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            Log.d("ResetPassword", "Affichage de l'écran avec token: $token")

            ResetPasswordScreen(
                resetToken = token,
                onNavigateBack = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
    }
}