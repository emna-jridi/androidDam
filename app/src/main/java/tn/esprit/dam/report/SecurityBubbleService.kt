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
import kotlin.math.pow
import kotlin.math.sqrt

class SecurityBubbleService : Service() {

    private lateinit var windowManager: WindowManager

    // Views
    private var headView: View? = null      // The floating icon
    private var expandedView: View? = null  // The card
    private var closeView: View? = null     // 🗑️ The Trash Bin

    // Data
    private var currentReport: AppSafetyReport? = null
    private var currentPackageName: String? = null

    // Layout Params
    private var paramsHead: WindowManager.LayoutParams? = null
    private var paramsClose: WindowManager.LayoutParams? = null

    // Dragging Logic
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    // Screen Dimensions for intersection calculation
    private var screenWidth = 0
    private var screenHeight = 0

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Get Screen Size
        val metrics = resources.displayMetrics
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels

        startForegroundServiceNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val packageName = intent?.getStringExtra("PACKAGE_NAME")

        if (packageName != null) {
            currentPackageName = packageName
            fetchData(packageName)

            if (headView == null && expandedView == null) {
                showHead()
            }
        }
        return START_NOT_STICKY
    }

    // ==========================================
    // 1. SHOW FLOATING HEAD & TRASH BIN
    // ==========================================
    private fun showHead() {
        if (headView != null) return

        // 1. Setup the Trash Bin (Hidden initially)
        setupCloseTarget()

        // 2. Setup the Head
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

                    // 👇 SHOW Trash Bin when dragging starts
                    closeView?.visibility = View.VISIBLE
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val rawX = event.rawX
                    val rawY = event.rawY

                    paramsHead!!.x = initialX + (rawX - initialTouchX).toInt()
                    paramsHead!!.y = initialY + (rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(view, paramsHead)

                    // 👇 Check Intersection with Trash Bin
                    if (isOverCloseTarget(rawX, rawY)) {
                        // Grow the trash bin to indicate "Ready to delete"
                        closeView?.animate()?.scaleX(1.5f)?.scaleY(1.5f)?.setDuration(100)?.start()
                    } else {
                        // Shrink back to normal
                        closeView?.animate()?.scaleX(1.0f)?.scaleY(1.0f)?.setDuration(100)?.start()
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // 👇 HIDE Trash Bin
                    closeView?.visibility = View.GONE
                    closeView?.scaleX = 1.0f
                    closeView?.scaleY = 1.0f

                    // 👇 Check if dropped ON Trash Bin
                    if (isOverCloseTarget(event.rawX, event.rawY)) {
                        stopSelf() // 🛑 KILL THE BUBBLE
                    } else {
                        // Normal Click Detection
                        val xDiff = abs(event.rawX - initialTouchX)
                        val yDiff = abs(event.rawY - initialTouchY)
                        if (xDiff < 10 && yDiff < 10) {
                            expandBubble()
                        }
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

    // Helper: Initialize the Trash Bin View
    private fun setupCloseTarget() {
        if (closeView != null) return // Already setup

        closeView = LayoutInflater.from(this).inflate(R.layout.layout_bubble_close, null)

        paramsClose = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            // FLAG_LAYOUT_NO_LIMITS lets it sit at the very bottom edge
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        paramsClose?.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        paramsClose?.y = 100 // Margin from bottom edge

        closeView?.visibility = View.GONE // Start hidden

        try {
            windowManager.addView(closeView, paramsClose)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Helper: Math to check if bubble overlaps trash bin
    private fun isOverCloseTarget(x: Float, y: Float): Boolean {
        // Target is at Bottom Center
        val targetX = screenWidth / 2
        val targetY = screenHeight - 150 // Approximate height of bottom area

        val diffX = x - targetX
        val diffY = y - targetY
        // Distance formula
        val distance = sqrt((diffX * diffX + diffY * diffY).toDouble())

        return distance < 200 // Detection radius in pixels
    }

    // ==========================================
    // 2. EXPAND TO CARD
    // ==========================================
    private fun expandBubble() {
        if (headView != null) {
            windowManager.removeView(headView)
            headView = null
        }
        // Ensure trash is hidden
        if (closeView != null) closeView?.visibility = View.GONE

        expandedView = LayoutInflater.from(this).inflate(R.layout.layout_security_bubble, null)

        val paramsExpanded = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_DIM_BEHIND,
            PixelFormat.TRANSLUCENT
        )
        paramsExpanded.dimAmount = 0.6f
        paramsExpanded.gravity = Gravity.CENTER

        updateExpandedViewUI()

        // --- BUTTONS ---
        val btnClose = expandedView!!.findViewById<Button>(R.id.btnClose)
        btnClose.setOnClickListener {
            collapseBubble()
        }

        val btnSettings = expandedView!!.findViewById<Button>(R.id.btnSettings)
        btnSettings.setOnClickListener {
            openAppSettings()
            collapseBubble()
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
        showHead() // Re-shows head and re-enables trash logic
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
            "high" -> Pair(Color.parseColor("#EF4444"), Color.parseColor("#EF4444"))
            "medium" -> Pair(Color.parseColor("#F59E0B"), Color.parseColor("#F59E0B"))
            else -> Pair(Color.parseColor("#10B981"), Color.parseColor("#10B981"))
        }

        tvRisk.setTextColor(riskColor)
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
            val channel = NotificationChannel(
                channelId,
                "Security Bubble",
                NotificationManager.IMPORTANCE_LOW
            )
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
        // Clean up all views
        if (headView != null) windowManager.removeView(headView)
        if (expandedView != null) windowManager.removeView(expandedView)
        if (closeView != null) windowManager.removeView(closeView)
    }
}