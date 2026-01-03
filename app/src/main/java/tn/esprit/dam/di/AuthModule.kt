package tn.esprit.dam.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tn.esprit.dam.data.repository.AuthRepository
import tn.esprit.dam.features.auth.domain.usecase.LoginUseCase
import tn.esprit.dam.features.scan.domain.usecase.CalculateRiskUseCase
import javax.inject.Singleton

/**
 * Hilt Auth Module
 * Provides authentication-related use cases and repositories
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    
    /**
     * Provides LoginUseCase
     */
    @Provides
    @Singleton
    fun provideLoginUseCase(
        authRepository: AuthRepository
    ): LoginUseCase = LoginUseCase(authRepository)
}
