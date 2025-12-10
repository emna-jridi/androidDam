package tn.esprit.dam.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.dam.ui.theme.DarkBackground
import tn.esprit.dam.ui.theme.DarkSurface
import tn.esprit.dam.ui.theme.PrimaryGradient
import tn.esprit.dam.ui.theme.TextOnPrimary
import tn.esprit.dam.ui.theme.DarkTextSecondary

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val gradient: Brush,
    val tips: List<String>
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,  // ✅ Non-composable callback
    onSkip: () -> Unit       // ✅ Non-composable callback
) {
    var currentPage by remember { mutableStateOf(0) }

    val pages = listOf(
        OnboardingPage(
            title = "Bienvenue dans ShadowGuard",
            description = "Protégez votre vie privée en analysant les applications qui collectent vos données",
            icon = Icons.Default.Shield,
            gradient = Brush.verticalGradient(
                colors = listOf(Color(0xFF6366F1), Color(0xFF5A67D8))
            ),
            tips = listOf(
                "Détectez les trackers cachés",
                "Comprenez les permissions",
                "Contrôlez vos données"
            )
        ),
        OnboardingPage(
            title = "Comment fonctionne le Scan",
            description = "Chaque scan analyse vos applications installées pour identifier les comportements suspects et les trackers",
            icon = Icons.Default.Verified,
            gradient = Brush.verticalGradient(
                colors = listOf(Color(0xFF10B981), Color(0xFF059669))
            ),
            tips = listOf(
                "Scan des permissions",
                "Détection des trackers",
                "Analyse du comportement"
            )
        ),
        OnboardingPage(
            title = "Comprendre votre Score",
            description = "Votre score de confidentialité (0-100) indique le niveau de protection de vos données",
            icon = Icons.Default.EmojiEvents,
            gradient = Brush.verticalGradient(
                colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
            ),
            tips = listOf(
                "0-25: Faible risque ✓",
                "26-50: Risque moyen ⚠️",
                "51-75: Risque élevé ⚠️",
                "76-100: Critique 🔴"
            )
        ),
        OnboardingPage(
            title = "Prêt à commencer?",
            description = "Lancez votre premier scan pour découvrir quelles applications collectent vos données",
            icon = Icons.Default.PlayArrow,
            gradient = Brush.verticalGradient(
                colors = listOf(Color(0xFFEC4899), Color(0xFFDB2777))
            ),
            tips = listOf(
                "Scan rapide: 30 secondes",
                "Aucune installation nécessaire",
                "Résultats immédiats"
            )
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Pages avec animation
        pages.forEachIndexed { index, page ->
            AnimatedVisibility(
                visible = currentPage == index,
                enter = slideInHorizontally(initialOffsetX = { if (index > currentPage - 1) 1000 else -1000 }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { if (index > currentPage) 1000 else -1000 }) + fadeOut()
            ) {
                OnboardingPageContent(page = page)
            }
        }

        // Navigation controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                repeat(pages.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (index == currentPage) Color(0xFF6366F1)
                                else Color.White.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentPage < pages.size - 1) {
                    // Skip button
                    OutlinedButton(
                        onClick = onSkip,  // ✅ CORRECT: Appel direct sans lambda
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextOnPrimary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFF5A67D8))
                            )
                        )
                    ) {
                        Text("Passer", fontWeight = FontWeight.SemiBold)
                    }

                    // Next button
                    Button(
                        onClick = { currentPage++ },  // ✅ CORRECT: Lambda locale
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Suivant", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    // Complete button
                    Button(
                        onClick = onComplete,  // ✅ CORRECT: Appel direct sans lambda
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Commencer", fontWeight = FontWeight.SemiBold)
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon container avec gradient
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(page.gradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextOnPrimary,
                fontSize = 28.sp
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = DarkTextSecondary,
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Tips cards
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            page.tips.forEach { tip ->
                TipCard(tip = tip, gradient = page.gradient)
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
private fun TipCard(tip: String, gradient: Brush) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Gradient checkbox
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = tip,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextOnPrimary
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}