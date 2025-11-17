package tn.esprit.dam.di


import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import tn.esprit.dam.data.remote.api.ScanApiService
import tn.esprit.dam.data.repository.ScanRepository
import javax.inject.Singleton

/**
 * ✅ Module Hilt principal
 * Fournit toutes les dépendances pour l'injection
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * ✅ Fournit l'instance Json pour la sérialisation
     */
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
            encodeDefaults = true
        }
    }

    /**
     * ✅ Fournit le HttpClient Ktor
     */
    @Provides
    @Singleton
    fun provideHttpClient(json: Json): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(json)
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 120_000 // 120 sec
                connectTimeoutMillis = 60_000
                socketTimeoutMillis = 120_000
            }

            install(Logging) {
                level = LogLevel.BODY
                logger = object : Logger {
                    override fun log(message: String) {
                        android.util.Log.d("Ktor", message)
                    }
                }
            }

            // Configuration supplémentaire
            engine {
                requestTimeout = 30_000
                endpoint {
                    connectTimeout = 30_000
                    socketTimeout = 30_000
                }
            }
        }
    }

    /**
     * ✅ Fournit le ScanApiService
     */
    @Provides
    @Singleton
    fun provideScanApiService(httpClient: HttpClient): ScanApiService {
        return ScanApiService(httpClient)
    }

    /**
     * ✅ Fournit le ScanRepository
     */
    @Provides
    @Singleton
    fun provideScanRepository(
        @ApplicationContext context: Context,
        scanApiService: ScanApiService
    ): ScanRepository {
        return ScanRepository(
            context = context,
            api = scanApiService
        )
    }
}