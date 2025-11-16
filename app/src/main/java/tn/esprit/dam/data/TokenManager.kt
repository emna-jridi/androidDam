

package tn.esprit.dam.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tn.esprit.dam.data.model.User

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

object TokenManager {
    private const val TAG = "TokenManager"

    // Keys
    private val ACCESS_TOKEN = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    private val USER_DATA = stringPreferencesKey("user_data")
    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }


    suspend fun saveTokens(
        context: Context,
        accessToken: String,
        refreshToken: String? = null
    ) {
        Log.d(TAG, "💾 Saving tokens...")
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            refreshToken?.let { prefs[REFRESH_TOKEN] = it }
            prefs[IS_LOGGED_IN] = true
        }
        Log.d(TAG, "✅ Tokens saved")
    }

    /**
     * Récupérer l'access token
     */
    suspend fun getAccessToken(context: Context): String? {
        return context.dataStore.data.map { it[ACCESS_TOKEN] }.first()
    }


    suspend fun getRefreshToken(context: Context): String? {
        return context.dataStore.data.map { it[REFRESH_TOKEN] }.first()
    }

    suspend fun saveUser(context: Context, user: User) {
        Log.d(TAG, "💾 Saving user: ${user.email}")
        context.dataStore.edit { prefs ->
            prefs[USER_DATA] = json.encodeToString(user)
        }
        Log.d(TAG, "✅ User saved")
    }


    suspend fun getUser(context: Context): User? {
        return try {
            val userData = context.dataStore.data.map { it[USER_DATA] }.first()
            userData?.let { json.decodeFromString<User>(it) }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting user: ${e.message}")
            null
        }
    }


    suspend fun isLoggedIn(context: Context): Boolean {
        return context.dataStore.data.map { it[IS_LOGGED_IN] ?: false }.first()
    }


    fun isLoggedInFlow(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { it[IS_LOGGED_IN] ?: false }
    }

    fun getUserFlow(context: Context): Flow<User?> {
        return context.dataStore.data.map { prefs ->
            try {
                prefs[USER_DATA]?.let { json.decodeFromString<User>(it) }
            } catch (e: Exception) {
                null
            }
        }
    }


    suspend fun clearAll(context: Context) {
        Log.d(TAG, "🗑️ Clearing all data...")
        context.dataStore.edit { it.clear() }
        Log.d(TAG, "✅ All data cleared")
    }
}