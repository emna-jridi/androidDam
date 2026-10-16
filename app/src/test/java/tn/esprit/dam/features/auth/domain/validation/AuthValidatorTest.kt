package tn.esprit.dam.features.auth.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun `email rules`() {
        assertEquals("Email requis", AuthValidator.validateEmail("  "))
        assertEquals("Email invalide", AuthValidator.validateEmail("not-an-email"))
        assertEquals("Email invalide", AuthValidator.validateEmail("a@b"))
        assertNull(AuthValidator.validateEmail(" emna.jridi@esen.tn "))
        assertNull(AuthValidator.validateEmail("first.last+tag@sub.domain.com"))
    }

    @Test
    fun `login password only checks length`() {
        assertEquals("Mot de passe requis", AuthValidator.validateLoginPassword(""))
        assertEquals("Minimum 6 caractères", AuthValidator.validateLoginPassword("abc12"))
        assertNull(AuthValidator.validateLoginPassword("abcdef"))
    }

    @Test
    fun `new password needs a letter and a digit`() {
        assertEquals("Doit contenir au moins un chiffre", AuthValidator.validateNewPassword("abcdefg"))
        assertEquals("Doit contenir au moins une lettre", AuthValidator.validateNewPassword("1234567"))
        assertNull(AuthValidator.validateNewPassword("abc123"))
    }

    @Test
    fun `confirmation and name`() {
        assertEquals("Confirmation requise", AuthValidator.validatePasswordConfirmation("abc123", ""))
        assertEquals("Les mots de passe ne correspondent pas", AuthValidator.validatePasswordConfirmation("abc123", "abc124"))
        assertNull(AuthValidator.validatePasswordConfirmation("abc123", "abc123"))
        assertEquals("Nom trop court", AuthValidator.validateName(" a "))
        assertNull(AuthValidator.validateName("Emna"))
    }

    @Test
    fun `otp rules`() {
        assertNull(AuthValidator.validateOtp("123456"))
        assertEquals("Code à 6 chiffres requis", AuthValidator.validateOtp("12345"))
        assertEquals("Code à 6 chiffres requis", AuthValidator.validateOtp("12345a"))
        assertTrue(AuthValidator.isOtpInput("123"))
        assertFalse(AuthValidator.isOtpInput("1234567"))
        assertFalse(AuthValidator.isOtpInput("12a"))
    }
}
