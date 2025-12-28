package tn.esprit.dam.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tn.esprit.dam.data.remote.KtorHttpClient
import com.shadowguard.dam.data.remote.api.VaultApi
import com.shadowguard.dam.data.repository.VaultRepository
import com.shadowguard.dam.data.remote.ai.OllamaPasswordAdvisor
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

    @Provides
    @Singleton
    fun provideDarkWebApi(client: KtorHttpClient): com.shadowguard.dam.data.remote.darkweb.DarkWebApi {
        return com.shadowguard.dam.data.remote.darkweb.DarkWebApi(client)
    }

    @Provides
    @Singleton
    fun provideDarkWebRepository(api: com.shadowguard.dam.data.remote.darkweb.DarkWebApi): com.shadowguard.dam.data.repository.DarkWebRepository {
        return com.shadowguard.dam.data.repository.DarkWebRepository(api)
    }
}
