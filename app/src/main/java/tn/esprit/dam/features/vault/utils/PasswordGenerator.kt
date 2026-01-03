package tn.esprit.dam.features.vault.utils

import kotlin.random.Random

object PasswordGenerator {

    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val NUMBERS = "0123456789"
    private const val SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?"

    // Simple word list for passphrases (truncated for brevity, real implementation would be larger)
    private val WORD_LIST = listOf(
        "apple", "brave", "crane", "drift", "eagle", "forest", "ghost", "harbor", "island", "jump",
        "kite", "lunar", "mango", "noble", "ocean", "piano", "quiet", "river", "solar", "tiger",
        "unity", "vivid", "whale", "xenon", "yacht", "zebra", "amber", "blue", "coral", "dawn",
        "echo", "flame", "gold", "halo", "iron", "jade", "karma", "leaf", "moss", "navy",
        "opal", "pearl", "ruby", "sand", "teal", "urban", "vault", "wind", "zinc", "bolt"
    )

    fun generatePassword(
        length: Int = 16,
        useUppercase: Boolean = true,
        useNumbers: Boolean = true,
        useSymbols: Boolean = true
    ): String {
        val charPool = StringBuilder(LOWERCASE)
        if (useUppercase) charPool.append(UPPERCASE)
        if (useNumbers) charPool.append(NUMBERS)
        if (useSymbols) charPool.append(SYMBOLS)
        
        return (1..length)
            .map { Random.nextInt(0, charPool.length) }
            .map(charPool::get)
            .joinToString("")
    }

    fun generatePassphrase(
        wordCount: Int = 4,
        separator: String = "-",
        capitalize: Boolean = true
    ): String {
        return (1..wordCount)
            .map { WORD_LIST.random() }
            .map { if (capitalize) it.replaceFirstChar { c -> c.uppercase() } else it }
            .joinToString(separator)
    }
}
