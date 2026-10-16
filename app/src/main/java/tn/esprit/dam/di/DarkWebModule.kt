package tn.esprit.dam.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tn.esprit.dam.core.network.KtorHttpClient
import tn.esprit.dam.features.darkweb.data.remote.DarkWebApi
import tn.esprit.dam.features.darkweb.data.repository.DarkWebRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DarkWebModule {
    @Provides
    @Singleton
    fun provideDarkWebApi(client: KtorHttpClient): DarkWebApi {
        return DarkWebApi(client)
    }

    @Provides
    @Singleton
    fun provideDarkWebRepository(api: DarkWebApi): DarkWebRepository {
        return DarkWebRepository(api)
    }
}
