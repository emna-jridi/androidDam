package tn.esprit.dam.screens.profile.components


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProfileActionsSection(
    onEditProfile: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onLogout: () -> Unit
) {
    Column {
        ActionCard(
            icon = Icons.Filled.Edit,
            title = "Modifier le profil",
            description = "Mettre à jour vos informations",
            onClick = onEditProfile,
            backgroundColor = Color(0xFF7C3AED)
        )

        Spacer(modifier = Modifier.height(12.dp))

        ActionCard(
            icon = Icons.Filled.Security,
            title = "Security Scan",
            description = "Analyser la sécurité de vos apps",
            onClick = onNavigateToSecurity,
            backgroundColor = Color(0xFF10B981)
        )

        Spacer(modifier = Modifier.height(12.dp))

        ActionCard(
            icon = Icons.Filled.ExitToApp,
            title = "Déconnexion",
            description = "Se déconnecter de votre compte",
            onClick = onLogout,
            backgroundColor = Color(0xFFEF4444)
        )
    }
}