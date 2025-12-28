package tn.esprit.dam.features.profile

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
import tn.esprit.dam.data.model.User
import tn.esprit.dam.features.profile.components.*
import tn.esprit.dam.ui.theme.*
import tn.esprit.dam.features.scan.presentation.ScanTheme

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
            .background(ScanTheme.DarkBg)
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
                    onEditProfile = { viewModel.showEditDialog() },
                    onLogout = { viewModel.logout(onLogout) },
                    onOpenVault = onOpenVault,
                    onCreateVault = onCreateVault,
                    onOpenDarkWeb = onOpenDarkWeb
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
            shape = RoundedCornerShape(ScanTheme.CornerLarge),
            colors = CardDefaults.cardColors(
                containerColor = ScanTheme.CardBg
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (isSessionExpired) Color(0xFFEF4444) else Color(0xFFFB923C),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    message,
                    color = ScanTheme.TextPrimary,
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
                                containerColor = Color(0xFF6366F1)
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
                                containerColor = Color(0xFFEF4444)
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
                                containerColor = Color(0xFFEF4444)
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
            .padding(top = ScanTheme.Spacing20)
    ) {
        ProfileHeader(
            user = user
        )

        Column(modifier = Modifier.padding(ScanTheme.Spacing24)) {
            // Profile Actions
            ProfileActionsSection(
                onEditProfile = onEditProfile,
                onLogout = onLogout
            )

            Spacer(modifier = Modifier.height(ScanTheme.Spacing16))

            // Password Vault section
            VaultStatusSection(
                onOpenVault = onOpenVault,
                onCreateVault = onCreateVault
            )

            // Dark Web Monitoring Section
            Spacer(modifier = Modifier.height(ScanTheme.Spacing16))
            Card(
                colors = CardDefaults.cardColors(containerColor = ScanTheme.CardBg),
                shape = RoundedCornerShape(ScanTheme.CornerLarge),
                modifier = Modifier.fillMaxWidth().clickable { onOpenDarkWeb() }
            ) {
                Row(
                    modifier = Modifier.padding(ScanTheme.Spacing16),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = "Dark Web",
                        tint = Color(0xFF6366F1)
                    )
                    Spacer(modifier = Modifier.width(ScanTheme.Spacing16))
                    Column {
                        Text(
                            text = "Dark Web Monitoring",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ScanTheme.TextPrimary
                        )
                        Text(
                            text = "Check for data breaches",
                            style = MaterialTheme.typography.bodySmall,
                            color = ScanTheme.TextSecondary
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

    Spacer(modifier = Modifier.height(ScanTheme.Spacing16))

    Card(
        colors = CardDefaults.cardColors(containerColor = ScanTheme.CardBg),
        shape = RoundedCornerShape(ScanTheme.CornerLarge),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ScanTheme.Spacing20),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(ScanTheme.CornerMedium))
                    .background(Color(0xFF7C3AED)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(ScanTheme.Spacing16))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Password Vault",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = ScanTheme.TextPrimary
                )
                Spacer(modifier = Modifier.height(ScanTheme.Spacing4))
                val statusText = when (status) {
                    is VaultStatusUiState.Loading -> "Checking status..."
                    is VaultStatusUiState.NoVault -> "Vault not configured"
                    is VaultStatusUiState.VaultExists -> "Vault configured"
                    is VaultStatusUiState.Error -> (status as VaultStatusUiState.Error).message
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = ScanTheme.TextSecondary
                )
            }
        }

        // Button at bottom
        when (status) {
            is VaultStatusUiState.NoVault -> {
                Button(
                    onClick = onCreateVault,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ScanTheme.Spacing20)
                        .padding(bottom = ScanTheme.Spacing20),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C3AED)
                    ),
                    shape = RoundedCornerShape(ScanTheme.CornerMedium)
                ) {
                    Text("Create Master Password")
                }
            }
            is VaultStatusUiState.VaultExists -> {
                Button(
                    onClick = onOpenVault,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ScanTheme.Spacing20)
                        .padding(bottom = ScanTheme.Spacing20),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C3AED)
                    ),
                    shape = RoundedCornerShape(ScanTheme.CornerMedium)
                ) {
                    Text("Open Vault")
                }
            }
            is VaultStatusUiState.Error -> {
                OutlinedButton(
                    onClick = { viewModel.checkStatus() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ScanTheme.Spacing20)
                        .padding(bottom = ScanTheme.Spacing20),
                    shape = RoundedCornerShape(ScanTheme.CornerMedium)
                ) {
                    Text("Retry")
                }
            }
            is VaultStatusUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ScanTheme.Spacing20),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF7C3AED),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
