package tn.esprit.dam.data.repository

import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import tn.esprit.dam.data.Config
import tn.esprit.dam.data.model.UpdateUserRequest
import tn.esprit.dam.data.model.User
import tn.esprit.dam.data.security.TokenRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UserRepository - Manages user profile data
 * Uses HttpClient with injected TokenRepository for secure requests
 */
@Singleton
class UserRepository @Inject constructor(
    private val httpClient: HttpClient,
    private val tokenRepository: TokenRepository
) {
    
    companion object {
        private const val TAG = "UserRepository"
        private const val ENDPOINT_PROFILE = "${Config.API_ROOT}/users/profile"
    }

    suspend fun getProfile(): User {
        return try {
            Log.d(TAG, "Fetching profile")
            val response = httpClient.get(ENDPOINT_PROFILE)
            
            return when (response.status) {
                HttpStatusCode.OK -> {
                    val user = response.body<User>()
                    Log.d(TAG, "Profile fetched: ${user.email}")
                    user
                }
                HttpStatusCode.Unauthorized -> {
                    Log.w(TAG, "Unauthorized")
                    tokenRepository.clearTokens()
                    throw Exception("Unauthorized")
                }
                else -> throw Exception("Failed to fetch profile: ${response.status}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting profile", e)
            throw e
        }
    }

    suspend fun updateProfile(request: UpdateUserRequest): User {
        return try {
            Log.d(TAG, "Updating profile")
            val response = httpClient.patch(ENDPOINT_PROFILE) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            return when (response.status) {
                HttpStatusCode.OK -> response.body<User>()
                else -> throw Exception("Failed to update profile: ${response.status}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile", e)
            throw e
        }
    }

    suspend fun updateName(name: String): User {
        return updateProfile(UpdateUserRequest(name = name))
    }

    suspend fun getCachedUser(): User? {
        return try {
            getProfile()
        } catch (e: Exception) {
            Log.w(TAG, "Could not get cached user", e)
            null
        }
    }
}
