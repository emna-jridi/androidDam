package tn.esprit.dam.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import tn.esprit.dam.BuildConfig
import tn.esprit.dam.core.network.Config
import tn.esprit.dam.core.session.TokenRepository
import tn.esprit.dam.features.auth.data.model.LoginResponse
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val REFRESH_PATH = "/api/auth/refresh"

    /** Endpoints where a 401 means "bad credentials", not "expired session". */
    private val credentialPaths = listOf(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/google",
        "/api/auth/biometric/challenge",
        "/api/auth/biometric/verify",
        REFRESH_PATH,
    )

    @Provides
    @Singleton
    fun provideHttpClient(tokenRepository: TokenRepository): HttpClient =
        buildHttpClient(Android.create(), tokenRepository)

    /** Separated from [provideHttpClient] so tests can plug in a MockEngine. */
    internal fun buildHttpClient(
        engine: HttpClientEngine,
        tokenRepository: TokenRepository,
        retryBackoff: Boolean = true,
    ): HttpClient {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
            coerceInputValues = true
        }

        return HttpClient(engine) {
            expectSuccess = false

            install(ContentNegotiation) {
                json(json)
            }

            install(HttpTimeout) {
                connectTimeoutMillis = Config.Timeouts.CONNECT_TIMEOUT_MS
                socketTimeoutMillis = Config.Timeouts.SOCKET_TIMEOUT_MS
            }

            // Only GETs are retried: replaying a POST/PUT/PATCH/DELETE after a timeout
            // can apply the same write twice on the server.
            install(HttpRequestRetry) {
                maxRetries = 3
                retryIf { request, response ->
                    request.method == HttpMethod.Get && response.status.value >= 500
                }
                retryOnExceptionIf { request, cause ->
                    request.method == HttpMethod.Get && cause !is CancellationException
                }
                if (retryBackoff) exponentialDelay() else delayMillis { 0L }
            }

            // The Auth plugin is the single owner of 401s: it attaches the token, refreshes it
            // once on 401 and replays the request; if refresh fails the session is cleared.
            install(Auth) {
                bearer {
                    sendWithoutRequest { true }

                    loadTokens {
                        tokenRepository.getAccessToken()
                            ?.takeIf { it.isNotBlank() }
                            ?.let { BearerTokens(it, tokenRepository.getRefreshToken().orEmpty()) }
                    }

                    refreshTokens {
                        val path = response.call.request.url.encodedPath
                        if (credentialPaths.any { path.startsWith(it) }) return@refreshTokens null

                        val refreshToken = tokenRepository.getRefreshToken()
                        if (refreshToken.isNullOrBlank()) {
                            tokenRepository.clearTokens()
                            return@refreshTokens null
                        }

                        val refreshResponse = try {
                            client.post(REFRESH_PATH) {
                                markAsRefreshTokenRequest()
                                setBody(mapOf("refreshToken" to refreshToken))
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            // Offline / timeout: keep the session, the caller just gets the 401.
                            return@refreshTokens null
                        }

                        val refreshed = if (refreshResponse.status.isSuccess()) {
                            runCatching { refreshResponse.body<LoginResponse>() }.getOrNull()
                        } else {
                            null
                        }

                        if (refreshed == null) {
                            // The server rejected the refresh token: the session is over.
                            tokenRepository.clearTokens()
                            null
                        } else {
                            val newRefreshToken = refreshed.refreshToken ?: refreshToken
                            tokenRepository.saveTokens(refreshed.accessToken, newRefreshToken)
                            BearerTokens(refreshed.accessToken, newRefreshToken)
                        }
                    }
                }
            }

            defaultRequest {
                url(BuildConfig.BASE_ROOT)
                contentType(ContentType.Application.Json)
            }
        }
    }
}
