package tn.esprit.dam.features.scan.data.remote.dto

import tn.esprit.dam.features.scan.domain.model.AnalysisType
import tn.esprit.dam.features.scan.domain.model.ScanLevel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScanStatusResponse(
    @SerialName("scanId")
    val scanId: String,
    @SerialName("status")
    val status: String? = null,
    @SerialName("analysisType")
    val analysisType: AnalysisType? = null,
    @SerialName("level")
    val level: ScanLevel? = null,
    @SerialName("progress")
    val progress: Int? = null,
    @SerialName("percentage")
    val percentage: Int? = null,
    @SerialName("currentStep")
    val currentStep: String? = null,
    @SerialName("steps")
    val steps: List<ScanStepProgress> = emptyList(),
    @SerialName("estimatedTimeRemaining")
    val estimatedTimeRemaining: Int? = null,
    @SerialName("elapsed")
    val elapsed: Int? = null,
    @SerialName("totalApps")
    val totalApps: Int? = null,
    @SerialName("scannedApps")
    val scannedApps: Int? = null,
    @SerialName("results")
    val results: ScanResultsDto? = null,
    @SerialName("confidenceScore")
    val confidenceScore: Double? = null,
    @SerialName("recommendDeepAnalysis")
    val recommendDeepAnalysis: Boolean? = null
)

@Serializable
data class ScanStepProgress(
    @SerialName("name")
    val name: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("progress")
    val progress: Int? = null,
    @SerialName("startTime")
    val startTime: String? = null,
    @SerialName("duration")
    val duration: Int? = null,
    @SerialName("endTime")
    val endTime: String? = null
)

@Serializable
data class ScanResultsDto(
    @SerialName("totalScanned")
    val totalScanned: Int,
    @SerialName("highRiskApps")
    val highRiskApps: Int,
    @SerialName("mediumRiskApps")
    val mediumRiskApps: Int,
    @SerialName("lowRiskApps")
    val lowRiskApps: Int,
    @SerialName("averageScore")
    val averageScore: Float,
    @SerialName("confidenceScore")
    val confidenceScore: Double? = null,
    @SerialName("recommendDeepAnalysis")
    val recommendDeepAnalysis: Boolean? = null
)
