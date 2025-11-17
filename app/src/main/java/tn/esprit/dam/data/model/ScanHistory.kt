package tn.esprit.dam.data.model

import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class ScanHistoryResponse(
    val success: Boolean,
    val data: ScanHistoryData
)

@Serializable
data class ScanHistoryData(
    val scans: List<ScanHistoryItem>,
    val pagination: Pagination,
    val stats: ScanStats?
)

@Serializable
data class ScanHistoryItem(
    val _id: String,
    val type: String,
    val userHash: String,
    val totalApps: Int,
    val report: ScanReport,
    val summary: ScanSummaryHistory? = null,  // ✅ RENDRE NULLABLE
    val createdAt: String? = null,
    val updatedAt: String? = null
)
@Serializable
data class ScanSummaryHistory(
    val avgScore: Int = 0,
    val totalAlerts: Int = 0,
    val highRiskApps: Int = 0,
    val mediumRiskApps: Int = 0,
    val lowRiskApps: Int = 0,
)

