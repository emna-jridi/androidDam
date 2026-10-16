package tn.esprit.dam.features.darkweb.presentation

import java.text.SimpleDateFormat
import java.util.Locale

internal fun formatBreachDate(isoDate: String?, outputPattern: String): String {
    if (isoDate.isNullOrBlank()) return "Unknown date"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat(outputPattern, Locale.getDefault())
        val cleanDate = isoDate.replace("Z", "").substringBefore(".")
        val date = inputFormat.parse(cleanDate)
        date?.let { outputFormat.format(it) } ?: isoDate.substringBefore("T")
    } catch (e: Exception) {
        isoDate.substringBefore("T")
    }
}
