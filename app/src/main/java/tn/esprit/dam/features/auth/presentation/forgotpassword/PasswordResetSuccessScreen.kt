package tn.esprit.dam.features.auth.presentation.forgotpassword

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import tn.esprit.dam.features.auth.presentation.components.AuthAutoRedirectNotice
import tn.esprit.dam.features.auth.presentation.components.AuthBackground
import tn.esprit.dam.features.auth.presentation.components.AuthCard
import tn.esprit.dam.features.auth.presentation.components.AuthColors
import tn.esprit.dam.features.auth.presentation.components.AuthSuccessBadge
import tn.esprit.dam.features.auth.presentation.components.AuthTitle

@Composable
fun PasswordResetSuccessScreen(
    onNavigateToLogin: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(3000)
        onNavigateToLogin()
    }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthSuccessBadge()
            Spacer(modifier = Modifier.height(32.dp))
            AuthTitle("Mot de Passe Réinitialiser", fontSize = 28.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))

            AuthCard(horizontalAlignment = Alignment.CenterHorizontally, contentPadding = 32.dp) {
                Text(
                    text = "Votre mot de passe a été réinitialisé avec succès !",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Vous pouvez maintenant vous connecter avec votre nouveau mot de passe.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuthColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AuthColors.Primary.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = AuthColors.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Conseils de sécurité ",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        SecurityTip(text = "Ne partagez jamais votre mot de passe")
                        SecurityTip(text = "Utilisez un mot de passe unique")
                        SecurityTip(text = "Activez l'authentification à deux facteurs")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AuthColors.Primary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Login,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Se connecter",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            AuthAutoRedirectNotice()
        }
    }
}

@Composable
fun SecurityTip(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = AuthColors.Success,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = AuthColors.TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PasswordResetSuccessScreenPreview() {
    PasswordResetSuccessScreen(
        onNavigateToLogin = {}
    )
}
