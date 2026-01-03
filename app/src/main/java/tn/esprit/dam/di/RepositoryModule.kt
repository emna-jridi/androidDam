package tn.esprit.dam.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import tn.esprit.dam.data.repository.AlertRepository
import tn.esprit.dam.data.repository.AuthRepository
import tn.esprit.dam.data.repository.ProfileRepository
import tn.esprit.dam.data.security.TokenRepository
import javax.inject.Singleton

/**
 * Hilt Repository Module
 * Provides all repository implementations for the application
 * All repositories are singletons that manage data access
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    /**
     * Provides AuthRepository for authentication data
     */
    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext context: Context
    ): AuthRepository = AuthRepository(context)
    
    /**
     * Provides AlertRepository for alert and notification data
     */
    @Provides
    @Singleton
    fun provideAlertRepository(
        httpClient: HttpClient,
        tokenRepository: TokenRepository
    ): AlertRepository = AlertRepository(httpClient, tokenRepository)
    
    /**
     * Provides ProfileRepository for user profile data
     */
    @Provides
    @Singleton
    fun provideProfileRepository(
        httpClient: HttpClient,
        tokenRepository: TokenRepository
    ): ProfileRepository = ProfileRepository(httpClient, tokenRepository)
}
