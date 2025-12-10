package tn.esprit.dam.alert
/*
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
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

    companion object {
        private const val TAG = "PrivacyMonitor"
        private const val CHANNEL_ID = "shadow_guard_monitor"
        private const val NOTIFICATION_ID = 1001
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Managers
    private lateinit var cameraManager: CameraManager
    private lateinit var audioManager: AudioManager
    private lateinit var usageStatsManager: UsageStatsManager

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate() called")

        // OK IMPORTANT: Démarrer en foreground AVANT toute autre opération
        startForegroundService()

        initializeManagers()
        startMonitoring()
    }

    private fun initializeManagers() {
        try {
            cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            Log.d(TAG, "Managers initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing managers", e)
        }
    }

    private fun startForegroundService() {
        try {
            createNotificationChannel()
            val notification = createNotification()

            // OK Utiliser FOREGROUND_SERVICE_TYPE_SPECIAL_USE pour Android 14+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }

            Log.d(TAG, "Foreground service started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting foreground service", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Surveillance de la Vie Privée",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitore l'utilisation de la caméra et du microphone"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ShadowGuard Actif")
            .setContentText("Surveillance caméra & micro en cours...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun startMonitoring() {
        Log.d(TAG, "[START] Starting Hardware Monitoring...")

        try {
            // 1. Monitor Camera Availability
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cameraManager.registerAvailabilityCallback(object : CameraManager.AvailabilityCallback() {
                    override fun onCameraUnavailable(cameraId: String) {
                        super.onCameraUnavailable(cameraId)
                        Log.w(TAG, "[CAMERA] Camera $cameraId is IN USE!")
                        identifyAndSendAlert("Camera Accessed")
                    }

                    override fun onCameraAvailable(cameraId: String) {
                        super.onCameraAvailable(cameraId)
                        Log.d(TAG, "[CAMERA] Camera $cameraId is now available")
                    }
                }, null)
                Log.d(TAG, "Camera monitoring registered")
            }

            // 2. Monitor Microphone Recording
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                audioManager.registerAudioRecordingCallback(object : AudioManager.AudioRecordingCallback() {
                    override fun onRecordingConfigChanged(configs: List<AudioRecordingConfiguration>?) {
                        super.onRecordingConfigChanged(configs)
                        if (!configs.isNullOrEmpty()) {
                            Log.w(TAG, "[MIC] Microphone is IN USE!")
                            identifyAndSendAlert("Microphone Accessed")
                        }
                    }
                }, null)
                Log.d(TAG, "Microphone monitoring registered")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up monitoring", e)
        }
    }

    private fun identifyAndSendAlert(eventType: String) {
        serviceScope.launch {
            try {
                // Find the foreground app
                val packageName = getForegroundApp()

                // Ignore if it's our own app
                if (packageName == this@PrivacyMonitorService.packageName) {
                    Log.d(TAG, "Ignoring self-triggered event")
                    return@launch
                }

                val appName = getAppNameFromPackage(packageName)
                Log.d(TAG, "[ALERT] CULPRIT FOUND: $appName ($packageName)")

                val details = mapOf(
                    "app_name" to appName,
                    "detection_method" to "HardwareCallback",
                    "timestamp" to System.currentTimeMillis().toString()
                )

                // Send alert to backend
                TokenManager.sendAlertEvent(
                    context = this@PrivacyMonitorService,
                    packageName = packageName,
                    event = eventType,
                    details = details
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error sending alert", e)
            }
        }
    }

    // Helper to find which app is currently on screen
    private fun getForegroundApp(): String {
        return try {
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
            lastPackage
        } catch (e: Exception) {
            Log.e(TAG, "Error getting foreground app", e)
            "Unknown"
        }
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand() called")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy() called - Stopping monitoring")
        // Cleanup if needed
    }
}*/
