package tn.esprit.dam.alert

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.AudioRecordingConfiguration
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import tn.esprit.dam.R
import tn.esprit.dam.data.TokenManager

class PrivacyMonitorService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Managers
    private lateinit var cameraManager: CameraManager
    private lateinit var audioManager: AudioManager
    private lateinit var usageStatsManager: UsageStatsManager

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        initializeManagers()
        startMonitoring()
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
            .setContentText("Scanning for Camera & Mic usage...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    private fun startMonitoring() {
        // 👇 THIS IS THE LOG YOU SHOULD SEE IN THE NEW VERSION
        Log.d("PrivacyMonitor", "🚀 Starting Hardware Monitoring...")

        // 1. Monitor Camera Availability
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cameraManager.registerAvailabilityCallback(object : CameraManager.AvailabilityCallback() {
                override fun onCameraUnavailable(cameraId: String) {
                    super.onCameraUnavailable(cameraId)
                    // Camera became unavailable -> Someone is using it!
                    Log.w("PrivacyMonitor", "📷 Camera $cameraId is IN USE!")
                    identifyAndSendAlert("Camera Accessed")
                }
            }, null)
        }

        // 2. Monitor Microphone Recording
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            audioManager.registerAudioRecordingCallback(object : AudioManager.AudioRecordingCallback() {
                override fun onRecordingConfigChanged(configs: List<AudioRecordingConfiguration>?) {
                    super.onRecordingConfigChanged(configs)
                    if (!configs.isNullOrEmpty()) {
                        Log.w("PrivacyMonitor", "🎤 Microphone is IN USE!")
                        identifyAndSendAlert("Microphone Accessed")
                    }
                }
            }, null)
        }
    }

    private fun identifyAndSendAlert(eventType: String) {
        serviceScope.launch {
            // We know the event happened, now find the Foreground App
            val packageName = getForegroundApp()

            if (packageName == this@PrivacyMonitorService.packageName) return@launch // Ignore self

            val appName = getAppNameFromPackage(packageName)
            Log.d("PrivacyMonitor", "⚠️ CULPRIT FOUND: $appName ($packageName)")

            val details = mapOf(
                "app_name" to appName,
                "detection_method" to "HardwareCallback"
            )

            TokenManager.sendAlertEvent(
                context = this@PrivacyMonitorService,
                packageName = packageName,
                event = eventType,
                details = details
            )
        }
    }

    // Helper to find which app is currently on screen
    private fun getForegroundApp(): String {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 10000 // Look back 10 seconds

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

    override fun onBind(intent: Intent?): IBinder? = null
}