package tn.esprit.dam

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
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                checkAndStartServiceIfLoggedIn()
            } else {
            }
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.CAMERA)
            }

            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                }
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
        if (!hasUsageStatsPermission() || !Settings.canDrawOverlays(this)) {
            return
        }

        val hasCamera = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        } else true

        val hasMic = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else true

        if (hasCamera && hasMic) {
            startPrivacyMonitoringService()
        } else {
        }
    }

    private fun startPrivacyMonitoringService() {
        try {
            val hasCameraPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

            val hasMicPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

            if (!hasCameraPermission || !hasMicPermission) {
                return
            }

            val hasUsageStatsPermission = hasUsageStatsPermission()

            if (!hasUsageStatsPermission) {
            }

            val serviceIntent = Intent(this, PrivacyMonitorService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    startForegroundService(serviceIntent)
                } catch (e: SecurityException) {
                } catch (e: IllegalStateException) {
                }
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
        }
    }

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
