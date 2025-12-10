package tn.esprit.dam.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tn.esprit.dam.data.remote.KtorHttpClient
import tn.esprit.dam.data.TokenManager
import javax.inject.Singleton

/**
 * Hilt Network Module
 * Note: KtorHttpClient is auto-injected via @Inject constructor
 * TokenManager is an object singleton and doesn't need a provider
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // All dependencies are auto-injected
}
