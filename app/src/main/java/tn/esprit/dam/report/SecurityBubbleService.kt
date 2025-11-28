package tn.esprit.dam.report


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import tn.esprit.dam.R
import tn.esprit.dam.data.ApiClient

class SecurityBubbleService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubbleView: View? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        // ✅ 1. Start as Foreground Service immediately to prevent crash
        startForegroundServiceNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val packageName = intent?.getStringExtra("PACKAGE_NAME")

        if (packageName != null) {
            if (bubbleView == null) {
                showBubble(packageName)
            } else {
                // If bubble already exists, just update it (optional logic)
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundServiceNotification() {
        val channelId = "shadow_guard_bubble"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Security Bubble",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("ShadowGuard Analysis")
            .setContentText("Scanning installed app...")
            .setSmallIcon(R.mipmap.ic_launcher) // Ensure this icon exists
            .setOngoing(true)
            .build()

        // ID must be different from PrivacyMonitorService (use 2)
        startForeground(2, notification)
    }

    private fun showBubble(packageName: String) {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = LayoutInflater.from(this)
        bubbleView = inflater.inflate(R.layout.layout_security_bubble, null)

        // Window Params
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = 150

        // UI References
        val tvTitle = bubbleView!!.findViewById<TextView>(R.id.tvTitle)
        val tvRisk = bubbleView!!.findViewById<TextView>(R.id.tvRisk)
        val tvPrivacy = bubbleView!!.findViewById<TextView>(R.id.tvPrivacy)
        val tvAdvice = bubbleView!!.findViewById<TextView>(R.id.tvAdvice)
        val btnClose = bubbleView!!.findViewById<Button>(R.id.btnClose)

        tvTitle.text = "Scanning $packageName..."

        // Call Backend
        scope.launch {
            val report = withContext(Dispatchers.IO) {
                ApiClient.getInstance(applicationContext).getAppSafetyReport(packageName)
            }

            if (report != null) {
                tvTitle.text = report.appName
                tvRisk.text = "Risk Level: ${report.riskLevel}"

                val riskColor = when(report.riskLevel.lowercase()) {
                    "high" -> 0xFFEF4444.toInt()
                    "medium" -> 0xFFF59E0B.toInt()
                    else -> 0xFF10B981.toInt()
                }
                tvRisk.setTextColor(riskColor)
                tvPrivacy.text = report.dataPrivacy

                val adviceList = StringBuilder()
                adviceList.append("💡 Recommendations:\n")
                report.recommendations.forEach { adviceList.append("• $it\n") }
                tvAdvice.text = adviceList.toString()
            } else {
                tvTitle.text = "Scan Failed"
                tvPrivacy.text = "Could not reach server."
            }
        }

        btnClose.setOnClickListener {
            stopSelf()
        }

        try {
            windowManager.addView(bubbleView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bubbleView != null) {
            windowManager.removeView(bubbleView)
            bubbleView = null
        }
    }
}