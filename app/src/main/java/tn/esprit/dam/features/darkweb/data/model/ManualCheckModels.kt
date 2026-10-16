package tn.esprit.dam.features.darkweb.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One result of POST /darkweb/check-email (LeakCheck format, not stored). */
@Serializable
data class ManualBreachResult(
    @SerialName("Name") val name: String = "",
    @SerialName("BreachDate") val breachDate: String = "",
    @SerialName("Description") val description: String = "",
    @SerialName("DataClasses") val dataClasses: List<String> = emptyList(),
    @SerialName("IsResolved") val isResolved: Boolean = false,
    @SerialName("Domain") val domain: String = ""
)

@Serializable
data class PasswordCheckResult(
    val count: Int = 0
)
