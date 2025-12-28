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
import android.util.Log
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
import tn.esprit.dam.alert.PrivacyMonitorService
import tn.esprit.dam.data.TokenManager
import tn.esprit.dam.navigation.AppNavGraph
import tn.esprit.dam.ui.theme.ShadowGuardTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity(), ImageLoaderFactory {

    private var permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup permission launcher BEFORE setContent
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                Log.d("MainActivity", "✅ All permissions granted")
                // Check if user is logged in before starting service
                checkAndStartServiceIfLoggedIn()
            } else {
                Log.w("MainActivity", "⚠️ Some permissions denied: $permissions")
                // Don't give up - we'll ask again next time
            }
        }

        // Check permissions on EVERY app launch
        checkAndRequestPermissions()

        setContent {
            ShadowGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val isLoggedIn by TokenManager.isLoggedInFlow(this).collectAsState(initial = false)

                    // Try to start service when user logs in (if permissions already granted)
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

    /**
     * Check permissions on every app launch and request if missing.
     * Logic adapted to enforce Usage Stats and Overlay permissions like androidDam.
     */
    private fun checkAndRequestPermissions() {
        // 1. Check Usage Stats (Critical for Alerts)
        if (!hasUsageStatsPermission()) {
            Log.w("MainActivity", "⚠️ Usage Stats permission missing! Redirecting user.")
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
            return
        }
        
        // 2. Check Overlay Permission (Critical for Bubble)
        if (!Settings.canDrawOverlays(this)) {
            Log.w("MainActivity", "⚠️ Overlay permission missing! Redirecting user.")
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            return
        }

        // 3. Check Runtime Permissions (Camera, Mic, Notifications)
        val permissionsToRequest = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check Camera permission
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.CAMERA)
            }
            
            // Check Microphone permission
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
            }
            
            // Check Notification permission (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            Log.d("MainActivity", "📋 Missing runtime permissions, requesting: $permissionsToRequest")
            permissionLauncher?.launch(permissionsToRequest.toTypedArray())
        } else {
            Log.d("MainActivity", "✅ All permissions (Special & Runtime) granted")
            checkAndStartServiceIfLoggedIn()
        }
    }

    /**
     * Check if user is logged in and start service if so
     */
    private fun checkAndStartServiceIfLoggedIn() {
        lifecycleScope.launch {
            val isLoggedIn = TokenManager.isLoggedIn(this@MainActivity)
            if (isLoggedIn) {
                startPrivacyMonitoringServiceIfPermitted()
            }
        }
    }

    /**
     * Start service only if permitted
     */
    private fun startPrivacyMonitoringServiceIfPermitted() {
        // Double check special permissions
        if (!hasUsageStatsPermission() || !Settings.canDrawOverlays(this)) {
            Log.w("MainActivity", "⚠️ Special permissions missing, cannot start service")
            return
        }

        // Check runtime permissions
        val hasCamera = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        } else true
        
        val hasMic = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else true
        
        if (hasCamera && hasMic) {
            startPrivacyMonitoringService()
        } else {
            Log.w("MainActivity", "⚠️ Cannot start service - missing Camera/Mic permissions")
        }
    }

    /**
     * Start the PrivacyMonitorService that monitors camera/microphone access
     */
    private fun startPrivacyMonitoringService() {
        try {
            Log.d("MainActivity", "🚀 Attempting to start PrivacyMonitorService...")
            
            // Check runtime permissions for camera and microphone
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
            
            // On Android 14+, foreground service with camera/mic type REQUIRES the runtime permissions
            if (!hasCameraPermission || !hasMicPermission) {
                Log.w("MainActivity", "⚠️ Missing CAMERA or RECORD_AUDIO permissions - cannot start PrivacyMonitorService")
                return
            }
            
            // Check if we have necessary permissions
            val hasUsageStatsPermission = hasUsageStatsPermission()
            
            if (!hasUsageStatsPermission) {
                Log.w("MainActivity", "⚠️ Missing Usage Stats permission - service may not identify apps correctly")
            }
            
            // Start the foreground service
            val serviceIntent = Intent(this, PrivacyMonitorService::class.java)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    startForegroundService(serviceIntent)
                    Log.d("MainActivity", "✅ PrivacyMonitorService started (foreground)")
                } catch (e: SecurityException) {
                    Log.e("MainActivity", "❌ Security exception starting foreground service", e)
                } catch (e: IllegalStateException) {
                    Log.e("MainActivity", "❌ Illegal state starting foreground service", e)
                }
            } else {
                startService(serviceIntent)
                Log.d("MainActivity", "✅ PrivacyMonitorService started")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "❌ Failed to start PrivacyMonitorService", e)
        }
    }

    /**
     * Check if app has Usage Stats permission (required to identify foreground apps)
     */
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
            Log.e("MainActivity", "Error checking usage stats permission", e)
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
