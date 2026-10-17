package tn.esprit.dam

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber
import tn.esprit.dam.features.alert.push.PushTokenRegistrar
import tn.esprit.dam.features.vault.data.VaultAutoLocker
import javax.inject.Inject

@HiltAndroidApp
class ShadowGuardApplication : Application() {

    @Inject lateinit var pushTokenRegistrar: PushTokenRegistrar
    @Inject lateinit var vaultAutoLocker: VaultAutoLocker

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // Release builds log nothing: no tokens or vault data can leak to logcat.
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        pushTokenRegistrar.start(appScope)
        vaultAutoLocker.start(appScope)
    }
}
