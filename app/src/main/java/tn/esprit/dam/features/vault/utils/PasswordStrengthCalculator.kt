package tn.esprit.dam.features.vault.utils

import kotlin.math.ln
import kotlin.math.pow
import kotlinx.serialization.Serializable

@Serializable
enum class PasswordRiskLevel {
    WEAK, MEDIUM, STRONG, VERY_STRONG
}

@Serializable
data class PasswordAnalysisMetrics(
    val score: Int, // 0-100
    val riskLevel: PasswordRiskLevel,
    val length: Int,
    val entropy: Double,
    val estimatedCrackTime: String,
    val composition: List<String>,
    val issues: List<String>
)

object PasswordStrengthCalculator {

    fun analyze(password: String): PasswordAnalysisMetrics {
        if (password.isEmpty()) return emptyMetrics()

        val length = password.length
        val charSetSize = calculateCharSetSize(password)
        val entropy = length * (ln(charSetSize.toDouble()) / ln(2.0))
        
        val (score, risk) = calculateScoreAndRisk(entropy, length, password)
        val crackTime = estimateCrackTime(entropy)
        val composition = getComposition(password)
        val issues = findIssues(password)

        return PasswordAnalysisMetrics(
            score = score,
            riskLevel = risk,
            length = length,
            entropy = (entropy * 10).toInt() / 10.0, // round to 1 decimal
            estimatedCrackTime = crackTime,
            composition = composition,
            issues = issues
        )
    }

    private fun calculateCharSetSize(password: String): Int {
        var size = 0
        if (password.any { it.isLowerCase() }) size += 26
        if (password.any { it.isUpperCase() }) size += 26
        if (password.any { it.isDigit() }) size += 10
        if (password.any { !it.isLetterOrDigit() }) size += 32
        return if (size == 0) 1 else size
    }

    private fun calculateScoreAndRisk(entropy: Double, length: Int, password: String): Pair<Int, PasswordRiskLevel> {
        // Base score on entropy
        var score = (entropy * 1.5).toInt() // Rough mapping: 60 bits -> 90 score
        
        // Bonus/Penalty
        if (length < 8) score -= 20
        if (length > 16) score += 10
        if (password.toLowerCase() == password || password.toUpperCase() == password) score -= 10
        if (password.all { it.isLetterOrDigit() }) score -= 5 // No symbols

        score = score.coerceIn(0, 100)

        val risk = when {
            score < 40 -> PasswordRiskLevel.WEAK
            score < 60 -> PasswordRiskLevel.MEDIUM
            score < 80 -> PasswordRiskLevel.STRONG
            else -> PasswordRiskLevel.VERY_STRONG
        }
        return score to risk
    }

    private fun estimateCrackTime(entropy: Double): String {
        // Assume 1 trillion guesses per second (optimistic for attacker)
        val guesses = 2.0.pow(entropy)
        val seconds = guesses / 1_000_000_000_000.0
        
        return when {
            seconds < 1 -> "Instantly"
            seconds < 60 -> "Few seconds"
            seconds < 3600 -> "${(seconds / 60).toInt()} minutes"
            seconds < 86400 -> "${(seconds / 3600).toInt()} hours"
            seconds < 31536000 -> "${(seconds / 86400).toInt()} days"
            seconds < 31536000 * 100 -> "${(seconds / 31536000).toInt()} years"
            else -> "Centuries"
        }
    }

    private fun getComposition(password: String): List<String> {
        val list = mutableListOf<String>()
        if (password.any { it.isLowerCase() }) list.add("Lowercase")
        if (password.any { it.isUpperCase() }) list.add("Uppercase")
        if (password.any { it.isDigit() }) list.add("Numbers")
        if (password.any { !it.isLetterOrDigit() }) list.add("Symbols")
        return list
    }

    private fun findIssues(password: String): List<String> {
        val issues = mutableListOf<String>()
        if (password.length < 12) issues.add("Short length (< 12 chars)")
        if (!password.any { !it.isLetterOrDigit() }) issues.add("No special characters")
        if (password.any { it.isDigit() } && !password.any { it.isLetter() }) issues.add("Numbers only")
        if (password.any { it.isLetter() } && !password.any { it.isDigit() }) issues.add("Letters only")
        // Basic pattern check
        if (listOf("123", "abc", "qwerty", "password").any { password.contains(it, ignoreCase = true) }) {
            issues.add("Contains common pattern")
        }
        return issues
    }

    private fun emptyMetrics() = PasswordAnalysisMetrics(0, PasswordRiskLevel.WEAK, 0, 0.0, "N/A", emptyList(), emptyList())
}
