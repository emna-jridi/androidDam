package tn.esprit.dam.data.repository

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import tn.esprit.dam.data.Config
import tn.esprit.dam.data.model.LoginRequest
import tn.esprit.dam.data.model.LoginResponse
import tn.esprit.dam.data.security.TokenRepository
import javax.inject.Inject

/**
 * ApiRepository - Injectable repository for API calls
 * Replaces ApiClient singleton pattern
 * Properly manages tokens through injected TokenRepository
 */
class ApiRepository @Inject constructor(
    private val httpClient: HttpClient,
    private val tokenRepository: TokenRepository
) {
    
    companion object {
        private const val TAG = "ApiRepository"
    }
    
    /**
     * Login with email and password
     * Saves tokens securely upon success
     */
    suspend fun login(email: String, password: String): LoginResponse {
        try {
            Log.d(TAG, "Logging in with email: $email")
            
            val response = httpClient.post("${Config.API_ROOT}/auth/login") {
                setBody(LoginRequest(email, password))
            }
            
            return when (response.status) {
                HttpStatusCode.OK -> {
                    val loginResponse = response.body<LoginResponse>()
                    
                    // Save tokens securely
                    tokenRepository.saveTokens(
                        loginResponse.accessToken ?: "",
                        loginResponse.refreshToken ?: ""
                    )
                    
                    // Mark as logged in
                    tokenRepository.setLoggedIn(true)
                    
                    Log.d(TAG, "Login successful")
                    loginResponse
                }
                HttpStatusCode.Unauthorized -> {
                    throw AuthException("Email ou mot de passe incorrect")
                }
                else -> {
                    throw ApiException(
                        "Login failed: ${response.bodyAsText()}",
                        response.status
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login error", e)
            throw e
        }
    }
    
    /**
     * Logout - clears all tokens
     */
    suspend fun logout() {
        try {
            Log.d(TAG, "Logging out")
            tokenRepository.clearTokens()
            Log.d(TAG, "Logout successful")
        } catch (e: Exception) {
            Log.e(TAG, "Logout error", e)
            throw e
        }
    }
    
    /**
     * Check if user is logged in
     */
    suspend fun isLoggedIn(): Boolean {
        return tokenRepository.isLoggedIn()
    }
    
    class AuthException(message: String) : Exception(message)
    class ApiException(message: String, val statusCode: HttpStatusCode) : Exception(message)
}
