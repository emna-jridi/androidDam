package tn.esprit.dam.features.vault.data.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

object VaultCrypto {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "shadowguard_vault_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val IV_SIZE = 12

    fun deriveKeyFromPassword(masterPassword: String, salt: String): SecretKey {
        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = javax.crypto.spec.PBEKeySpec(
            masterPassword.toCharArray(),
            salt.toByteArray(),
            100_000,
            256
        )
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    fun encrypt(plaintext: String, key: SecretKey): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = ByteArray(IV_SIZE)
        Random.Default.nextBytes(iv)

        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)

        val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decrypt(encryptedBase64: String, key: SecretKey): String {
        val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)

        val iv = combined.sliceArray(0 until IV_SIZE)
        val encrypted = combined.sliceArray(IV_SIZE until combined.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted, Charsets.UTF_8)
    }

    fun generatePassword(
        length: Int = 16,
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowercase = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"

        var charset = ""
        if (includeUppercase) charset += uppercase
        if (includeLowercase) charset += lowercase
        if (includeNumbers) charset += numbers
        if (includeSymbols) charset += symbols

        if (charset.isEmpty()) charset = lowercase

        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }

    fun generatePassphrase(wordCount: Int = 4): String {
        val words = listOf(
            "shadow", "guard", "vault", "secure", "protect", "shield",
            "cipher", "crypt", "key", "lock", "safe", "trust",
            "defend", "armor", "fortress", "castle", "tower", "wall",
            "dragon", "phoenix", "eagle", "lion", "tiger", "wolf",
            "ocean", "mountain", "forest", "river", "storm", "thunder"
        )

        return (1..wordCount)
            .map { words.random().replaceFirstChar { it.uppercase() } }
            .joinToString("-")
    }

    fun hashMasterPassword(masterPassword: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(masterPassword.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    fun storeKeyInKeystore(keyData: ByteArray): Boolean {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )

            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationValidityDurationSeconds(30)
                .build()

            keyGenerator.init(spec)
            keyGenerator.generateKey()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getKeyFromKeystore(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearKey(key: SecretKey?) {
        key?.encoded?.fill(0)
    }
}
