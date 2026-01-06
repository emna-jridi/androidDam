package tn.esprit.dam.data.api

import android.content.Context
import android.net.Uri
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.forms.formData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import tn.esprit.dam.R
import tn.esprit.dam.data.api.models.ApiResponse
import tn.esprit.dam.data.api.models.ApiResult
import tn.esprit.dam.data.api.models.AppDetailsResponse
import tn.esprit.dam.data.api.models.LatestScanResponse
import tn.esprit.dam.data.api.models.ScanStatusResponse
import tn.esprit.dam.data.api.models.StartScanAppDto
import tn.esprit.dam.data.api.models.StartScanRequest
import tn.esprit.dam.data.api.models.StartScanResponse
import tn.esprit.dam.data.api.models.AnalysisType
import tn.esprit.dam.data.api.models.ScanLevel
import tn.esprit.dam.data.api.models.SearchAppRequest
import tn.esprit.dam.data.api.models.SearchAppResponse
import tn.esprit.dam.data.api.models.ScanHistoryResponse
import tn.esprit.dam.data.api.models.ScanResultResponse
import tn.esprit.dam.data.TokenManager
import java.io.IOException
import java.io.InputStream

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
        expectApiWrapper: Boolean = true,
        block: suspend () -> HttpResponse
    ): ApiResult<T> {
        return try {
            val response = block()
            val statusCode = response.status.value
            
            Log.d(TAG, "[$endpoint] HTTP $statusCode")
            
            if (!response.status.isSuccess()) {
                // Non-2xx status - read raw response
                val rawBody = response.bodyAsText()
                Log.e(TAG, "[$endpoint] HTTP $statusCode - Error response: $rawBody")
                
                // Try to parse error message from response
                val errorMessage = if (expectApiWrapper) {
                    try {
                        val errorResponse = response.body<ApiResponse<T>>()
                        errorResponse.error ?: errorResponse.message ?: rawBody
                    } catch (e: Exception) {
                        rawBody.take(200)  // Show first 200 chars of raw response
                    }
                } else {
                    rawBody.take(200)
                }
                
                return ApiResult.ApiError(
                    message = context.getString(R.string.error_server, statusCode, errorMessage),
                    code = statusCode
                )
            }
            
            if (expectApiWrapper) {
                // Success - deserialize wrapped response
                val apiResponse = response.body<ApiResponse<T>>()
                
                if (apiResponse.success && apiResponse.data != null) {
                    ApiResult.Success(apiResponse.data!!)
                } else {
                    // ✅ Backend uses 'error' field for error messages
                    val errorMsg = apiResponse.error ?: apiResponse.message ?: context.getString(R.string.error_unknown)
                    Log.e(TAG, "[$endpoint] API error: $errorMsg")
                    ApiResult.ApiError(
                        message = errorMsg,
                        code = statusCode
                    )
                }
            } else {
                // Some endpoints return bare JSON without the ApiResponse wrapper
                val body = response.body<T>()
                ApiResult.Success(body)
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
     * @param apps List of package names to scan (validates non-empty, max 100 apps)
     * @param userId User identifier (validates non-blank)
     * @param deviceId Device identifier (validates non-blank)
     */
    suspend fun startScan(
        apps: List<String>,
        userId: String,
        deviceId: String,
        includeSystemApps: Boolean = false,
        level: ScanLevel = ScanLevel.SMART
    ): ApiResult<StartScanResponse> {
        // ✅ INPUT VALIDATION
        if (apps.isEmpty()) {
            return ApiResult.ApiError(context.getString(R.string.error_no_apps_selected), -1)
        }
        if (apps.size > 100) {
            return ApiResult.ApiError(context.getString(R.string.error_too_many_apps), -1)
        }
        if (userId.isBlank()) {
            return ApiResult.ApiError(context.getString(R.string.error_user_id_required), -1)
        }
        if (deviceId.isBlank()) {
            return ApiResult.ApiError(context.getString(R.string.error_device_id_required), -1)
        }
        
        // Validate package names (basic format check)
        val invalidPackages = apps.filter { !it.matches(Regex("^[a-zA-Z0-9._]+$")) }
        if (invalidPackages.isNotEmpty()) {
            return ApiResult.ApiError(context.getString(R.string.error_invalid_package, invalidPackages.first()), -1)
        }

        val appDtos = apps.map { packageName ->
            StartScanAppDto(
                packageName = packageName,
                displayName = "",
                category = null
            )
        }

        // Generate ISO 8601 timestamp (compatible with API 24+)
        val timestamp = java.time.Instant.now().toString()

        val request = StartScanRequest(
            deviceId = deviceId,
            platform = "android",
            includeSystemApps = false,
            apps = appDtos,
            userId = userId,
            timestamp = timestamp,
            level = level,
            analysisType = AnalysisType.INSTALLED_APP
        )

        // ✅ SAFE LOGGING: Avoid logging sensitive user data in production
        Log.d(TAG, "[/api/scan/start] === SCAN REQUEST ===")
        Log.d(TAG, "  platform: android")
        Log.d(TAG, "  includeSystemApps: false")
        Log.d(TAG, "  timestamp: $timestamp")
        Log.d(TAG, "  apps count: ${appDtos.size}")
        appDtos.take(3).forEachIndexed { index, app ->
            Log.d(TAG, "  app[$index]: ${app.packageName}")
        }
        if (appDtos.size > 3) {
            Log.d(TAG, "  ... and ${appDtos.size - 3} more apps")
        }
        Log.d(TAG, "===============================")

        return safeApiCall("/scan/start", expectApiWrapper = false) {
            client.post("/scan/start") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    /**
     * Get the status of an ongoing scan
     * @param scanId Scan identifier (validates format)
     */
    suspend fun getScanStatus(scanId: String): ApiResult<ScanStatusResponse> {
        // ✅ INPUT VALIDATION
        if (scanId.isBlank()) {
            return ApiResult.ApiError(context.getString(R.string.error_scan_id_required), -1)
        }
        if (scanId.length > 100) {
            return ApiResult.ApiError(context.getString(R.string.error_scan_id_invalid), -1)
        }
        
        return safeApiCall("/scan/$scanId/progress", expectApiWrapper = false) {
            client.get("/scan/$scanId/progress")
        }
    }

    /**
     * Get scan result by scanId
     * Use this to get full scan details after completion
     */
    suspend fun getScanResult(scanId: String): ApiResult<ScanResultResponse> {
        return safeApiCall("/scan/$scanId", expectApiWrapper = false) {
            client.get("/scan/$scanId")
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
    suspend fun getAppDetails(packageName: String, userId: String): ApiResult<AppDetailsResponse> {
        return safeApiCall("/scan/app/$packageName", expectApiWrapper = false) {
            client.get("/scan/app/$packageName") {
                headers.append("x-user-id", userId)
            }
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
        Log.d(TAG, "[/scan/history/list] Fetching history for user=$userId (limit=$limit, offset=$offset)")

        return safeApiCall("/scan/history/list", expectApiWrapper = false) {
            client.get("/scan/history/list?userId=$userId&limit=$limit&skip=$offset") {
                headers.append("x-user-id", userId)
            }
        }
    }

    /**
     * Get full app details from scan endpoint (includes store data and analysis)
     */
    suspend fun getFullAppDetails(packageName: String, userId: String): ApiResult<AppDetailsResponse> {
        return safeApiCall("/scan/app/$packageName", expectApiWrapper = false) {
            client.get("/scan/app/$packageName") {
                headers.append("x-user-id", userId)
            }
        }
    }

    /**
     * Upload and analyze an APK (MobSF-backed)
     */
    suspend fun scanApk(uri: Uri, userId: String, deviceId: String?): ApiResult<AppDetailsResponse> {
        val contentResolver = context.contentResolver
        val fileName = queryFileName(uri) ?: "apk-${System.currentTimeMillis()}.apk"

        Log.d(TAG, "[scanApk] Preparing upload: $fileName")

        val inputStream: InputStream = contentResolver.openInputStream(uri)
            ?: return ApiResult.ApiError(context.getString(R.string.error_cannot_read_apk), -1)

        val bytes = inputStream.use { it.readBytes() }
        Log.d(TAG, "[scanApk] Read ${bytes.size} bytes from APK")

        return safeApiCall("/api/scan/apk") {
            client.submitFormWithBinaryData(
                url = "/api/scan/apk",
                formData = formData {
                    // Add userId and deviceId as text fields
                    append("userId", userId)
                    deviceId?.let { append("deviceId", it) }
                    
                    // Add file as binary part with proper headers
                    append(
                        key = "file",
                        value = bytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            append(HttpHeaders.ContentType, "application/vnd.android.package-archive")
                        }
                    )
                }
            )
        }
    }

    private fun queryFileName(uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return null
        return cursor.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && it.moveToFirst()) it.getString(nameIndex) else null
        }
    }
}
