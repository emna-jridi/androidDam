package tn.esprit.dam.features.scan.presentation.scan
import tn.esprit.dam.R
import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import tn.esprit.dam.features.scan.domain.model.ScanLevel
import tn.esprit.dam.features.scan.presentation.model.LocalAppInfo
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing

@Composable
fun ScanBottomBar(
    selectedCount: Int,
    onStartScan: () -> Unit
) {
    val canScan = selectedCount > 0

    Surface(
        color = AppColors.background,
        shadowElevation = 10.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg)
        ) {
            if (!canScan) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = AppSpacing.md),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = AppColors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(AppSpacing.sm))
                    Text(
                        stringResource(R.string.select_at_least_one_application),
                        color = AppColors.textSecondary
                    )
                }
            }

            Button(
                onClick = onStartScan,
                enabled = canScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(AppCorners.large),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canScan) AppColors.primaryLight else AppColors.surfaceVariant,
                    disabledContainerColor = AppColors.surfaceVariant
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (canScan) Icons.Default.Security else Icons.Default.Lock,
                        contentDescription = null,
                        tint = (if (canScan) AppColors.onPrimary else AppColors.textPrimary)
                    )
                    Column {
                        Text(
                            if (canScan) "Analyze Applications" else "Select apps",
                            color = (if (canScan) AppColors.onPrimary else AppColors.textPrimary),
                            fontWeight = FontWeight.Bold
                        )
                        if (canScan) {
                            Text(
                                "$selectedCount app${if (selectedCount > 1) "s" else ""}",
                                color = AppColors.onPrimary.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectionContent(
    availableApps: List<LocalAppInfo>,
    selectedCount: Int,
    error: String?,
    scanLevel: ScanLevel,
    onToggleApp: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onClearError: () -> Unit,
    onLevelChange: (ScanLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val userApps = availableApps.filterNot { it.isSystemApp }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 16.dp,
            bottom = 140.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HeaderCard(
                selectedCount = selectedCount,
                onSelectAll = onSelectAll,
                onDeselectAll = onDeselectAll,
                scanLevel = scanLevel,
                onLevelChange = onLevelChange
            )
        }

        if (error != null) {
            item {
                ErrorCard(error, onClearError)
            }
        }

        items(userApps) { app ->
            AppRow(
                app = app,
                onToggle = { onToggleApp(app.packageName) }
            )
        }
    }
}

@Composable
fun ErrorCard(
    message: String,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.surface
        ),
        border = BorderStroke(
            1.dp,
            AppColors.error.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(AppCorners.large)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = AppColors.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onClear) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.clear_error),
                    tint = AppColors.error
                )
            }
        }
    }
}

@Composable
fun HeaderCard(
    selectedCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    scanLevel: ScanLevel,
    onLevelChange: (ScanLevel) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppColors.surface),
            shape = RoundedCornerShape(AppCorners.large),
            border = BorderStroke(
                1.dp,
                if (selectedCount > 0) AppColors.primary.copy(alpha = 0.3f)
                else AppColors.surfaceVariant.copy(alpha = 0.2f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "$selectedCount application${if (selectedCount != 1) "s" else ""}",
                        color = AppColors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        stringResource(R.string.select_the_apps_you_want_to_analyze),
                        color = AppColors.textSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = if (selectedCount > 0) onDeselectAll else onSelectAll,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.primary
                    ),
                    shape = RoundedCornerShape(AppCorners.medium),
                    modifier = Modifier
                        .height(40.dp)
                        .padding(start = AppSpacing.md),
                    contentPadding = PaddingValues(horizontal = AppSpacing.md)
                ) {
                    Text(
                        if (selectedCount > 0) "Deselect All" else "Select All",
                        color = AppColors.onPrimary,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Text(
            stringResource(R.string.scan_type_2),
            color = AppColors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        ScanLevelToggle(scanLevel = scanLevel, onLevelChange = onLevelChange)
    }
}

@Composable
fun ScanLevelToggle(
    scanLevel: ScanLevel,
    onLevelChange: (ScanLevel) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LevelChip(
            label = stringResource(R.string.smart),
            description = stringResource(R.string.fast_scan),
            selected = scanLevel == ScanLevel.SMART,
            onClick = { onLevelChange(ScanLevel.SMART) },
            modifier = Modifier.weight(1f)
        )
        LevelChip(
            label = stringResource(R.string.deep),
            description = stringResource(R.string.thorough),
            selected = scanLevel == ScanLevel.DEEP,
            onClick = { onLevelChange(ScanLevel.DEEP) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun LevelChip(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = if (selected) AppColors.primary else AppColors.surface.copy(alpha = 0.5f)
    val border = if (selected) AppColors.primary else AppColors.textMuted.copy(alpha = 0.3f)
    val textColor = if (selected) AppColors.onPrimary else AppColors.textSecondary

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = container),
        border = BorderStroke(1.5.dp, border),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                description,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) AppColors.onPrimary.copy(alpha = 0.8f) else AppColors.textSecondary.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun AppRow(app: LocalAppInfo, onToggle: () -> Unit) {
    val selectedBgColor = AppColors.primary.copy(alpha = 0.12f)
    val selectedBorderColor = AppColors.primary
    val unselectedBg = AppColors.surface.copy(alpha = 0.5f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(role = Role.Checkbox) { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (app.isSelected) selectedBgColor else unselectedBg
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (app.isSelected) 2.dp else 0.dp
        ),
        border = BorderStroke(
            width = if (app.isSelected) 1.5.dp else 0.5.dp,
            color = if (app.isSelected) selectedBorderColor.copy(alpha = 0.4f)
                    else AppColors.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (app.isSelected) selectedBorderColor.copy(alpha = 0.1f)
                        else AppColors.border.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (app.icon != null) {
                    Image(
                        bitmap = app.icon.toBitmap(96, 96).asImageBitmap(),
                        contentDescription = app.displayName,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    Text(
                        text = app.displayName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (app.isSelected) selectedBorderColor else AppColors.textSecondary
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = app.displayName,
                    color = AppColors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    color = AppColors.textSecondary.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        if (app.isSelected) selectedBorderColor
                        else AppColors.surfaceVariant.copy(alpha = 0.4f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (app.isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.selected),
                        tint = AppColors.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
