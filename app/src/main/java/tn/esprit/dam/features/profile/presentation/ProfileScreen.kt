package tn.esprit.dam.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.core.model.User
import tn.esprit.dam.features.profile.presentation.components.*
import tn.esprit.dam.core.ui.theme.*
import tn.esprit.dam.core.ui.theme.ScanTheme
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.core.ui.components.AppErrorState
import tn.esprit.dam.core.ui.components.AppPrimaryButton

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onOpenVault: () -> Unit,
    onCreateVault: () -> Unit,
    onOpenDarkWeb: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            is ProfileUiState.Error -> {
                    AppErrorState(
                    message = state.message,
                    onRetry = { viewModel.loadProfile() },
                    retryText = "Réessayer"
                )
            }

            is ProfileUiState.Success -> {
                    ProfileContent(
                    user = state.user,
                    onEditProfile = { viewModel.showEditDialog() },
                    onLogout = { viewModel.logout(onLogout) },
                    onOpenVault = onOpenVault,
                    onCreateVault = onCreateVault,
                    onOpenDarkWeb = onOpenDarkWeb
                )
            }
        }
    }

    if (showEditDialog && uiState is ProfileUiState.Success) {
        EditProfileDialog(
            user = (uiState as ProfileUiState.Success).user,
            onDismiss = { viewModel.hideEditDialog() },
            onSave = { newName ->
                viewModel.updateProfile(newName)
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
            .padding(top = AppSpacing.lg)
    ) {
        ProfileHeader(
            user = user
        )

        Column(modifier = Modifier.padding(AppSpacing.lg)) {
            ProfileActionsSection(
                onEditProfile = onEditProfile,
                onLogout = onLogout
            )

            Spacer(modifier = Modifier.height(AppSpacing.md))

            VaultStatusSection(
                onOpenVault = onOpenVault,
                onCreateVault = onCreateVault
            )

            Spacer(modifier = Modifier.height(AppSpacing.md))
            Card(
                colors = CardDefaults.cardColors(containerColor = AppColors.surface),
                shape = RoundedCornerShape(AppCorners.large),
                modifier = Modifier.fillMaxWidth().clickable { onOpenDarkWeb() }
            ) {
                Row(
                    modifier = Modifier.padding(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = "Dark Web",
                        tint = AppColors.primary
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.md))
                    Column {
                        Text(
                            text = "Dark Web Monitoring",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.textPrimary
                        )
                        Text(
                            text = "Check for data breaches",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultStatusSection(
    onOpenVault: () -> Unit,
    onCreateVault: () -> Unit
) {
    val viewModel: VaultStatusViewModel = hiltViewModel()
    val status by viewModel.status.collectAsState()

    LaunchedEffect(Unit) { viewModel.checkStatus() }

    when (status) {
        is VaultStatusUiState.Loading -> {}
        is VaultStatusUiState.NoVault -> {}
        is VaultStatusUiState.VaultExists -> {}
        is VaultStatusUiState.Error -> {}
    }

    Spacer(modifier = Modifier.height(AppSpacing.md))

    Card(
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        shape = RoundedCornerShape(AppCorners.large),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(AppCorners.medium))
                    .background(AppColors.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(AppSpacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Password Vault",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AppColors.textPrimary
                )
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                val statusText = when (status) {
                    is VaultStatusUiState.Loading -> "Checking status..."
                    is VaultStatusUiState.NoVault -> "Vault not configured"
                    is VaultStatusUiState.VaultExists -> "Vault configured"
                    is VaultStatusUiState.Error -> (status as VaultStatusUiState.Error).message
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.textSecondary
                )
            }
        }

        when (status) {
            is VaultStatusUiState.NoVault -> {
                Button(
                    onClick = onCreateVault,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.lg)
                        .padding(bottom = AppSpacing.lg),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.primary
                    ),
                    shape = RoundedCornerShape(AppCorners.medium)
                ) {
                    Text("Create Master Password")
                }
            }
            is VaultStatusUiState.VaultExists -> {
                Button(
                    onClick = onOpenVault,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.lg)
                        .padding(bottom = AppSpacing.lg),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.primary
                    ),
                    shape = RoundedCornerShape(AppCorners.medium)
                ) {
                    Text("Open Vault")
                }
            }
            is VaultStatusUiState.Error -> {
                OutlinedButton(
                    onClick = { viewModel.checkStatus() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.lg)
                        .padding(bottom = AppSpacing.lg),
                    shape = RoundedCornerShape(AppCorners.medium)
                ) {
                    Text("Retry")
                }
            }
            is VaultStatusUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AppColors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
