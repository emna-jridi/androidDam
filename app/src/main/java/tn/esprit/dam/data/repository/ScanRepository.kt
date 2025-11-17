package tn.esprit.dam.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import tn.esprit.dam.data.TokenManager
import tn.esprit.dam.data.model.*
import tn.esprit.dam.data.remote.api.ScanApiService
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: ScanApiService
) {
    companion object {
        private const val TAG = "ScanRepository"
    }

    /**
     * ✅ CORRIGÉ: Retourne Result<ScanItem>
     */
    suspend fun createScan(userHash: String, apps: List<InstalledAppDto>): Result<ScanItem> {
        return try {
            val token = TokenManager.getAccessToken(context)
                ?: return Result.failure(Exception("No access token"))

            Log.d(TAG, "📤 Creating scan...")
            Log.d(TAG, "   UserHash: $userHash")
            Log.d(TAG, "   Apps: ${apps.size}")

            val analyzeRequest = AnalyzeInstalledAppsDto(
                userHash = userHash,
                apps = apps
            )

            // ✅ Appel API qui retourne ScanResultResponse
            val analyzeResponse = api.analyzeInstalledApps(token, analyzeRequest)

            Log.d(TAG, "✅ Analysis complete")
            Log.d(TAG, "   ScanId: ${analyzeResponse.scanId}")
            Log.d(TAG, "   Results: ${analyzeResponse.results.size}")

            // ✅ Créer un ScanItem à partir de la réponse
            val scanItem = ScanItem(
                _id = analyzeResponse.scanId,
                type = "batch_installed",
                userHash = userHash,
                totalApps = analyzeResponse.totalApps,
                report = ScanReport(results = analyzeResponse.results),
                createdAt = SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    Locale.getDefault()
                ).format(Date()),
                updatedAt = null
            )

            Result.success(scanItem)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Create scan failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * ✅ CORRIGÉ: Retourne Result<ScanItem?>
     */
    suspend fun getLatestScan(userHash: String): Result<ScanItem?> {
        return try {
            val token = TokenManager.getAccessToken(context)
                ?: return Result.failure(Exception("No access token"))

            api.getLatestScan(token, userHash)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Get latest scan failed: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * ✅ Récupérer un scan par ID
     */
    suspend fun getScanHistoryById(token: String, scanId: String): Result<ScanItem> {
        return api.getScanHistoryById(token, scanId)
    }

    /**
     * ✅ Récupérer l'historique des scans
     */
    suspend fun getScanHistory(
        token: String,
        userHash: String,
        page: Int = 1,
        limit: Int = 10
    ): Result<ScanHistoryResponse> {
        return api.getScanHistory(token, userHash, page, limit, "desc")
    }

    /**
     * ✅ Récupérer les applications installées
     */
    fun getInstalledApps(): List<InstalledAppDto> {
        return try {
            val packageManager = context.packageManager
            val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledPackages(
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            }

            packages
                .filter { pkg ->
                    // Filtrer les apps utilisateur uniquement
                    (pkg.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                }
                .map { pkg ->
                    InstalledAppDto(
                        packageName = pkg.packageName,
                        name = pkg.applicationInfo?.loadLabel(packageManager).toString(),
                        versionName = pkg.versionName ?: "Unknown",
                        versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            pkg.longVersionCode.toInt()
                        } else {
                            @Suppress("DEPRECATION")
                            pkg.versionCode
                        },
                        permissions = pkg.requestedPermissions?.toList() ?: emptyList()
                    )
                }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Get installed apps failed: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * ✅ Supprimer un scan
     */
    suspend fun deleteScan(token: String, scanId: String, userHash: String): Result<Boolean> {
        return api.deleteScanHistory(token, scanId, userHash)
    }

    /**
     * ✅ Comparer deux scans
     */
    suspend fun compareScans(
        token: String,
        userHash: String,
        scanId1: String,
        scanId2: String
    ): Result<ComparisonResponse> {
        return api.compareScanHistory(token, userHash, scanId1, scanId2)
    }

    /**
     * ✅ Récupérer les statistiques
     */
    suspend fun getStatistics(token: String, userHash: String): Result<StatisticsResponse> {
        return api.getScanHistoryStatistics(token, userHash)
    }
}