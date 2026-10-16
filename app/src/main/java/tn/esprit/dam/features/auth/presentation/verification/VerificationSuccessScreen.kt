package tn.esprit.dam.features.auth.presentation.verification

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
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
fun VerificationSuccessScreen(
    userName: String = "",
    onContinue: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(3000)
        onContinue()
    }

    AuthScreenLayout {
        Spacer(modifier = Modifier.height(AppSpacing.xl))
        AuthHeroHeader(
            icon = Icons.Filled.MarkEmailRead,
            title = stringResource(R.string.email_verified_title),
            subtitle = stringResource(R.string.votre_compte_a_ete_cree_avec_succes_et_v),
            accent = AppColors.success,
            accentContainer = AppColors.success.copy(alpha = 0.18f)
        )

        Spacer(modifier = Modifier.height(AppSpacing.xl))

        if (userName.isNotBlank()) {
            Text(
                text = stringResource(R.string.welcome_user, userName),
                style = AppTypography.titleMedium,
                color = AppColors.textPrimary
            )
            Spacer(modifier = Modifier.height(AppSpacing.sm))
        }
        AuthCheckItem(stringResource(R.string.protection_avancee_activee), Icons.Default.Security)
        AuthCheckItem(stringResource(R.string.analyse_d_applications_disponible), Icons.Default.Shield)
        AuthCheckItem(stringResource(R.string.compte_100_securise), Icons.Default.Verified)

        Spacer(modifier = Modifier.height(AppSpacing.xl))

        AuthPrimaryButton(text = stringResource(R.string.commencer), onClick = onContinue)
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AuthAutoRedirectNotice()
        }
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
