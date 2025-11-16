package tn.esprit.dam.data.repository


import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tn.esprit.dam.data.TokenManager
import tn.esprit.dam.data.model.*
import tn.esprit.dam.data.remote.api.ScanApiService
import java.text.SimpleDateFormat
import java.util.*

class ScanRepository(private val context: Context) {

    companion object {
        private const val TAG = "ScanRepository"
    }

    suspend fun getInstalledApps(): List<InstalledAppDto> = withContext(Dispatchers.IO) {
        try {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

            packages
                .filter { appInfo ->
                    (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                }
                .mapNotNull { appInfo ->
                    try {
                        val packageInfo = pm.getPackageInfo(appInfo.packageName, 0)
                        val permissionInfo = try {
                            pm.getPackageInfo(appInfo.packageName, PackageManager.GET_PERMISSIONS)
                        } catch (e: Exception) {
                            null
                        }

                        InstalledAppDto(
                            packageName = appInfo.packageName,
                            name = pm.getApplicationLabel(appInfo).toString(),
                            versionName = packageInfo.versionName ?: "Unknown",
                            versionCode = packageInfo.versionCode,
                            permissions = permissionInfo?.requestedPermissions?.toList() ?: emptyList()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createScan(userHash: String, apps: List<InstalledAppDto>): Result<SavedScan> {
        return try {
            val token = TokenManager.getAccessToken(context)
                ?: return Result.failure(Exception("No access token"))

            Log.d(TAG, "📤 Creating scan...")
            Log.d(TAG, "   UserHash: $userHash")
            Log.d(TAG, "   Apps: ${apps.size}")

            // 1. Analyser les apps
            val analyzeRequest = AnalyzeInstalledAppsDto(
                userHash = userHash,
                apps = apps
            )

            val analyzeResponse = ScanApiService.analyzeInstalledApps(token, analyzeRequest)

            Log.d(TAG, "✅ Analysis complete")
            Log.d(TAG, "   ScanId: ${analyzeResponse.scanId}")
            Log.d(TAG, "   Results: ${analyzeResponse.results.size}")

            // 2. Créer SavedScan
            val savedScan = SavedScan(
                _id = null,
                userHash = userHash,
                scanDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date()),
                totalApps = analyzeResponse.totalApps,
                results = analyzeResponse.results,
                summary = analyzeResponse.summary,
                scanId = analyzeResponse.scanId,
                createdAt = null
            )

            // 3. Optionnel : Sauvegarder en BD (si l'endpoint existe)
            try {
                val saveRequest = SaveScanRequest(
                    userHash = userHash,
                    scanId = analyzeResponse.scanId,
                    totalApps = analyzeResponse.totalApps,
                    results = analyzeResponse.results,
                    summary = analyzeResponse.summary
                )

                val saveResponse = ScanApiService.saveScan(token, saveRequest)
                Log.d(TAG, "✅ Scan saved in database")

                Result.success(saveResponse.scan)
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Could not save to DB, using local: ${e.message}")
                Result.success(savedScan)
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Create scan failed: ${e.message}", e)
            Result.failure(e)
        }
    }


    suspend fun getLatestScan(userHash: String): Result<SavedScan?> {
        return try {
            val token = TokenManager.getAccessToken(context)
                ?: return Result.failure(Exception("No access token"))

            val scan = ScanApiService.getLatestScan(token, userHash)
            Result.success(scan)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Get latest scan failed: ${e.message}")
            Result.failure(e)
        }
    }


    suspend fun getUserScans(userHash: String): Result<List<SavedScan>> {
        return try {
            val token = TokenManager.getAccessToken(context)
                ?: return Result.failure(Exception("No access token"))

            val scans = ScanApiService.getUserScans(token, userHash)
            Result.success(scans)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Get user scans failed: ${e.message}")
            Result.failure(e)
        }
    }
}