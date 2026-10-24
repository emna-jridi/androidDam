package tn.esprit.dam.features.scan.data.repository

import javax.inject.Inject
import tn.esprit.dam.features.scan.data.remote.ScanApi
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.features.scan.data.remote.dto.AppDetailsResponse
import tn.esprit.dam.features.scan.data.remote.dto.LatestScanResponse
import tn.esprit.dam.features.scan.data.remote.dto.ScanStatusResponse
import tn.esprit.dam.features.scan.data.remote.dto.ScanResultResponse
import tn.esprit.dam.features.scan.data.remote.dto.SearchAppResponse
import tn.esprit.dam.features.scan.data.remote.dto.ScanHistoryResponse

class ScanRepositoryImpl @Inject constructor(
    private val apiService: ScanApi
) : ScanRepository {
    override suspend fun getScanStatus(scanId: String): ApiResult<ScanStatusResponse> {
        return apiService.getScanStatus(scanId)
    }

    override suspend fun getScanResult(scanId: String): ApiResult<ScanResultResponse> {
        return apiService.getScanResult(scanId)
    }

    override suspend fun getLatestScan(): ApiResult<LatestScanResponse> {
        return apiService.getLatestScan()
    }

    override suspend fun getAppDetails(packageName: String): ApiResult<AppDetailsResponse> {
        return apiService.getAppDetails(packageName)
    }

    override suspend fun searchApps(
        query: String,
        platform: String,
        limit: Int
    ): ApiResult<SearchAppResponse> {
        return apiService.searchApps(query, platform, limit)
    }

    override suspend fun getScanHistory(
        limit: Int,
        offset: Int
    ): ApiResult<ScanHistoryResponse> {
        return apiService.getScanHistory(limit, offset)
    }

    override suspend fun scanApk(
        uri: android.net.Uri,
        deviceId: String?
    ): ApiResult<AppDetailsResponse> {
        return apiService.scanApk(uri, deviceId)
    }

    override suspend fun scanInstalledApp(
        packageName: String,
        deviceId: String?
    ): ApiResult<AppDetailsResponse> {
        return apiService.scanInstalledApp(packageName, deviceId)
    }
}
