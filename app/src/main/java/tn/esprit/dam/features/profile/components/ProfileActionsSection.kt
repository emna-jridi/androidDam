package tn.esprit.dam.features.profile.components


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
    onLogout: () -> Unit
) {
    Column {
        ActionCard(
            icon = Icons.Filled.Edit,
            title = "Modifier le profil",
            description = "Mettre Ã  jour vos informations",
            onClick = onEditProfile,
            backgroundColor = Color(0xFF7C3AED)
        )

        Spacer(modifier = Modifier.height(12.dp))

        ActionCard(
            icon = Icons.Filled.ExitToApp,
            title = "DÃ©connexion",
            description = "Se dÃ©connecter de votre compte",
            onClick = onLogout,
            backgroundColor = Color(0xFFEF4444)
        )
    }
}
