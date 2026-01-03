package tn.esprit.dam.features.scan.domain

import tn.esprit.dam.features.scan.data.LocalAppInfo
import java.util.Locale

/**
 * Normalized Risk Result (0-100)
 */
data class ScanRiskResult(
    val score: Int, // 0 = Dangerous, 100 = Safe
    val riskLevel: RiskLevel,
    val permissionScore: Int,
    val trackerScore: Int,
    val codeScore: Int,
    val criticalIssues: List<String>,
    val warnings: List<String>,
    val confidence: ConfidenceLevel
)

enum class RiskLevel(val label: String) {
    SAFE("SÃ»r"),
    LOW("Faible"),
    MEDIUM("Moyen"),
    HIGH("Élevé"),
    CRITICAL("Critique")
}

enum class ConfidenceLevel(val label: String) {
    HIGH("Haute"),
    MEDIUM("Moyenne"),
    LOW("Faible")
}

object SecurityUtils {

    // 1. Permission Categorization & Risk
    data class PermissionAnalysis(
        val name: String,
        val label: String,
        val description: String,
        val category: Category,
        val risk: RiskLevel
    )

    enum class Category(val label: String) {
        PRIVACY("Vie privée"),
        DEVICE("Appareil"),
        NETWORK("Réseau"),
        SYSTEM("Système"),
        OTHER("Autre")
    }

    private val DANGEROUS_PERMISSIONS = mapOf(
        "android.permission.CAMERA" to PermissionAnalysis(
            "CAMERA", "Accès Caméra", "Peut prendre des photos/vidéos à votre insu.", Category.PRIVACY, RiskLevel.HIGH
        ),
        "android.permission.RECORD_AUDIO" to PermissionAnalysis(
            "MICROPHONE", "Accès Micro", "Peut enregistrer l'audio environnemental.", Category.PRIVACY, RiskLevel.HIGH
        ),
        "android.permission.ACCESS_FINE_LOCATION" to PermissionAnalysis(
            "LOCATION", "Localisation Précise", "Suit vos déplacements avec précision.", Category.PRIVACY, RiskLevel.HIGH
        ),
        "android.permission.ACCESS_COARSE_LOCATION" to PermissionAnalysis(
            "LOCATION", "Localisation Approximative", "Connaît votre région générale.", Category.PRIVACY, RiskLevel.MEDIUM
        ),
        "android.permission.READ_CONTACTS" to PermissionAnalysis(
            "CONTACTS", "Lecture Contacts", "Peut voler votre carnet d'adresses.", Category.PRIVACY, RiskLevel.HIGH
        ),
        "android.permission.READ_SMS" to PermissionAnalysis(
            "SMS", "Lecture SMS", "Peut lire vos messages personnels et codes 2FA.", Category.PRIVACY, RiskLevel.CRITICAL
        ),
        "android.permission.SEND_SMS" to PermissionAnalysis(
            "SMS", "Envoi SMS", "Peut envoyer des SMS surtaxés.", Category.DEVICE, RiskLevel.HIGH
        ),
        "android.permission.READ_CALL_LOG" to PermissionAnalysis(
            "CALLS", "Historique Appels", "Sait qui vous appelez et quand.", Category.PRIVACY, RiskLevel.HIGH
        ),
        "android.permission.READ_EXTERNAL_STORAGE" to PermissionAnalysis(
            "STORAGE", "Lire le stockage", "Accès à vos photos et fichiers.", Category.PRIVACY, RiskLevel.MEDIUM
        ),
        "android.permission.WRITE_EXTERNAL_STORAGE" to PermissionAnalysis(
            "STORAGE", "Modifier le stockage", "Peut modifier ou supprimer vos fichiers.", Category.DEVICE, RiskLevel.MEDIUM
        )
    )

    fun analyzePermission(permission: String): PermissionAnalysis {
        val key = permission.uppercase(Locale.getDefault())
        // Try exact match first
        DANGEROUS_PERMISSIONS[permission]?.let { return it }
        
        // Try suffix match logic
        return when {
            key.contains("CAMERA") -> PermissionAnalysis(
                "CAMERA", "Caméra", "Accès potentiel à l'objectif.", Category.PRIVACY, RiskLevel.MEDIUM
            )
            key.contains("LOCATION") -> PermissionAnalysis(
                "LOCATION", "Localisation", "Accès aux données de position.", Category.PRIVACY, RiskLevel.MEDIUM
            )
            key.contains("RECORD") || key.contains("AUDIO") -> PermissionAnalysis(
                "AUDIO", "Audio", "Accès potentiel au micro.", Category.PRIVACY, RiskLevel.MEDIUM
            )
            key.contains("contacts") -> PermissionAnalysis(
                "CONTACTS", "Contacts", "Accès aux contacts.", Category.PRIVACY, RiskLevel.MEDIUM
            )
            key.contains("SMS") || key.contains("MMS") -> PermissionAnalysis(
                "SMS", "Messagerie", "Accès aux messages texte.", Category.PRIVACY, RiskLevel.HIGH
            )
            key.contains("INTERNET") -> PermissionAnalysis(
                "INTERNET", "Internet", "Communication réseau.", Category.NETWORK, RiskLevel.LOW
            )
            else -> PermissionAnalysis(
                permission.substringAfterLast("."), 
                permission.substringAfterLast("."), 
                "Permission standard Android.", 
                Category.OTHER, 
                RiskLevel.LOW
            )
        }
    }

    // 2. Tracker Analysis
    data class TrackerAnalysis(
        val name: String,
        val category: String,
        val risk: RiskLevel,
        val description: String
    )

    private val KNOWN_TRACKERS = mapOf(
        "Google Analytics" to TrackerAnalysis("Google Analytics", "Analytics", RiskLevel.LOW, "Collecte des données d'usage anonymisées."),
        "Firebase Analytics" to TrackerAnalysis("Firebase", "Analytics", RiskLevel.LOW, "Analyse les performances de l'application."),
        "Facebook SDK" to TrackerAnalysis("Facebook", "Social & Ads", RiskLevel.HIGH, "Partage des données avec Facebook pour le ciblage publicitaire."),
        "Facebook Login" to TrackerAnalysis("Facebook Login", "Social", RiskLevel.MEDIUM, "Authentification via Facebook."),
        "Google AdMob" to TrackerAnalysis("AdMob", "Publicité", RiskLevel.MEDIUM, "Réseau publicitaire de Google."),
        "Unity Ads" to TrackerAnalysis("Unity Ads", "Publicité", RiskLevel.MEDIUM, "Publicités vidéo pour jeux."),
        "AppsFlyer" to TrackerAnalysis("AppsFlyer", "Marketing", RiskLevel.HIGH, "Attribution marketing et analyse comportementale."),
        "Adjust" to TrackerAnalysis("Adjust", "Marketing", RiskLevel.HIGH, "Analyse l'origine des installations."),
        "Crashlytics" to TrackerAnalysis("Crashlytics", "Crash Reporting", RiskLevel.LOW, "Rapports de plantage (généralement sûr).")
    )

    fun analyzeTracker(trackerName: String): TrackerAnalysis {
        KNOWN_TRACKERS.forEach { (key, value) ->
            if (trackerName.contains(key, ignoreCase = true)) return value
        }
        
        return if (trackerName.contains("ads", ignoreCase = true) || trackerName.contains("chartboost", ignoreCase = true)) {
            TrackerAnalysis(trackerName, "Publicité", RiskLevel.MEDIUM, "Réseau publicitaire tiers.")
        } else if (trackerName.contains("analytics", ignoreCase = true)) {
             TrackerAnalysis(trackerName, "Analytics", RiskLevel.LOW, "Outil d'analyse d'audience.")
        } else {
            TrackerAnalysis(trackerName, "Inconnu", RiskLevel.LOW, "Tracker tiers non classifié.")
        }
    }

    // 3. Risk Calculation Engine
    fun calculateAppRisk(
        permissions: List<String>,
        trackers: List<String>,
        isSystemApp: Boolean
    ): ScanRiskResult {
        
        // Base score starts at 100 (Safe)
        var currentScore = 100.0
        val criticalIssues = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // -- Analyze Permissions --
        var permDeduction = 0.0
        permissions.forEach { perm ->
            val analysis = analyzePermission(perm)
            when (analysis.risk) {
                RiskLevel.CRITICAL -> {
                    permDeduction += 15
                    criticalIssues.add(analysis.label)
                }
                RiskLevel.HIGH -> {
                    permDeduction += 8
                    warnings.add(analysis.label)
                }
                RiskLevel.MEDIUM -> {
                    permDeduction += 3
                }
                else -> { /* Low/Safe: No penalty */ }
            }
        }
        // Cap permission deduction
        if (permDeduction > 60) permDeduction = 60.0
        val permScore = (100 - permDeduction).toInt()


        // -- Analyze Trackers --
        var trackerDeduction = 0.0
        trackers.forEach { tracker ->
            val analysis = analyzeTracker(tracker)
            when (analysis.risk) {
                RiskLevel.CRITICAL, RiskLevel.HIGH -> {
                    trackerDeduction += 10
                    if (analysis.risk == RiskLevel.HIGH) warnings.add("Tracker: ${analysis.name}")
                }
                RiskLevel.MEDIUM -> trackerDeduction += 5
                else -> trackerDeduction += 2
            }
        }
        // Cap tracker deduction
        if (trackerDeduction > 40) trackerDeduction = 40.0
        val trackerScore = (100 - trackerDeduction).toInt()

        // -- Combine Scores --
        // Permissions weight: 60%, Trackers weight: 40%
        // If system app, we trust it more (reduce deductions by 50%)
        
        var totalDeduction = (permDeduction * 0.7) + (trackerDeduction * 0.3)
        if (isSystemApp) totalDeduction *= 0.5

        currentScore -= totalDeduction

        // Ensure bounds
        val finalScore = currentScore.coerceIn(0.0, 100.0).toInt()

        // Determine Risk Level
        val riskLevel = when (finalScore) {
            in 0..40 -> RiskLevel.HIGH
            in 41..70 -> RiskLevel.MEDIUM
            in 71..85 -> RiskLevel.LOW
            else -> RiskLevel.SAFE
        }

        // Determine Confidence Level
        // If we have minimal info (no perms, no trackers), confidence is lower unless it's a very simple app
        val confidence = if (permissions.isEmpty() && trackers.isEmpty()) {
            ConfidenceLevel.LOW
        } else if (permissions.isNotEmpty()) {
            ConfidenceLevel.HIGH
        } else {
            ConfidenceLevel.MEDIUM
        }

        return ScanRiskResult(
            score = finalScore,
            riskLevel = riskLevel,
            permissionScore = permScore,
            trackerScore = trackerScore,
            codeScore = 100, // Placeholder for now (Static analysis usually handled by server)
            criticalIssues = criticalIssues,
            warnings = warnings,
            confidence = confidence
        )
    }
}
