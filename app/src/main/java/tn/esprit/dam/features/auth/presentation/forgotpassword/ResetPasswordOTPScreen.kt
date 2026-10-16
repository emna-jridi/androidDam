package tn.esprit.dam.features.auth.presentation.forgotpassword
import tn.esprit.dam.core.ui.asString
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.features.auth.presentation.components.OtpVerificationLayout

@Composable
fun ResetPasswordOTPScreen(
    email: String,
    onOTPVerified: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.currentStep) {
        if (uiState.currentStep == ResetPasswordStep.NEW_PASSWORD) {
            onOTPVerified()
        }
    }

    OtpVerificationLayout(
        icon = Icons.Filled.VpnKey,
        title = stringResource(R.string.code_de_verification),
        subtitle = stringResource(R.string.nous_avons_envoye_un_code_a_6_chiffres),
        email = email,
        otp = uiState.otp,
        onOtpChange = { viewModel.onOtpChange(it) },
        isLoading = uiState.isLoading,
        verifyLabel = "Vérifier le code",
        onVerify = { viewModel.verifyResetOTP() },
        canResend = uiState.canResend,
        resendCountdown = uiState.resendCountdown,
        onResend = { viewModel.resendResetOTP() },
        successMessage = uiState.successMessage?.asString(),
        errorMessage = uiState.errorMessage?.asString(),
        infoText = "Le code expire dans 10 minutes",
        backLabel = "Retour",
        onBack = onNavigateBack,
        columnModifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ResetPasswordOTPScreenPreview() {
    ResetPasswordOTPScreen(
        email = "john@example.com",
        onOTPVerified = {},
        onNavigateBack = {}
    )
}
