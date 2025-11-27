package tn.esprit.dam.report


import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import tn.esprit.dam.R
import tn.esprit.dam.data.ApiClient

class SecurityBubbleService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubbleView: View? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val packageName = intent?.getStringExtra("PACKAGE_NAME")

        if (packageName != null && bubbleView == null) {
            showBubble(packageName)
        }
        return START_NOT_STICKY
    }

    private fun showBubble(packageName: String) {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = LayoutInflater.from(this)
        bubbleView = inflater.inflate(R.layout.layout_security_bubble, null)

        // 1. Configure Window Parameters (Floating)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // Allows touch outside to pass through
            PixelFormat.TRANSLUCENT
        )

        // Position: Top Center
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = 150

        // 2. Initialize UI Elements
        val tvTitle = bubbleView!!.findViewById<TextView>(R.id.tvTitle)
        val tvRisk = bubbleView!!.findViewById<TextView>(R.id.tvRisk)
        val tvPrivacy = bubbleView!!.findViewById<TextView>(R.id.tvPrivacy)
        val tvAdvice = bubbleView!!.findViewById<TextView>(R.id.tvAdvice)
        val btnClose = bubbleView!!.findViewById<Button>(R.id.btnClose)

        tvTitle.text = "Scanning $packageName..."

        // 3. Fetch Data from Backend
        scope.launch {
            val report = withContext(Dispatchers.IO) {
                ApiClient.getInstance(applicationContext).getAppSafetyReport(packageName)
            }

            if (report != null) {
                tvTitle.text = report.appName
                tvRisk.text = "Risk Level: ${report.riskLevel}"

                // Set Color based on Risk
                val riskColor = when(report.riskLevel.lowercase()) {
                    "high" -> 0xFFEF4444.toInt() // Red
                    "medium" -> 0xFFF59E0B.toInt() // Orange
                    else -> 0xFF10B981.toInt() // Green
                }
                tvRisk.setTextColor(riskColor)

                tvPrivacy.text = report.dataPrivacy

                val adviceList = StringBuilder()
                adviceList.append("💡 Recommendations:\n")
                report.recommendations.forEach { adviceList.append("• $it\n") }
                tvAdvice.text = adviceList.toString()
            } else {
                tvTitle.text = "Analysis Failed"
                tvPrivacy.text = "Could not reach ShadowGuard server."
            }
        }

        // 4. Close Button Logic
        btnClose.setOnClickListener {
            stopSelf() // Kills the service
        }

        // 5. Add to Screen
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