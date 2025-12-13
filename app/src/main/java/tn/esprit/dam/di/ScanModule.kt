package tn.esprit.dam.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tn.esprit.dam.data.api.ScanApiService
import tn.esprit.dam.data.TokenManager
import tn.esprit.dam.data.repository.ScanRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ScanModule {

    @Singleton
    @Provides
    fun provideTokenManager(): TokenManager = TokenManager

    @Singleton
    @Provides
    fun provideScanApiService(
        @ApplicationContext context: Context,
        tokenManager: TokenManager
    ): ScanApiService {
        return ScanApiService(context, tokenManager)
    }

    @Singleton
    @Provides
    fun provideScanRepository(
        scanApiService: ScanApiService
    ): ScanRepository = ScanRepository(scanApiService)
}
