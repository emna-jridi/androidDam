package tn.esprit.dam.features.report.service
import timber.log.Timber

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
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
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import tn.esprit.dam.R
import tn.esprit.dam.features.report.data.model.AppSafetyReport
import tn.esprit.dam.features.report.data.repository.ReportRepository
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

@AndroidEntryPoint
class SecurityBubbleService : Service() {
    @Inject lateinit var reportRepository: ReportRepository

    companion object {
        private const val CHANNEL_ID = "shadow_guard_bubble"
        private const val NOTIFICATION_ID = 2
    }

    private lateinit var windowManager: WindowManager

    private var headView: View? = null
    private var expandedView: View? = null
    private var closeView: View? = null

    private var currentReport: AppSafetyReport? = null
    private var currentPackageName: String? = null

    private var paramsHead: WindowManager.LayoutParams? = null
    private var paramsClose: WindowManager.LayoutParams? = null

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private var screenWidth = 0
    private var screenHeight = 0

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val metrics = resources.displayMetrics
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        startForegroundServiceNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        val packageName = intent?.getStringExtra("PACKAGE_NAME")

        if (packageName != null) {
            currentPackageName = packageName
            fetchData(packageName)

            if (headView == null && expandedView == null) {
                showHead()
            }
        } else {
        }
        return START_NOT_STICKY
    }

    private fun showHead() {
        if (headView != null) {
            return
        }

        if (!Settings.canDrawOverlays(this)) {
            return
        }

        try {
            setupCloseTarget()

            headView = LayoutInflater.from(this).inflate(R.layout.layout_bubble_head, null)

            val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            paramsHead = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
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

                        closeView?.visibility = View.VISIBLE
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val rawX = event.rawX
                        val rawY = event.rawY

                        paramsHead!!.x = initialX + (rawX - initialTouchX).toInt()
                        paramsHead!!.y = initialY + (rawY - initialTouchY).toInt()
                        try {
                            windowManager.updateViewLayout(view, paramsHead)
                        } catch (e: Exception) {
                            Timber.w(e, "SecurityBubbleService.showHead failed")
                        }

                        if (isOverCloseTarget(rawX, rawY)) {
                            closeView?.animate()?.scaleX(1.5f)?.scaleY(1.5f)?.setDuration(100)?.start()
                        } else {
                            closeView?.animate()?.scaleX(1.0f)?.scaleY(1.0f)?.setDuration(100)?.start()
                        }
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        closeView?.visibility = View.GONE
                        closeView?.scaleX = 1.0f
                        closeView?.scaleY = 1.0f

                        if (isOverCloseTarget(event.rawX, event.rawY)) {
                            stopSelf()
                        } else {
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

            windowManager.addView(headView, paramsHead)
        } catch (e: WindowManager.BadTokenException) {
            headView = null
            stopSelf()
        } catch (e: SecurityException) {
            headView = null
            stopSelf()
        } catch (e: Exception) {
            Timber.w(e, "SecurityBubbleService.showHead failed")
            headView = null
        }
    }

    private fun setupCloseTarget() {
        if (closeView != null) return

        closeView = LayoutInflater.from(this).inflate(R.layout.layout_bubble_close, null)

        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        paramsClose = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        paramsClose?.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        paramsClose?.y = 100

        closeView?.visibility = View.GONE

        try {
            windowManager.addView(closeView, paramsClose)
        } catch (e: Exception) {
            Timber.w(e, "SecurityBubbleService.setupCloseTarget failed")
            closeView = null
        }
    }

    private fun isOverCloseTarget(x: Float, y: Float): Boolean {
        val targetX = screenWidth / 2
        val targetY = screenHeight - 150

        val diffX = x - targetX
        val diffY = y - targetY
        val distance = sqrt((diffX * diffX + diffY * diffY).toDouble())

        return distance < 200
    }

    private fun expandBubble() {
        if (headView != null) {
            windowManager.removeView(headView)
            headView = null
        }
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
            Timber.e(e, "SecurityBubbleService failed")
        }
    }

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
            Timber.e(e, "SecurityBubbleService failed")
        }
    }

    private fun fetchData(packageName: String) {
        scope.launch {
            val report = withContext(Dispatchers.IO) {
                reportRepository.getAppSafetyReport(packageName)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Security Bubble",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows security analysis bubble overlay"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ShadowGuard Security")
            .setContentText("Analyzing app security...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (e2: Exception) {
                Timber.w(e2, "SecurityBubbleService.startForegroundServiceNotification failed")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            headView?.let {
                windowManager.removeView(it)
            }
        } catch (e: Exception) {
            Timber.w(e, "SecurityBubbleService.onDestroy failed")
        }

        try {
            expandedView?.let {
                windowManager.removeView(it)
            }
        } catch (e: Exception) {
            Timber.w(e, "SecurityBubbleService.onDestroy failed")
        }

        try {
            closeView?.let {
                windowManager.removeView(it)
            }
        } catch (e: Exception) {
            Timber.w(e, "SecurityBubbleService.onDestroy failed")
        }

        headView = null
        expandedView = null
        closeView = null

        scope.cancel()
    }
}
