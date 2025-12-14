package tn.esprit.dam.screens.scan

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tn.esprit.dam.ui.theme.*

@Composable
fun ScanProgressScreen(scanned: Int, total: Int, onCancel: (() -> Unit)? = null) {
    val progressAnim by animateFloatAsState(
        targetValue = if (total>0) scanned.toFloat()/total.toFloat() else 0f,
        animationSpec = tween(400), label = "progressAnim"
    )
    Column(modifier = Modifier.fillMaxSize().padding(spacing_32), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator(modifier = Modifier.size(80.dp), color = PrimaryLight)
        Spacer(Modifier.height(spacing_16))
        Text(text = "Analyse en cours...", style = HeadlineMedium, color = OnSurface)
        Spacer(Modifier.height(spacing_8))
        Text(text = "$scanned/$total applications analysées", style = BodyMedium, color = OnSurfaceSecondary)
        Spacer(Modifier.height(spacing_24))
        androidx.compose.material3.LinearProgressIndicator(progress = progressAnim, modifier = Modifier.fillMaxWidth().height(8.dp), color = Primary, trackColor = SurfaceVariant)
        Spacer(Modifier.height(spacing_8))
        Text(text = "${(progressAnim*100).toInt()}%", style = BodySmall, color = OnSurfaceSecondary)
        Spacer(Modifier.height(spacing_16))
        if (onCancel!=null) {
            Button(onClick = onCancel, colors = ButtonDefaults.buttonColors(containerColor = DangerRed)) { Text("Annuler", color = OnSurface) }
        }
    }
}
