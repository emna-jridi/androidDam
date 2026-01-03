package tn.esprit.dam.data.security

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * TokenRepositoryImpl - Implementation using EncryptedSharedPreferences
 * All tokens are encrypted at rest
 */
class TokenRepositoryImpl(
    private val context: Context
) : TokenRepository {
    
    companion object {
        private const val TAG = "TokenRepository"
        private const val PREFS_NAME = "encrypted_auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedSharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    // StateFlow for reactive access token updates
    private val _accessTokenFlow = MutableStateFlow<String?>(null)
    override fun observeAccessToken(): Flow<String?> = _accessTokenFlow.asStateFlow()
    
    override suspend fun saveTokens(accessToken: String, refreshToken: String): Unit =
        withContext(Dispatchers.IO) {
            try {
                encryptedSharedPreferences.edit()
                    .putString(KEY_ACCESS_TOKEN, accessToken)
                    .putString(KEY_REFRESH_TOKEN, refreshToken)
                    .apply()
                
                _accessTokenFlow.value = accessToken
                Log.d(TAG, "Tokens saved securely")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving tokens", e)
                throw e
            }
        }
    
    override suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        try {
            val token = encryptedSharedPreferences.getString(KEY_ACCESS_TOKEN, null)
            if (token != null && _accessTokenFlow.value != token) {
                _accessTokenFlow.value = token
            }
            token
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving access token", e)
            null
        }
    }
    
    override suspend fun getRefreshToken(): String? = withContext(Dispatchers.IO) {
        try {
            encryptedSharedPreferences.getString(KEY_REFRESH_TOKEN, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving refresh token", e)
            null
        }
    }
    
    override suspend fun clearTokens(): Unit = withContext(Dispatchers.IO) {
        try {
            encryptedSharedPreferences.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_USER_DATA)
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .apply()
            
            _accessTokenFlow.value = null
            Log.d(TAG, "Tokens cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing tokens", e)
            throw e
        }
    }
    
    override suspend fun hasTokens(): Boolean = withContext(Dispatchers.IO) {
        try {
            val accessToken = encryptedSharedPreferences.getString(KEY_ACCESS_TOKEN, null)
            val refreshToken = encryptedSharedPreferences.getString(KEY_REFRESH_TOKEN, null)
            accessToken != null && refreshToken != null
        } catch (e: Exception) {
            Log.e(TAG, "Error checking tokens", e)
            false
        }
    }
    
    override suspend fun saveUserData(userData: String): Unit = withContext(Dispatchers.IO) {
        try {
            encryptedSharedPreferences.edit()
                .putString(KEY_USER_DATA, userData)
                .apply()
            Log.d(TAG, "User data saved")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user data", e)
            throw e
        }
    }
    
    override suspend fun getUserData(): String? = withContext(Dispatchers.IO) {
        try {
            encryptedSharedPreferences.getString(KEY_USER_DATA, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving user data", e)
            null
        }
    }
    
    override suspend fun setLoggedIn(isLoggedIn: Boolean): Unit = withContext(Dispatchers.IO) {
        try {
            encryptedSharedPreferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
                .apply()
            Log.d(TAG, "Login status saved: $isLoggedIn")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting login status", e)
            throw e
        }
    }
    
    override suspend fun isLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        try {
            encryptedSharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking login status", e)
            false
        }
    }
}
