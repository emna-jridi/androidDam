package tn.esprit.dam

import timber.log.Timber
import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tn.esprit.dam.features.alert.service.PrivacyMonitorService
import tn.esprit.dam.core.session.TokenRepository
import javax.inject.Inject
import tn.esprit.dam.navigation.AppNavGraph
import tn.esprit.dam.core.ui.theme.ShadowGuardTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity(), ImageLoaderFactory {
    @Inject lateinit var tokenRepository: TokenRepository

    private var permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.values.all { it }) checkAndStartServiceIfLoggedIn()
        }

        checkAndRequestPermissions()

        setContent {
            ShadowGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val isLoggedIn by tokenRepository.isLoggedInFlow().collectAsState(initial = false)

                    LaunchedEffect(isLoggedIn) {
                        if (isLoggedIn) {
                            delay(500)
                            startPrivacyMonitoringServiceIfPermitted()
                        }
                    }

                    AppNavGraph(
                        navController = navController,
                        startDestination = if (isLoggedIn) "home" else "login"
                    )
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (!hasUsageStatsPermission()) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
            return
        }

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            return
        }

        val permissionsToRequest = mutableListOf<String>()

        if (!isGranted(Manifest.permission.CAMERA)) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        if (!isGranted(Manifest.permission.RECORD_AUDIO)) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!isGranted(Manifest.permission.POST_NOTIFICATIONS)) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher?.launch(permissionsToRequest.toTypedArray())
        } else {
            checkAndStartServiceIfLoggedIn()
        }
    }

    private fun checkAndStartServiceIfLoggedIn() {
        lifecycleScope.launch {
            val isLoggedIn = tokenRepository.isLoggedIn()
            if (isLoggedIn) {
                startPrivacyMonitoringServiceIfPermitted()
            }
        }
    }

    private fun startPrivacyMonitoringServiceIfPermitted() {
        val canMonitor = hasUsageStatsPermission() &&
            Settings.canDrawOverlays(this) &&
            isGranted(Manifest.permission.CAMERA) &&
            isGranted(Manifest.permission.RECORD_AUDIO)
        if (!canMonitor) return

        val serviceIntent = Intent(this, PrivacyMonitorService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: SecurityException) {
            Timber.w(e, "Privacy monitor service not allowed to start")
        } catch (e: IllegalStateException) {
            // Android 12+ refuses foreground-service starts from the background.
            Timber.w(e, "Privacy monitor service start refused")
        }
    }

    private fun isGranted(permission: String): Boolean =
        checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    private fun hasUsageStatsPermission(): Boolean {
        return try {
            val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            Timber.w(e, "MainActivity.hasUsageStatsPermission failed")
            false
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components { add(SvgDecoder.Factory()) }
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
