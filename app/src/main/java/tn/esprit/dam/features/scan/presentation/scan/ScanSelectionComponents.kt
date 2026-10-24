package tn.esprit.dam.features.scan.presentation.scan

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.components.AppPrimaryButton
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.scan.presentation.model.LocalAppInfo

/** Sticky footer with the one primary action of the selection step. */
@Composable
fun ScanBottomBar(
    selectedCount: Int,
    onStartScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.background)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(AppSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppPrimaryButton(
            text = if (selectedCount > 0) {
                pluralStringResource(R.plurals.scan_analyze_n, selectedCount, selectedCount)
            } else {
                stringResource(R.string.scan_select_at_least_one)
            },
            onClick = onStartScan,
            enabled = selectedCount > 0
        )
    }
}

@Composable
fun SelectionContent(
    availableApps: List<LocalAppInfo>,
    selectedCount: Int,
    error: String?,
    onToggleApp: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    val userApps = remember(availableApps) { availableApps.filterNot { it.isSystemApp } }
    val shownApps = remember(userApps, query) {
        if (query.isBlank()) userApps
        else userApps.filter { it.displayName.contains(query.trim(), ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        item {
            ScanSearchField(query = query, onQueryChange = { query = it })
        }

        if (error != null) {
            item { ErrorCard(error, onClearError) }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.scan_selected_count, selectedCount, userApps.size),
                    style = AppTypography.titleMedium,
                    color = AppColors.textPrimary
                )
                TextButton(onClick = if (selectedCount > 0) onDeselectAll else onSelectAll) {
                    Text(
                        text = stringResource(
                            if (selectedCount > 0) R.string.scan_deselect_all else R.string.scan_select_all
                        ),
                        style = AppTypography.labelMedium,
                        color = AppColors.primary
                    )
                }
            }
        }

        if (shownApps.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.scan_no_app_found),
                    style = AppTypography.bodyMedium,
                    color = AppColors.textTertiary,
                    modifier = Modifier.padding(vertical = AppSpacing.lg)
                )
            }
        }

        items(shownApps, key = { it.packageName }) { app ->
            AppRow(app = app, onToggle = { onToggleApp(app.packageName) })
        }
    }
}

@Composable
private fun ScanSearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(stringResource(R.string.scan_search_hint), color = AppColors.textMuted) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.textTertiary) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_error), tint = AppColors.textTertiary)
                }
            }
        } else null,
        shape = RoundedCornerShape(AppCorners.medium),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppColors.textPrimary,
            unfocusedTextColor = AppColors.textPrimary,
            focusedBorderColor = AppColors.primary,
            unfocusedBorderColor = AppColors.surfaceVariant,
            focusedContainerColor = AppColors.surface,
            unfocusedContainerColor = AppColors.surface,
            cursorColor = AppColors.primary
        )
    )
}

@Composable
fun ErrorCard(
    message: String,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.large))
            .background(AppColors.error.copy(alpha = 0.10f))
            .border(1.dp, AppColors.error.copy(alpha = 0.4f), RoundedCornerShape(AppCorners.large))
            .padding(start = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            color = AppColors.errorLight,
            style = AppTypography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onClear) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_error), tint = AppColors.errorLight)
        }
    }
}

@Composable
fun AppRow(app: LocalAppInfo, onToggle: () -> Unit) {
    val shape = RoundedCornerShape(AppCorners.large)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppColors.surface)
            .border(1.dp, if (app.isSelected) AppColors.primary.copy(alpha = 0.45f) else AppColors.surface, shape)
            .clickable(role = Role.Checkbox, onClick = onToggle)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm + AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScanAppIcon(app = app, size = 40.dp)
        Spacer(modifier = Modifier.width(AppSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.displayName,
                style = AppTypography.bodyMedium,
                color = AppColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = app.category ?: stringResource(R.string.scan_app_permissions, app.permissions.size),
                style = AppTypography.labelMedium,
                color = AppColors.textTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(AppSpacing.sm))
        SelectBox(checked = app.isSelected)
    }
}

@Composable
private fun SelectBox(checked: Boolean) {
    val shape = RoundedCornerShape(AppCorners.small)
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(shape)
            .background(if (checked) AppColors.primary else AppColors.surface)
            .border(1.5.dp, if (checked) AppColors.primary else AppColors.surfaceVariant, shape),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                Icons.Default.Check,
                contentDescription = stringResource(R.string.selected),
                tint = AppColors.onPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/** The app's real launcher icon, or its first letter when the icon is not available. */
@Composable
fun ScanAppIcon(app: LocalAppInfo, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(AppCorners.medium))
            .background(AppColors.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val icon = app.icon
        if (icon != null) {
            val bitmap = remember(icon) { icon.toBitmap(96, 96).asImageBitmap() }
            Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.size(size))
        } else {
            Text(
                text = app.displayName.firstOrNull()?.uppercase() ?: "?",
                style = AppTypography.titleMedium,
                color = AppColors.textPrimary
            )
        }
    }
}
