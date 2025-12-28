package tn.esprit.dam.features.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tn.esprit.dam.data.model.User
import tn.esprit.dam.features.scan.presentation.ScanTheme

@Composable
fun ProfileHeader(
    user: User
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScanTheme.Spacing24),
        shape = RoundedCornerShape(ScanTheme.CornerLarge),
        colors = CardDefaults.cardColors(
            containerColor = ScanTheme.CardBg
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ScanTheme.Spacing24),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(ScanTheme.SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Profile",
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(ScanTheme.Spacing16))

            // Name
            Text(
                text = user.name,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = ScanTheme.TextPrimary
            )

            Spacer(modifier = Modifier.height(ScanTheme.Spacing4))

            // Email
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = ScanTheme.TextSecondary
            )
        }
    }
}
