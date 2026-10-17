package tn.esprit.dam.features.vault.data

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import tn.esprit.dam.core.session.TokenRepository
import tn.esprit.dam.features.vault.data.repository.VaultRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Drops the in-memory vault key when the app goes to the background or the session ends,
 * so an unlocked vault never outlives the user's attention or their login. Ending the
 * session also turns biometric vault unlock off.
 */
@Singleton
class VaultAutoLocker @Inject constructor(
    private val vaultRepository: VaultRepository,
    private val tokenRepository: TokenRepository
) {
    /** Must be called on the main thread (registers a lifecycle observer). */
    fun start(scope: CoroutineScope) {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) = vaultRepository.lockVault()
        })

        scope.launch {
            tokenRepository.isLoggedInFlow()
                .distinctUntilChanged()
                .filter { !it }
                .collect {
                    vaultRepository.lockVault()
                    // The wrapped key belongs to this account's vault, not the next one to log in
                    vaultRepository.disableBiometricUnlock()
                }
        }
    }
}
