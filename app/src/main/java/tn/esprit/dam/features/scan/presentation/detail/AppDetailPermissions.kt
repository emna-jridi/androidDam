package tn.esprit.dam.features.scan.presentation.detail
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.dam.features.scan.domain.SecurityUtils

@Composable
fun PermissionsAnalysisSection(app: AppDetailUi) {
    var showAllPermissions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = AppColors.primaryLight,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.permissions_analysis),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )
            }
            Surface(
                color = AppColors.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${app.permissions.size}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )
            }
        }

        val permissionsToShow = if (showAllPermissions) app.permissions else app.permissions.take(6)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    permissionsToShow.forEach { perm ->
                        EnhancedPermissionChip(permission = perm)
                    }
                }

                if (app.permissions.size > 6) {
                    TextButton(
                        onClick = { showAllPermissions = !showAllPermissions },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = if (showAllPermissions) "Show Less" else "Show All (${app.permissions.size})",
                            color = AppColors.primaryLight,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = if (showAllPermissions) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = AppColors.primaryLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedPermissionChip(permission: String) {
    val permissionData = remember(permission) {
        SecurityUtils.analyzePermission(permission)
    }

    val icon: ImageVector = when {
        permission.contains("CAMERA", ignoreCase = true) -> Icons.Default.CameraAlt
        permission.contains("LOCATION", ignoreCase = true) -> Icons.Default.LocationOn
        permission.contains("MICROPHONE", ignoreCase = true) || permission.contains("RECORD_AUDIO", ignoreCase = true) -> Icons.Default.Mic
        permission.contains("CONTACT", ignoreCase = true) -> Icons.Default.Contacts
        permission.contains("STORAGE", ignoreCase = true) || permission.contains("READ_EXTERNAL", ignoreCase = true) -> Icons.Default.Folder
        permission.contains("SMS", ignoreCase = true) -> Icons.Default.Message
        permission.contains("CALL", ignoreCase = true) || permission.contains("PHONE", ignoreCase = true) -> Icons.Default.Call
        permission.contains("INTERNET", ignoreCase = true) -> Icons.Default.Wifi
        permission.contains("BLUETOOTH", ignoreCase = true) -> Icons.Default.Bluetooth
        else -> Icons.Default.Security
    }

    val color: Color = when {
        permission.contains("CAMERA", ignoreCase = true) -> AppColors.error
        permission.contains("LOCATION", ignoreCase = true) -> AppColors.orange
        permission.contains("MICROPHONE", ignoreCase = true) || permission.contains("RECORD_AUDIO", ignoreCase = true) -> AppColors.errorDark
        permission.contains("CONTACT", ignoreCase = true) -> AppColors.accentViolet
        permission.contains("STORAGE", ignoreCase = true) || permission.contains("READ_EXTERNAL", ignoreCase = true) -> AppColors.info
        permission.contains("SMS", ignoreCase = true) -> AppColors.accentPink
        permission.contains("CALL", ignoreCase = true) || permission.contains("PHONE", ignoreCase = true) -> AppColors.success
        permission.contains("INTERNET", ignoreCase = true) -> AppColors.primary
        permission.contains("BLUETOOTH", ignoreCase = true) -> AppColors.accentSky
        else -> AppColors.textTertiary
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(width = 90.dp, height = 80.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = permission,
                modifier = Modifier.size(28.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = permissionData.label.take(10),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = AppColors.textPrimary,
                maxLines = 2,
                lineHeight = 12.sp
            )
        }
    }
}
