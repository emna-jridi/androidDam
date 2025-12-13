package tn.esprit.dam.data.repository

import javax.inject.Inject
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
     * Start a security scan for the provided apps
     */
    suspend fun startScan(
        apps: List<String>,
        userId: String,
        deviceId: String,
        includeSystemApps: Boolean = false
    ): ApiResult<StartScanResponse> {
        return apiService.startScan(apps, userId, deviceId, includeSystemApps)
    }

    /**
     * Poll the status of an ongoing scan
     */
    suspend fun getScanStatus(scanId: String): ApiResult<ScanStatusResponse> {
        return apiService.getScanStatus(scanId)
    }

    /**
     * Get latest scan results for a user - ONLY method for Home screen
     */
    suspend fun getLatestScan(userId: String): ApiResult<LatestScanResponse> {
        return apiService.getLatestScan(userId)
    }

    /**
     * Get detailed analysis for a specific app
     */
    suspend fun getAppDetails(packageName: String): ApiResult<AppDetailsResponse> {
        return apiService.getAppDetails(packageName)
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
     * Get full app details including store data and analysis
     */
    suspend fun getFullAppDetails(packageName: String): ApiResult<AppDetailsResponse> {
        return apiService.getFullAppDetails(packageName)
    }
}
