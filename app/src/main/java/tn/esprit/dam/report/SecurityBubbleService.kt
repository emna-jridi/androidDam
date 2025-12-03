package tn.esprit.dam.report


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import tn.esprit.dam.R
import tn.esprit.dam.data.ApiClient
import tn.esprit.dam.data.model.AppSafetyReport
import kotlin.math.abs

class SecurityBubbleService : Service() {

    private lateinit var windowManager: WindowManager

    // Views
    private var headView: View? = null
    private var expandedView: View? = null

    // Data
    private var currentReport: AppSafetyReport? = null
    private var currentPackageName: String? = null

    // Dragging Variables
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var paramsHead: WindowManager.LayoutParams? = null

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startForegroundServiceNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val packageName = intent?.getStringExtra("PACKAGE_NAME")

        if (packageName != null) {
            currentPackageName = packageName

            // 1. Fetch Data
            fetchData(packageName)

            // 2. Show Head if nothing is on screen
            if (headView == null && expandedView == null) {
                showHead()
            }
        }
        return START_NOT_STICKY
    }

    // ==========================================
    // 1. SHOW FLOATING HEAD (ICON)
    // ==========================================
    private fun showHead() {
        if (headView != null) return

        headView = LayoutInflater.from(this).inflate(R.layout.layout_bubble_head, null)

        paramsHead = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        paramsHead?.gravity = Gravity.TOP or Gravity.START
        paramsHead?.x = 50
        paramsHead?.y = 200

        headView?.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = paramsHead!!.x
                    initialY = paramsHead!!.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    paramsHead!!.x = initialX + (event.rawX - initialTouchX).toInt()
                    paramsHead!!.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(view, paramsHead)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val xDiff = abs(event.rawX - initialTouchX)
                    val yDiff = abs(event.rawY - initialTouchY)
                    if (xDiff < 10 && yDiff < 10) {
                        expandBubble()
                    }
                    true
                }
                else -> false
            }
        }

        try {
            windowManager.addView(headView, paramsHead)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==========================================
    // 2. EXPAND TO CARD
    // ==========================================
    private fun expandBubble() {
        if (headView != null) {
            windowManager.removeView(headView)
            headView = null
        }

        expandedView = LayoutInflater.from(this).inflate(R.layout.layout_security_bubble, null)

        val paramsExpanded = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_DIM_BEHIND, // Dim background
            PixelFormat.TRANSLUCENT
        )
        paramsExpanded.dimAmount = 0.6f
        paramsExpanded.gravity = Gravity.CENTER

        // Update UI immediately (if data exists)
        updateExpandedViewUI()

        // --- BUTTON LISTENERS ---

        // 1. Close Button
        val btnClose = expandedView!!.findViewById<Button>(R.id.btnClose)
        btnClose.setOnClickListener {
            collapseBubble()
        }

        // 2. Settings Button (The feature you added!)
        val btnSettings = expandedView!!.findViewById<Button>(R.id.btnSettings)
        btnSettings.setOnClickListener {
            openAppSettings()
            collapseBubble() // Close bubble so user can see settings
        }

        try {
            windowManager.addView(expandedView, paramsExpanded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==========================================
    // 3. COLLAPSE
    // ==========================================
    private fun collapseBubble() {
        if (expandedView != null) {
            windowManager.removeView(expandedView)
            expandedView = null
        }
        showHead()
    }

    private fun openAppSettings() {
        val pkg = currentPackageName ?: return
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", pkg, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==========================================
    // DATA & UI UPDATES
    // ==========================================
    private fun fetchData(packageName: String) {
        scope.launch {
            val report = withContext(Dispatchers.IO) {
                ApiClient.getInstance(applicationContext).getAppSafetyReport(packageName)
            }
            currentReport = report

            // If expanded view is open, refresh it
            if (expandedView != null) {
                updateExpandedViewUI()
            }
        }
    }

    private fun updateExpandedViewUI() {
        val view = expandedView ?: return
        val report = currentReport

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvRisk = view.findViewById<TextView>(R.id.tvRisk)
        val tvPrivacy = view.findViewById<TextView>(R.id.tvPrivacy)
        val tvAdvice = view.findViewById<TextView>(R.id.tvAdvice)
        val imgShield = view.findViewById<ImageView>(R.id.imgShield)

        if (report == null) {
            tvTitle.text = "Scanning $currentPackageName..."
            return
        }

        tvTitle.text = report.appName
        tvRisk.text = report.riskLevel.uppercase()
        tvPrivacy.text = report.dataPrivacy

        val (riskColor, shieldColor) = when (report.riskLevel.lowercase()) {
            "high" -> Pair(Color.parseColor("#EF4444"), Color.parseColor("#EF4444")) // Red
            "medium" -> Pair(Color.parseColor("#F59E0B"), Color.parseColor("#F59E0B")) // Orange
            else -> Pair(Color.parseColor("#10B981"), Color.parseColor("#10B981")) // Green
        }

        tvRisk.setTextColor(riskColor)
        // tvRisk.backgroundTintList = ColorStateList.valueOf(riskColor) // Optional: Tint the pill background

        if (imgShield != null) {
            imgShield.setColorFilter(shieldColor)
        }

        val adviceList = StringBuilder()
        if (report.recommendations.isNotEmpty()) {
            adviceList.append("RECOMMENDATIONS:\n")
            report.recommendations.forEach { adviceList.append("• $it\n") }
        }
        tvAdvice.text = adviceList.toString()
    }

    private fun startForegroundServiceNotification() {
        val channelId = "shadow_guard_bubble"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Security Bubble", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("ShadowGuard")
            .setContentText("Active")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
        startForeground(2, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (headView != null) windowManager.removeView(headView)
        if (expandedView != null) windowManager.removeView(expandedView)
    }
}