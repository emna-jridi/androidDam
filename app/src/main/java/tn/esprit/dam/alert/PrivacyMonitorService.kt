package tn.esprit.dam.alert

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.AudioRecordingConfiguration
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import tn.esprit.dam.R
import tn.esprit.dam.data.TokenManager
import tn.esprit.dam.report.SecurityBubbleService // Import the Bubble Service
import java.util.concurrent.ConcurrentHashMap

class PrivacyMonitorService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Managers
    private lateinit var cameraManager: CameraManager
    private lateinit var audioManager: AudioManager
    private lateinit var usageStatsManager: UsageStatsManager

    // Cooldown Tracker
    private val lastAlertTime = ConcurrentHashMap<String, Long>()
    private val COOLDOWN_MS = 10000L

    // 👇 NEW: Dynamic Receiver for App Installs
    private val appInstallReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_PACKAGE_ADDED) {
                Log.d("PrivacyMonitor", "📦 NEW APP DETECTED via Dynamic Receiver!")

                if (!Settings.canDrawOverlays(context)) {
                    Log.w("PrivacyMonitor", "❌ Overlay permission missing.")
                    return
                }

                val packageName = intent.data?.schemeSpecificPart ?: return
                Log.d("PrivacyMonitor", "🔍 Analyzing: $packageName")

                val bubbleIntent = Intent(context, SecurityBubbleService::class.java).apply {
                    putExtra("PACKAGE_NAME", packageName)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(bubbleIntent)
                } else {
                    context.startService(bubbleIntent)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        initializeManagers()
        startMonitoring()
        registerInstallReceiver() // 👈 Trigger registration
    }

    private fun registerInstallReceiver() {
        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addDataScheme("package") // Crucial: Listen for package:// events
            }
            registerReceiver(appInstallReceiver, filter)
            Log.d("PrivacyMonitor", "✅ App Install Listener Registered")
        } catch (e: Exception) {
            Log.e("PrivacyMonitor", "❌ Failed to register Install Receiver", e)
        }
    }

    private fun initializeManagers() {
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    private fun startForegroundService() {
        val channelId = "shadow_guard_monitor"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Privacy Monitor", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("ShadowGuard Active")
            .setContentText("Monitoring Camera, Mic & New Installs...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    private fun startMonitoring() {
        Log.d("PrivacyMonitor", "🚀 Starting Hardware Monitoring...")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cameraManager.registerAvailabilityCallback(object : CameraManager.AvailabilityCallback() {
                override fun onCameraUnavailable(cameraId: String) {
                    super.onCameraUnavailable(cameraId)
                    identifyAndSendAlert("Camera Accessed")
                }
            }, null)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            audioManager.registerAudioRecordingCallback(object : AudioManager.AudioRecordingCallback() {
                override fun onRecordingConfigChanged(configs: List<AudioRecordingConfiguration>?) {
                    super.onRecordingConfigChanged(configs)
                    if (!configs.isNullOrEmpty()) {
                        identifyAndSendAlert("Microphone Accessed")
                    }
                }
            }, null)
        }
    }

    private fun identifyAndSendAlert(eventType: String) {
        serviceScope.launch {
            val packageName = getForegroundApp()
            if (packageName == this@PrivacyMonitorService.packageName || packageName == "Unknown") return@launch

            val currentTime = System.currentTimeMillis()
            val lastTime = lastAlertTime[packageName] ?: 0L

            if (currentTime - lastTime < COOLDOWN_MS) return@launch

            lastAlertTime[packageName] = currentTime
            val appName = getAppNameFromPackage(packageName)
            Log.d("PrivacyMonitor", "⚠️ CULPRIT FOUND: $appName ($packageName)")

            TokenManager.sendAlertEvent(
                context = this@PrivacyMonitorService,
                packageName = packageName,
                event = eventType,
                details = mapOf("app_name" to appName, "detection_method" to "HardwareCallback")
            )
        }
    }

    private fun getForegroundApp(): String {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 10000
        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var lastPackage = "Unknown"
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastPackage = event.packageName
            }
        }
        return lastPackage
    }

    private fun getAppNameFromPackage(packageName: String): String {
        return try {
            val pm = packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // ✅ Unregister to avoid memory leaks
        try {
            unregisterReceiver(appInstallReceiver)
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}