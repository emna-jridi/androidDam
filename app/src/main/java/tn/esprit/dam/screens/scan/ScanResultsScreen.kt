package tn.esprit.dam.screens.scan

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tn.esprit.dam.screens.scan.components.ScoreCard
import tn.esprit.dam.ui.theme.*

@Composable
fun ScanResultsScreen(score: Int, high: Int, medium: Int, low: Int, appCount: Int) {
    Column(modifier = Modifier.fillMaxSize().padding(spacing_16)) {
        Text(text = "Analyse terminée", style = HeadlineMedium, color = OnSurface)
        Text(text = "$appCount applications analysées", style = BodySmall, color = OnSurfaceSecondary)
        Spacer(Modifier.height(spacing_16))
        ScoreCard(score = score, high = high, medium = medium, low = low)
        Spacer(Modifier.height(spacing_16))
        val summary = when {
            score>=80 -> "Bon niveau de sécurité"
            score>=60 -> "Sécurité correcte"
            else -> "Attention requise"
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(text = summary, style = BodyLarge, color = if (score>=60) SuccessGreen else DangerRed)
        }
    }
}
