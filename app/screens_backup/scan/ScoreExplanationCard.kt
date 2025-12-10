package tn.esprit.dam.screens.scan


import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.dam.ui.theme.*

/**
 * Composable pour expliquer comment le score est calculé
 * À afficher dans le ScanScreen après le premier scan
 */
@Composable
fun ScoreExplanationCard(
    score: Int = 0,
    modifier: Modifier = Modifier,
    onLearnMore: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header clickable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Icon avec gradient
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF6366F1), Color(0xFF5A67D8))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Comment fonctionne le Score?",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextOnPrimary
                            )
                        )
                        Text(
                            text = if (isExpanded) "Voir moins" else "Cliquez pour en savoir plus",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Expanded content
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(
                    color = Color.White.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Score components explanation
                ScoreComponentsExplanation()

                Spacer(modifier = Modifier.height(16.dp))

                // Risk levels
                RiskLevelsExplanation()

                Spacer(modifier = Modifier.height(16.dp))

                // CTA Button
                Button(
                    onClick = onLearnMore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Mode Apprentissage",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreComponentsExplanation() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Composants du Score",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TextOnPrimary,
                fontSize = 12.sp
            )
        )

        val components = listOf(
            "Apps Installées" to "40%",
            "Permissions Demandées" to "30%",
            "Trackers Détectés" to "20%",
            "Comportements Suspects" to "10%"
        )

        components.forEach { (name, weight) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextOnPrimary,
                        fontSize = 12.sp
                    )
                )

                Text(
                    text = weight,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6366F1)
                    )
                )
            }
        }
    }
}

@Composable
fun RiskLevelsExplanation() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Niveaux de Risque",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TextOnPrimary,
                fontSize = 12.sp
            )
        )

        val riskLevels = listOf(
            RiskLevelInfo("Faible", "0-25", Color(0xFF10B981), "✓ Bien protégé"),
            RiskLevelInfo("Moyen", "26-50", Color(0xFFF59E0B), "⚠️ À surveiller"),
            RiskLevelInfo("Élevé", "51-75", Color(0xFFF97316), "⚠️ À améliorer"),
            RiskLevelInfo("Critique", "76-100", Color(0xFFEF4444), "🔴 Action requise")
        )

        riskLevels.forEach { risk ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(risk.color),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = risk.range.split("-")[0],
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = risk.name,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextOnPrimary,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = risk.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DarkTextSecondary,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

data class RiskLevelInfo(
    val name: String,
    val range: String,
    val color: Color,
    val description: String
)

@Composable
fun ScoreGauge(
    score: Int,
    modifier: Modifier = Modifier
) {
    val riskColor = when (score) {
        in 0..25 -> Color(0xFF10B981)    // Green
        in 26..50 -> Color(0xFFF59E0B)   // Yellow
        in 51..75 -> Color(0xFFF97316)   // Orange
        else -> Color(0xFFEF4444)        // Red
    }

    val riskLabel = when (score) {
        in 0..25 -> "Faible"
        in 26..50 -> "Moyen"
        in 51..75 -> "Élevé"
        else -> "Critique"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score circle
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(riskColor, riskColor.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = score.toString(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 48.sp
                        )
                    )
                    Text(
                        text = "/100",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = riskLabel,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = riskColor
                )
            )

            Text(
                text = when (score) {
                    in 0..25 -> "Bien protégé ✓"
                    in 26..50 -> "À surveiller ⚠️"
                    in 51..75 -> "À améliorer ⚠️"
                    else -> "Action requise 🔴"
                },
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DarkTextSecondary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = score / 100f,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                trackColor = Color.White.copy(alpha = 0.1f),
                color = riskColor
            )
        }
    }
}