package tn.esprit.dam.utils

import java.nio.charset.StandardCharsets

/**
 * UTF-8 String Encoding Utilities
 * Fixes encoding issues from backend responses and corrupted text
 */

/**
 * Fixes encoding issues when text is decoded as ISO-8859-1 instead of UTF-8
 */
fun String?.fixEncoding(): String {
    if (this == null || this.isEmpty()) return this ?: ""
    
    return try {
        val bytes = this.toByteArray(StandardCharsets.ISO_8859_1)
        String(bytes, StandardCharsets.UTF_8)
    } catch (e: Exception) {
        this
    }
}

/**
 * Sanitizes text for UI display
 */
fun String?.sanitizeForUI(): String {
    if (this == null) return ""
    
    val fixed = this.fixEncoding()
    
    var result = fixed
    
    // Replace common encoding corruption patterns
    val replacements = mapOf(
        "Ã©" to "é",
        "Ã¨" to "è",
        "Ã°" to "ð",
        "Ã¬" to "ì",
        "Ã¢" to "â",
        "Ã§" to "ç",
        "Ã±" to "ñ",
        "Â" to "",
        "Â " to " "
    )
    
    for ((corrupted, correct) in replacements) {
        result = result.replace(corrupted, correct)
    }
    
    return result.trim()
}

/**
 * Extension function to fix encoding
 */
fun String.fixEncodingExt(): String = this.fixEncoding()

/**
 * Extension function to sanitize for UI
 */
fun String.sanitizeForUIExt(): String = this.sanitizeForUI()

/**
 * Batch sanitize a list of strings
 */
fun List<String>?.sanitizeAll(): List<String> {
    return this?.map { it.sanitizeForUI() } ?: emptyList()
}

/**
 * Sanitize a map of strings
 */
fun <K> Map<K, String>?.sanitizeAll(): Map<K, String> {
    return this?.mapValues { (_, value) -> value.sanitizeForUI() } ?: emptyMap()
}
