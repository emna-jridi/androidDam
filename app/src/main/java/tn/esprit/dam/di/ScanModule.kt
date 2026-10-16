package tn.esprit.dam.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import tn.esprit.dam.features.scan.data.remote.ScanApiService
import tn.esprit.dam.features.scan.data.repository.ScanRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ScanModule {
    @Singleton
    @Provides
    fun provideScanApiService(
        httpClient: HttpClient,
        @ApplicationContext context: Context
    ): ScanApiService {
        return ScanApiService(httpClient, context)
    }

    @Singleton
    @Provides
    fun provideScanRepository(
        scanApiService: ScanApiService
    ): ScanRepository = ScanRepository(scanApiService)
}
