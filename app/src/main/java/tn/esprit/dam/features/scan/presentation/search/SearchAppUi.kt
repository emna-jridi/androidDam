package tn.esprit.dam.features.scan.presentation.search

import tn.esprit.dam.features.scan.data.remote.dto.SearchResultDto

/** What the search screen shows for one app — keeps the screen independent of the API model. */
data class SearchAppUi(
    val packageName: String,
    val appName: String,
    val iconUrl: String,
    val rating: Float,
    val downloads: String,
    val developer: String,
    val riskScore: Int,
    val riskLevel: String
)

fun SearchResultDto.toUi() = SearchAppUi(
    packageName = packageName,
    appName = appName,
    iconUrl = icon,
    rating = rating,
    downloads = downloads,
    developer = developer,
    riskScore = predictedRiskScore,
    riskLevel = predictedRiskLevel
)
