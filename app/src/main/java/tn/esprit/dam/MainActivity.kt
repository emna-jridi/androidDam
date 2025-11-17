package tn.esprit.dam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import tn.esprit.dam.data.TokenManager
import tn.esprit.dam.navigation.AppNavGraph
import tn.esprit.dam.navigation.Screens
import tn.esprit.dam.ui.theme.ShadowGuardTheme

/**
 * ✅ AJOUT: @AndroidEntryPoint
 * Ceci permet à Hilt d'injecter les dépendances
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity(), ImageLoaderFactory {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShadowGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // ✅ Déterminer la destination de départ
                    val startDestination = runBlocking {
                        val token = TokenManager.getAccessToken(this@MainActivity)
                        if (token != null) Screens.Home.route else Screens.Login.route
                    }

                    // ✅ SIMPLIFICATION: Plus besoin de passer les ViewModels !
                    // Hilt les injecte automatiquement
                    AppNavGraph(
                        navController = navController,
                        startDestination = Screens.Login.route
                    )
                }
            }
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024)
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false)
            .build()
    }
}