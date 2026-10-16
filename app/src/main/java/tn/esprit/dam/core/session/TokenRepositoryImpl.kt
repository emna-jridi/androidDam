package tn.esprit.dam.core.session

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tn.esprit.dam.core.model.User

class TokenRepositoryImpl(
    private val context: Context
) : TokenRepository {
    companion object {
        private const val PREFS_NAME = "encrypted_auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
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

    private val _accessTokenFlow = MutableStateFlow<String?>(encryptedSharedPreferences.getString(KEY_ACCESS_TOKEN, null))
    private val _userFlow = MutableStateFlow<User?>(
        try {
            encryptedSharedPreferences.getString(KEY_USER_DATA, null)?.let { json.decodeFromString<User>(it) }
        } catch (e: Exception) {
            null
        }
    )
    private val _isLoggedInFlow = MutableStateFlow(encryptedSharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false))

    override fun observeAccessToken(): Flow<String?> = _accessTokenFlow.asStateFlow()
    override fun getUserFlow(): Flow<User?> = _userFlow.asStateFlow()
    override fun isLoggedInFlow(): Flow<Boolean> = _isLoggedInFlow.asStateFlow()

    override suspend fun saveTokens(accessToken: String, refreshToken: String): Unit =
        withContext(Dispatchers.IO) {
            try {
                encryptedSharedPreferences.edit()
                    .putString(KEY_ACCESS_TOKEN, accessToken)
                    .putString(KEY_REFRESH_TOKEN, refreshToken)
                    .putBoolean(KEY_IS_LOGGED_IN, true)
                    .apply()

                _accessTokenFlow.value = accessToken
                _isLoggedInFlow.value = true
            } catch (e: Exception) {
                throw e
            }
        }

    override suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        try {
            val token = encryptedSharedPreferences.getString(KEY_ACCESS_TOKEN, null)
            if (token != _accessTokenFlow.value) {
                _accessTokenFlow.value = token
            }
            token
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getRefreshToken(): String? = withContext(Dispatchers.IO) {
        try {
            encryptedSharedPreferences.getString(KEY_REFRESH_TOKEN, null)
        } catch (e: Exception) {
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
            _userFlow.value = null
            _isLoggedInFlow.value = false
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun hasTokens(): Boolean = withContext(Dispatchers.IO) {
        try {
            val accessToken = encryptedSharedPreferences.getString(KEY_ACCESS_TOKEN, null)
            val refreshToken = encryptedSharedPreferences.getString(KEY_REFRESH_TOKEN, null)
            !accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun saveUser(user: User): Unit = withContext(Dispatchers.IO) {
        try {
            val userJson = json.encodeToString(user)
            saveUserData(userJson)
            _userFlow.value = user
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun saveUserData(userData: String): Unit = withContext(Dispatchers.IO) {
        try {
            encryptedSharedPreferences.edit()
                .putString(KEY_USER_DATA, userData)
                .apply()
            try {
                _userFlow.value = json.decodeFromString<User>(userData)
            } catch (_: Exception) {}
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getUser(): User? = withContext(Dispatchers.IO) {
        try {
            val raw = getUserData()
            val user = raw?.let { json.decodeFromString<User>(it) }
            if (_userFlow.value != user) {
                _userFlow.value = user
            }
            user
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getUserData(): String? = withContext(Dispatchers.IO) {
        try {
            encryptedSharedPreferences.getString(KEY_USER_DATA, null)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun setLoggedIn(isLoggedIn: Boolean): Unit = withContext(Dispatchers.IO) {
        try {
            encryptedSharedPreferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
                .apply()
            _isLoggedInFlow.value = isLoggedIn
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun isLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        try {
            encryptedSharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        } catch (e: Exception) {
            false
        }
    }
}
