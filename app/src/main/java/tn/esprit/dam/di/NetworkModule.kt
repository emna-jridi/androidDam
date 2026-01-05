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
import tn.esprit.dam.data.security.TokenRepository
import javax.inject.Singleton

/**
 * Hilt Network Module
 * Provides network-related dependencies (HttpClient, etc.)
 * Uses TokenRepository for token management (aligned with encrypted storage)
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private const val TAG = "NetworkModule"
    
    /**
    * Provides Ktor HttpClient with bearer token authentication
    * Uses TokenRepository for token storage/retrieval
     */
    @Provides
    @Singleton
    fun provideHttpClient(
        @ApplicationContext context: Context,
        tokenRepository: TokenRepository
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
                            // Primary: encrypted token storage
                            val repoToken = tokenRepository.getAccessToken()
                            if (!repoToken.isNullOrBlank()) {
                                Log.d(TAG, "[AUTH] Bearer token loaded (repo): ${repoToken.take(20)}...")
                                return@runBlocking BearerTokens(repoToken, "")
                            }

                            // Fallback: legacy DataStore storage (TokenManager)
                            val legacyToken = TokenManager.getAccessToken(context)
                            if (!legacyToken.isNullOrBlank()) {
                                Log.d(TAG, "[AUTH] Bearer token loaded (legacy): ${legacyToken.take(20)}...")
                                // Migrate to repository for future requests using legacy refresh token when available
                                val legacyRefresh = TokenManager.getRefreshToken(context)
                                if (!legacyRefresh.isNullOrBlank()) {
                                    tokenRepository.saveTokens(legacyToken, legacyRefresh)
                                }
                                return@runBlocking BearerTokens(legacyToken, "")
                            }

                            Log.w(TAG, "[AUTH] No bearer token available")
                            null
                        }
                    }
                    
                    refreshTokens {
                        Log.d(TAG, "[REFRESH] Token refresh triggered")
                        runBlocking {
                            val refreshToken = tokenRepository.getRefreshToken()
                                ?: TokenManager.getRefreshToken(context)

                            if (!refreshToken.isNullOrBlank()) {
                                Log.d(TAG, "[REFRESH] Refresh token available, attempting refresh...")
                                // TODO: implement refresh call; returning null forces re-auth
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

