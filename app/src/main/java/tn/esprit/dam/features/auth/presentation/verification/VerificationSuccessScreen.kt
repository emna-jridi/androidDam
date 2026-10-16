package tn.esprit.dam.features.auth.presentation.verification
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import tn.esprit.dam.features.auth.presentation.components.AuthAutoRedirectNotice
import tn.esprit.dam.features.auth.presentation.components.AuthBackground
import tn.esprit.dam.features.auth.presentation.components.AuthCard
import tn.esprit.dam.features.auth.presentation.components.AuthPrimaryButton
import tn.esprit.dam.features.auth.presentation.components.AuthSuccessBadge
import tn.esprit.dam.features.auth.presentation.components.AuthTitle

@Composable
fun VerificationSuccessScreen(
    userName: String = "Utilisateur",
    onContinue: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(3000)
        onContinue()
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
            AuthTitle("Email Vérifié ! ✓", textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))

            AuthCard(horizontalAlignment = Alignment.CenterHorizontally, contentPadding = 32.dp) {
                Text(
                    text = "Bienvenue $userName !",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AppColors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.votre_compte_a_ete_cree_avec_succes_et_v),
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.textSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                FeatureItem(icon = Icons.Default.Security, text = stringResource(R.string.protection_avancee_activee))
                Spacer(modifier = Modifier.height(12.dp))
                FeatureItem(icon = Icons.Default.Shield, text = stringResource(R.string.analyse_d_applications_disponible))
                Spacer(modifier = Modifier.height(12.dp))
                FeatureItem(icon = Icons.Default.Verified, text = stringResource(R.string.compte_100_securise))
            }

            Spacer(modifier = Modifier.height(32.dp))
            AuthPrimaryButton(text = stringResource(R.string.commencer), onClick = onContinue)
            Spacer(modifier = Modifier.height(16.dp))
            AuthAutoRedirectNotice()
        }
    }
}

@Composable
fun FeatureItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.success,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            color = AppColors.textPrimary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VerificationSuccessScreenPreview() {
    VerificationSuccessScreen(
        userName = "John Doe",
        onContinue = {}
    )
}
