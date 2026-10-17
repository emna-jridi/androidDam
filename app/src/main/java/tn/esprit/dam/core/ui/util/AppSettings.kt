package tn.esprit.dam.core.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

/**
 * Opens Android's own settings page for [packageName], where the user can revoke permissions.
 * Shows [notInstalledMessage] when the app is not on this device.
 */
fun openAppSettings(context: Context, packageName: String, notInstalledMessage: String) {
    val isInstalled = runCatching { context.packageManager.getPackageInfo(packageName, 0) }.isSuccess
    if (!isInstalled) {
        Toast.makeText(context, notInstalledMessage, Toast.LENGTH_SHORT).show()
        return
    }
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, notInstalledMessage, Toast.LENGTH_SHORT).show()
    }
}
