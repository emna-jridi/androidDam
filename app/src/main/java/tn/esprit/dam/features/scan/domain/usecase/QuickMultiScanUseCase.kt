package tn.esprit.dam.features.scan.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tn.esprit.dam.R
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.core.ui.UiText
import tn.esprit.dam.features.scan.data.repository.ScanRepository
import tn.esprit.dam.features.scan.presentation.model.LocalAppInfo
import javax.inject.Inject

sealed interface MultiScanEvent {
    data class Progress(
        val scannedApps: Int,
        val apps: List<LocalAppInfo>,
        val counts: RiskCounts,
        val averageScore: Float
    ) : MultiScanEvent

    data class Done(
        val apps: List<LocalAppInfo>,
        val counts: RiskCounts,
        val averageScore: Float,
        val note: UiText,
        val explanation: String,
        val recommendations: List<String>,
        val riskFactors: List<String>,
        val safetyTips: List<String>,
        val analysisSource: String
    ) : MultiScanEvent
}

/**
 * Scans installed apps one by one: each app's APK is uploaded and analyzed by the server
 * (LightGBM + trackers + SAAT). Falls back to on-device risk scoring when the APK can't be
 * read or the server can't be reached.
 */
class QuickMultiScanUseCase @Inject constructor(
    private val repository: ScanRepository
) {
    operator fun invoke(apps: List<LocalAppInfo>, deviceId: String?): Flow<MultiScanEvent> = flow {
        val scanned = mutableListOf<LocalAppInfo>()
        var localFallbacks = 0

        apps.forEachIndexed { index, app ->
            val mapped = when (val result = repository.scanInstalledApp(app.packageName, deviceId)) {
                is ApiResult.Success -> result.data.toLocalAppInfo(app.packageName, app.displayName)
                else -> {
                    localFallbacks++
                    app.withLocalRisk()
                }
            }
            scanned += mapped
            emit(MultiScanEvent.Progress(index + 1, scanned.toList(), RiskCounts.of(scanned), scanned.averageRiskScore()))
        }

        val counts = RiskCounts.of(scanned)
        emit(
            MultiScanEvent.Done(
                apps = scanned.toList(),
                counts = counts,
                averageScore = scanned.averageRiskScore(),
                note = if (localFallbacks > 0) {
                    UiText.Res(R.string.scan_note_done_local, localFallbacks)
                } else {
                    UiText.Res(R.string.scan_note_done, scanned.size)
                },
                explanation = when {
                    counts.high > 0 -> "⚠️ ${counts.high} application(s) present high risk. Check the recommendations below."
                    counts.medium > 0 -> "ℹ️ ${counts.medium} application(s) present moderate risk. Check the granted permissions."
                    else -> "✅ All analyzed applications present a low risk level."
                },
                recommendations = scanned.flatMap { it.mlAnalysis?.recommendations.orEmpty() }.distinct().take(5),
                riskFactors = scanned.flatMap { it.mlAnalysis?.riskFactors.orEmpty() }.distinct().take(5),
                safetyTips = scanned.flatMap { it.mlAnalysis?.safetyTips.orEmpty() }.distinct().take(4),
                analysisSource = scanned.firstOrNull()?.mlAnalysis?.analysisSource ?: "hybrid"
            )
        )
    }
}
