package tn.esprit.dam.features.scan.data.remote

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.streams.asInput
import tn.esprit.dam.R
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.core.network.safeApiCall
import tn.esprit.dam.features.scan.data.remote.dto.AppDetailsResponse
import tn.esprit.dam.features.scan.data.remote.dto.LatestScanResponse
import tn.esprit.dam.features.scan.data.remote.dto.ScanStatusResponse
import tn.esprit.dam.features.scan.data.remote.dto.StartScanResponse
import tn.esprit.dam.features.scan.data.remote.dto.SearchAppResponse
import tn.esprit.dam.features.scan.data.remote.dto.ScanHistoryResponse
import tn.esprit.dam.features.scan.data.remote.dto.ScanResultResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File
import kotlinx.coroutines.delay

@Singleton
class ScanApi @Inject constructor(
    private val client: HttpClient,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val APK_SCAN_POLL_INTERVAL_MS = 3_000L
        private const val APK_SCAN_MAX_POLLS = 100 // 5 min: upload + LightGBM + SAAT
        private const val ANALYSIS_APK_UPLOAD = "apk_upload"
        private const val ANALYSIS_INSTALLED_APP = "installed_app"
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

    suspend fun getLatestScan(): ApiResult<LatestScanResponse> {
        return safeApiCall {
            client.get("/api/scan/latest")
        }
    }

    suspend fun getAppDetails(packageName: String): ApiResult<AppDetailsResponse> {
        return safeApiCall {
            client.get("/api/scan/app/$packageName")
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

    suspend fun getScanHistory(limit: Int = 10, offset: Int = 0): ApiResult<ScanHistoryResponse> {
        return safeApiCall {
            client.get("/api/scan/history/list?limit=$limit&skip=$offset")
        }
    }

    /** Scans an APK the user picked (file manager, download...). */
    suspend fun scanApk(uri: Uri, deviceId: String?): ApiResult<AppDetailsResponse> {
        val contentResolver = context.contentResolver
        val fileName = queryFileName(uri) ?: "apk-${System.currentTimeMillis()}.apk"

        contentResolver.openInputStream(uri)?.close()
            ?: return ApiResult.ApiError(context.getString(R.string.error_cannot_read_apk), -1)

        val apk = InputProvider {
            (contentResolver.openInputStream(uri) ?: error("APK no longer readable")).asInput()
        }
        return uploadAndPoll(fileName, ANALYSIS_APK_UPLOAD, deviceId, apk)
    }

    /** Scans an installed app by uploading its own APK (base.apk is readable by any app). */
    suspend fun scanInstalledApp(packageName: String, deviceId: String?): ApiResult<AppDetailsResponse> {
        val file = installedApkFile(packageName)
            ?: return ApiResult.ApiError(context.getString(R.string.error_cannot_read_apk), -1)

        val apk = InputProvider(file.length()) { file.inputStream().asInput() }
        return uploadAndPoll("$packageName.apk", ANALYSIS_INSTALLED_APP, deviceId, apk)
    }

    private fun installedApkFile(packageName: String): File? = runCatching {
        val pm = context.packageManager
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getApplicationInfo(packageName, 0)
        }
        File(info.sourceDir).takeIf { it.canRead() }
    }.getOrNull()

    /** Streams the APK to /scan/start (no full copy in memory), then polls until the scan ends. */
    private suspend fun uploadAndPoll(
        fileName: String,
        analysisType: String,
        deviceId: String?,
        apk: InputProvider
    ): ApiResult<AppDetailsResponse> {
        val started = safeApiCall<StartScanResponse> {
            client.submitFormWithBinaryData(
                url = "/api/scan/start",
                formData = formData {
                    deviceId?.let { append("deviceId", it) }
                    append("analysisType", analysisType)

                    append(
                        key = "apkFile",
                        value = apk,
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
