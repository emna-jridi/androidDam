package tn.esprit.dam.features.scan.presentation.home

import tn.esprit.dam.features.scan.data.remote.dto.AppResult

/** An app shown in the home screen's "risky apps" list. */
data class RiskyAppUi(
    val packageName: String?,
    val appName: String?,
    val score: Float
)

fun AppResult.toRiskyAppUi() = RiskyAppUi(
    packageName = packageName,
    appName = appName,
    score = aiRiskScore ?: finalScore ?: 0f
)
