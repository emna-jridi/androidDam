package tn.esprit.dam.data.remote.api

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import tn.esprit.dam.data.model.*

object ScanApiService {
    private const val TAG = "ScanApiService"

    private const val BASE_URL = "http://192.168.1.6:3000/api/v1"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
                coerceInputValues = true
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.v("KtorClient", message)
                }
            }
            level = LogLevel.HEADERS         }
    }


    suspend fun analyzeInstalledApps(
        token: String,
        request: AnalyzeInstalledAppsDto
    ): ScanResultResponse {  // ✅ CHANGÉ: AnalyzeInstalledAppsResponse → ScanResultResponse
        return try {
            Log.d(TAG, "📤 Analyzing ${request.apps.size} apps...")
            Log.d(TAG, "   UserHash: ${request.userHash}")

            val response: HttpResponse = client.post("$BASE_URL/scan/installed") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
                setBody(request)
            }

            Log.d(TAG, "📥 Response status: ${response.status}")

            val result = response.body<ScanResultResponse>()  // ✅ CHANGÉ: AnalyzeInstalledAppsResponse → ScanResultResponse

            Log.d(TAG, "✅ Analysis complete:")
            Log.d(TAG, "   ScanId: ${result.scanId}")
            Log.d(TAG, "   TotalApps: ${result.totalApps}")
            Log.d(TAG, "   Results: ${result.results.size}")

            result

        } catch (e: Exception) {
            Log.e(TAG, "❌ Analyze failed: ${e.message}", e)
            throw e
        }
    }

    /**
     * Sauvegarder un scan en BD
     */
    suspend fun saveScan(
        token: String,
        request: SaveScanRequest
    ): SaveScanResponse {
        return try {
            Log.d(TAG, "💾 Saving scan to database...")

            val response: HttpResponse = client.post("$BASE_URL/scans") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
                setBody(request)
            }

            val result = response.body<SaveScanResponse>()
            Log.d(TAG, "✅ Scan saved: ${result.scan._id}")

            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Save scan failed: ${e.message}", e)
            throw e
        }
    }

    /**
     * Récupérer le dernier scan
     */
    suspend fun getLatestScan(token: String, userHash: String): SavedScan? {
        return try {
            Log.d(TAG, "📥 Fetching latest scan for $userHash...")

            val response = client.get("$BASE_URL/scans/latest/$userHash") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            val scan = response.body<SavedScan>()
            Log.d(TAG, "✅ Latest scan found: ${scan.totalApps} apps")
            scan

        } catch (e: Exception) {
            Log.d(TAG, "ℹ️ No previous scan found")
            null
        }
    }

    /**
     * Récupérer tous les scans
     */
    suspend fun getUserScans(token: String, userHash: String): List<SavedScan> {
        return try {
            Log.d(TAG, "📥 Fetching all scans for $userHash...")

            val response = client.get("$BASE_URL/scans/user/$userHash") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            val result = response.body<GetScansResponse>()
            Log.d(TAG, "✅ Found ${result.scans.size} scans")

            result.scans
        } catch (e: Exception) {
            Log.e(TAG, "❌ Get scans failed: ${e.message}")
            emptyList()
        }
    }

    /**
     * Récupérer un scan par ID
     */
    suspend fun getScanById(token: String, scanId: String): SavedScan {
        return client.get("$BASE_URL/scans/$scanId") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body()
    }

    /**
     * Supprimer un scan
     */
    suspend fun deleteScan(token: String, scanId: String): Boolean {
        return try {
            client.delete("$BASE_URL/scans/$scanId") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}