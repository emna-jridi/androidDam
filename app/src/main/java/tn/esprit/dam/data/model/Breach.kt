package tn.esprit.dam.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Breach(
    val _id: String,
    val userId: String,
    val source: String,
    val domain: String?,
    val breachDate: String?,
    val description: String?,
    val dataClasses: List<String>,
    val isResolved: Boolean = false,
    val logoPath: String?,
    val isVerified: Boolean = false
)
