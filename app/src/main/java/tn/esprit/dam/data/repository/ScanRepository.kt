package tn.esprit.dam.data.repository

import javax.inject.Inject
import kotlinx.coroutines.delay
import tn.esprit.dam.data.api.ScanApiService
import tn.esprit.dam.data.api.models.ApiResult
import tn.esprit.dam.data.api.models.AppDetailsResponse
import tn.esprit.dam.data.api.models.LatestScanResponse
import tn.esprit.dam.data.api.models.ScanStatusResponse
import tn.esprit.dam.data.api.models.StartScanResponse
import tn.esprit.dam.data.api.models.SearchAppResponse
import tn.esprit.dam.data.api.models.ScanHistoryResponse

class ScanRepository @Inject constructor(
    private val apiService: ScanApiService
) {

    /**
     * Retry helper with exponential backoff for transient network failures
     * @param maxAttempts Maximum retry attempts (default 3)
     * @param initialDelayMs Initial delay before first retry (default 1000ms)
     * @param block Suspending API call to retry
     */
    private suspend fun <T> retryWithBackoff(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 1000,
        block: suspend () -> ApiResult<T>
    ): ApiResult<T> {
        var currentDelay = initialDelayMs
        repeat(maxAttempts - 1) { attempt ->
            val result = block()
            
            // Only retry on network errors (not API errors or success)
            when (result) {
                is ApiResult.NetworkError -> {
                    delay(currentDelay)
                    currentDelay *= 2  // Exponential backoff
                }
                else -> return result  // Success or API error - don't retry
            }
        }
        // Final attempt
        return block()
    }

    /**
     * Start a security scan for the provided apps (with retry)
     */
    suspend fun startScan(
        apps: List<String>,
        userId: String,
        deviceId: String,
        includeSystemApps: Boolean = false
    ): ApiResult<StartScanResponse> {
        return retryWithBackoff {
            apiService.startScan(apps, userId, deviceId, includeSystemApps)
        }
    }

    /**
     * Poll the status of an ongoing scan
     */
    suspend fun getScanStatus(scanId: String): ApiResult<ScanStatusResponse> {
        return apiService.getScanStatus(scanId)
    }

    /**
     * Get latest scan results for a user - ONLY method for Home screen (with retry)
     */
    suspend fun getLatestScan(userId: String): ApiResult<LatestScanResponse> {
        return retryWithBackoff {
            apiService.getLatestScan(userId)
        }
    }

    /**
     * Get detailed analysis for a specific app (includes store data, with retry)
     * Unified method replaces getAppDetails + getFullAppDetails
     */
    suspend fun getAppDetails(packageName: String): ApiResult<AppDetailsResponse> {
        return retryWithBackoff {
            apiService.getFullAppDetails(packageName)
        }
    }

    /**
     * Search for apps in Play Store
     */
    suspend fun searchApps(
        query: String,
        platform: String = "android",
        limit: Int = 10
    ): ApiResult<SearchAppResponse> {
        return apiService.searchApps(query, platform, limit)
    }

    /**
     * Get scan history for a user
     */
    suspend fun getScanHistory(
        userId: String,
        limit: Int = 10,
        offset: Int = 0
    ): ApiResult<ScanHistoryResponse> {
        return apiService.getScanHistory(userId, limit, offset)
    }

    /**
     * Upload and analyze an APK using MobSF backend (with retry)
     */
    suspend fun scanApk(
        uri: android.net.Uri,
        userId: String,
        deviceId: String?
    ): ApiResult<AppDetailsResponse> {
        return retryWithBackoff(maxAttempts = 2) {  // APK uploads are heavy, only 1 retry
            apiService.scanApk(uri, userId, deviceId)
        }
    }
}
