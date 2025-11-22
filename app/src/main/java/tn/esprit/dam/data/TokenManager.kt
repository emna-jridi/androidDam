package tn.esprit.dam.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.messaging.FirebaseMessaging
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

// ✅ 1. Single DataStore Instance
private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

// ✅ 2. DTO to fix serialization crash (Mixed types support)
@Serializable
data class AlertEventDto(
    val packageName: String,
    val event: String,
    val timestamp: Long,
    val details: Map<String, String> // Values must be Strings to be safe
)

@Serializable
data class DeviceTokenDto(
    val token: String,
    val platform: String
)

object TokenManager {

    private const val TAG = "TokenManager"

    // 👇 IMPORTANT: Ensure this matches your PC IP
    private const val BASE_URL = "http://172.20.10.3:3000"

    // Keys
    private val ACCESS_TOKEN = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    private val USER_DATA = stringPreferencesKey("user_data")
    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    // ============================================================
    // AUTH & TOKENS
    // ============================================================

    suspend fun saveTokens(context: Context, accessToken: String, refreshToken: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            refreshToken?.let { prefs[REFRESH_TOKEN] = it }
            prefs[IS_LOGGED_IN] = true
        }
    }

    suspend fun getAccessToken(context: Context): String? =
        context.dataStore.data.map { it[ACCESS_TOKEN] }.first()

    suspend fun getRefreshToken(context: Context): String? =
        context.dataStore.data.map { it[REFRESH_TOKEN] }.first()

    suspend fun saveUser(context: Context, user: User) {
        context.dataStore.edit { prefs ->
            prefs[USER_DATA] = json.encodeToString(user)
        }
    }

    suspend fun getUser(context: Context): User? {
        return try {
            val data = context.dataStore.data.map { it[USER_DATA] }.first()
            data?.let { json.decodeFromString<User>(it) }
        } catch (e: Exception) {
            null
        }
    }

    fun getUserFlow(context: Context): Flow<User?> =
        context.dataStore.data.map { prefs ->
            try {
                prefs[USER_DATA]?.let { json.decodeFromString<User>(it) }
            } catch (e: Exception) {
                null
            }
        }

    suspend fun isLoggedIn(context: Context): Boolean =
        context.dataStore.data.map { it[IS_LOGGED_IN] ?: false }.first()

    fun isLoggedInFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[IS_LOGGED_IN] ?: false }

    suspend fun clearAll(context: Context) {
        context.dataStore.edit { it.clear() }
    }

    // ============================================================
    // FCM & ALERTS (Backend Communication)
    // ============================================================

    suspend fun getCurrentFcmToken(): String? {
        return try {
            suspendCoroutine { cont ->
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) cont.resume(task.result)
                    else cont.resume(null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ getCurrentFcmToken failed", e)
            null
        }
    }

    suspend fun sendFcmTokenToBackend(context: Context, fcmToken: String?) {
        if (fcmToken.isNullOrEmpty()) return
        try {
            val accessToken = getAccessToken(context) ?: return

            val payload = DeviceTokenDto(token = fcmToken, platform = "android")

            withContext(Dispatchers.IO) {
                client.post("$BASE_URL/alerts/device-token") {
                    header("Authorization", "Bearer $accessToken")
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to send FCM token", e)
        }
    }

    // ✅ FIXED: Using DTO to prevent Serialization Crash
    suspend fun sendAlertEvent(
        context: Context,
        packageName: String,
        event: String,
        details: Map<String, String> = emptyMap()
    ) {
        try {
            val accessToken = getAccessToken(context)
            if (accessToken.isNullOrEmpty()) {
                Log.e(TAG, "❌ No access token, cannot send Alert")
                return
            }

            // Use the DTO class defined at the top
            val payload = AlertEventDto(
                packageName = packageName,
                event = event,
                timestamp = System.currentTimeMillis(),
                details = details
            )

            withContext(Dispatchers.IO) {
                val response = client.post("$BASE_URL/alerts/event") {
                    header("Authorization", "Bearer $accessToken")
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                }

                if (response.status.isSuccess()) {
                    Log.d(TAG, "✅ Alert sent successfully: $packageName used $event")
                } else {
                    Log.e(TAG, "❌ Failed to send alert: ${response.status}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error sending alert", e)
        }
    }
}