package tn.esprit.dam.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tn.esprit.dam.data.model.User
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// OK 1. Single DataStore Instance
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
        try {
            Log.d(TAG, "[SAVE] Starting token save operation...")
            Log.d(TAG, "[SAVE] ACCESS_TOKEN length=${accessToken.length}, prefix=${accessToken.take(30)}")
            if (refreshToken != null) {
                Log.d(TAG, "[SAVE] REFRESH_TOKEN length=${refreshToken.length}")
            }
            
            context.dataStore.edit { prefs ->
                Log.d(TAG, "[SAVE] Writing ACCESS_TOKEN to DataStore...")
                prefs[ACCESS_TOKEN] = accessToken
                
                if (refreshToken != null) {
                    Log.d(TAG, "[SAVE] Writing REFRESH_TOKEN to DataStore...")
                    prefs[REFRESH_TOKEN] = refreshToken
                }
                
                Log.d(TAG, "[SAVE] Setting IS_LOGGED_IN to true...")
                prefs[IS_LOGGED_IN] = true
            }
            
            Log.d(TAG, "[OK] ✓ Tokens saved successfully - verify in next retrieval")
        } catch (e: Exception) {
            Log.e(TAG, "[ERROR] Failed to save tokens: ${e.message}", e)
            throw e
        }
    }

    /**
     * RÃ©cupÃ©rer l'access token
     */
    suspend fun getAccessToken(context: Context): String? {
        return try {
            Log.d(TAG, "[RETRIEVE] Getting ACCESS_TOKEN from DataStore...")
            val token = context.dataStore.data.map { it[ACCESS_TOKEN] }.first()
            
            if (token != null) {
                Log.d(TAG, "[OK] ACCESS_TOKEN retrieved successfully - length=${token.length}, prefix=${token.take(30)}")
                token
            } else {
                Log.w(TAG, "[WARN] ACCESS_TOKEN is NULL in DataStore - user may not be logged in")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "[ERROR] Failed to retrieve ACCESS_TOKEN: ${e.message}", e)
            null
        }
    }


    suspend fun getRefreshToken(context: Context): String? {
        return try {
            Log.d(TAG, "[RETRIEVE] Getting REFRESH_TOKEN from DataStore...")
            val token = context.dataStore.data.map { it[REFRESH_TOKEN] }.first()
            
            if (token != null) {
                Log.d(TAG, "[OK] REFRESH_TOKEN retrieved successfully - length=${token.length}")
                token
            } else {
                Log.w(TAG, "[WARN] REFRESH_TOKEN is NULL in DataStore")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "[ERROR] Failed to retrieve REFRESH_TOKEN: ${e.message}", e)
            null
        }
    }

    suspend fun saveUser(context: Context, user: User) {
        Log.d(TAG, "[SAVE] Saving user: ${user.email}")
        context.dataStore.edit { prefs ->
            prefs[USER_DATA] = json.encodeToString(user)
        }
        Log.d(TAG, "[OK] User saved")
    }

    suspend fun getUser(context: Context): User? {
        return try {
            val userData = context.dataStore.data.map { it[USER_DATA] }.first()
            userData?.let { json.decodeFromString<User>(it) }
        } catch (e: Exception) {
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
        Log.d(TAG, "[CLEAR] Clearing all data...")
        context.dataStore.edit { it.clear() }
        Log.d(TAG, "[OK] All data cleared")
    }
    suspend fun getUserHash(context: Context): String? {
        return getUser(context)?.userHash
    }

}
