package tn.esprit.dam.features.scan.presentation.detail

import tn.esprit.dam.features.scan.presentation.model.TrackerUi
import tn.esprit.dam.features.scan.data.remote.dto.AnalysisResultDto
import tn.esprit.dam.features.scan.data.remote.dto.AppDetailsResponse
import tn.esprit.dam.features.scan.data.remote.dto.AppInfoDto
import tn.esprit.dam.features.scan.data.remote.dto.AppScanHistoryDto
import tn.esprit.dam.features.scan.data.remote.dto.SimpleTrackerInfo
import tn.esprit.dam.features.scan.data.remote.dto.StoreDataDto

// What the app detail screen shows — keeps the composables independent of the API models.

data class AppDetailUi(
    val packageName: String,
    val displayName: String?,
    val category: String?,
    val permissions: List<String>,
    val trackers: List<TrackerUi>,
    val storeData: StoreDataUi?,
    val scanResults: AnalysisResultUi?,
    val finalScore: Float,
    val lastScanned: String?
)

data class StoreDataUi(
    val version: String?,
    val downloads: String?,
    val rating: Float?,
    val developer: String?,
    val icon: String?,
    val lastUpdate: String?
)

data class AnalysisResultUi(
    val aiRiskScore: Float,
    val aiRiskLevel: String,
    val aiSummary: String,
    val aiRecommendations: List<String>,
    val permissionsScore: Float?,
    val trackersScore: Float?,
    val aiStatus: String?
)

data class AppScanHistoryUi(
    val scanDate: String,
    val score: Float,
    val riskLevel: String
)

/**
 * Uses the backend's `app` block when present; otherwise rebuilds one from the top-level
 * scan fields (older scans and SMART scans don't send `app`).
 */
fun AppDetailsResponse.toAppDetailUi(requestedPackage: String): AppDetailUi =
    app?.toUi() ?: AppDetailUi(
        packageName = packageName ?: requestedPackage,
        displayName = appName ?: requestedPackage,
        category = null,
        permissions = permissions,
        trackers = trackers?.trackers?.map { TrackerUi(name = it.name, riskLevel = it.category) }.orEmpty(),
        storeData = null,
        scanResults = AnalysisResultUi(
            aiRiskScore = 100f - (securityScore ?: 0f),
            aiRiskLevel = globalRisk ?: "LOW",
            aiSummary = "Security Score: ${securityScore?.toInt() ?: 0}/100, Privacy Score: ${privacyScore?.toInt() ?: 0}/100",
            aiRecommendations = recommendations,
            permissionsScore = privacyScore,
            trackersScore = trackers?.privacyScore?.toFloat(),
            aiStatus = "fallback"
        ),
        finalScore = overallScore ?: securityScore ?: 0f,
        lastScanned = null
    )

private fun AppInfoDto.toUi() = AppDetailUi(
    packageName = packageName,
    displayName = displayName,
    category = category,
    permissions = permissions,
    trackers = trackers.map { it.toUi() },
    storeData = storeData?.toUi(),
    scanResults = scanResults?.toUi(),
    finalScore = finalScore,
    lastScanned = lastScanned
)

private fun SimpleTrackerInfo.toUi() = TrackerUi(name = name, riskLevel = riskLevel)

private fun StoreDataDto.toUi() = StoreDataUi(
    version = version,
    downloads = downloads,
    rating = rating,
    developer = developer,
    icon = icon,
    lastUpdate = lastUpdate
)

private fun AnalysisResultDto.toUi() = AnalysisResultUi(
    aiRiskScore = aiRiskScore,
    aiRiskLevel = aiRiskLevel,
    aiSummary = aiSummary,
    aiRecommendations = aiRecommendations,
    permissionsScore = permissionsScore,
    trackersScore = trackersScore,
    aiStatus = aiStatus
)

fun AppScanHistoryDto.toUi() = AppScanHistoryUi(scanDate = scanDate, score = score, riskLevel = riskLevel)
