package tn.esprit.dam.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import tn.esprit.dam.data.model.*

object AvatarApiService {
    private const val BASE_URL = "${tn.esprit.dam.data.Config.API_ROOT}/avatar"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
    }

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    /**
     * Récupérer l'avatar de l'utilisateur
     * Backend returns: { success: boolean, data: Avatar, message?: string }
     */
    suspend fun getAvatar(token: String, userHash: String): Avatar {
        val envelope = client.get("$BASE_URL/$userHash") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body<ApiEnvelope<Avatar>>()
        
        return envelope.unwrapOrThrow()
    }

    /**
     * Mettre à jour l'avatar
     * Backend returns: { success: boolean, data: AvatarResponse, message?: string }
     */
    suspend fun updateAvatar(
        token: String,
        userHash: String,
        updateDto: UpdateAvatarDto
    ): AvatarResponse {
        val envelope = client.put("$BASE_URL/$userHash") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
                append(HttpHeaders.ContentType, "application/json")
            }
            setBody(updateDto)
        }.body<ApiEnvelope<AvatarResponse>>()
        
        return envelope.unwrapOrThrow()
    }

    /**
     * Générer un avatar aléatoire
     * Backend returns: { success: boolean, data: AvatarResponse, message?: string }
     */
    suspend fun generateRandomAvatar(
        token: String,
        userHash: String
    ): AvatarResponse {
        val envelope = client.post("$BASE_URL/random/$userHash") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body<ApiEnvelope<AvatarResponse>>()
        
        return envelope.unwrapOrThrow()
    }

    /**
     * Générer un avatar cohérent
     * Backend returns: { success: boolean, data: AvatarResponse, message?: string }
     */
    suspend fun generateConsistentAvatar(
        token: String,
        userHash: String
    ): AvatarResponse {
        val envelope = client.post("$BASE_URL/consistent/$userHash") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body<ApiEnvelope<AvatarResponse>>()
        
        return envelope.unwrapOrThrow()
    }
}
