package tn.esprit.dam.features.scan.domain.usecase

import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.features.scan.data.remote.dto.AppDetailsResponse
import tn.esprit.dam.features.scan.data.remote.dto.AppInfoDto
import tn.esprit.dam.features.scan.data.remote.dto.SimpleTrackerInfo
import tn.esprit.dam.features.scan.domain.RiskLevel
import tn.esprit.dam.features.scan.domain.SecurityUtils
import tn.esprit.dam.features.scan.presentation.model.LocalAppInfo
import tn.esprit.dam.features.scan.presentation.model.toUiModel

data class RiskCounts(val high: Int = 0, val medium: Int = 0, val low: Int = 0) {
    companion object {
        fun of(apps: List<LocalAppInfo>): RiskCounts = apps.fold(RiskCounts()) { acc, app ->
            when (app.riskResult?.riskLevel) {
                RiskLevel.CRITICAL, RiskLevel.HIGH -> acc.copy(high = acc.high + 1)
                RiskLevel.MEDIUM -> acc.copy(medium = acc.medium + 1)
                else -> acc.copy(low = acc.low + 1)
            }
        }
    }
}

fun List<LocalAppInfo>.averageRiskScore(): Float =
    if (isEmpty()) 0f else sumOf { it.riskResult?.score ?: 0 }.toFloat() / size

/** User-facing message for a failed call, keeping the wording the scan screens already used. */
fun ApiResult<*>.errorMessage(): String? = when (this) {
    is ApiResult.Success -> null
    is ApiResult.ApiError -> message
    is ApiResult.NetworkError -> "Network error: ${exception.message ?: "check your connection"}"
    is ApiResult.SerializationError -> "Data error: ${exception.message}"
}

internal fun AppDetailsResponse.toLocalAppInfo(fallbackPackage: String, fallbackName: String): LocalAppInfo {
    val trackerList = trackers?.trackers?.map {
        SimpleTrackerInfo(name = it.name, riskLevel = it.category)
    } ?: app?.trackers ?: emptyList()

    val permissionList = permissions.ifEmpty { app?.permissions ?: emptyList() }
    val base = app ?: AppInfoDto(
        packageName = packageName ?: fallbackPackage,
        displayName = appName ?: fallbackName,
        permissions = permissionList,
        trackers = trackerList,
        finalScore = overallScore ?: securityScore ?: 0f
    )

    return base.copy(
        displayName = base.displayName ?: appName ?: fallbackName,
        permissions = permissionList,
        trackers = trackerList,
        finalScore = overallScore ?: securityScore ?: base.finalScore
    ).toUiModel()
}

/** Risk computed on-device when the server has no analysis for this app. */
internal fun LocalAppInfo.withLocalRisk(): LocalAppInfo = copy(
    riskResult = SecurityUtils.calculateAppRisk(
        permissions = permissions,
        trackers = trackers.map { it.name },
        isSystemApp = isSystemApp
    )
)
