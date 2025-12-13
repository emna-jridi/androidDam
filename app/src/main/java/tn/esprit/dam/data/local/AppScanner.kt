package tn.esprit.dam.data.local

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import tn.esprit.dam.features.scan.data.LocalAppInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getInstalledApps(): List<LocalAppInfo> {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        return packages.mapNotNull { app ->
            try {
                val appName = pm.getApplicationLabel(app).toString()
                val packageName = app.packageName
                
                LocalAppInfo(
                    packageName = packageName,
                    displayName = appName,
                    category = null,
                    isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    permissions = emptyList(),
                    trackers = emptyList(),
                    isSelected = false
                )
            } catch (e: Exception) {
                null // Skip apps that can't be read
            }
        }
    }
}
