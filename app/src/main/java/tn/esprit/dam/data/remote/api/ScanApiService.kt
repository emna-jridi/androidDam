package tn.esprit.dam.data.remote.api

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import tn.esprit.dam.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ✅ Service Ktor pour tous les appels liés aux scans
 */
@Singleton
class ScanApiService @Inject constructor(
    private val client: HttpClient
) {
    companion object {
        private const val TAG = "ScanApiService"

        // Racine du backend
        private const val ROOT_URL = "http://172.18.4.239:3000"

        private const val SCAN_BASE_URL = "$ROOT_URL/api/v1/scan"

        // Module SCANS (historique avancé, stats, comparaison, etc.)
        private const val SCANS_BASE_URL = "$ROOT_URL/api/v1/scan"
    }

    // ----------------------------------------------------------
    //  ANALYZE INSTALLED APPS (POST /api/v1/scan/installed)
    // ----------------------------------------------------------
    suspend fun analyzeInstalledApps(
        token: String,
        request: AnalyzeInstalledAppsDto
    ): ScanResultResponse {
        return client.post("$SCAN_BASE_URL/installed") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // ----------------------------------------------------------
    //  SAVE SCAN (OPTIONNEL / SCANS MODULE)
    //  POST /api/v1/scans
    // ----------------------------------------------------------
    suspend fun saveScan(
        token: String,
        request: SaveScanRequest
    ): SaveScanResponse {
        return try {
            Log.d(TAG, "💾 Saving scan to DB...")

            val response: HttpResponse = client.post(SCANS_BASE_URL) {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            val result: SaveScanResponse = response.body()
            Log.d(TAG, "✅ Scan saved: ${result.scan._id}")

            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ saveScan failed: ${e.message}", e)
            throw e
        }
    }

    // ----------------------------------------------------------
    //  GET USER SCANS SIMPLES (SCAN MODULE)
    //  GET /api/v1/scan/user/:userHash
    //  On mappe sur GetScansResponse -> SavedScan
    // ----------------------------------------------------------
    suspend fun getUserScans(
        token: String,
        userHash: String
    ): List<SavedScan> {
        return try {
            Log.d(TAG, "📥 Fetching scans for user: $userHash")

            val response: GetScansResponse = client.get("$SCAN_BASE_URL/user/$userHash") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body()

            Log.d(TAG, "✅ Found ${response.data.scans.size} scans")
            response.data.scans
        } catch (e: Exception) {
            Log.e(TAG, "❌ Get scans failed: ${e.message}", e)
            emptyList()
        }
    }

    // ----------------------------------------------------------
    //  GET LATEST SCAN (SCAN MODULE)
    //  GET /api/v1/scan/latest/:userHash
    //  Renvoie ScanItem? (forme complète avec report)
    // ----------------------------------------------------------
    suspend fun getLatestScan(
        token: String,
        userHash: String
    ): Result<ScanItem?> {
        return try {
            Log.d(TAG, "📌 Fetching latest scan for: $userHash")

            val response: LatestScanResponse = client.get("$SCANS_BASE_URL/latest/$userHash") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body()

            Log.d(TAG, "✅ Latest scan: ${response.data?._id}")
            Result.success(response.data)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Get latest scan failed", e)
            Result.failure(e)
        }
    }

    // ----------------------------------------------------------
    //  GET A SCAN BY ID (SCAN MODULE)
    //  GET /api/v1/scan/:scanId
    // ----------------------------------------------------------
    suspend fun getScanById(
        token: String,
        scanId: String
    ): ScanItem {
        val response: SingleScanResponse = client.get("$SCAN_BASE_URL/$scanId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()

        return response.data
    }

    // ----------------------------------------------------------
    //  DELETE A SCAN (SCAN MODULE)
    //  DELETE /api/v1/scan/:scanId
    // ----------------------------------------------------------
    suspend fun deleteScan(
        token: String,
        scanId: String,
        userHash: String
    ): Boolean {
        return try {
            client.delete("$SCAN_BASE_URL/$scanId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(mapOf("userHash" to userHash))
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Delete scan failed: ${e.message}", e)
            false
        }
    }

    // ----------------------------------------------------------
    //  COMPARE SCANS (SCAN MODULE)
    //  POST /api/v1/scan/compare
    // ----------------------------------------------------------
    suspend fun compareScans(
        token: String,
        userHash: String,
        scanId1: String,
        scanId2: String
    ): ComparisonResponse {
        return client.post("$SCAN_BASE_URL/compare") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header("x-user-hash", userHash)
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "scanId1" to scanId1,
                    "scanId2" to scanId2
                )
            )
        }.body()
    }

    // ----------------------------------------------------------
    //  STATISTICS (SCAN MODULE)
    //  GET /api/v1/scan/stats/:userHash
    // ----------------------------------------------------------
    suspend fun getStatistics(
        token: String,
        userHash: String
    ): StatisticsResponse {
        return client.get("$SCAN_BASE_URL/stats/$userHash") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    // ========================================================================
    //  HISTORIQUE AVANCÉ (SCANS MODULE : /api/v1/scans/...)
    // ========================================================================

    /**
     * ✅ Historique paginé
     * GET /api/v1/scans/user/:userHash?page=&limit=&sortOrder=
     */
    suspend fun getScanHistory(
        token: String,
        userHash: String,
        page: Int = 1,
        limit: Int = 10,
        sortOrder: String = "desc"
    ): Result<ScanHistoryResponse> {
        return try {
            Log.d(TAG, "📜 Fetching scan history for: $userHash (page=$page, limit=$limit)")

            val response: ScanHistoryResponse = client.get("$SCANS_BASE_URL/user/$userHash") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("page", page)
                parameter("limit", limit)
                parameter("sortOrder", sortOrder)
            }.body()

            Log.d(TAG, "✅ History fetched: ${response.data.scans.size} items")
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Get scan history failed", e)
            Result.failure(e)
        }
    }

    /**
     * ✅ Dernier scan via module /scans (si tu l'utilises)
     * GET /api/v1/scans/latest/:userHash
     */
    suspend fun getLatestScanHistory(
        token: String,
        userHash: String
    ): Result<ScanHistoryItem?> {
        return try {
            val response: ScanHistoryResponse = client.get("$SCANS_BASE_URL/latest/$userHash") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body()

            val scan = response.data.scans.firstOrNull()
            Result.success(scan)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Get latest scan history failed", e)
            Result.failure(e)
        }
    }

    /**
     * ✅ Détail d’un scan pour l’historique
     * GET /api/v1/scans/:scanId
     */
    suspend fun getScanHistoryById(
        token: String,
        scanId: String
    ): Result<ScanItem> {
        return try {
            Log.d(TAG, "🔍 Fetching scan: $scanId")

            // ✅ Utiliser SingleScanResponse au lieu de ScanHistoryResponse
            val response: SingleScanResponse = client.get("$SCANS_BASE_URL/$scanId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body()

            Log.d(TAG, "✅ Scan found: ${response.data._id}")
            Result.success(response.data)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Get scan by ID failed", e)
            Result.failure(e)
        }
    }

    /**
     * ✅ Supprimer un scan (historique)
     * DELETE /api/v1/scans/:scanId
     */
    suspend fun deleteScanHistory(
        token: String,
        scanId: String,
        userHash: String
    ): Result<Boolean> {
        return try {
            Log.d(TAG, "🗑️ Deleting history scan: $scanId")

            client.delete("$SCANS_BASE_URL/$scanId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(mapOf("userHash" to userHash))
            }

            Log.d(TAG, "✅ History scan deleted successfully")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Delete history scan failed", e)
            Result.failure(e)
        }
    }

    /**
     * ✅ Comparer deux scans (historique)
     * POST /api/v1/scans/compare
     */
    suspend fun compareScanHistory(
        token: String,
        userHash: String,
        scanId1: String,
        scanId2: String
    ): Result<ComparisonResponse> {
        return try {
            val response: ComparisonResponse = client.post("$SCANS_BASE_URL/compare") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header("x-user-hash", userHash)
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "scanId1" to scanId1,
                        "scanId2" to scanId2
                    )
                )
            }.body()

            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Compare scans (history) failed", e)
            Result.failure(e)
        }
    }

    /**
     * ✅ Statistiques globales (historique)
     * GET /api/v1/scans/stats/:userHash
     */
    suspend fun getScanHistoryStatistics(
        token: String,
        userHash: String
    ): Result<StatisticsResponse> {
        return try {
            Log.d(TAG, "📊 Fetching history statistics for: $userHash")

            val response: StatisticsResponse = client.get("$SCANS_BASE_URL/stats/$userHash") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body()

            Log.d(TAG, "✅ History statistics fetched")
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Get history statistics failed", e)
            Result.failure(e)
        }
    }
}
