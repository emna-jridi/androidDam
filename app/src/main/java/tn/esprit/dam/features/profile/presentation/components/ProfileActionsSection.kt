package tn.esprit.dam.features.profile.presentation.components
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tn.esprit.dam.core.ui.theme.AppSpacing

@Composable
fun ProfileActionsSection(
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    Column {
        ActionCard(
            icon = Icons.Filled.Edit,
            title = stringResource(R.string.modifier_le_profil),
            description = stringResource(R.string.mettre_a_jour_vos_informations),
            onClick = onEditProfile,
            backgroundColor = AppColors.primary
        )

        Spacer(modifier = Modifier.height(AppSpacing.sm + AppSpacing.xs))

        ActionCard(
            icon = Icons.Filled.ExitToApp,
            title = stringResource(R.string.deconnexion),
            description = stringResource(R.string.se_deconnecter_de_votre_compte),
            onClick = onLogout,
            backgroundColor = AppColors.error
        )
    }
}
