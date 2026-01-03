package tn.esprit.dam.utils

/**
 * Security scoring helper for user-friendly score presentation
 * 
 * Aligns with backend risk bands:
 * - low: 0-39
 * - medium: 40-69
 * - high: 70-84
 * - critical: 85-100
 * 
 * Philosophy:
 * - Avoid panic: Clamp minimum to 40 for realistic perspective
 * - No false perfection: Clamp maximum to 95
 * - Smooth display: Use backend score as primary input
 */
object SecurityScoring {
    
    /**
     * Backend risk bands (must match backend thresholds)
     */
    val riskBands = mapOf(
        "low" to 0..39,
        "medium" to 40..69,
        "high" to 70..84,
        "critical" to 85..100
    )
    
    /**
     * Compute user-friendly security score
     * 
     * @param rawScore Raw backend score (0-100)
     * @param appCount Number of apps analyzed (not used for smoothing; backend handles this)
     * @param dangerousPermCount Number of dangerous permissions
     * @param trackerCount Number of trackers detected
     * @param riskLevel Risk level from backend ("low", "medium", "high", "critical")
     * @return Adjusted score between 40-95
     */
    fun computeUserFriendlyScore(
        rawScore: Float,
        appCount: Int = 1,
        dangerousPermCount: Int = 0,
        trackerCount: Int = 0,
        riskLevel: String = "low"
    ): Int {
        // Backend score is authoritative; apply minimal smoothing
        val baseScore = rawScore.toInt().coerceIn(0, 100)
        
        // Only adjust if score contradicts risk level (safety check)
        val levelBand = riskBands[riskLevel.lowercase()] ?: riskBands["medium"]!!
        val adjustedScore = if (baseScore !in levelBand) {
            // Clamp to band minimum if out of range (safety correction)
            levelBand.first.coerceAtLeast(baseScore)
        } else {
            baseScore
        }
        
        // Clamp between 40 and 95 (realistic range, no panic, no false perfection)
        return adjustedScore.coerceIn(40, 95)
    }
    
    /**
     * Get security level description in French
     */
    fun getSecurityLevelText(score: Int): String {
        return when {
            score >= 80 -> "Bon niveau de sécurité"
            score >= 60 -> "Sécurité correcte"
            else -> "Sécurité à améliorer"
        }
    }
    
    /**
     * Get display label for risk level based on backend bands
     * @param score Score (0-100)
     * @return Risk level label aligned with backend bands
     */
    fun getDisplayRiskLabel(score: Int): String {
        return when (score) {
            in riskBands["critical"]!! -> "CRITIQUE"
            in riskBands["high"]!! -> "ÉLEVÉ"
            in riskBands["medium"]!! -> "MOYEN"
            in riskBands["low"]!! -> "FAIBLE"
            else -> "INCONNU"
        }
    }
    
    /**
     * Get reassuring risk level text (French)
     */
    fun getRiskLevelText(riskLevel: String): String {
        return when (riskLevel.lowercase()) {
            "critical" -> "Risque critique"
            "high" -> "Risque important"
            "medium" -> "Risque modéré"
            "low" -> "Risque limité"
            else -> "Risque inconnu"
        }
    }
    
    /**
     * Predefined security recommendations (French)
     */
    fun getDefaultRecommendations(): List<String> {
        return listOf(
            "Évitez d'accorder des permissions inutiles aux applications",
            "Désinstallez les applications que vous n'utilisez plus",
            "Vérifiez régulièrement les trackers publicitaires",
            "Mettez à jour vos applications pour corriger les failles de sécurité",
            "Évitez d'installer des applications provenant de sources inconnues",
            "Révoquez les permissions sensibles pour les applications peu utilisées"
        )
    }
    
    /**
     * Get top N recommendations based on context
     */
    fun getRelevantRecommendations(
        dangerousPermCount: Int,
        trackerCount: Int,
        aiRecommendations: List<String> = emptyList(),
        count: Int = 3
    ): List<String> {
        // If AI provided good recommendations, use them
        if (aiRecommendations.isNotEmpty()) {
            return aiRecommendations.take(count)
        }
        
        // Otherwise, provide contextual recommendations
        val recommendations = mutableListOf<String>()
        
        if (dangerousPermCount > 5) {
            recommendations.add("Certaines applications demandent trop de permissions. Vérifiez-les dans les paramètres.")
        }
        
        if (trackerCount > 3) {
            recommendations.add("Plusieurs trackers détectés. Envisagez d'utiliser un bloqueur de publicités.")
        }
        
        // Fill with default recommendations
        val defaults = getDefaultRecommendations().shuffled()
        recommendations.addAll(defaults.take(count - recommendations.size))
        
        return recommendations.take(count)
    }
}
