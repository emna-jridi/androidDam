package tn.esprit.dam.data.security

import kotlinx.coroutines.flow.Flow

/**
 * TokenRepository Interface - abstraction for token management
 * Encrypts sensitive data using EncryptedSharedPreferences
 * Replaces the service locator pattern of TokenManager
 */
interface TokenRepository {
    
    /**
     * Save access and refresh tokens securely
     */
    suspend fun saveTokens(accessToken: String, refreshToken: String)
    
    /**
     * Retrieve the access token
     */
    suspend fun getAccessToken(): String?
    
    /**
     * Retrieve the refresh token
     */
    suspend fun getRefreshToken(): String?
    
    /**
     * Observe access token changes
     */
    fun observeAccessToken(): Flow<String?>
    
    /**
     * Clear all tokens (logout)
     */
    suspend fun clearTokens()
    
    /**
     * Check if tokens exist
     */
    suspend fun hasTokens(): Boolean
    
    /**
     * Save user data along with tokens
     */
    suspend fun saveUserData(userData: String)
    
    /**
     * Retrieve user data
     */
    suspend fun getUserData(): String?
    
    /**
     * Mark user as logged in
     */
    suspend fun setLoggedIn(isLoggedIn: Boolean)
    
    /**
     * Check if user is logged in
     */
    suspend fun isLoggedIn(): Boolean
}
