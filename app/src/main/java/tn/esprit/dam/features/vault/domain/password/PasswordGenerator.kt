package tn.esprit.dam.features.vault.domain.password

import java.security.SecureRandom

/** The app's only password/passphrase generator. Uses [SecureRandom]: these are real secrets. */
object PasswordGenerator {
    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val NUMBERS = "0123456789"
    private const val SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?"

    private val WORD_LIST = listOf(
        "apple", "brave", "crane", "drift", "eagle", "forest", "ghost", "harbor", "island", "jump",
        "kite", "lunar", "mango", "noble", "ocean", "piano", "quiet", "river", "solar", "tiger",
        "unity", "vivid", "whale", "xenon", "yacht", "zebra", "amber", "blue", "coral", "dawn",
        "echo", "flame", "gold", "halo", "iron", "jade", "karma", "leaf", "moss", "navy",
        "opal", "pearl", "ruby", "sand", "teal", "urban", "vault", "wind", "zinc", "bolt"
    )

    private val random = SecureRandom()

    /** Always contains at least one character from every enabled group. */
    fun generatePassword(
        length: Int = 16,
        useUppercase: Boolean = true,
        useNumbers: Boolean = true,
        useSymbols: Boolean = true
    ): String {
        val groups = buildList {
            add(LOWERCASE)
            if (useUppercase) add(UPPERCASE)
            if (useNumbers) add(NUMBERS)
            if (useSymbols) add(SYMBOLS)
        }
        val pool = groups.joinToString("")
        val size = length.coerceAtLeast(groups.size)

        val chars = groups.map { it.randomChar() } + List(size - groups.size) { pool.randomChar() }
        return chars.shuffled(random).joinToString("")
    }

    fun generatePassphrase(
        wordCount: Int = 4,
        separator: String = "-",
        capitalize: Boolean = true
    ): String {
        return List(wordCount) { WORD_LIST[random.nextInt(WORD_LIST.size)] }
            .map { if (capitalize) it.replaceFirstChar { c -> c.uppercase() } else it }
            .joinToString(separator)
    }

    private fun String.randomChar(): Char = this[random.nextInt(length)]
}
