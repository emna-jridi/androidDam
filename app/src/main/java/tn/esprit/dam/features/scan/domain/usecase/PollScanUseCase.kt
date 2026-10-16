package tn.esprit.dam.features.scan.domain.usecase

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.features.scan.domain.model.ScanLevel
import tn.esprit.dam.features.scan.data.remote.dto.ScanResultResponse
import tn.esprit.dam.features.scan.data.remote.dto.ScanStatusResponse
import tn.esprit.dam.features.scan.data.repository.ScanRepository
import tn.esprit.dam.features.scan.domain.ConfidenceLevel
import tn.esprit.dam.features.scan.domain.RiskLevel
import tn.esprit.dam.features.scan.domain.ScanRiskResult
import tn.esprit.dam.features.scan.presentation.model.LocalAppInfo
import javax.inject.Inject

sealed interface ScanPollEvent {
    data class Progress(val status: ScanStatusResponse) : ScanPollEvent

    data class Completed(
        val app: LocalAppInfo,
        val confidenceScore: Int?,
        val recommendDeepAnalysis: Boolean
    ) : ScanPollEvent

    /** The server says the scan finished but the result could not be fetched. */
    data object CompletedWithoutDetails : ScanPollEvent
    data object Failed : ScanPollEvent
    data object TimedOut : ScanPollEvent
}

/** Polls a server-side scan until it completes, fails or times out. */
class PollScanUseCase @Inject constructor(
    private val repository: ScanRepository
) {
    operator fun invoke(scanId: String, level: ScanLevel): Flow<ScanPollEvent> = flow {
        val maxAttempts = if (level == ScanLevel.DEEP) DEEP_MAX_POLLS else SMART_MAX_POLLS

        repeat(maxAttempts) {
            delay(POLL_INTERVAL_MS)
            // Transient errors are ignored: the next poll simply tries again.
            val status = (repository.getScanStatus(scanId) as? ApiResult.Success)?.data ?: return@repeat

            when {
                status.isFailed() -> {
                    emit(ScanPollEvent.Failed)
                    return@flow
                }
                status.isCompleted() -> {
                    val result = repository.getScanResult(scanId)
                    delay(RESULT_SETTLE_DELAY_MS)
                    emit(
                        if (result is ApiResult.Success) result.data.toCompletedEvent()
                        else ScanPollEvent.CompletedWithoutDetails
                    )
                    return@flow
                }
                else -> emit(ScanPollEvent.Progress(status))
            }
        }
        emit(ScanPollEvent.TimedOut)
    }

    private fun ScanStatusResponse.isFailed() = status.equals("failed", ignoreCase = true)

    private fun ScanStatusResponse.isCompleted() =
        status.equals("completed", ignoreCase = true) ||
            (steps.isNotEmpty() && steps.all { it.status.equals("COMPLETED", ignoreCase = true) }) ||
            (percentage ?: progress ?: 0) >= 100

    private fun ScanResultResponse.toCompletedEvent(): ScanPollEvent.Completed {
        val pkgName = packageName ?: "unknown"
        val score = overallScore?.toInt() ?: securityScore?.toInt() ?: 0
        val riskLevel = when (globalRisk?.lowercase()) {
            "low", "safe" -> RiskLevel.LOW
            "medium" -> RiskLevel.MEDIUM
            "high" -> RiskLevel.HIGH
            "critical" -> RiskLevel.CRITICAL
            else -> when {
                score >= 85 -> RiskLevel.LOW
                score >= 70 -> RiskLevel.MEDIUM
                score >= 40 -> RiskLevel.HIGH
                else -> RiskLevel.CRITICAL
            }
        }
        val confidence = confidenceScore ?: 0.0

        val app = LocalAppInfo(
            packageName = pkgName,
            displayName = appName ?: pkgName,
            category = null,
            isSystemApp = false,
            permissions = permissions,
            trackers = emptyList(),
            isSelected = false,
            riskResult = ScanRiskResult(
                score = score,
                riskLevel = riskLevel,
                permissionScore = 100,
                trackerScore = 100 - ((trackers?.count ?: 0) * 10),
                codeScore = ((1f - (ml?.malwareProbability ?: 0f)) * 100).toInt(),
                criticalIssues = errors,
                warnings = warnings,
                confidence = when {
                    confidence > 80 -> ConfidenceLevel.HIGH
                    confidence > 50 -> ConfidenceLevel.MEDIUM
                    else -> ConfidenceLevel.LOW
                }
            )
        )
        return ScanPollEvent.Completed(app, confidenceScore?.toInt(), recommendDeepAnalysis ?: false)
    }

    private companion object {
        const val POLL_INTERVAL_MS = 3_000L
        const val RESULT_SETTLE_DELAY_MS = 300L
        const val SMART_MAX_POLLS = 40
        const val DEEP_MAX_POLLS = 100
    }
}
