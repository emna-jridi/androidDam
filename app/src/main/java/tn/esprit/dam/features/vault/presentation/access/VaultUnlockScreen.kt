package tn.esprit.dam.features.vault.presentation.access

import androidx.biometric.BiometricManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.asString
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.auth.presentation.components.AuthFieldLabel
import tn.esprit.dam.features.auth.presentation.components.AuthHeroHeader
import tn.esprit.dam.features.auth.presentation.components.AuthPasswordField
import tn.esprit.dam.features.auth.presentation.components.AuthPrimaryButton
import tn.esprit.dam.features.auth.presentation.components.AuthScreenLayout
import tn.esprit.dam.features.auth.presentation.components.AuthStatusMessage
import tn.esprit.dam.features.auth.presentation.components.AuthTrustBadge
import tn.esprit.dam.features.vault.presentation.util.BiometricHelper

@Composable
fun VaultUnlockScreen(
    vaultViewModel: VaultViewModel,
    onUnlocked: () -> Unit
) {
    val uiState by vaultViewModel.uiState.collectAsState()
    val biometricEnabled by vaultViewModel.biometricEnabled.collectAsState()
    val biometricRegistered by vaultViewModel.biometricRegistered.collectAsState()
    val biometricOperationState by vaultViewModel.biometricOperationState.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val activity = remember(context) { context as? FragmentActivity }

    var masterPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var attemptFailed by remember { mutableStateOf(false) }
    var showBiometricSetupDialog by remember { mutableStateOf(false) }

    val promptTitle = stringResource(R.string.vault_biometric_prompt_title)
    val promptSubtitle = stringResource(R.string.vault_biometric_prompt_subtitle)

    val shakeOffset by animateFloatAsState(
        targetValue = 0f,
        animationSpec = if (attemptFailed) {
            keyframes {
                durationMillis = 300
                -10f at 50
                10f at 100
                -8f at 150
                8f at 200
                0f at 300
            }
        } else spring(),
        finishedListener = { attemptFailed = false },
        label = "unlockShake"
    )

    val canAuthenticate = remember(context) {
        BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
    }
    val biometricReady = canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS &&
        (biometricRegistered || biometricEnabled) && activity != null

    val unlockWithBiometric: () -> Unit = {
        if (activity != null) {
            if (biometricRegistered) {
                vaultViewModel.authenticateWithCryptoBiometric(activity)
            } else {
                BiometricHelper.authenticateWithBiometric(
                    activity = activity,
                    title = promptTitle,
                    subtitle = promptSubtitle,
                    onSuccess = { vaultViewModel.unlockVaultWithBiometric() },
                    onError = {}
                )
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is VaultUiState.Unlocked) {
            onUnlocked()
        } else if (uiState is VaultUiState.Error) {
            attemptFailed = true
        }
    }

    LaunchedEffect(Unit) {
        if (biometricRegistered && activity != null && vaultViewModel.isBiometricAvailable()) {
            vaultViewModel.authenticateWithCryptoBiometric(activity)
        } else if (biometricEnabled && activity != null && BiometricHelper.isBiometricAvailable(activity)) {
            BiometricHelper.authenticateWithBiometric(
                activity = activity,
                title = promptTitle,
                subtitle = promptSubtitle,
                onSuccess = { vaultViewModel.unlockVaultWithBiometric() },
                onError = {}
            )
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(biometricOperationState) {
        when (val state = biometricOperationState) {
            is BiometricOperationState.Success -> {
                snackbarHostState.showSnackbar(state.message.asString(context))
                vaultViewModel.clearBiometricOperationState()
            }
            is BiometricOperationState.Error -> {
                snackbarHostState.showSnackbar(state.message.asString(context))
                vaultViewModel.clearBiometricOperationState()
            }
            else -> {}
        }
    }

    if (showBiometricSetupDialog && activity != null) {
        AlertDialog(
            onDismissRequest = { showBiometricSetupDialog = false },
            containerColor = AppColors.surface,
            icon = { Icon(Icons.Filled.Fingerprint, contentDescription = null, tint = AppColors.primary) },
            title = { Text(stringResource(R.string.enable_biometric_login), color = AppColors.textPrimary) },
            text = {
                Text(
                    stringResource(R.string.would_you_like_to_enable_fingerprint_or) + " " +
                        stringResource(R.string.vault_biometric_dialog_extra),
                    color = AppColors.textSecondary
                )
            },
            confirmButton = {
                Button(onClick = {
                    showBiometricSetupDialog = false
                    vaultViewModel.registerBiometricDevice(activity)
                }) {
                    Text(stringResource(R.string.enable))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBiometricSetupDialog = false }) {
                    Text(stringResource(R.string.not_now), color = AppColors.textSecondary)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AuthScreenLayout(applySystemBarsPadding = false) {
            AuthHeroHeader(
                icon = Icons.Filled.Lock,
                title = stringResource(R.string.unlock_your_vault),
                subtitle = stringResource(R.string.enter_your_master_password_to_decrypt_an)
            )

            Spacer(modifier = Modifier.height(AppSpacing.xl))

            if (biometricReady) {
                BiometricUnlockButton(
                    loading = biometricOperationState is BiometricOperationState.Loading,
                    onClick = unlockWithBiometric
                )
                Spacer(modifier = Modifier.height(AppSpacing.lg))
                OrDivider()
                Spacer(modifier = Modifier.height(AppSpacing.lg))
            }

            AuthFieldLabel(stringResource(R.string.master_password))
            Box(modifier = Modifier.graphicsLayer(translationX = shakeOffset)) {
                AuthPasswordField(
                    value = masterPassword,
                    onValueChange = {
                        masterPassword = it
                        attemptFailed = false
                    },
                    visible = showPassword,
                    onToggleVisibility = { showPassword = !showPassword },
                    enabled = uiState !is VaultUiState.Loading,
                    error = null,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        if (masterPassword.isNotBlank()) vaultViewModel.unlockVault(masterPassword)
                    })
                )
            }

            (uiState as? VaultUiState.Error)?.let { error ->
                AuthStatusMessage(
                    text = error.message?.asString() ?: stringResource(R.string.vault_auth_failed),
                    isError = true
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            AuthPrimaryButton(
                text = stringResource(
                    if (uiState is VaultUiState.Loading) R.string.unlocking else R.string.unlock_vault
                ),
                onClick = {
                    focusManager.clearFocus()
                    vaultViewModel.unlockVault(masterPassword)
                },
                enabled = masterPassword.isNotBlank() && uiState !is VaultUiState.Loading,
                isLoading = uiState is VaultUiState.Loading
            )

            BiometricSetupLink(
                canAuthenticate = canAuthenticate,
                alreadySetUp = biometricRegistered || biometricEnabled,
                hasActivity = activity != null,
                onEnable = { showBiometricSetupDialog = true },
                onOpenSettings = { BiometricHelper.openBiometricSettings(context) }
            )

            Spacer(modifier = Modifier.height(AppSpacing.lg))

            AuthTrustBadge(
                text = stringResource(R.string.your_passwords_are_encrypted_with_zero_k),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(AppSpacing.md)
        )
    }
}

/** Big round fingerprint button, the fastest way in. */
@Composable
private fun BiometricUnlockButton(loading: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AppColors.primaryContainer)
                .border(1.dp, AppColors.primary.copy(alpha = 0.4f), CircleShape)
                .clickable(enabled = !loading, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(color = AppColors.primary, strokeWidth = 2.dp, modifier = Modifier.size(32.dp))
            } else {
                Icon(
                    Icons.Outlined.Fingerprint,
                    contentDescription = stringResource(R.string.vault_unlock_with_biometric),
                    tint = AppColors.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Text(
            text = stringResource(R.string.vault_unlock_with_biometric),
            style = AppTypography.labelMedium,
            color = AppColors.textSecondary
        )
    }
}

@Composable
private fun OrDivider() {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = AppColors.surfaceVariant)
        Text(
            text = stringResource(R.string.or),
            modifier = Modifier.padding(horizontal = AppSpacing.md),
            style = AppTypography.labelMedium,
            color = AppColors.textTertiary
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = AppColors.surfaceVariant)
    }
}

/** One quiet link under the button: enable biometrics, or add a fingerprint in system settings. */
@Composable
private fun BiometricSetupLink(
    canAuthenticate: Int,
    alreadySetUp: Boolean,
    hasActivity: Boolean,
    onEnable: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val (label, icon, action) = when {
        !hasActivity -> return
        canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS && !alreadySetUp ->
            Triple(R.string.enable_secure_biometric_login, Icons.Outlined.Fingerprint, onEnable)
        canAuthenticate == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
            Triple(R.string.open_settings_to_add_fingerprint, Icons.Outlined.Settings, onOpenSettings)
        else -> return
    }
    Spacer(modifier = Modifier.height(AppSpacing.sm))
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        TextButton(onClick = action) {
            Icon(icon, contentDescription = null, tint = AppColors.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.size(AppSpacing.sm))
            Text(stringResource(label), style = AppTypography.labelMedium, color = AppColors.primary)
        }
    }
}
