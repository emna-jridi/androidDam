package tn.esprit.dam.features.scan.data.repository

import javax.inject.Inject
import kotlinx.coroutines.delay
import tn.esprit.dam.features.scan.data.remote.ScanApiService
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.features.scan.data.remote.dto.AppDetailsResponse
import tn.esprit.dam.features.scan.data.remote.dto.LatestScanResponse
import tn.esprit.dam.features.scan.data.remote.dto.ScanStatusResponse
import tn.esprit.dam.features.scan.data.remote.dto.ScanResultResponse
import tn.esprit.dam.features.scan.data.remote.dto.StartScanResponse
import tn.esprit.dam.features.scan.data.remote.dto.ScanLevel
import tn.esprit.dam.features.scan.data.remote.dto.SearchAppResponse
import tn.esprit.dam.features.scan.data.remote.dto.ScanHistoryResponse

class ScanRepository @Inject constructor(
    private val apiService: ScanApiService
) {
    private suspend fun <T> retryWithBackoff(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 1000,
        block: suspend () -> ApiResult<T>
    ): ApiResult<T> {
        var currentDelay = initialDelayMs
        repeat(maxAttempts - 1) { attempt ->
            val result = block()

            when (result) {
                is ApiResult.NetworkError -> {
                    delay(currentDelay)
                    currentDelay *= 2
                }
                else -> return result
            }
        }
        return block()
    }

    suspend fun startScan(
        apps: List<String>,
        userId: String,
        deviceId: String,
        includeSystemApps: Boolean = false,
        level: ScanLevel = ScanLevel.SMART
    ): ApiResult<StartScanResponse> {
        return retryWithBackoff {
            apiService.startScan(apps, userId, deviceId, includeSystemApps, level)
        }
    }

    suspend fun getScanStatus(scanId: String): ApiResult<ScanStatusResponse> {
        return apiService.getScanStatus(scanId)
    }

    suspend fun getScanResult(scanId: String): ApiResult<ScanResultResponse> {
        return apiService.getScanResult(scanId)
    }

    suspend fun getLatestScan(userId: String): ApiResult<LatestScanResponse> {
        return retryWithBackoff {
            apiService.getLatestScan(userId)
        }
    }

    suspend fun getAppDetails(packageName: String, userId: String): ApiResult<AppDetailsResponse> {
        return retryWithBackoff {
            apiService.getFullAppDetails(packageName, userId)
        }
    }

    suspend fun searchApps(
        query: String,
        platform: String = "android",
        limit: Int = 10
    ): ApiResult<SearchAppResponse> {
        return apiService.searchApps(query, platform, limit)
    }

    suspend fun getScanHistory(
        userId: String,
        limit: Int = 10,
        offset: Int = 0
    ): ApiResult<ScanHistoryResponse> {
        return apiService.getScanHistory(userId, limit, offset)
    }

    suspend fun scanApk(
        uri: android.net.Uri,
        userId: String,
        deviceId: String?
    ): ApiResult<AppDetailsResponse> {
        return retryWithBackoff(maxAttempts = 2) {
            apiService.scanApk(uri, userId, deviceId)
        }
    }
}
