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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tn.esprit.dam.Screens.AppDetailsScreen
import tn.esprit.dam.Screens.ForgotPasswordScreen
import tn.esprit.dam.Screens.HistoryScreen
import tn.esprit.dam.Screens.HomeScreen
import tn.esprit.dam.Screens.LoginScreen
import tn.esprit.dam.Screens.ProfileScreen
import tn.esprit.dam.Screens.RegisterScreen
import tn.esprit.dam.Screens.ResetPasswordScreen
import tn.esprit.dam.Screens.ScanScreen
import tn.esprit.dam.Screens.SearchScreen
import tn.esprit.dam.Screens.TopAppsScreen
import tn.esprit.dam.data.api.ApiProvider

class MainActivity : ComponentActivity() {

    private var pendingDeepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Initialiser ApiProvider au démarrage
        ApiProvider.initialize(this)

        // Gérer le deep link au démarrage
        handleDeepLink(intent)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        initialDeepLinkToken = pendingDeepLink
                    )
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

    override fun onDestroy() {
        super.onDestroy()
        // ✅ Nettoyer les ressources de l'API
        ApiProvider.cleanup()
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
fun AppNavigation(
    initialDeepLinkToken: String?
) {
    val navController = rememberNavController()
    var hasNavigated by remember { mutableStateOf(false) }

    // ✅ Récupérer l'API depuis ApiProvider (à utiliser dans les composables)
    val api = remember { ApiProvider.getApi() }

    // Gérer la navigation automatique vers reset-password si un token existe
    LaunchedEffect(initialDeepLinkToken) {
        if (initialDeepLinkToken != null && !hasNavigated) {
            Log.d("Navigation", "Navigation vers reset-password avec token")
            navController.navigate("reset-password/$initialDeepLinkToken") {
                popUpTo("login") { inclusive = false }
            }
            hasNavigated = true
        }
    }

    NavHost(navController, startDestination = "login") {
        // ============================================
        // AUTHENTICATION ROUTES
        // ============================================
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("shadowguard/home") },
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

        composable("forgot") {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

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

        // ============================================
        // PROFILE ROUTE
        // ============================================
        composable("profile") {
            ProfileScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSecurity = { navController.navigate("shadowguard/home") }
            )
        }

        // ============================================
        // SHADOWGUARD SECURITY ROUTES
        // ============================================

        // ShadowGuard Home
        composable("shadowguard/home") {
            HomeScreen(
                onNavigateToScan = { navController.navigate("shadowguard/scan") },
                onNavigateToSearch = { navController.navigate("shadowguard/search") },
                onNavigateToHistory = { navController.navigate("shadowguard/history") },
                onNavigateToTopApps = { navController.navigate("shadowguard/topapps") },
                onNavigateToProfile = { navController.navigate("profile") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // Scan Screen - ✅ Plus besoin de passer api en paramètre
        composable("shadowguard/scan") {
            ScanScreen(
                onBack = { navController.popBackStack() },
                onAppDetails = { packageName ->
                    navController.navigate("shadowguard/app-details/$packageName")
                }
            )
        }

        // Search Screen
        composable("shadowguard/search") {
            SearchScreen(
                api = api,
                onBack = { navController.popBackStack() },
                onAppDetails = { packageName ->
                    navController.navigate("shadowguard/app-details/$packageName")
                }
            )
        }

        // App Details Screen
        composable("shadowguard/app-details/{packageName}") { backStackEntry ->
            val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
            AppDetailsScreen(
                packageName = packageName,
                api = api,
                onBack = { navController.popBackStack() }
            )
        }

        // History Screen
        composable("shadowguard/history") {
            HistoryScreen(
                api = api,
                onBack = { navController.popBackStack() }
            )
        }

        // Top Apps Screen
        composable("shadowguard/topapps") {
            TopAppsScreen(
                api = api,
                onBack = { navController.popBackStack() },
                onAppDetails = { packageName ->
                    navController.navigate("shadowguard/app-details/$packageName")
                }
            )
        }
    }
}

