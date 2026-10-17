package tn.esprit.dam.features.scan.presentation.search

import tn.esprit.dam.features.scan.data.remote.dto.SearchResultDto

/**
 * What the search screen shows for one app — keeps the screen independent of the API model.
 * No risk score here: search results are not analyzed until the user opens one.
 */
data class SearchAppUi(
    val packageName: String,
    val appName: String,
    val iconUrl: String,
    val rating: Float?,
    val developer: String?
)

fun SearchResultDto.toUi() = SearchAppUi(
    packageName = packageName,
    appName = appName,
    iconUrl = icon,
    rating = rating?.takeIf { it > 0f },
    developer = developer?.takeIf { it.isNotBlank() }
)
