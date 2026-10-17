package tn.esprit.dam.features.darkweb.presentation

import java.text.SimpleDateFormat
import java.util.Locale

/** Returns null when there is no date, so the caller can show its own "unknown date" text. */
internal fun formatBreachDate(isoDate: String?, outputPattern: String): String? {
    if (isoDate.isNullOrBlank()) return null
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val outputFormat = SimpleDateFormat(outputPattern, Locale.getDefault())
        val cleanDate = isoDate.replace("Z", "").substringBefore(".")
        val date = inputFormat.parse(cleanDate)
        date?.let { outputFormat.format(it) } ?: isoDate.substringBefore("T")
    } catch (e: Exception) {
        isoDate.substringBefore("T")
    }
}
