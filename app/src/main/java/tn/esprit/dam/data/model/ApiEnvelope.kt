package tn.esprit.dam.data.model

import kotlinx.serialization.Serializable

/**
 * Generic API response wrapper that matches backend structure
 * Backend always returns: { success: boolean, data: T, message?: string }
 */
@Serializable
data class ApiEnvelope<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)

/**
 * Extension function to unwrap API envelope or throw exception
 */
fun <T> ApiEnvelope<T>.unwrapOrThrow(): T {
    return if (success && data != null) {
        data
    } else {
        throw Exception(message ?: "API call failed")
    }
}
