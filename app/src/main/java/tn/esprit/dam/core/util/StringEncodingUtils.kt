package tn.esprit.dam.core.util

import java.nio.charset.StandardCharsets

fun String?.fixEncoding(): String {
    if (this == null || this.isEmpty()) return this ?: ""

    return try {
        val bytes = this.toByteArray(StandardCharsets.ISO_8859_1)
        String(bytes, StandardCharsets.UTF_8)
    } catch (e: Exception) {
        this
    }
}

fun String?.sanitizeForUI(): String {
    if (this == null) return ""

    val fixed = this.fixEncoding()

    var result = fixed

    val replacements = mapOf(
        "ÃƒÂ©" to "é",
        "ÃƒÂ¨" to "è",
        "ÃƒÂ°" to "ð",
        "ÃƒÂ¬" to "ì",
        "ÃƒÂ¢" to "â",
        "ÃƒÂ§" to "ç",
        "ÃƒÂ±" to "ñ",
        "Ã©" to "é",
        "Ã¨" to "è",
        "Ã§" to "ç",
        "Ã¢" to "â",
        "Ã " to "à",
        "Ã„" to "",
        "Ã‚" to "",
        "Ã‚ " to " ",
        "â€¢" to "•",
        "â€œ" to "\"",
        "â€" to "\"",
        "â€" to "—",
        "â€" to "–",
    )

    for ((corrupted, correct) in replacements) {
        result = result.replace(corrupted, correct)
    }

    return result.trim()
}

fun List<String>?.sanitizeAll(): List<String> {
    return this?.map { it.sanitizeForUI() } ?: emptyList()
}

fun <K> Map<K, String>?.sanitizeAll(): Map<K, String> {
    return this?.mapValues { (_, value) -> value.sanitizeForUI() } ?: emptyMap()
}
