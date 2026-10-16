package tn.esprit.dam.features.vault.domain.password

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordToolsTest {

    @Test
    fun `generated password has requested length and every enabled group`() {
        repeat(200) {
            val pwd = PasswordGenerator.generatePassword(length = 12)
            assertEquals(12, pwd.length)
            assertTrue(pwd.any { it.isLowerCase() })
            assertTrue(pwd.any { it.isUpperCase() })
            assertTrue(pwd.any { it.isDigit() })
            assertTrue(pwd.any { !it.isLetterOrDigit() })
        }
    }

    @Test
    fun `disabled groups never appear`() {
        repeat(200) {
            val pwd = PasswordGenerator.generatePassword(length = 20, useUppercase = false, useNumbers = false, useSymbols = false)
            assertTrue(pwd.all { it.isLowerCase() })
        }
    }

    @Test
    fun `passwords are not repeated`() {
        assertEquals(500, List(500) { PasswordGenerator.generatePassword() }.toSet().size)
    }

    @Test
    fun `passphrase has requested word count`() {
        val phrase = PasswordGenerator.generatePassphrase(wordCount = 5, separator = "_")
        assertEquals(5, phrase.split("_").size)
    }

    @Test
    fun `generated passwords score as strong`() {
        val metrics = PasswordStrengthCalculator.analyze(PasswordGenerator.generatePassword(length = 16))
        assertTrue(metrics.riskLevel >= PasswordRiskLevel.STRONG)
    }

    @Test
    fun `weak password scores weak`() {
        val metrics = PasswordStrengthCalculator.analyze("abc123")
        assertEquals(PasswordRiskLevel.WEAK, metrics.riskLevel)
        assertNotEquals(0, metrics.issues.size)
    }
}
