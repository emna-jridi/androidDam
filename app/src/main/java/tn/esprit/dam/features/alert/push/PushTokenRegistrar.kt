package tn.esprit.dam.features.alert.push

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import tn.esprit.dam.core.session.TokenRepository
import tn.esprit.dam.features.alert.data.repository.AlertRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Sends this device's FCM token to the backend so alerts can be pushed to it.
 * Runs on every login (password, Google or biometric) and whenever Firebase rotates the token.
 */
@Singleton
class PushTokenRegistrar @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val alertRepository: AlertRepository
) {
    fun start(scope: CoroutineScope) {
        scope.launch {
            var wasLoggedIn = false
            tokenRepository.isLoggedInFlow()
                .distinctUntilChanged()
                .collect { loggedIn ->
                    if (loggedIn) register() else if (wasLoggedIn) unregister()
                    wasLoggedIn = loggedIn
                }
        }
    }

    /**
     * Session ended (logout or expired): kill this device's FCM token so the backend's copy
     * stops reaching it. A fresh token is created and registered on the next login.
     */
    private fun unregister() {
        FirebaseMessaging.getInstance().deleteToken()
            .addOnFailureListener { Timber.w(it, "FCM token deletion failed") }
    }

    /** [token] is passed by onNewToken; otherwise the current one is fetched from Firebase. */
    suspend fun register(token: String? = null) {
        if (!tokenRepository.isLoggedIn()) return
        try {
            val fcmToken = token ?: fetchFcmToken()
            val ok = alertRepository.registerDeviceToken(fcmToken)
            if (!ok) Timber.w("Push token registration rejected by backend")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Push token registration failed")
        }
    }

    private suspend fun fetchFcmToken(): String = suspendCancellableCoroutine { cont ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val result = task.result
            if (task.isSuccessful && result != null) cont.resume(result)
            else cont.resumeWithException(task.exception ?: IllegalStateException("No FCM token"))
        }
    }
}
