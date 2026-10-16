package tn.esprit.dam.core.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class User(
    // Auth endpoints send "id", /users/profile sends Mongo's "_id"
    @JsonNames("id", "_id")
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
) {
    fun getAvatarUrl(): String? {
        return avatarFileName?.let {
            tn.esprit.dam.core.network.Config.Endpoints.avatarUploadUrl(it)
        }
    }
}
