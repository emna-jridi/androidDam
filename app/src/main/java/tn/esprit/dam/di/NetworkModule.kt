package tn.esprit.dam.di

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import tn.esprit.dam.BuildConfig
import tn.esprit.dam.data.TokenManager
import javax.inject.Singleton

/**
 * Hilt Network Module
 * Provides network-related dependencies (HttpClient, etc.)
 * Uses TokenManager for token management (same as ApiClient)
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private const val TAG = "NetworkModule"
    
    /**
     * Provides Ktor HttpClient with bearer token authentication
     * Uses TokenManager for token storage/retrieval (consistent with ApiClient)
     */
    @Provides
    @Singleton
    fun provideHttpClient(
        @ApplicationContext context: Context
    ): HttpClient {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
            coerceInputValues = true
            prettyPrint = true
        }
        
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(json)
            }
            
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("KtorClient", message)
                    }
                }
                level = LogLevel.ALL
            }
            
            install(Auth) {
                bearer {
                    loadTokens {
                        runBlocking {
                            val token = TokenManager.getAccessToken(context)
                            if (token != null) {
                                Log.d(TAG, "[AUTH] Bearer token loaded: ${token.take(20)}...")
                                BearerTokens(token, "")
                            } else {
                                Log.w(TAG, "[AUTH] No bearer token available")
                                null
                            }
                        }
                    }
                    
                    refreshTokens {
                        Log.d(TAG, "[REFRESH] Token refresh triggered")
                        runBlocking {
                            val refreshToken = TokenManager.getRefreshToken(context)
                            if (refreshToken != null) {
                                Log.d(TAG, "[REFRESH] Refresh token available, attempting refresh...")
                                // Token refresh would be handled here, but for now return null
                                // to trigger re-authentication
                                null
                            } else {
                                Log.w(TAG, "[WARN] No refresh token available")
                                null
                            }
                        }
                    }
                }
            }
            
            expectSuccess = false
            
            defaultRequest {
                url(BuildConfig.BASE_ROOT)
                contentType(ContentType.Application.Json)
            }
            
            engine {
                connectTimeout = 30_000
                socketTimeout = 30_000
            }
        }
    }
}

