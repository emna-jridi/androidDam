package tn.esprit.dam.features.scan.presentation.history

import tn.esprit.dam.features.scan.data.remote.dto.ScanHistoryItemDto

/** One row of the scan history screen — keeps the screen independent of the API model. */
data class ScanHistoryItemUi(
    val scanId: String,
    val status: String,
    val totalApps: Int,
    val scannedApps: Int,
    val averageScore: Float?,
    val highRiskApps: Int?,
    val mediumRiskApps: Int?,
    val lowRiskApps: Int?,
    val createdAt: String,
    val duration: Long?,
    val packageName: String?,
    val appName: String?,
    val globalRisk: String?,
    val overallScore: Float?
)

fun ScanHistoryItemDto.toUi() = ScanHistoryItemUi(
    scanId = scanId,
    status = status,
    totalApps = totalApps,
    scannedApps = scannedApps,
    averageScore = averageScore,
    highRiskApps = highRiskApps,
    mediumRiskApps = mediumRiskApps,
    lowRiskApps = lowRiskApps,
    createdAt = createdAt,
    duration = duration,
    packageName = packageName,
    appName = appName,
    globalRisk = globalRisk,
    overallScore = overallScore
)
