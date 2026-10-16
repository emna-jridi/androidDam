package tn.esprit.dam.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tn.esprit.dam.core.session.TokenRepository
import tn.esprit.dam.core.session.TokenRepositoryImpl
import tn.esprit.dam.features.alert.data.repository.AlertRepository
import tn.esprit.dam.features.alert.data.repository.AlertRepositoryImpl
import tn.esprit.dam.features.auth.data.repository.AuthRepository
import tn.esprit.dam.features.auth.data.repository.AuthRepositoryImpl
import tn.esprit.dam.features.darkweb.data.repository.DarkWebRepository
import tn.esprit.dam.features.darkweb.data.repository.DarkWebRepositoryImpl
import tn.esprit.dam.features.profile.data.repository.AvatarRepository
import tn.esprit.dam.features.profile.data.repository.AvatarRepositoryImpl
import tn.esprit.dam.features.profile.data.repository.UserRepository
import tn.esprit.dam.features.profile.data.repository.UserRepositoryImpl
import tn.esprit.dam.features.report.data.repository.ReportRepository
import tn.esprit.dam.features.report.data.repository.ReportRepositoryImpl
import tn.esprit.dam.features.scan.data.repository.ScanRepository
import tn.esprit.dam.features.scan.data.repository.ScanRepositoryImpl
import tn.esprit.dam.features.vault.data.repository.BiometricAuthRepository
import tn.esprit.dam.features.vault.data.repository.BiometricAuthRepositoryImpl
import tn.esprit.dam.features.vault.data.repository.VaultRepository
import tn.esprit.dam.features.vault.data.repository.VaultRepositoryImpl
import javax.inject.Singleton

/** Binds every repository interface to its implementation. Everything else uses @Inject constructors. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindTokenRepository(impl: TokenRepositoryImpl): TokenRepository

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds @Singleton
    abstract fun bindAvatarRepository(impl: AvatarRepositoryImpl): AvatarRepository

    @Binds @Singleton
    abstract fun bindAlertRepository(impl: AlertRepositoryImpl): AlertRepository

    @Binds @Singleton
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @Binds @Singleton
    abstract fun bindScanRepository(impl: ScanRepositoryImpl): ScanRepository

    @Binds @Singleton
    abstract fun bindDarkWebRepository(impl: DarkWebRepositoryImpl): DarkWebRepository

    // Singleton matters: the unlocked vault key lives in this instance.
    @Binds @Singleton
    abstract fun bindVaultRepository(impl: VaultRepositoryImpl): VaultRepository

    @Binds @Singleton
    abstract fun bindBiometricAuthRepository(impl: BiometricAuthRepositoryImpl): BiometricAuthRepository
}
