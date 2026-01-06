package tn.esprit.dam.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import tn.esprit.dam.features.scan.data.KtorEnrichmentClient
import javax.inject.Singleton

/**
 * Hilt module for providing Ktor-based enrichment services
 * HttpClient is provided by NetworkModule
 */
@Module
@InstallIn(SingletonComponent::class)
object KtorModule {

    @Singleton
    @Provides
    fun provideKtorEnrichmentClient(httpClient: HttpClient): KtorEnrichmentClient {
        return KtorEnrichmentClient(httpClient)
    }
}
