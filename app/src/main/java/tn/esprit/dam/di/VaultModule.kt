package tn.esprit.dam.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tn.esprit.dam.core.network.KtorHttpClient
import tn.esprit.dam.features.vault.data.remote.VaultApi
import tn.esprit.dam.features.vault.data.repository.VaultRepository
import tn.esprit.dam.features.vault.data.ai.OllamaPasswordAdvisor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VaultModule {
    @Provides
    @Singleton
    fun provideVaultApi(client: KtorHttpClient): VaultApi {
        return VaultApi(client)
    }

    @Provides
    @Singleton
    fun provideVaultRepository(api: VaultApi): VaultRepository {
        return VaultRepository(api)
    }

    @Provides
    @Singleton
    fun provideOllamaPasswordAdvisor(repository: VaultRepository): OllamaPasswordAdvisor {
        return OllamaPasswordAdvisor(repository)
    }
}
