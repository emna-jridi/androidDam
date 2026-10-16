package tn.esprit.dam.features.scan.data.remote

import android.content.Context
import android.net.Uri
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.forms.formData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import tn.esprit.dam.R
import tn.esprit.dam.features.scan.data.remote.dto.ApiResponse
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.core.network.flatMap
import tn.esprit.dam.core.network.safeApiCall
import tn.esprit.dam.features.scan.data.remote.dto.AppDetailsResponse
import tn.esprit.dam.features.scan.data.remote.dto.LatestScanResponse
import tn.esprit.dam.features.scan.data.remote.dto.ScanStatusResponse
import tn.esprit.dam.features.scan.data.remote.dto.StartScanAppDto
import tn.esprit.dam.features.scan.data.remote.dto.StartScanRequest
import tn.esprit.dam.features.scan.data.remote.dto.StartScanResponse
import tn.esprit.dam.features.scan.data.remote.dto.AnalysisType
import tn.esprit.dam.features.scan.data.remote.dto.ScanLevel
import tn.esprit.dam.features.scan.data.remote.dto.SearchAppResponse
import tn.esprit.dam.features.scan.data.remote.dto.ScanHistoryResponse
import tn.esprit.dam.features.scan.data.remote.dto.ScanResultResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.io.InputStream
import kotlinx.coroutines.delay

@Singleton
class ScanApiService @Inject constructor(
    private val client: HttpClient,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val APK_SCAN_POLL_INTERVAL_MS = 3_000L
        private const val APK_SCAN_MAX_POLLS = 100
    }

    suspend fun startScan(
        apps: List<String>,
        userId: String,
        deviceId: String,
        includeSystemApps: Boolean = false,
        level: ScanLevel = ScanLevel.SMART
    ): ApiResult<StartScanResponse> {
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

        return safeApiCall {
            client.post("/api/scan/start") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
    }

    suspend fun getScanStatus(scanId: String): ApiResult<ScanStatusResponse> {
        if (scanId.isBlank()) {
            return ApiResult.ApiError(context.getString(R.string.error_scan_id_required), -1)
        }
        if (scanId.length > 100) {
            return ApiResult.ApiError(context.getString(R.string.error_scan_id_invalid), -1)
        }

        return safeApiCall {
            client.get("/api/scan/$scanId/progress")
        }
    }

    suspend fun getScanResult(scanId: String): ApiResult<ScanResultResponse> {
        return safeApiCall {
            client.get("/api/scan/$scanId")
        }
    }

    suspend fun getLatestScan(userId: String): ApiResult<LatestScanResponse> {
        return safeApiCall<ApiResponse<LatestScanResponse>> {
            client.get("/api/scan/latest/$userId")
        }.flatMap { response ->
            val data = response.data
            if (response.success && data != null) {
                ApiResult.Success(data)
            } else {
                ApiResult.ApiError(
                    response.error ?: response.message ?: context.getString(R.string.error_unknown),
                    200
                )
            }
        }
    }

    suspend fun getAppDetails(packageName: String, userId: String): ApiResult<AppDetailsResponse> {
        return safeApiCall {
            client.get("/api/scan/app/$packageName") {
                headers.append("x-user-id", userId)
            }
        }
    }

    suspend fun searchApps(query: String, platform: String = "android", limit: Int = 10): ApiResult<SearchAppResponse> {
        return safeApiCall {
            client.get("/api/scan/apps/search") {
                parameter("q", query)
                parameter("limit", limit)
            }
        }
    }

    suspend fun getScanHistory(userId: String, limit: Int = 10, offset: Int = 0): ApiResult<ScanHistoryResponse> {
        return safeApiCall {
            client.get("/api/scan/history/list?userId=$userId&limit=$limit&skip=$offset") {
                headers.append("x-user-id", userId)
            }
        }
    }

    suspend fun getFullAppDetails(packageName: String, userId: String): ApiResult<AppDetailsResponse> {
        return safeApiCall {
            client.get("/api/scan/app/$packageName") {
                headers.append("x-user-id", userId)
            }
        }
    }

    suspend fun scanApk(uri: Uri, userId: String, deviceId: String?): ApiResult<AppDetailsResponse> {
        val contentResolver = context.contentResolver
        val fileName = queryFileName(uri) ?: "apk-${System.currentTimeMillis()}.apk"

        val inputStream: InputStream = contentResolver.openInputStream(uri)
            ?: return ApiResult.ApiError(context.getString(R.string.error_cannot_read_apk), -1)

        val bytes = inputStream.use { it.readBytes() }

        val started = safeApiCall<StartScanResponse> {
            client.submitFormWithBinaryData(
                url = "/api/scan/start",
                formData = formData {
                    append("userId", userId)
                    deviceId?.let { append("deviceId", it) }
                    append("analysisType", "apk_upload")

                    append(
                        key = "apkFile",
                        value = bytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            append(HttpHeaders.ContentType, "application/vnd.android.package-archive")
                        }
                    )
                }
            )
        }

        val scanId = when (started) {
            is ApiResult.Success -> started.data.scanId
            is ApiResult.ApiError -> return started
            is ApiResult.NetworkError -> return started
            is ApiResult.SerializationError -> return started
        }

        repeat(APK_SCAN_MAX_POLLS) {
            delay(APK_SCAN_POLL_INTERVAL_MS)
            val result = safeApiCall<AppDetailsResponse> {
                client.get("/api/scan/$scanId")
            }
            if (result !is ApiResult.Success) return result
            when (result.data.status?.lowercase()) {
                "completed", "partial" -> return result
                "failed" -> return ApiResult.ApiError(context.getString(R.string.error_scan_failed), 500)
            }
        }
        return ApiResult.ApiError(context.getString(R.string.error_scan_timeout), 408)
    }

    private fun queryFileName(uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return null
        return cursor.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && it.moveToFirst()) it.getString(nameIndex) else null
        }
    }
}
