package tn.esprit.dam.data.api

import android.content.Context
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import tn.esprit.dam.data.api.models.ApiResponse
import tn.esprit.dam.data.api.models.ApiResult
import tn.esprit.dam.data.api.models.AppDetailsResponse
import tn.esprit.dam.data.api.models.LatestScanResponse
import tn.esprit.dam.data.api.models.ScanStatusResponse
import tn.esprit.dam.data.api.models.StartScanAppDto
import tn.esprit.dam.data.api.models.StartScanRequest
import tn.esprit.dam.data.api.models.StartScanResponse
import tn.esprit.dam.data.api.models.SearchAppRequest
import tn.esprit.dam.data.api.models.SearchAppResponse
import tn.esprit.dam.data.api.models.ScanHistoryResponse
import tn.esprit.dam.data.TokenManager
import java.io.IOException

class ScanApiService(
    private val context: Context,
    private val tokenManager: TokenManager
) {
    private val client: HttpClient = KtorClient.getInstance(context, tokenManager)
    private val TAG = "ScanApiService"

    /**
     * Safe API call wrapper that checks HTTP status before deserializing
     */
    private suspend inline fun <reified T> safeApiCall(
        endpoint: String,
        block: suspend () -> HttpResponse
    ): ApiResult<T> {
        return try {
            val response = block()
            val statusCode = response.status.value
            
            Log.d(TAG, "[$endpoint] HTTP $statusCode")
            
            if (!response.status.isSuccess()) {
                // Non-2xx status - read raw response
                val rawBody = response.bodyAsText()
                Log.e(TAG, "[$endpoint] Error response: $rawBody")
                return ApiResult.ApiError(
                    message = "Erreur serveur (HTTP $statusCode)",
                    code = statusCode
                )
            }
            
            // Success - deserialize response
            val apiResponse = response.body<ApiResponse<T>>()
            
            if (apiResponse.success && apiResponse.data != null) {
                ApiResult.Success(apiResponse.data!!)
            } else {
                ApiResult.ApiError(
                    message = apiResponse.message ?: "Erreur inconnue",
                    code = statusCode
                )
            }
        } catch (e: IOException) {
            Log.e(TAG, "[$endpoint] Network error", e)
            ApiResult.NetworkError(e)
        } catch (e: Exception) {
            Log.e(TAG, "[$endpoint] Serialization error", e)
            ApiResult.SerializationError(e, null)
        }
    }

    /**
     * Start a security scan for selected apps
     */
    suspend fun startScan(
        apps: List<String>,
        userId: String,
        deviceId: String,
        includeSystemApps: Boolean = false
    ): ApiResult<StartScanResponse> {
        val appDtos = apps.map { packageName ->
            StartScanAppDto(
                packageName = packageName,
                displayName = "",
                category = null
            )
        }

        // Generate ISO 8601 timestamp (compatible with API 24+)
        val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.format(java.util.Date())

        val request = StartScanRequest(
            deviceId = deviceId,
            platform = "android",
            includeSystemApps = false,
            apps = appDtos,
            userId = userId,
            timestamp = timestamp
        )

        // Log request body before sending
        Log.d(TAG, "[/api/scan/start] Request body:")
        Log.d(TAG, "  deviceId: $deviceId")
        Log.d(TAG, "  platform: android")
        Log.d(TAG, "  includeSystemApps: false")
        Log.d(TAG, "  userId: $userId")
        Log.d(TAG, "  timestamp: $timestamp")
        Log.d(TAG, "  apps count: ${appDtos.size}")

        return safeApiCall("/api/scan/start") {
            client.post("/api/scan/start") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    /**
     * Get the status of an ongoing scan
     */
    suspend fun getScanStatus(scanId: String): ApiResult<ScanStatusResponse> {
        return safeApiCall("/api/scan/status/$scanId") {
            client.get("/api/scan/status/$scanId")
        }
    }

    /**
     * Get latest scan results for a user
     * This is the ONLY method to get scan data for Home screen
     */
    suspend fun getLatestScan(userId: String): ApiResult<LatestScanResponse> {
        return safeApiCall("/api/scan/latest/$userId") {
            client.get("/api/scan/latest/$userId")
        }
    }

    /**
     * Get detailed analysis for a specific app
     */
    suspend fun getAppDetails(packageName: String): ApiResult<AppDetailsResponse> {
        return safeApiCall("/api/scan/app/$packageName") {
            client.get("/api/scan/app/$packageName")
        }
    }

    /**
     * Search for apps in Play Store
     */
    suspend fun searchApps(query: String, platform: String = "android", limit: Int = 10): ApiResult<SearchAppResponse> {
        val request = SearchAppRequest(
            query = query,
            platform = platform,
            limit = limit
        )

        Log.d(TAG, "[/api/apps/search] Searching for: $query")

        return safeApiCall("/api/apps/search") {
            client.post("/api/apps/search") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    /**
     * Get scan history for a user
     */
    suspend fun getScanHistory(userId: String, limit: Int = 10, offset: Int = 0): ApiResult<ScanHistoryResponse> {
        Log.d(TAG, "[/api/scan/history] Fetching history for user: $userId")

        return safeApiCall("/api/scan/history") {
            client.get("/api/scan/history?userId=$userId&limit=$limit&offset=$offset")
        }
    }

    /**
     * Get full app details from apps endpoint (includes store data and analysis)
     */
    suspend fun getFullAppDetails(packageName: String): ApiResult<AppDetailsResponse> {
        return safeApiCall("/api/apps/$packageName") {
            client.get("/api/apps/$packageName")
        }
    }
}
