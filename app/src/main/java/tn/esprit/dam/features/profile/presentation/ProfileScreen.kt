package tn.esprit.dam.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.R
import tn.esprit.dam.core.model.User
import tn.esprit.dam.core.ui.asString
import tn.esprit.dam.core.ui.components.AppErrorState
import tn.esprit.dam.core.ui.components.AppSkeletonList
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.profile.presentation.components.EditProfileDialog
import tn.esprit.dam.features.profile.presentation.components.ProfileHeader
import tn.esprit.dam.features.vault.presentation.access.VaultStatusUiState
import tn.esprit.dam.features.vault.presentation.access.VaultStatusViewModel

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onOpenVault: () -> Unit,
    onCreateVault: () -> Unit,
    onOpenDarkWeb: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()
    var confirmLogout by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        when (val state = uiState) {
            is ProfileUiState.Loading -> AppSkeletonList(itemCount = 3, modifier = Modifier.padding(AppSpacing.md))

            is ProfileUiState.Error -> AppErrorState(
                message = state.message.asString(),
                onRetry = { viewModel.loadProfile() },
                retryText = stringResource(R.string.profile_retry)
            )

            is ProfileUiState.Success -> ProfileContent(
                user = state.user,
                onEditProfile = { viewModel.showEditDialog() },
                onLogout = { confirmLogout = true },
                onOpenVault = onOpenVault,
                onCreateVault = onCreateVault,
                onOpenDarkWeb = onOpenDarkWeb
            )
        }
    }

    if (showEditDialog && uiState is ProfileUiState.Success) {
        EditProfileDialog(
            user = (uiState as ProfileUiState.Success).user,
            onDismiss = { viewModel.hideEditDialog() },
            onSave = { newName -> viewModel.updateProfile(newName) }
        )
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            containerColor = AppColors.surface,
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = AppColors.error) },
            title = { Text(stringResource(R.string.profile_logout_confirm_title), color = AppColors.textPrimary) },
            text = { Text(stringResource(R.string.profile_logout_confirm_text), color = AppColors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        confirmLogout = false
                        viewModel.logout(onLogout)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.error, contentColor = AppColors.textPrimary)
                ) {
                    Text(stringResource(R.string.deconnexion))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) {
                    Text(stringResource(R.string.profile_cancel), color = AppColors.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun ProfileContent(
    user: User,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onOpenVault: () -> Unit,
    onCreateVault: () -> Unit,
    onOpenDarkWeb: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        ProfileHeader(user = user, onEdit = onEditProfile)

        SettingsGroup(title = stringResource(R.string.profile_section_security)) {
            VaultRow(onOpenVault = onOpenVault, onCreateVault = onCreateVault)
            GroupDivider()
            SettingsRow(
                icon = Icons.Default.TravelExplore,
                title = stringResource(R.string.profile_darkweb_title),
                subtitle = stringResource(R.string.profile_darkweb_desc),
                onClick = onOpenDarkWeb
            )
        }

        SettingsGroup(title = stringResource(R.string.profile_section_account)) {
            SettingsRow(
                icon = Icons.Default.Edit,
                title = stringResource(R.string.modifier_le_profil),
                subtitle = stringResource(R.string.mettre_a_jour_vos_informations),
                onClick = onEditProfile
            )
            GroupDivider()
            SettingsRow(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = stringResource(R.string.deconnexion),
                subtitle = stringResource(R.string.se_deconnecter_de_votre_compte),
                onClick = onLogout,
                accent = AppColors.error,
                showChevron = false
            )
        }
    }
}

/** Vault status as a settings row: live status line, and "Créer" when no vault exists yet. */
@Composable
private fun VaultRow(onOpenVault: () -> Unit, onCreateVault: () -> Unit) {
    val viewModel: VaultStatusViewModel = hiltViewModel()
    val status by viewModel.status.collectAsState()

    LaunchedEffect(Unit) { viewModel.checkStatus() }

    val (subtitle, onClick) = when (val s = status) {
        VaultStatusUiState.Loading -> stringResource(R.string.profile_vault_checking) to {}
        VaultStatusUiState.NoVault -> stringResource(R.string.profile_vault_none) to onCreateVault
        VaultStatusUiState.VaultExists -> stringResource(R.string.profile_vault_ready) to onOpenVault
        is VaultStatusUiState.Error -> stringResource(R.string.profile_vault_error) to { viewModel.checkStatus() }
    }

    SettingsRow(
        icon = Icons.Default.Lock,
        title = stringResource(R.string.profile_vault_title),
        subtitle = subtitle,
        onClick = onClick,
        accent = if (status is VaultStatusUiState.Error) AppColors.riskMedium else AppColors.primary,
        trailing = when (status) {
            VaultStatusUiState.Loading -> {
                { CircularProgressIndicator(color = AppColors.primary, strokeWidth = 2.dp, modifier = Modifier.size(18.dp)) }
            }
            VaultStatusUiState.NoVault -> {
                {
                    Text(
                        text = stringResource(R.string.profile_vault_create),
                        style = AppTypography.labelMedium,
                        color = AppColors.onPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AppColors.primary)
                            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs)
                    )
                }
            }
            else -> null
        }
    )
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text(
            text = title.uppercase(),
            style = AppTypography.labelMedium,
            color = AppColors.textTertiary,
            modifier = Modifier.padding(start = AppSpacing.xs)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppCorners.large))
                .background(AppColors.surface)
        ) {
            content()
        }
    }
}

@Composable
private fun GroupDivider() {
    HorizontalDivider(color = AppColors.surfaceVariant, modifier = Modifier.padding(start = 68.dp))
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    accent: Color = AppColors.primary,
    showChevron: Boolean = true,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm + AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(AppCorners.medium))
                .background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(AppSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.bodyMedium,
                color = if (accent == AppColors.error) AppColors.error else AppColors.textPrimary
            )
            Text(text = subtitle, style = AppTypography.labelMedium, color = AppColors.textTertiary)
        }
        when {
            trailing != null -> trailing()
            showChevron -> Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.textMuted)
        }
    }
}
