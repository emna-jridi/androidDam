package tn.esprit.dam.features.vault.presentation.util

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.crypto.Cipher

object BiometricHelper {
    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun isBiometricNotEnrolled(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )) {
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> true
            else -> false
        }
    }

    fun openBiometricSettings(context: Context) {
        val intent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
            putExtra(
                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            )
        }
        context.startActivity(intent)
    }

    /**
     * Shows a BiometricPrompt bound to [cipher]: the Keystore only lets the cipher work after
     * a real biometric match, so success can't be faked by skipping the prompt.
     * A wrong finger keeps the prompt open; [onError] gets final errors, [onCancel] user cancels.
     */
    fun authenticateWithCipher(
        activity: FragmentActivity,
        cipher: Cipher,
        title: String,
        subtitle: String,
        negativeText: String,
        onSuccess: (Cipher) -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit = {}
    ) {
        val biometricPrompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    result.cryptoObject?.cipher?.let(onSuccess)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    val cancelled = errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_CANCELED
                    if (cancelled) onCancel() else onError(errString.toString())
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }
}
