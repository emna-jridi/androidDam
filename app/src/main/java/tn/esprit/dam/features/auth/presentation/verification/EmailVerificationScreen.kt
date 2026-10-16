package tn.esprit.dam.features.auth.presentation.verification

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
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
fun EmailVerificationScreen(
    email: String,
    onVerificationSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: EmailVerificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isVerified) {
        if (uiState.isVerified) {
            onVerificationSuccess()
        }
    }

    OtpVerificationLayout(
        icon = Icons.Filled.MarkEmailRead,
        title = "Vérification Email",
        subtitle = "Nous avons envoyé un code à 6 chiffres à",
        email = email,
        otp = uiState.otp,
        onOtpChange = { viewModel.onOtpChange(it) },
        isLoading = uiState.isLoading,
        verifyLabel = "Vérifier",
        onVerify = { viewModel.verifyEmail(email) },
        canResend = uiState.canResend,
        resendCountdown = uiState.resendCountdown,
        onResend = { viewModel.resendOTP(email) },
        successMessage = uiState.resendMessage,
        errorMessage = uiState.errorMessage,
        infoText = "Vérifiez vos spams si vous ne voyez pas l'email",
        backLabel = "Retour à la connexion",
        onBack = onBackToLogin,
        columnModifier = Modifier
            .fillMaxWidth(0.9f)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EmailVerificationScreenPreview() {
    EmailVerificationScreen(
        email = "john@example.com",
        onVerificationSuccess = {},
        onBackToLogin = {}
    )
}
