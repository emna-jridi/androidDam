package tn.esprit.dam.features.auth.presentation.verification

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.asString
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
        title = stringResource(R.string.email_verify_title),
        email = email,
        otp = uiState.otp,
        onOtpChange = { viewModel.onOtpChange(it) },
        isLoading = uiState.isLoading,
        onVerify = { viewModel.verifyEmail(email) },
        canResend = uiState.canResend,
        resendCountdown = uiState.resendCountdown,
        onResend = { viewModel.resendOTP(email) },
        successMessage = uiState.resendMessage?.asString(),
        errorMessage = uiState.errorMessage?.asString(),
        infoText = stringResource(R.string.otp_check_spam),
        onBack = onBackToLogin
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
