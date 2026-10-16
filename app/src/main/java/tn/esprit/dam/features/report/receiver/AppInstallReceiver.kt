package tn.esprit.dam.features.report.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import tn.esprit.dam.features.report.service.SecurityBubbleService

class AppInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_PACKAGE_ADDED) {
            if (!Settings.canDrawOverlays(context)) {
                return
            }

            val packageName = intent.data?.schemeSpecificPart ?: return

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
