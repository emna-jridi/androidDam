package tn.esprit.dam.features.vault.domain.password

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object PasswordPatternDetector {
    data class PatternWarning(
        val type: PatternType,
        val message: String,
        val severity: Severity
    )

    enum class PatternType {
        DATE_PATTERN,
        SEQUENTIAL,
        REPEATED_CHARS,
        COMMON_SUBSTITUTION,
        KEYBOARD_PATTERN,
        COMMON_WORD
    }

    enum class Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    private val commonWords = setOf(
        "password", "admin", "user", "login", "welcome",
        "shadow", "guard", "secure", "vault", "master"
    )

    private val keyboardPatterns = listOf(
        "qwerty", "asdfgh", "zxcvbn", "qwertz",
        "azerty", "12345", "098765"
    )

    fun detectPatterns(password: String): List<PatternWarning> {
        val warnings = mutableListOf<PatternWarning>()
        val lower = password.lowercase()

        if (containsDatePattern(password)) {
            warnings.add(
                PatternWarning(
                    PatternType.DATE_PATTERN,
                    "Contains date pattern (e.g., birthdate). Easily guessable.",
                    Severity.HIGH
                )
            )
        }

        if (containsSequentialPattern(lower)) {
            warnings.add(
                PatternWarning(
                    PatternType.SEQUENTIAL,
                    "Contains sequential characters (abc, 123). Very predictable.",
                    Severity.CRITICAL
                )
            )
        }

        if (containsRepeatedChars(password)) {
            warnings.add(
                PatternWarning(
                    PatternType.REPEATED_CHARS,
                    "Contains repeated characters (aaa, 111). Reduces entropy.",
                    Severity.MEDIUM
                )
            )
        }

        if (containsCommonSubstitution(lower)) {
            warnings.add(
                PatternWarning(
                    PatternType.COMMON_SUBSTITUTION,
                    "Uses common substitutions (@ for a, 3 for e). Attackers know these.",
                    Severity.MEDIUM
                )
            )
        }

        if (containsKeyboardPattern(lower)) {
            warnings.add(
                PatternWarning(
                    PatternType.KEYBOARD_PATTERN,
                    "Contains keyboard pattern (qwerty, asdf). First tried by attackers.",
                    Severity.CRITICAL
                )
            )
        }

        if (containsCommonWord(lower)) {
            warnings.add(
                PatternWarning(
                    PatternType.COMMON_WORD,
                    "Contains common word. Use unrelated random words instead.",
                    Severity.HIGH
                )
            )
        }

        return warnings
    }

    fun generateSmartRecommendation(password: String, warnings: List<PatternWarning>): String {
        if (warnings.isEmpty()) {
            return "✓ Excellent! No predictable patterns detected. Your password looks strong."
        }

        val criticalCount = warnings.count { it.severity == Severity.CRITICAL }
        val highCount = warnings.count { it.severity == Severity.HIGH }

        return buildString {
            append("⚠️ Security Alert: ")
            when {
                criticalCount > 0 -> append("$criticalCount critical weakness(es) found. ")
                highCount > 0 -> append("$highCount high-risk pattern(s) detected. ")
                else -> append("${warnings.size} improvement(s) suggested. ")
            }

            appendLine()
            appendLine()
            append("💡 Smart Fix: ")
            when {
                warnings.any { it.type == PatternType.KEYBOARD_PATTERN } ->
                    append("Replace keyboard sequences with random characters. Try the generator!")

                warnings.any { it.type == PatternType.DATE_PATTERN } ->
                    append("Avoid personal dates. Use our passphrase generator for memorable but random passwords.")

                warnings.any { it.type == PatternType.COMMON_WORD } ->
                    append("Combine 4-5 random unrelated words instead of common terms.")

                else ->
                    append("Use our password generator with all character types enabled.")
            }
        }
    }

    private fun containsDatePattern(password: String): Boolean {
        val datePatterns = listOf(
            Regex("19\\d{2}|20\\d{2}"),
            Regex("(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])"),
            Regex("(0[1-9]|[12]\\d|3[01])(0[1-9]|1[0-2])"),
            Regex("\\d{4}[-/]\\d{2}[-/]\\d{2}")
        )
        return datePatterns.any { it.containsMatchIn(password) }
    }

    private fun containsSequentialPattern(text: String): Boolean {
        for (i in 0 until text.length - 2) {
            val c1 = text[i].code
            val c2 = text[i + 1].code
            val c3 = text[i + 2].code

            if (c2 == c1 + 1 && c3 == c2 + 1) return true
            if (c2 == c1 - 1 && c3 == c2 - 1) return true
        }
        return false
    }

    private fun containsRepeatedChars(text: String): Boolean {
        return Regex("(.)\\1{2,}").containsMatchIn(text)
    }

    private fun containsCommonSubstitution(text: String): Boolean {
        val reversed = text
            .replace("@", "a")
            .replace("3", "e")
            .replace("1", "i")
            .replace("0", "o")
            .replace("5", "s")
            .replace("7", "t")
            .replace("$", "s")

        return commonWords.any { reversed.contains(it) }
    }

    private fun containsKeyboardPattern(text: String): Boolean {
        return keyboardPatterns.any { text.contains(it) }
    }

    private fun containsCommonWord(text: String): Boolean {
        return commonWords.any { text.contains(it) }
    }
}
