package tn.esprit.dam.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import tn.esprit.dam.data.model.*
object AvatarApiService {
    private const val BASE_URL = "${tn.esprit.dam.data.Config.API_ROOT}/avatar"

    private val client = HttpClient {
        // Minimal client â€” ApiClient is preferred for authenticated calls
    }

    /**
     * RÃ©cupÃ©rer l'avatar de l'utilisateur
     */
    suspend fun getAvatar(token: String, userHash: String): Avatar {
        return client.get("$BASE_URL/$userHash") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body()
    }

    /**
     * Mettre Ã  jour l'avatar
     */
    suspend fun updateAvatar(
        token: String,
        userHash: String,
        updateDto: UpdateAvatarDto
    ): AvatarResponse {
        return client.put("$BASE_URL/$userHash") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
                append(HttpHeaders.ContentType, "application/json")
            }
            setBody(updateDto)
        }.body()
    }

    /**
     * GÃ©nÃ©rer un avatar alÃ©atoire
     */
    suspend fun generateRandomAvatar(
        token: String,
        userHash: String
    ): AvatarResponse {
        return client.post("$BASE_URL/random/$userHash") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body()
    }

    /**
     * GÃ©nÃ©rer un avatar cohÃ©rent
     */
    suspend fun generateConsistentAvatar(
        token: String,
        userHash: String
    ): AvatarResponse {
        return client.post("$BASE_URL/consistent/$userHash") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body()
    }
}
