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

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
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
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0E27),
                        Color(0xFF1A1F3A)
                    )
                )
            )
    ) {
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF7C3AED))
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
                    onLogout = { viewModel.logout(onLogout) }
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
                containerColor = Color(0xFF1E2139)
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (isSessionExpired) Color(0xFFEF4444) else Color(0xFFF59E0B),
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
                                containerColor = Color(0xFF7C3AED)
                            )
                        ) {
                            Text("RÃ©essayer", color = Color.White)
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
    localAvatarPath: String?, // âœ… ParamÃ¨tre requis
    onEditProfile: () -> Unit,
    onEditAvatar: () -> Unit,
    onLogout: () -> Unit
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
        }
    }
}
