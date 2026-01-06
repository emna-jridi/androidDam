package tn.esprit.dam.features.vault.biometric

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.security.Signature
import kotlin.coroutines.resume

/**
 * Repository that orchestrates the complete biometric authentication flow.
 * 
 * Registration Flow:
 * 1. Generate EC key pair in Android Keystore
 * 2. Export public key as PEM
 * 3. Register device with backend (POST /auth/biometric/register)
 * 
 * Authentication Flow:
 * 1. Request challenge from backend (POST /auth/biometric/challenge)
 * 2. Show BiometricPrompt with CryptoObject (Signature)
 * 3. Sign challenge with authenticated Signature
 * 4. Send signature to backend (POST /auth/biometric/verify)
 * 5. Receive JWT tokens on success
 */
class BiometricAuthRepository(private val context: Context) {
    
    companion object {
        private const val TAG = "BiometricAuthRepo"
        private const val PREFS_NAME = "biometric_prefs"
        private const val KEY_DEVICE_REGISTERED = "device_registered"
        private const val KEY_DEVICE_NAME = "device_name"
    }
    
    private val apiService = BiometricApiService.getInstance(context)
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Result of biometric authentication.
     */
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
    
    /**
     * Result of device registration.
     */
    sealed class RegistrationResult {
        data class Success(val message: String) : RegistrationResult()
        data class Error(val message: String) : RegistrationResult()
        object BiometricNotAvailable : RegistrationResult()
        object Cancelled : RegistrationResult()
    }
    
    /**
     * Check if biometric authentication is available on this device.
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }
    
    /**
     * Check if biometric needs enrollment.
     */
    fun needsEnrollment(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
    }
    
    /**
     * Check if this device is already registered for biometric auth.
     */
    fun isDeviceRegistered(): Boolean {
        val deviceId = BiometricKeyManager.getDeviceId(context)
        return prefs.getBoolean(KEY_DEVICE_REGISTERED, false) &&
                BiometricKeyManager.hasKey(deviceId)
    }
    
    /**
     * Get the device ID for this device.
     */
    fun getDeviceId(): String {
        return BiometricKeyManager.getDeviceId(context)
    }
    
    /**
     * Get the stored device name.
     */
    fun getDeviceName(): String {
        return prefs.getString(KEY_DEVICE_NAME, null) ?: getDefaultDeviceName()
    }
    
    /**
     * Register this device for biometric authentication.
     * Must be called while user is logged in with password.
     * 
     * @param activity FragmentActivity for BiometricPrompt
     * @param deviceName Optional custom device name
     * @return RegistrationResult indicating success or failure
     */
    suspend fun registerDevice(
        activity: FragmentActivity,
        deviceName: String = getDefaultDeviceName()
    ): RegistrationResult = withContext(Dispatchers.Main) {
        
        if (!isBiometricAvailable()) {
            return@withContext RegistrationResult.BiometricNotAvailable
        }
        
        val deviceId = BiometricKeyManager.getDeviceId(context)
        
        Log.d(TAG, "🔐 Starting biometric registration for device: $deviceId")
        
        // Step 1: Generate key pair
        val keyResult = BiometricKeyManager.generateKeyPair(deviceId)
        if (keyResult.isFailure) {
            val error = keyResult.exceptionOrNull()?.message ?: "Key generation failed"
            Log.e(TAG, "❌ Key generation failed: $error")
            return@withContext RegistrationResult.Error(error)
        }
        
        val keyGenResult = keyResult.getOrNull()!!
        Log.d(TAG, "✅ Key pair generated successfully")
        
        // Step 2: Verify the key works by prompting biometric
        val verified = verifyKeyWithBiometric(activity, deviceId)
        if (!verified) {
            Log.e(TAG, "❌ Biometric verification failed during setup")
            BiometricKeyManager.deleteKey(deviceId)
            return@withContext RegistrationResult.Cancelled
        }
        
        Log.d(TAG, "✅ Biometric verification successful")
        
        // Step 3: Register with backend
        return@withContext withContext(Dispatchers.IO) {
            val request = apiService.buildRegistrationRequest(
                deviceId = deviceId,
                publicKeyPem = keyGenResult.publicKeyPem,
                deviceName = deviceName
            )
            
            val registerResult = apiService.registerDevice(request)
            
            if (registerResult.isSuccess) {
                // Save registration status
                prefs.edit()
                    .putBoolean(KEY_DEVICE_REGISTERED, true)
                    .putString(KEY_DEVICE_NAME, deviceName)
                    .apply()
                
                Log.d(TAG, "✅ Device registered with backend")
                RegistrationResult.Success("Biometric authentication enabled successfully!")
            } else {
                val error = registerResult.exceptionOrNull()?.message ?: "Registration failed"
                Log.e(TAG, "❌ Backend registration failed: $error")
                
                // Clean up local key since backend registration failed
                BiometricKeyManager.deleteKey(deviceId)
                
                RegistrationResult.Error(error)
            }
        }
    }
    
    /**
     * Authenticate using biometrics.
     * This is the main entry point for biometric login.
     * 
     * @param activity FragmentActivity for BiometricPrompt
     * @return BiometricAuthResult with tokens on success
     */
    suspend fun authenticateWithBiometric(
        activity: FragmentActivity
    ): BiometricAuthResult = withContext(Dispatchers.Main) {
        
        if (!isBiometricAvailable()) {
            return@withContext BiometricAuthResult.BiometricNotAvailable
        }
        
        val deviceId = BiometricKeyManager.getDeviceId(context)
        
        if (!isDeviceRegistered()) {
            Log.w(TAG, "⚠️ Device not registered for biometric auth")
            return@withContext BiometricAuthResult.NotRegistered
        }
        
        Log.d(TAG, "🔐 Starting biometric authentication for device: $deviceId")
        
        // Step 1: Request challenge from server
        val challengeResult = withContext(Dispatchers.IO) {
            apiService.requestChallenge(deviceId)
        }
        
        if (challengeResult.isFailure) {
            val error = challengeResult.exceptionOrNull()?.message ?: "Failed to get challenge"
            Log.e(TAG, "❌ Challenge request failed: $error")
            return@withContext BiometricAuthResult.Error(error)
        }
        
        val challenge = challengeResult.getOrNull()!!.challenge
        Log.d(TAG, "✅ Challenge received: ${challenge.take(20)}...")
        
        // Step 2: Sign challenge with biometric authentication
        val signatureResult = signChallengeWithBiometric(activity, deviceId, challenge)
        
        if (signatureResult == null) {
            Log.w(TAG, "⚠️ Biometric authentication cancelled")
            return@withContext BiometricAuthResult.Cancelled
        }
        
        Log.d(TAG, "✅ Challenge signed successfully")
        
        // Step 3: Verify with backend
        return@withContext withContext(Dispatchers.IO) {
            val verifyResult = apiService.verifyBiometric(
                deviceId = deviceId,
                challenge = challenge,
                signature = signatureResult
            )
            
            if (verifyResult.isSuccess) {
                val response = verifyResult.getOrNull()!!
                Log.d(TAG, "✅ Biometric authentication successful!")
                
                BiometricAuthResult.Success(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                    userEmail = response.user.email
                )
            } else {
                val error = verifyResult.exceptionOrNull()?.message ?: "Verification failed"
                Log.e(TAG, "❌ Biometric verification failed: $error")
                BiometricAuthResult.Error(error)
            }
        }
    }
    
    /**
     * Remove biometric authentication for this device.
     * Deletes local key and optionally removes from backend.
     */
    suspend fun removeDevice(removeFromBackend: Boolean = true): Boolean {
        val deviceId = BiometricKeyManager.getDeviceId(context)
        
        Log.d(TAG, "🗑️ Removing biometric device: $deviceId")
        
        // Remove from backend if requested
        if (removeFromBackend) {
            withContext(Dispatchers.IO) {
                apiService.deleteDevice(deviceId)
            }
        }
        
        // Delete local key
        BiometricKeyManager.deleteKey(deviceId)
        
        // Clear prefs
        prefs.edit()
            .remove(KEY_DEVICE_REGISTERED)
            .remove(KEY_DEVICE_NAME)
            .apply()
        
        Log.d(TAG, "✅ Device removed")
        return true
    }
    
    /**
     * Get list of registered devices for current user.
     */
    suspend fun getDevices(): Result<DevicesListResponse> {
        return withContext(Dispatchers.IO) {
            apiService.getDevices()
        }
    }
    
    /**
     * Get biometric status for current user.
     */
    suspend fun getBiometricStatus(): Result<BiometricStatusResponse> {
        return withContext(Dispatchers.IO) {
            apiService.getBiometricStatus()
        }
    }
    
    // ==================== PRIVATE HELPERS ====================
    
    /**
     * Show BiometricPrompt to verify the key works during registration.
     */
    private suspend fun verifyKeyWithBiometric(
        activity: FragmentActivity,
        deviceId: String
    ): Boolean = suspendCancellableCoroutine { continuation ->
        
        val signature = BiometricKeyManager.getSignatureForSigning(deviceId)
        if (signature == null) {
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }
        
        val executor = ContextCompat.getMainExecutor(context)
        
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                Log.d(TAG, "✅ Biometric verification succeeded during setup")
                continuation.resume(true)
            }
            
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Log.e(TAG, "❌ Biometric error during setup: $errorCode - $errString")
                continuation.resume(false)
            }
            
            override fun onAuthenticationFailed() {
                Log.w(TAG, "⚠️ Biometric failed (wrong finger/face)")
                // Don't resume - let user try again
            }
        }
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Setup Biometric Login")
            .setSubtitle("Verify your fingerprint or face to enable biometric login")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        
        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        
        try {
            biometricPrompt.authenticate(
                promptInfo,
                BiometricPrompt.CryptoObject(signature)
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to show biometric prompt: ${e.message}")
            continuation.resume(false)
        }
    }
    
    /**
     * Sign a challenge using BiometricPrompt with CryptoObject.
     * Returns base64 signature or null if cancelled/failed.
     */
    private suspend fun signChallengeWithBiometric(
        activity: FragmentActivity,
        deviceId: String,
        challenge: String
    ): String? = suspendCancellableCoroutine { continuation ->
        
        val signature = BiometricKeyManager.getSignatureForSigning(deviceId)
        if (signature == null) {
            Log.e(TAG, "❌ Failed to get signature object")
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        
        val executor = ContextCompat.getMainExecutor(context)
        
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                Log.d(TAG, "✅ Biometric authentication succeeded")
                
                val authenticatedSignature = result.cryptoObject?.signature
                if (authenticatedSignature == null) {
                    Log.e(TAG, "❌ No signature in crypto object")
                    continuation.resume(null)
                    return
                }
                
                // Sign the challenge
                val signResult = BiometricKeyManager.signData(
                    authenticatedSignature,
                    challenge.toByteArray(Charsets.UTF_8)
                )
                
                continuation.resume(signResult.getOrNull())
            }
            
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Log.e(TAG, "❌ Biometric error: $errorCode - $errString")
                continuation.resume(null)
            }
            
            override fun onAuthenticationFailed() {
                Log.w(TAG, "⚠️ Biometric failed (wrong finger/face)")
                // Don't resume - let user try again
            }
        }
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Vault")
            .setSubtitle("Use your fingerprint or face to authenticate")
            .setNegativeButtonText("Use Password")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        
        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        
        try {
            biometricPrompt.authenticate(
                promptInfo,
                BiometricPrompt.CryptoObject(signature)
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to show biometric prompt: ${e.message}")
            continuation.resume(null)
        }
    }
    
    /**
     * Get default device name from system.
     */
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
