package tn.esprit.dam.data

import android.content.Context
import android.util.Log
import tn.esprit.dam.data.security.TokenRepositoryImpl

/**
 * SessionManager - Centralized session management
 * Handles logout across all token storage systems to prevent data leakage
 */
object SessionManager {
    private const val TAG = "SessionManager"

    /**
     * Complete logout - clears all token storages
     * Must be called for proper logout to prevent user data leakage
     */
    suspend fun logout(context: Context) {
        Log.d(TAG, "🚪 Starting complete logout...")
        
        // 1. Clear TokenManager (DataStore - legacy storage)
        try {
            TokenManager.clearAll(context)
            Log.d(TAG, "✅ TokenManager (DataStore) cleared")
        } catch (e: Exception) {
            Log.e(TAG, "⚠️ Failed to clear TokenManager: ${e.message}")
        }
        
        // 2. Clear TokenRepository (EncryptedSharedPreferences)
        try {
            val tokenRepository = TokenRepositoryImpl(context)
            tokenRepository.clearTokens()
            Log.d(TAG, "✅ TokenRepository (EncryptedPrefs) cleared")
        } catch (e: Exception) {
            Log.e(TAG, "⚠️ Failed to clear TokenRepository: ${e.message}")
        }
        
        Log.d(TAG, "✅ Complete logout finished - all storages cleared")
    }
    
    /**
     * Check if user is logged in (checks both storages)
     */
    suspend fun isLoggedIn(context: Context): Boolean {
        // Check TokenManager first (primary for login flow)
        val tokenManagerLoggedIn = TokenManager.isLoggedIn(context)
        if (tokenManagerLoggedIn) return true
        
        // Check TokenRepository as fallback
        try {
            val tokenRepository = TokenRepositoryImpl(context)
            return tokenRepository.isLoggedIn()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking TokenRepository login state: ${e.message}")
        }
        
        return false
    }
}
