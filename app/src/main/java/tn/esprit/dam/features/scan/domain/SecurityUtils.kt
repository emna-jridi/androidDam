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
        PRIVACY("Privacy"),
        DEVICE("Device"),
        NETWORK("Network"),
        SYSTEM("System"),
        OTHER("Other")
    }

    private val DANGEROUS_PERMISSIONS = mapOf(
        "android.permission.CAMERA" to PermissionAnalysis(
            "CAMERA", "Camera Access", "Can take photos/videos without your knowledge.", Category.PRIVACY, RiskLevel.HIGH
        ),
        "android.permission.RECORD_AUDIO" to PermissionAnalysis(
            "MICROPHONE", "Microphone Access", "Can record environmental audio.", Category.PRIVACY, RiskLevel.HIGH
        ),
        "android.permission.ACCESS_FINE_LOCATION" to PermissionAnalysis(
            "LOCATION", "Precise Location", "Tracks your movements precisely.", Category.PRIVACY, RiskLevel.HIGH
        ),
        "android.permission.ACCESS_COARSE_LOCATION" to PermissionAnalysis(
            "LOCATION", "Approximate Location", "Knows your general area.", Category.PRIVACY, RiskLevel.MEDIUM
        ),
        "android.permission.READ_CONTACTS" to PermissionAnalysis(
            "CONTACTS", "Read Contacts", "Can steal your address book.", Category.PRIVACY, RiskLevel.HIGH
        ),
        "android.permission.READ_SMS" to PermissionAnalysis(
            "SMS", "Read SMS", "Can read your personal messages and 2FA codes.", Category.PRIVACY, RiskLevel.CRITICAL
        ),
        "android.permission.SEND_SMS" to PermissionAnalysis(
            "SMS", "Send SMS", "Can send premium SMS.", Category.DEVICE, RiskLevel.HIGH
        ),
        "android.permission.READ_CALL_LOG" to PermissionAnalysis(
            "CALLS", "Call History", "Knows who you call and when.", Category.PRIVACY, RiskLevel.HIGH
        ),
        "android.permission.READ_EXTERNAL_STORAGE" to PermissionAnalysis(
            "STORAGE", "Read Storage", "Access to your photos and files.", Category.PRIVACY, RiskLevel.MEDIUM
        ),
        "android.permission.WRITE_EXTERNAL_STORAGE" to PermissionAnalysis(
            "STORAGE", "Modify Storage", "Can modify or delete your files.", Category.DEVICE, RiskLevel.MEDIUM
        )
    )

    fun analyzePermission(permission: String): PermissionAnalysis {
        val key = permission.uppercase(Locale.getDefault())
        // Try exact match first
        DANGEROUS_PERMISSIONS[permission]?.let { return it }
        
        // Try suffix match logic
        return when {
            key.contains("CAMERA") -> PermissionAnalysis(
                "CAMERA", "Camera", "Potential access to the lens.", Category.PRIVACY, RiskLevel.MEDIUM
            )
            key.contains("LOCATION") -> PermissionAnalysis(
                "LOCATION", "Location", "Access to location data.", Category.PRIVACY, RiskLevel.MEDIUM
            )
            key.contains("RECORD") || key.contains("AUDIO") -> PermissionAnalysis(
                "AUDIO", "Audio", "Potential microphone access.", Category.PRIVACY, RiskLevel.MEDIUM
            )
            key.contains("contacts") -> PermissionAnalysis(
                "CONTACTS", "Contacts", "Access to contacts.", Category.PRIVACY, RiskLevel.MEDIUM
            )
            key.contains("SMS") || key.contains("MMS") -> PermissionAnalysis(
                "SMS", "Messaging", "Access to text messages.", Category.PRIVACY, RiskLevel.HIGH
            )
            key.contains("INTERNET") -> PermissionAnalysis(
                "INTERNET", "Internet", "Network communication.", Category.NETWORK, RiskLevel.LOW
            )
            else -> PermissionAnalysis(
                permission.substringAfterLast("."), 
                permission.substringAfterLast("."), 
                "Standard Android permission.", 
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
        "Google Analytics" to TrackerAnalysis("Google Analytics", "Analytics", RiskLevel.LOW, "Collects anonymized usage data."),
        "Firebase Analytics" to TrackerAnalysis("Firebase", "Analytics", RiskLevel.LOW, "Analyzes app performance."),
        "Facebook SDK" to TrackerAnalysis("Facebook", "Social & Ads", RiskLevel.HIGH, "Shares data with Facebook for ad targeting."),
        "Facebook Login" to TrackerAnalysis("Facebook Login", "Social", RiskLevel.MEDIUM, "Authentication via Facebook."),
        "Google AdMob" to TrackerAnalysis("AdMob", "Advertising", RiskLevel.MEDIUM, "Google advertising network."),
        "Unity Ads" to TrackerAnalysis("Unity Ads", "Advertising", RiskLevel.MEDIUM, "Video ads for games."),
        "AppsFlyer" to TrackerAnalysis("AppsFlyer", "Marketing", RiskLevel.HIGH, "Marketing attribution and behavioral analysis."),
        "Adjust" to TrackerAnalysis("Adjust", "Marketing", RiskLevel.HIGH, "Analyzes installation sources."),
        "Crashlytics" to TrackerAnalysis("Crashlytics", "Crash Reporting", RiskLevel.LOW, "Crash reports (generally safe).")
    )

    fun analyzeTracker(trackerName: String): TrackerAnalysis {
        KNOWN_TRACKERS.forEach { (key, value) ->
            if (trackerName.contains(key, ignoreCase = true)) return value
        }
        
        return if (trackerName.contains("ads", ignoreCase = true) || trackerName.contains("chartboost", ignoreCase = true)) {
            TrackerAnalysis(trackerName, "Advertising", RiskLevel.MEDIUM, "Third-party advertising network.")
        } else if (trackerName.contains("analytics", ignoreCase = true)) {
             TrackerAnalysis(trackerName, "Analytics", RiskLevel.LOW, "Audience analytics tool.")
        } else {
            TrackerAnalysis(trackerName, "Unknown", RiskLevel.LOW, "Unclassified third-party tracker.")
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









