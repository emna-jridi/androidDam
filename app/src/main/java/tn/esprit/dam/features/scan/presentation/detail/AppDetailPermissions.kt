package tn.esprit.dam.features.scan.presentation.detail

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.scan.domain.RiskLevel
import tn.esprit.dam.features.scan.domain.SecurityUtils

/** How sensitive a permission is, for grouping. */
enum class PermissionGroup { Critical, High, Medium, Normal }

/** A permission as shown to the user: French name, what it allows, icon and risk group. */
data class PermissionUi(
    val raw: String,
    @StringRes val label: Int?,
    val fallbackLabel: String,
    @StringRes val description: Int,
    val icon: ImageVector,
    val group: PermissionGroup
) {
    val isSensitive: Boolean get() = group == PermissionGroup.Critical || group == PermissionGroup.High
}

fun String.toPermissionUi(): PermissionUi {
    val key = uppercase()
    val (label, description, icon) = when {
        "CAMERA" in key -> Triple(R.string.perm_camera, R.string.perm_camera_desc, Icons.Default.CameraAlt)
        "RECORD_AUDIO" in key -> Triple(R.string.perm_mic, R.string.perm_mic_desc, Icons.Default.Mic)
        "BACKGROUND_LOCATION" in key -> Triple(R.string.perm_location_bg, R.string.perm_location_bg_desc, Icons.Default.LocationOn)
        "FINE_LOCATION" in key -> Triple(R.string.perm_location_fine, R.string.perm_location_fine_desc, Icons.Default.LocationOn)
        "COARSE_LOCATION" in key -> Triple(R.string.perm_location_coarse, R.string.perm_location_coarse_desc, Icons.Default.LocationOn)
        "CONTACTS" in key -> Triple(R.string.perm_contacts, R.string.perm_contacts_desc, Icons.Default.Contacts)
        "SMS" in key || "MMS" in key -> Triple(R.string.perm_sms, R.string.perm_sms_desc, Icons.Default.Sms)
        "CALL_LOG" in key -> Triple(R.string.perm_call_log, R.string.perm_call_log_desc, Icons.Default.History)
        "PHONE" in key || "CALL" in key -> Triple(R.string.perm_phone, R.string.perm_phone_desc, Icons.Default.Call)
        "STORAGE" in key || "MEDIA_" in key -> Triple(R.string.perm_storage, R.string.perm_storage_desc, Icons.Default.Folder)
        "CALENDAR" in key -> Triple(R.string.perm_calendar, R.string.perm_calendar_desc, Icons.Default.CalendarMonth)
        "BODY_SENSORS" in key || "ACTIVITY_RECOGNITION" in key -> Triple(R.string.perm_sensors, R.string.perm_sensors_desc, Icons.Default.MonitorHeart)
        "BLUETOOTH" in key -> Triple(R.string.perm_bluetooth, R.string.perm_bluetooth_desc, Icons.Default.Bluetooth)
        "ACCOUNTS" in key -> Triple(R.string.perm_accounts, R.string.perm_accounts_desc, Icons.Default.AccountCircle)
        "INTERNET" in key -> Triple(R.string.perm_internet, R.string.perm_internet_desc, Icons.Default.Wifi)
        else -> Triple(null, R.string.perm_other_desc, Icons.Default.Tune)
    }
    val group = when (SecurityUtils.analyzePermission(this).risk) {
        RiskLevel.CRITICAL -> PermissionGroup.Critical
        RiskLevel.HIGH -> PermissionGroup.High
        RiskLevel.MEDIUM -> PermissionGroup.Medium
        RiskLevel.LOW, RiskLevel.SAFE -> PermissionGroup.Normal
    }
    return PermissionUi(this, label, substringAfterLast('.'), description, icon, group)
}

@Composable
fun PermissionUi.labelText(): String = label?.let { stringResource(it) } ?: fallbackLabel

private fun PermissionGroup.color(): Color = when (this) {
    PermissionGroup.Critical -> AppColors.riskCritical
    PermissionGroup.High -> AppColors.riskHigh
    PermissionGroup.Medium -> AppColors.riskMedium
    PermissionGroup.Normal -> AppColors.textTertiary
}

/** Permissions tab: sensitive groups open, normal permissions folded behind one row. */
@Composable
fun PermissionsTab(permissions: List<PermissionUi>) {
    var showNormal by rememberSaveable { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        if (permissions.isEmpty()) {
            Text(
                text = stringResource(R.string.detail_no_permissions),
                style = AppTypography.bodyMedium,
                color = AppColors.textTertiary
            )
            return@Column
        }

        listOf(
            PermissionGroup.Critical to R.string.detail_group_critical,
            PermissionGroup.High to R.string.detail_group_high,
            PermissionGroup.Medium to R.string.detail_group_medium
        ).forEach { (group, title) ->
            val items = permissions.filter { it.group == group }
            if (items.isNotEmpty()) {
                GroupHeader(title = stringResource(title), count = items.size, color = group.color())
                items.forEach { PermissionRow(it) }
            }
        }

        val normal = permissions.filter { it.group == PermissionGroup.Normal }
        if (normal.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppCorners.medium))
                    .background(AppColors.surface)
                    .clickable { showNormal = !showNormal }
                    .padding(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pluralStringResource(R.plurals.detail_group_normal, normal.size, normal.size),
                    style = AppTypography.bodyMedium,
                    color = AppColors.textSecondary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(if (showNormal) R.string.detail_hide else R.string.detail_show),
                    style = AppTypography.labelMedium,
                    color = AppColors.primary
                )
            }
            if (showNormal) normal.forEach { PermissionRow(it) }
        }
    }
}

@Composable
private fun GroupHeader(title: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = AppTypography.titleMedium, color = AppColors.textPrimary, modifier = Modifier.weight(1f))
        Text(
            text = count.toString(),
            style = AppTypography.labelMedium,
            color = color,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = 0.14f))
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
        )
    }
}

@Composable
fun PermissionRow(permission: PermissionUi) {
    val color = permission.group.color()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.medium))
            .background(AppColors.surface)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm + AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(AppCorners.medium))
                .background(color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(permission.icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(AppSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = permission.labelText(),
                style = AppTypography.bodyMedium,
                color = AppColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(permission.description),
                style = AppTypography.labelMedium,
                color = AppColors.textTertiary
            )
        }
    }
}
