package tn.esprit.dam.features.scan.data.repository

import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.features.scan.data.remote.dto.AppDetailsResponse
import tn.esprit.dam.features.scan.data.remote.dto.LatestScanResponse
import tn.esprit.dam.features.scan.data.remote.dto.ScanStatusResponse
import tn.esprit.dam.features.scan.data.remote.dto.ScanResultResponse
import tn.esprit.dam.features.scan.data.remote.dto.StartScanResponse
import tn.esprit.dam.features.scan.data.remote.dto.SearchAppResponse
import tn.esprit.dam.features.scan.data.remote.dto.ScanHistoryResponse

interface ScanRepository {
    suspend fun startScan(
        apps: List<String>,
        deviceId: String,
        includeSystemApps: Boolean = false
    ): ApiResult<StartScanResponse>

    suspend fun getScanStatus(scanId: String): ApiResult<ScanStatusResponse>

    suspend fun getScanResult(scanId: String): ApiResult<ScanResultResponse>

    suspend fun getLatestScan(): ApiResult<LatestScanResponse>

    suspend fun getAppDetails(packageName: String): ApiResult<AppDetailsResponse>

    suspend fun searchApps(
        query: String,
        platform: String = "android",
        limit: Int = 10
    ): ApiResult<SearchAppResponse>

    suspend fun getScanHistory(
        limit: Int = 10,
        offset: Int = 0
    ): ApiResult<ScanHistoryResponse>

    suspend fun scanApk(
        uri: android.net.Uri,
        deviceId: String?
    ): ApiResult<AppDetailsResponse>
}
