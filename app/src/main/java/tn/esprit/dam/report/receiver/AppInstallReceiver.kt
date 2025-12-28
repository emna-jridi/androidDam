package tn.esprit.dam.report.receiver


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import tn.esprit.dam.report.SecurityBubbleService

class AppInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_PACKAGE_ADDED) {

            Log.d("AppInstallReceiver", "🆕 New App Installed detected!")

            // 1. Check if we have permission to draw over apps
            if (!Settings.canDrawOverlays(context)) {
                Log.w("AppInstallReceiver", "❌ Overlay permission missing. Cannot show bubble.")
                return
            }

            // 2. Get the package name (it comes as "package:com.example.app")
            val packageName = intent.data?.schemeSpecificPart ?: return

            Log.d("AppInstallReceiver", "📦 Package: $packageName")

            // 3. Start the Bubble Service
            val serviceIntent = Intent(context, SecurityBubbleService::class.java).apply {
                putExtra("PACKAGE_NAME", packageName)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}