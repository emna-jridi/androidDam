package tn.esprit.dam.data.repository

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import tn.esprit.dam.data.Config
import tn.esprit.dam.data.model.User
import tn.esprit.dam.data.security.TokenRepository
import javax.inject.Inject

/**
 * ProfileRepository - Handles user profile data
 * Manages API calls for profile information, avatar, and user settings
 */
class ProfileRepository @Inject constructor(
    private val httpClient: HttpClient,
    private val tokenRepository: TokenRepository
) {
    
    companion object {
        private const val TAG = "ProfileRepository"
        private const val ENDPOINT_PROFILE = "${Config.API_ROOT}/users/profile"
    }
    
    /**
     * Get current user profile
     */
    suspend fun getProfile(): User? {
        return try {
            Log.d(TAG, "Fetching user profile")
            
            val response = httpClient.get(ENDPOINT_PROFILE)
            
            return when (response.status) {
                HttpStatusCode.OK -> {
                    val user = response.body<User>()
                    Log.d(TAG, "Profile fetched: ${user.email}")
                    user
                }
                HttpStatusCode.Unauthorized -> {
                    Log.w(TAG, "Unauthorized to fetch profile")
                    tokenRepository.clearTokens()
                    null
                }
                else -> {
                    Log.e(TAG, "Failed to fetch profile: ${response.status}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching profile", e)
            null
        }
    }
    
    /**
     * Update user profile
     */
    suspend fun updateProfile(user: User): Boolean {
        return try {
            Log.d(TAG, "Updating profile for user: ${user.email}")
            
            val response = httpClient.patch(ENDPOINT_PROFILE) {
                contentType(ContentType.Application.Json)
                setBody(user)
            }
            
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile", e)
            false
        }
    }
    
    /**
     * Update avatar for user
     */
    suspend fun updateAvatar(avatarData: ByteArray): Boolean {
        return try {
            Log.d(TAG, "Updating user avatar")
            
            val response = httpClient.patch("$ENDPOINT_PROFILE/avatar") {
                contentType(ContentType.Application.OctetStream)
                setBody(avatarData)
            }
            
            return response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            Log.e(TAG, "Error updating avatar", e)
            false
        }
    }
    
    /**
     * Get user statistics (scan count, risk level, etc.)
     */
    suspend fun getUserStats(): Map<String, Any>? {
        return try {
            Log.d(TAG, "Fetching user statistics")
            
            val response = httpClient.get("$ENDPOINT_PROFILE/stats")
            
            return when (response.status) {
                HttpStatusCode.OK -> response.body<Map<String, Any>>()
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user stats", e)
            null
        }
    }
    
    /**
     * Delete user account
     */
    suspend fun deleteAccount(password: String): Boolean {
        return try {
            Log.d(TAG, "Deleting user account")
            
            val response = httpClient.patch(ENDPOINT_PROFILE) {
                contentType(ContentType.Application.Json)
                setBody(mapOf("action" to "delete", "password" to password))
            }
            
            return response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting account", e)
            false
        }
    }
}
