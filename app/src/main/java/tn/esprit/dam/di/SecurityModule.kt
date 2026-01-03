package tn.esprit.dam.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tn.esprit.dam.data.security.TokenRepository
import tn.esprit.dam.data.security.TokenRepositoryImpl
import javax.inject.Singleton

/**
 * Hilt Security Module
 * Provides security-related dependencies, particularly TokenRepository
 * All provided dependencies are Singletons for the application lifecycle
 */
@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {
    
    /**
     * Provides TokenRepository as a singleton
     * Uses EncryptedSharedPreferences for secure token storage
     */
    @Provides
    @Singleton
    fun provideTokenRepository(
        @ApplicationContext context: Context
    ): TokenRepository = TokenRepositoryImpl(context)
}
