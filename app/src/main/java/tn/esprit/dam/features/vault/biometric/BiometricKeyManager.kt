package tn.esprit.dam.features.vault.biometric

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import javax.crypto.Cipher

/**
 * BiometricKeyManager handles cryptographic key operations for biometric authentication.
 * 
 * Uses Android Keystore to generate and store EC P-256 keys that require biometric
 * authentication for use. The public key is exported and registered with the backend,
 * while the private key never leaves the secure hardware.
 * 
 * Security Features:
 * - Keys are hardware-backed on supported devices
 * - Private key operations require biometric authentication
 * - Keys are invalidated if biometrics are changed
 * - Uses EC P-256 (secp256r1) for strong security
 */
object BiometricKeyManager {
    
    private const val TAG = "BiometricKeyManager"
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS_PREFIX = "biometric_auth_key_"
    private const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
    
    /**
     * Result of key generation containing the public key in PEM format
     */
    data class KeyGenerationResult(
        val publicKeyPem: String,
        val keyAlias: String,
        val keyType: String = "EC"
    )
    
    /**
     * Generate a new EC P-256 key pair in the Android Keystore.
     * The key requires biometric authentication for signing operations.
     * 
     * @param deviceId Unique device identifier to create a unique key alias
     * @return KeyGenerationResult containing the public key in PEM format
     */
    fun generateKeyPair(deviceId: String): Result<KeyGenerationResult> {
        return try {
            val keyAlias = getKeyAlias(deviceId)
            
            // Delete existing key if present
            deleteKey(deviceId)
            
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                KEYSTORE_PROVIDER
            )
            
            val builder = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            ).apply {
                setDigests(KeyProperties.DIGEST_SHA256)
                setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                
                // Require user authentication (biometric)
                setUserAuthenticationRequired(true)
                
                // Configure authentication parameters based on Android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setUserAuthenticationParameters(
                        0, // 0 = require authentication for every use
                        KeyProperties.AUTH_BIOMETRIC_STRONG
                    )
                } else {
                    @Suppress("DEPRECATION")
                    setUserAuthenticationValidityDurationSeconds(-1) // Every use
                }
                
                // Invalidate key if biometrics are changed
                setInvalidatedByBiometricEnrollment(true)
            }
            
            keyPairGenerator.initialize(builder.build())
            val keyPair = keyPairGenerator.generateKeyPair()
            
            val publicKeyPem = convertPublicKeyToPem(keyPair.public)
            
            Log.d(TAG, "✅ Key pair generated successfully for device: $deviceId")
            Log.d(TAG, "📝 Public key (first 50 chars): ${publicKeyPem.take(50)}...")
            
            Result.success(KeyGenerationResult(
                publicKeyPem = publicKeyPem,
                keyAlias = keyAlias,
                keyType = "EC"
            ))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to generate key pair: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get the private key for signing operations.
     * This must be used with BiometricPrompt's CryptoObject.
     * 
     * @param deviceId Device identifier
     * @return PrivateKey or null if not found
     */
    fun getPrivateKey(deviceId: String): PrivateKey? {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            
            val keyAlias = getKeyAlias(deviceId)
            keyStore.getKey(keyAlias, null) as? PrivateKey
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get private key: ${e.message}", e)
            null
        }
    }
    
    /**
     * Get a Signature object initialized for signing.
     * This should be wrapped in BiometricPrompt.CryptoObject for biometric auth.
     * 
     * @param deviceId Device identifier
     * @return Signature object ready for signing, or null if key not found
     */
    fun getSignatureForSigning(deviceId: String): Signature? {
        return try {
            val privateKey = getPrivateKey(deviceId) ?: return null
            
            val signature = Signature.getInstance(SIGNATURE_ALGORITHM)
            signature.initSign(privateKey)
            signature
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to create signature object: ${e.message}", e)
            null
        }
    }
    
    /**
     * Sign data using the biometrically-protected key.
     * This should only be called AFTER BiometricPrompt authentication succeeds.
     * 
     * @param authenticatedSignature The Signature object from CryptoObject after auth
     * @param data The data to sign (challenge from server)
     * @return Base64-encoded signature
     */
    fun signData(authenticatedSignature: Signature, data: ByteArray): Result<String> {
        return try {
            authenticatedSignature.update(data)
            val signatureBytes = authenticatedSignature.sign()
            val base64Signature = Base64.encodeToString(signatureBytes, Base64.NO_WRAP)
            
            Log.d(TAG, "✅ Data signed successfully")
            Result.success(base64Signature)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to sign data: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if a biometric key exists for the given device.
     * 
     * @param deviceId Device identifier
     * @return true if key exists
     */
    fun hasKey(deviceId: String): Boolean {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            keyStore.containsAlias(getKeyAlias(deviceId))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to check key existence: ${e.message}", e)
            false
        }
    }
    
    /**
     * Delete the biometric key for a device.
     * 
     * @param deviceId Device identifier
     * @return true if deleted successfully
     */
    fun deleteKey(deviceId: String): Boolean {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            
            val keyAlias = getKeyAlias(deviceId)
            if (keyStore.containsAlias(keyAlias)) {
                keyStore.deleteEntry(keyAlias)
                Log.d(TAG, "🗑️ Deleted key for device: $deviceId")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to delete key: ${e.message}", e)
            false
        }
    }
    
    /**
     * Delete all biometric keys from this app.
     * Useful for account logout or app reset.
     */
    fun deleteAllKeys(): Boolean {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            
            val aliases = keyStore.aliases().toList()
            var deletedCount = 0
            
            for (alias in aliases) {
                if (alias.startsWith(KEY_ALIAS_PREFIX)) {
                    keyStore.deleteEntry(alias)
                    deletedCount++
                }
            }
            
            Log.d(TAG, "🗑️ Deleted $deletedCount biometric keys")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to delete all keys: ${e.message}", e)
            false
        }
    }
    
    /**
     * Get the public key PEM for an existing key.
     * 
     * @param deviceId Device identifier
     * @return Public key in PEM format, or null if not found
     */
    fun getPublicKeyPem(deviceId: String): String? {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            
            val keyAlias = getKeyAlias(deviceId)
            val certificate = keyStore.getCertificate(keyAlias)
            val publicKey = certificate?.publicKey ?: return null
            
            convertPublicKeyToPem(publicKey)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to get public key: ${e.message}", e)
            null
        }
    }
    
    /**
     * Convert a PublicKey to PEM format.
     */
    private fun convertPublicKeyToPem(publicKey: PublicKey): String {
        val encoded = publicKey.encoded
        val base64 = Base64.encodeToString(encoded, Base64.NO_WRAP)
        
        return buildString {
            append("-----BEGIN PUBLIC KEY-----\n")
            // Split into 64-character lines
            base64.chunked(64).forEach { line ->
                append(line)
                append("\n")
            }
            append("-----END PUBLIC KEY-----")
        }
    }
    
    /**
     * Get the key alias for a device.
     */
    private fun getKeyAlias(deviceId: String): String {
        return KEY_ALIAS_PREFIX + deviceId
    }
    
    /**
     * Get device ID from Settings.Secure.ANDROID_ID.
     * This should be called from a context-aware location.
     */
    fun getDeviceId(context: android.content.Context): String {
        return android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: java.util.UUID.randomUUID().toString()
    }
}
