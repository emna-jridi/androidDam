package tn.esprit.dam.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import tn.esprit.dam.data.ApiClient
import tn.esprit.dam.data.TokenManager
import tn.esprit.dam.data.UpdateUserRequest
import tn.esprit.dam.data.User
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToSecurity: () -> Unit = {} // Pour Security Scan
) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Charger le profil
    fun loadProfile() {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                val token = TokenManager.getAccessToken(context)
                if (token != null) {
                    user = ApiClient.getProfile(token)
                } else {
                    errorMessage = "Token non trouvé"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Erreur de chargement"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadProfile() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Mon Profil",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { loadProfile() }) { Text("Réessayer") }
                    }
                }
            }
            user != null -> {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .offset(y = (-50).dp)
                            .size(120.dp)
                            .clip(CircleShape)
                            .clickable { showEditDialog = true }
                    ) {
                        val avatarUrl =
                            user!!.avatar?.takeIf { it.isNotBlank() }
                                ?: "https://api.dicebear.com/8.x/adventurer/svg?seed=${user!!.email}"
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Modifier",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(-30.dp))
                    Text(user!!.name ?: "Nom non défini", style = MaterialTheme.typography.headlineMedium)
                    Text(user!!.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(24.dp))

                    ProfileInfoCard(Icons.Filled.Phone, "Téléphone", user!!.phone ?: "Non renseigné")
                    Spacer(modifier = Modifier.height(12.dp))
                    ProfileInfoCard(Icons.Filled.Home, "Adresse", user!!.address ?: "Non renseignée")
                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { showEditDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Edit, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Modifier le profil")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 🔒 Security Scan Button
                    OutlinedButton(
                        onClick = { onNavigateToSecurity() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Security, contentDescription = "Security Scan")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Security Scan")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                TokenManager.clearTokens(context)
                                onLogout()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Filled.ExitToApp, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Déconnexion")
                    }
                }
            }
        }
    }

    if (showEditDialog && user != null) {
        EditProfileDialog(
            user = user!!,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newPhone, newAddress, newAvatarUrl ->
                scope.launch {
                    try {
                        val token = TokenManager.getAccessToken(context)
                        if (token != null) {
                            val updateRequest = UpdateUserRequest(
                                name = newName ?: user!!.name,
                                phone = newPhone ?: user!!.phone,
                                address = newAddress ?: user!!.address,
                                avatar = newAvatarUrl ?: user!!.avatar
                            )
                            val updatedUser = ApiClient.updateProfile(token, updateRequest)
                            user = updatedUser
                            showEditDialog = false
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message
                    }
                }
            }
        )
    }
}
@Composable
fun ProfileInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (String?, String?, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf(user.name ?: "") }
    var phone by remember { mutableStateOf(user.phone ?: "") }
    var address by remember { mutableStateOf(user.address ?: "") }

    var previewAvatarUrl by remember {
        mutableStateOf(
            user.avatar?.takeIf { it.isNotBlank() }
                ?: "https://api.dicebear.com/8.x/adventurer/svg?seed=${user.email}"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier le profil") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Avatar Preview avec bouton de génération
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        AsyncImage(
                            model = previewAvatarUrl,
                            contentDescription = "Avatar preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bouton pour générer un nouvel avatar
                    Button(
                        onClick = {
                            val timestamp = System.currentTimeMillis()
                            previewAvatarUrl = "https://api.dicebear.com/8.x/adventurer/svg?seed=${user.email}-$timestamp"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Générer un nouvel avatar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Filled.Person, null) },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Téléphone") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Filled.Phone, null) },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Adresse") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Filled.Home, null) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Envoyer seulement les champs qui ont changé
                    val currentName = user.name ?: ""
                    val currentPhone = user.phone ?: ""
                    val currentAddress = user.address ?: ""

                    onSave(
                        if (name != currentName) name else null,
                        if (phone != currentPhone) phone else null,
                        if (address != currentAddress) address else null,
                        if (previewAvatarUrl != user.avatar) previewAvatarUrl else null
                    )
                }
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}