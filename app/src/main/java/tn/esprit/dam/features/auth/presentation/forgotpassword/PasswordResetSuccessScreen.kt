package tn.esprit.dam.features.auth.presentation.forgotpassword

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.auth.presentation.components.AuthAutoRedirectNotice
import tn.esprit.dam.features.auth.presentation.components.AuthCheckItem
import tn.esprit.dam.features.auth.presentation.components.AuthHeroHeader
import tn.esprit.dam.features.auth.presentation.components.AuthPrimaryButton
import tn.esprit.dam.features.auth.presentation.components.AuthScreenLayout

@Composable
fun PasswordResetSuccessScreen(
    onNavigateToLogin: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(3000)
        onNavigateToLogin()
    }

    AuthScreenLayout {
        Spacer(modifier = Modifier.height(AppSpacing.xl))
        AuthHeroHeader(
            icon = Icons.Filled.CheckCircle,
            title = stringResource(R.string.reset_success_title),
            subtitle = stringResource(R.string.vous_pouvez_maintenant_vous_connecter_av),
            accent = AppColors.success,
            accentContainer = AppColors.success.copy(alpha = 0.18f)
        )

        Spacer(modifier = Modifier.height(AppSpacing.xl))

        Text(
            text = stringResource(R.string.conseils_de_securite),
            style = AppTypography.titleMedium,
            color = AppColors.textPrimary
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        AuthCheckItem(stringResource(R.string.ne_partagez_jamais_votre_mot_de_passe))
        AuthCheckItem(stringResource(R.string.utilisez_un_mot_de_passe_unique))
        AuthCheckItem(stringResource(R.string.activez_l_authentification_a_deux_facteu))

        Spacer(modifier = Modifier.height(AppSpacing.xl))

        AuthPrimaryButton(text = stringResource(R.string.se_connecter), onClick = onNavigateToLogin)
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AuthAutoRedirectNotice()
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PasswordResetSuccessScreenPreview() {
    PasswordResetSuccessScreen(
        onNavigateToLogin = {}
    )
}
