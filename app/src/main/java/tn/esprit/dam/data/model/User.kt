package tn.esprit.dam.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("_id")
    val id: String? = null,
    val email: String,
    val name: String,
    val role: String = "user",
    val avatarFileName: String? = null,
    val userHash: String? = null,
    val provider: String = "local",
    val isEmailVerified: Boolean = false,
    val isVerified: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

{

fun getAvatarUrl(baseUrl: String = "http://172.18.4.239:3000"): String? {
    return avatarFileName?.let {
        "$baseUrl/uploads/avatars/$it"
    }
}
}