package tn.esprit.dam.features.alert.service

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
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import tn.esprit.dam.R
import tn.esprit.dam.features.alert.data.repository.AlertRepository
import tn.esprit.dam.features.report.service.SecurityBubbleService
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@AndroidEntryPoint
class PrivacyMonitorService : Service() {
    @Inject lateinit var alertRepository: AlertRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var cameraManager: CameraManager
    private lateinit var audioManager: AudioManager
    private lateinit var usageStatsManager: UsageStatsManager

    private val lastAlertTime = ConcurrentHashMap<String, Long>()
    private val COOLDOWN_MS = 10000L

    private val appInstallReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_PACKAGE_ADDED) {
                if (!Settings.canDrawOverlays(context)) {
                    return
                }

                val packageName = intent.data?.schemeSpecificPart ?: return

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
        registerInstallReceiver()
    }

    private fun registerInstallReceiver() {
        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addDataScheme("package")
            }
            registerReceiver(appInstallReceiver, filter)
        } catch (e: Exception) {
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

            try {
                alertRepository.sendAlertEvent(
                    packageName = packageName,
                    event = eventType,
                    details = mapOf("app_name" to appName, "detection_method" to "HardwareCallback")
                )
            } catch (e: Exception) {
            }
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
        try {
            unregisterReceiver(appInstallReceiver)
        } catch (e: Exception) {
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
