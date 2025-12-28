package tn.esprit.dam.features.profile.components


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tn.esprit.dam.features.scan.presentation.ScanTheme

@Composable
fun ProfileActionsSection(
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    Column {
        ActionCard(
            icon = Icons.Filled.Edit,
            title = "Modifier le profil",
            description = "Mettre à jour vos informations",
            onClick = onEditProfile,
            backgroundColor = Color(0xFF6366F1)
        )

        Spacer(modifier = Modifier.height(ScanTheme.Spacing12))

        ActionCard(
            icon = Icons.Filled.ExitToApp,
            title = "Déconnexion",
            description = "Se déconnecter de votre compte",
            onClick = onLogout,
            backgroundColor = Color(0xFFEF4444)
        )
    }
}
