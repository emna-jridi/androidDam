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
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import tn.esprit.dam.alert.PrivacyMonitorService
import tn.esprit.dam.data.TokenManager
import tn.esprit.dam.navigation.AppNavGraph
import tn.esprit.dam.navigation.Screens
import tn.esprit.dam.ui.theme.ShadowGuardTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity(), ImageLoaderFactory {

    // ✅ 1. Setup Permission Launcher for Android 13+ Notifications
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted. Starting service.")
            startPrivacyService()
        } else {
            Log.w("MainActivity", "Notification permission denied. Service notification may not appear.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 2. Get FCM token and send to backend (Existing Logic)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM_TOKEN", "Device token: ${token ?: "No token"}")

                lifecycleScope.launch {
                    TokenManager.sendFcmTokenToBackend(this@MainActivity, token)
                }
            } else {
                Log.e("FCM_TOKEN", "Fetching FCM token failed", task.exception)
            }
        }

        // ✅ 3. Check Permissions and Start Privacy Monitor
        // 👇 CHANGED: First we check for Usage Stats (Critical for identifying apps)
        if (!hasUsageStatsPermission()) {
            Log.w("MainActivity", "Usage Stats permission missing! Redirecting user.")
            // Direct the user to the specific settings page
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        } else {
            // If Usage Stats are granted, we check for Notifications and start the service
            checkPermissionsAndStartService()
        }

        setContent {
            ShadowGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // ✅ Determine start destination dynamically
                    val startDestination = runBlocking {
                        val token = TokenManager.getAccessToken(this@MainActivity)
                        if (token != null) Screens.Home.route else Screens.Login.route
                    }

                    AppNavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }

    // 👇 NEW FUNCTION: Checks if user granted Usage Access
    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // ✅ 4. Helper function to check permissions (Notifications)
    private fun checkPermissionsAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                startPrivacyService()
            } else {
                // Request permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Android 12 or lower doesn't need runtime permission for notifications
            startPrivacyService()
        }
    }

    // ✅ 5. Helper function to actually start the service
    private fun startPrivacyService() {
        try {
            val intent = Intent(this, PrivacyMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Log.d("MainActivity", "PrivacyMonitorService started command sent.")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to start service", e)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components { add(SvgDecoder.Factory()) }
            .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.25).build() }
            .diskCache { DiskCache.Builder().directory(cacheDir.resolve("image_cache")).maxSizeBytes(50L * 1024 * 1024).build() }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false)
            .build()
    }
}