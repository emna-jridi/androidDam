package tn.esprit.dam.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tn.esprit.dam.features.scan.domain.usecase.CalculateRiskUseCase
import javax.inject.Singleton

/**
 * Hilt Scan Module
 * Provides scan-related use cases and repositories
 */
@Module
@InstallIn(SingletonComponent::class)
object ScanUseCasesModule {
    
    /**
     * Provides CalculateRiskUseCase
     */
    @Provides
    @Singleton
    fun provideCalculateRiskUseCase(): CalculateRiskUseCase = CalculateRiskUseCase()
}
