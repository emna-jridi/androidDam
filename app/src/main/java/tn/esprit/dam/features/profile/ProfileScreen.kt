package tn.esprit.dam.features.profile

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.data.model.User
import tn.esprit.dam.features.profile.components.*
import tn.esprit.dam.ui.theme.*
import tn.esprit.dam.features.scan.presentation.ScanTheme

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onOpenVault: () -> Unit,
    onCreateVault: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()
    val showAvatarCustomizer by viewModel.showAvatarCustomizer.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
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
                    ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.loadProfile() },
                    onLogout = { viewModel.logout(onLogout) },
                    isSessionExpired = state.message.contains("session") || state.message.contains("Unauthorized")
                )
            }

            is ProfileUiState.Success -> {
                    ProfileContent(
                    user = state.user,
                    localAvatarPath = state.localAvatarPath,
                    onEditProfile = { viewModel.showEditDialog() },
                    onEditAvatar = { viewModel.showAvatarCustomizer() },
                    onLogout = { viewModel.logout(onLogout) },
                    onOpenVault = onOpenVault,
                    onCreateVault = onCreateVault
                )
            }
        }
    }

    // Dialog modification nom
    if (showEditDialog && uiState is ProfileUiState.Success) {
        EditProfileDialog(
            user = (uiState as ProfileUiState.Success).user,
            onDismiss = { viewModel.hideEditDialog() },
            onSave = { newName ->
                viewModel.updateProfile(newName)
            }
        )
    }

    // Dialog personnalisation avatar
    if (showAvatarCustomizer && uiState is ProfileUiState.Success) {
        val user = (uiState as ProfileUiState.Success).user

        AvatarCustomizerDialog(
            userHash = user.userHash ?: user.id ?: "default",
            onDismiss = { viewModel.hideAvatarCustomizer() },
            onSaveSuccess = {
                // ✅ Recharger le profil après la sauvegarde
                viewModel.updateAvatarAfterCustomization()
            }
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onLogout: () -> Unit = {},
    isSessionExpired: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (isSessionExpired) DangerRed else WarningOrange,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isSessionExpired) {
                        Button(
                            onClick = onRetry,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary
                            )
                        ) {
                            Text("Réessayer", color = Color.White)
                        }
                    }
                    if (isSessionExpired) {
                        Button(
                            onClick = onLogout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DangerRed
                            )
                        ) {
                            Text("Se reconnecter", color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = onLogout,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DangerRed
                            )
                        ) {
                            Text("Quitter", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ProfileContent(
    user: User,
    localAvatarPath: String?, // âœ… ParamÃ¨tre requis
    onEditProfile: () -> Unit,
    onEditAvatar: () -> Unit,
    onLogout: () -> Unit,
    onOpenVault: () -> Unit,
    onCreateVault: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeader(
            user = user,
            localAvatarPath = localAvatarPath, // âœ… Passer au header
            onEditAvatar = onEditAvatar
        )

        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ProfileActionsSection(
                onEditProfile = onEditProfile,
                onLogout = onLogout
            )

            // Password Vault section
            VaultStatusSection(
                onOpenVault = onOpenVault,
                onCreateVault = onCreateVault
            )
        }
    }
}

@Composable
private fun VaultStatusSection(
    onOpenVault: () -> Unit,
    onCreateVault: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { VaultStatusViewModel(context) }
    val status by viewModel.status.collectAsState()

    LaunchedEffect(Unit) { viewModel.checkStatus() }

    // Debug logs for UI state transitions
    when (status) {
        is VaultStatusUiState.Loading -> android.util.Log.d("VaultStatusUI", "Loading status...")
        is VaultStatusUiState.NoVault -> android.util.Log.d("VaultStatusUI", "No vault configured")
        is VaultStatusUiState.VaultExists -> android.util.Log.d("VaultStatusUI", "Vault exists")
        is VaultStatusUiState.Error -> android.util.Log.e("VaultStatusUI", (status as VaultStatusUiState.Error).message)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        shape = RoundedCornerShape(tn.esprit.dam.features.scan.presentation.ScanTheme.CornerLarge),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Password Vault",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            val statusText = when (status) {
                is VaultStatusUiState.Loading -> "Checking status..."
                is VaultStatusUiState.NoVault -> "Vault not configured"
                is VaultStatusUiState.VaultExists -> "Vault configured"
                is VaultStatusUiState.Error -> (status as VaultStatusUiState.Error).message
            }
            Text(statusText, color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                when (status) {
                    is VaultStatusUiState.NoVault -> {
                        Button(onClick = onCreateVault) {
                            Text("Create Master Password")
                        }
                    }
                    is VaultStatusUiState.VaultExists -> {
                        Button(onClick = onOpenVault) {
                            Text("Open Vault")
                        }
                    }
                    is VaultStatusUiState.Error -> {
                        OutlinedButton(onClick = { viewModel.checkStatus() }) {
                            Text("Retry")
                        }
                    }
                    else -> {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
