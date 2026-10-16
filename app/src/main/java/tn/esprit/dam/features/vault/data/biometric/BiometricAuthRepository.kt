package tn.esprit.dam.features.vault.data.biometric

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.security.Signature
import kotlin.coroutines.resume

class BiometricAuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: BiometricApiService
) {
    companion object {
        private const val PREFS_NAME = "biometric_prefs"
        private const val KEY_DEVICE_REGISTERED = "device_registered"
        private const val KEY_DEVICE_NAME = "device_name"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    sealed class BiometricAuthResult {
        data class Success(
            val accessToken: String,
            val refreshToken: String?,
            val userEmail: String
        ) : BiometricAuthResult()

        data class Error(val message: String) : BiometricAuthResult()
        object Cancelled : BiometricAuthResult()
        object NotRegistered : BiometricAuthResult()
        object BiometricNotAvailable : BiometricAuthResult()
    }

    sealed class RegistrationResult {
        data class Success(val message: String) : RegistrationResult()
        data class Error(val message: String) : RegistrationResult()
        object BiometricNotAvailable : RegistrationResult()
        object Cancelled : RegistrationResult()
    }

    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun needsEnrollment(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
    }

    fun isDeviceRegistered(): Boolean {
        val deviceId = BiometricKeyManager.getDeviceId(context)
        return prefs.getBoolean(KEY_DEVICE_REGISTERED, false) &&
                BiometricKeyManager.hasKey(deviceId)
    }

    fun getDeviceId(): String {
        return BiometricKeyManager.getDeviceId(context)
    }

    fun getDeviceName(): String {
        return prefs.getString(KEY_DEVICE_NAME, null) ?: getDefaultDeviceName()
    }

    suspend fun registerDevice(
        activity: FragmentActivity,
        deviceName: String = getDefaultDeviceName()
    ): RegistrationResult = withContext(Dispatchers.Main) {
        if (!isBiometricAvailable()) {
            return@withContext RegistrationResult.BiometricNotAvailable
        }

        val deviceId = BiometricKeyManager.getDeviceId(context)

        val keyResult = BiometricKeyManager.generateKeyPair(deviceId)
        if (keyResult.isFailure) {
            val error = keyResult.exceptionOrNull()?.message ?: "Key generation failed"
            return@withContext RegistrationResult.Error(error)
        }

        val keyGenResult = keyResult.getOrNull()!!

        val verified = verifyKeyWithBiometric(activity, deviceId)
        if (!verified) {
            BiometricKeyManager.deleteKey(deviceId)
            return@withContext RegistrationResult.Cancelled
        }

        return@withContext withContext(Dispatchers.IO) {
            val request = apiService.buildRegistrationRequest(
                deviceId = deviceId,
                publicKeyPem = keyGenResult.publicKeyPem,
                deviceName = deviceName
            )

            val registerResult = apiService.registerDevice(request)

            if (registerResult.isSuccess) {
                prefs.edit()
                    .putBoolean(KEY_DEVICE_REGISTERED, true)
                    .putString(KEY_DEVICE_NAME, deviceName)
                    .apply()

                RegistrationResult.Success("Biometric authentication enabled successfully!")
            } else {
                val error = registerResult.exceptionOrNull()?.message ?: "Registration failed"

                BiometricKeyManager.deleteKey(deviceId)

                RegistrationResult.Error(error)
            }
        }
    }

    suspend fun authenticateWithBiometric(
        activity: FragmentActivity
    ): BiometricAuthResult = withContext(Dispatchers.Main) {
        if (!isBiometricAvailable()) {
            return@withContext BiometricAuthResult.BiometricNotAvailable
        }

        val deviceId = BiometricKeyManager.getDeviceId(context)

        if (!isDeviceRegistered()) {
            return@withContext BiometricAuthResult.NotRegistered
        }

        val challengeResult = withContext(Dispatchers.IO) {
            apiService.requestChallenge(deviceId)
        }

        if (challengeResult.isFailure) {
            val error = challengeResult.exceptionOrNull()?.message ?: "Failed to get challenge"
            return@withContext BiometricAuthResult.Error(error)
        }

        val challenge = challengeResult.getOrNull()!!.challenge

        val signatureResult = signChallengeWithBiometric(activity, deviceId, challenge)

        if (signatureResult == null) {
            return@withContext BiometricAuthResult.Cancelled
        }

        return@withContext withContext(Dispatchers.IO) {
            val verifyResult = apiService.verifyBiometric(
                deviceId = deviceId,
                challenge = challenge,
                signature = signatureResult
            )

            if (verifyResult.isSuccess) {
                val response = verifyResult.getOrNull()!!

                BiometricAuthResult.Success(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                    userEmail = response.user.email
                )
            } else {
                val error = verifyResult.exceptionOrNull()?.message ?: "Verification failed"
                BiometricAuthResult.Error(error)
            }
        }
    }

    suspend fun removeDevice(removeFromBackend: Boolean = true): Boolean {
        val deviceId = BiometricKeyManager.getDeviceId(context)

        if (removeFromBackend) {
            withContext(Dispatchers.IO) {
                apiService.deleteDevice(deviceId)
            }
        }

        BiometricKeyManager.deleteKey(deviceId)

        prefs.edit()
            .remove(KEY_DEVICE_REGISTERED)
            .remove(KEY_DEVICE_NAME)
            .apply()

        return true
    }

    suspend fun getDevices(): Result<DevicesListResponse> {
        return withContext(Dispatchers.IO) {
            apiService.getDevices()
        }
    }

    suspend fun getBiometricStatus(): Result<BiometricStatusResponse> {
        return withContext(Dispatchers.IO) {
            apiService.getBiometricStatus()
        }
    }

    private suspend fun verifyKeyWithBiometric(
        activity: FragmentActivity,
        deviceId: String
    ): Boolean = authenticateWithDeviceKey(
        activity = activity,
        deviceId = deviceId,
        title = "Setup Biometric Login",
        subtitle = "Verify your fingerprint or face to enable biometric login",
        negativeButtonText = "Cancel",
        failureValue = false
    ) { true }

    private suspend fun signChallengeWithBiometric(
        activity: FragmentActivity,
        deviceId: String,
        challenge: String
    ): String? = authenticateWithDeviceKey<String?>(
        activity = activity,
        deviceId = deviceId,
        title = "Unlock Vault",
        subtitle = "Use your fingerprint or face to authenticate",
        negativeButtonText = "Use Password",
        failureValue = null
    ) { result ->
        result.cryptoObject?.signature?.let { authenticatedSignature ->
            BiometricKeyManager.signData(
                authenticatedSignature,
                challenge.toByteArray(Charsets.UTF_8)
            ).getOrNull()
        }
    }

    private suspend fun <T> authenticateWithDeviceKey(
        activity: FragmentActivity,
        deviceId: String,
        title: String,
        subtitle: String,
        negativeButtonText: String,
        failureValue: T,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> T
    ): T = suspendCancellableCoroutine { continuation ->

        val signature = BiometricKeyManager.getSignatureForSigning(deviceId)
        if (signature == null) {
            continuation.resume(failureValue)
            return@suspendCancellableCoroutine
        }

        val executor = ContextCompat.getMainExecutor(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                continuation.resume(onSuccess(result))
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                continuation.resume(failureValue)
            }

            override fun onAuthenticationFailed() {
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        try {
            biometricPrompt.authenticate(
                promptInfo,
                BiometricPrompt.CryptoObject(signature)
            )
        } catch (e: Exception) {
            continuation.resume(failureValue)
        }
    }

    private fun getDefaultDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        return if (model.lowercase().startsWith(manufacturer.lowercase())) {
            model
        } else {
            "$manufacturer $model"
        }
    }
}
