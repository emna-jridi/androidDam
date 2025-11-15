package tn.esprit.dam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import tn.esprit.dam.data.TokenManager
import tn.esprit.dam.navigation.AppNavGraph
import tn.esprit.dam.navigation.Screen
import tn.esprit.dam.ui.theme.ShadowGuardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ShadowGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Déterminer l'écran de départ
                    var startDestination by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(Unit) {
                        // Vérifier si l'utilisateur est déjà connecté
                        val isLoggedIn = TokenManager.isLoggedIn(this@MainActivity)

                        startDestination = if (isLoggedIn) {
                            Screen.Home.route
                        } else {
                            Screen.Login.route
                        }
                    }

                    // Afficher la navigation seulement quand on connaît le départ
                    startDestination?.let { start ->
                        AppNavGraph(
                            navController = navController,
                            startDestination = start
                        )
                    }
                }
            }
        }
    }
}
