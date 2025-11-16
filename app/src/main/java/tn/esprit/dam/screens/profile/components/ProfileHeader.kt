package tn.esprit.dam.screens.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import tn.esprit.dam.data.model.User
import java.io.File
@Composable
fun ProfileHeader(
    user: User,
    localAvatarPath: String?,
    onEditAvatar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF7C3AED),
                            Color(0xFF9333EA)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2139))
                    .padding(6.dp)
            ) {
                // ✅ Priorité 1 : URL depuis le serveur (Coil gère le cache)
                // ✅ Priorité 2 : Fichier local si téléchargé
                // ✅ Priorité 3 : Fallback Dicebear
                val avatarModel = when {
                    user.avatarFileName != null -> user.getAvatarUrl() // ✅ URL SVG
                    localAvatarPath != null -> File(localAvatarPath)
                    else -> "https://api.dicebear.com/7.x/avataaars/svg?seed=${user.email}"
                }

                AsyncImage(
                    model = avatarModel,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFF2D3250)),
                    contentScale = ContentScale.Crop
                )

                // Bouton édition
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7C3AED))
                        .clickable(onClick = onEditAvatar)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "Modifier avatar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                user.name,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB4B4C6)
            )
        }
    }
}