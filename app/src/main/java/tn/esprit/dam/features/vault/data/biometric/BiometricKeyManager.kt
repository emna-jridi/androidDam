package tn.esprit.dam.features.vault.data.biometric

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import javax.crypto.Cipher

object BiometricKeyManager {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS_PREFIX = "biometric_auth_key_"
    private const val SIGNATURE_ALGORITHM = "SHA256withECDSA"

    data class KeyGenerationResult(
        val publicKeyPem: String,
        val keyAlias: String,
        val keyType: String = "EC"
    )

    fun generateKeyPair(deviceId: String): Result<KeyGenerationResult> {
        return try {
            val keyAlias = getKeyAlias(deviceId)

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

                setUserAuthenticationRequired(true)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setUserAuthenticationParameters(
                        0,
                        KeyProperties.AUTH_BIOMETRIC_STRONG
                    )
                } else {
                    @Suppress("DEPRECATION")
                    setUserAuthenticationValidityDurationSeconds(-1)
                }

                setInvalidatedByBiometricEnrollment(true)
            }

            keyPairGenerator.initialize(builder.build())
            val keyPair = keyPairGenerator.generateKeyPair()

            val publicKeyPem = convertPublicKeyToPem(keyPair.public)

            Result.success(KeyGenerationResult(
                publicKeyPem = publicKeyPem,
                keyAlias = keyAlias,
                keyType = "EC"
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPrivateKey(deviceId: String): PrivateKey? {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)

            val keyAlias = getKeyAlias(deviceId)
            keyStore.getKey(keyAlias, null) as? PrivateKey
        } catch (e: Exception) {
            null
        }
    }

    fun getSignatureForSigning(deviceId: String): Signature? {
        return try {
            val privateKey = getPrivateKey(deviceId) ?: return null

            val signature = Signature.getInstance(SIGNATURE_ALGORITHM)
            signature.initSign(privateKey)
            signature
        } catch (e: Exception) {
            null
        }
    }

    fun signData(authenticatedSignature: Signature, data: ByteArray): Result<String> {
        return try {
            authenticatedSignature.update(data)
            val signatureBytes = authenticatedSignature.sign()
            val base64Signature = Base64.encodeToString(signatureBytes, Base64.NO_WRAP)

            Result.success(base64Signature)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun hasKey(deviceId: String): Boolean {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            keyStore.containsAlias(getKeyAlias(deviceId))
        } catch (e: Exception) {
            false
        }
    }

    fun deleteKey(deviceId: String): Boolean {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)

            val keyAlias = getKeyAlias(deviceId)
            if (keyStore.containsAlias(keyAlias)) {
                keyStore.deleteEntry(keyAlias)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

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

            true
        } catch (e: Exception) {
            false
        }
    }

    fun getPublicKeyPem(deviceId: String): String? {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)

            val keyAlias = getKeyAlias(deviceId)
            val certificate = keyStore.getCertificate(keyAlias)
            val publicKey = certificate?.publicKey ?: return null

            convertPublicKeyToPem(publicKey)
        } catch (e: Exception) {
            null
        }
    }

    private fun convertPublicKeyToPem(publicKey: PublicKey): String {
        val encoded = publicKey.encoded
        val base64 = Base64.encodeToString(encoded, Base64.NO_WRAP)

        return buildString {
            append("-----BEGIN PUBLIC KEY-----\n")
            base64.chunked(64).forEach { line ->
                append(line)
                append("\n")
            }
            append("-----END PUBLIC KEY-----")
        }
    }

    private fun getKeyAlias(deviceId: String): String {
        return KEY_ALIAS_PREFIX + deviceId
    }

    fun getDeviceId(context: android.content.Context): String {
        return android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: java.util.UUID.randomUUID().toString()
    }
}
