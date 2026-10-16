package tn.esprit.dam.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tn.esprit.dam.core.session.TokenRepository
import tn.esprit.dam.core.session.TokenRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {
    @Provides
    @Singleton
    fun provideTokenRepository(
        @ApplicationContext context: Context
    ): TokenRepository = TokenRepositoryImpl(context)
}
