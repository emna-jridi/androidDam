package tn.esprit.dam.screens.profile

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
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.esprit.dam.data.model.User
import tn.esprit.dam.screens.profile.components.*

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToSecurity: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()
    val showAvatarCustomizer by viewModel.showAvatarCustomizer.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile(context)
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
                    onRetry = { viewModel.loadProfile(context) }
                )
            }

            is ProfileUiState.Success -> {
                ProfileContent(
                    user = state.user,
                    localAvatarPath = state.localAvatarPath,
                    onEditProfile = { viewModel.showEditDialog() },
                    onEditAvatar = { viewModel.showAvatarCustomizer() },
                    onNavigateToSecurity = onNavigateToSecurity,
                    onLogout = { viewModel.logout(context, onLogout) }
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
                viewModel.updateProfile(context, newName)
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
                viewModel.updateAvatarAfterCustomization(context)
            }
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
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
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C3AED)
                    )
                ) {
                    Text("Réessayer", color = Color.White)
                }
            }
        }
    }
}


@Composable
private fun ProfileContent(
    user: User,
    localAvatarPath: String?, // ✅ Paramètre requis
    onEditProfile: () -> Unit,
    onEditAvatar: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeader(
            user = user,
            localAvatarPath = localAvatarPath, // ✅ Passer au header
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
                onNavigateToSecurity = onNavigateToSecurity,
                onLogout = onLogout
            )
        }
    }
}
