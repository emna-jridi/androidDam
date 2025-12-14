package tn.esprit.dam.screens.scan.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tn.esprit.dam.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info

@Composable
fun ScoreCard(score: Int, high: Int, medium: Int, low: Int) {
    val anim = animateFloatAsState(
        targetValue = score/100f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "score"
    )
    Card(
        modifier = Modifier.fillMaxWidth().height(180.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        shape = RoundedCornerShape(cornerLarge),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryLight)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(spacing_24), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = anim.value, strokeWidth = 8.dp, color = SuccessGreen)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = score.toString(), style = HeadlineLarge, color = OnSurface)
                    Text(text = "/100", style = BodyMedium, color = OnSurface)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(spacing_16)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Warning, contentDescription=null, tint = DangerRed); Spacer(Modifier.width(spacing_8)); Text("$high Risque important", style = BodyMedium, color = OnSurface) }
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Info, contentDescription=null, tint = WarningOrange); Spacer(Modifier.width(spacing_8)); Text("$medium Risque modéré", style = BodyMedium, color = OnSurface) }
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, contentDescription=null, tint = SuccessGreen); Spacer(Modifier.width(spacing_8)); Text("$low Risque limité", style = BodyMedium, color = OnSurface) }
            }
        }
    }
}
