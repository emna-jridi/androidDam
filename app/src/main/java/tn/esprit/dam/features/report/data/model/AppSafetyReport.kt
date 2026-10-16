package tn.esprit.dam.features.report.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSafetyReport(
    val packageName: String,
    val appName: String,
    val riskLevel: String,
    val dataPrivacy: String,
    val recommendations: List<String>
)
