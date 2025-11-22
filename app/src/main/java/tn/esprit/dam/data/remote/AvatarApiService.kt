package tn.esprit.dam.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import tn.esprit.dam.data.model.*
object AvatarApiService {
    private const val BASE_URL = "http://172.20.10.3/api/v1/avatar"

    private val client = HttpClient {
        // Même configuration que ApiClient
    }

    /**
     * Récupérer l'avatar de l'utilisateur
     */
    suspend fun getAvatar(token: String, userHash: String): Avatar {
        return client.get("$BASE_URL/$userHash") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body()
    }

    /**
     * Mettre à jour l'avatar
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
     * Générer un avatar aléatoire
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
     * Générer un avatar cohérent
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