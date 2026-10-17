package tn.esprit.dam.features.vault.data.crypto

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the vault key wrapped by a Keystore key that only works after a strong biometric
 * check (one BiometricPrompt per use). Only the wrapped bytes are stored; enrolling a new
 * fingerprint invalidates the Keystore key, which turns biometric unlock off.
 */
@Singleton
class VaultBiometricStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "shadowguard_vault_biometric_wrap"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH = 128
        const val PREFS_NAME = "vault_biometric"
        const val PREF_WRAPPED_KEY = "wrapped_key"
        const val PREF_IV = "iv"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(): Boolean = prefs.contains(PREF_WRAPPED_KEY) && keyStore().containsAlias(KEY_ALIAS)

    /** Cipher to hand to BiometricPrompt for turning biometric unlock on. */
    fun encryptCipher(): Cipher? = try {
        Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, getOrCreateKey()) }
    } catch (e: KeyPermanentlyInvalidatedException) {
        // Stale key from an older enrollment: start over with a fresh one
        clear()
        runCatching {
            Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, getOrCreateKey()) }
        }.getOrNull()
    } catch (e: Exception) {
        Timber.w(e, "VaultBiometricStore.encryptCipher failed")
        null
    }

    /** Cipher to hand to BiometricPrompt for unlocking; null if biometrics changed or it's off. */
    fun decryptCipher(): Cipher? {
        val iv = prefs.getString(PREF_IV, null) ?: return null
        return try {
            val key = keyStore().getKey(KEY_ALIAS, null) as? SecretKey ?: return null
            Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, Base64.decode(iv, Base64.NO_WRAP)))
            }
        } catch (e: KeyPermanentlyInvalidatedException) {
            clear()
            null
        } catch (e: Exception) {
            Timber.w(e, "VaultBiometricStore.decryptCipher failed")
            null
        }
    }

    /** [cipher] must come back authenticated from BiometricPrompt. */
    fun save(cipher: Cipher, vaultKey: ByteArray) {
        val wrapped = cipher.doFinal(vaultKey)
        prefs.edit()
            .putString(PREF_WRAPPED_KEY, Base64.encodeToString(wrapped, Base64.NO_WRAP))
            .putString(PREF_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .apply()
    }

    /** [cipher] must come back authenticated from BiometricPrompt. */
    fun read(cipher: Cipher): ByteArray? {
        val wrapped = prefs.getString(PREF_WRAPPED_KEY, null) ?: return null
        return cipher.doFinal(Base64.decode(wrapped, Base64.NO_WRAP))
    }

    fun clear() {
        prefs.edit().clear().apply()
        runCatching { keyStore().deleteEntry(KEY_ALIAS) }
    }

    private fun keyStore(): KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private fun getOrCreateKey(): SecretKey {
        (keyStore().getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).apply {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setKeySize(256)
            setUserAuthenticationRequired(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
            } else {
                @Suppress("DEPRECATION")
                setUserAuthenticationValidityDurationSeconds(-1)
            }
            setInvalidatedByBiometricEnrollment(true)
        }.build()

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            .apply { init(spec) }
            .generateKey()
    }
}
