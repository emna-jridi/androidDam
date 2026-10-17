package tn.esprit.dam.features.vault.presentation.passwords

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.asString
import tn.esprit.dam.core.ui.components.AppEmptyState
import tn.esprit.dam.core.ui.components.AppErrorState
import tn.esprit.dam.core.ui.components.AppSkeletonList
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.vault.data.model.PasswordCategory
import tn.esprit.dam.features.vault.data.model.PasswordEntry
import tn.esprit.dam.features.vault.presentation.components.StrengthBucket
import tn.esprit.dam.features.vault.presentation.components.VaultSecurityDashboard
import tn.esprit.dam.features.vault.presentation.components.strengthBucket
import tn.esprit.dam.features.vault.presentation.components.strengthLabel

/** "All", "Favorites" and "Weak" filter locally; a category chip asks the ViewModel to filter. */
private sealed interface ListFilter {
    data object All : ListFilter
    data object Favorites : ListFilter
    data object Weak : ListFilter
    data class Category(val key: String) : ListFilter
}

@StringRes
private fun categoryLabel(key: String): Int = when (key) {
    PasswordCategory.SOCIAL -> R.string.vault_cat_social
    PasswordCategory.EMAIL -> R.string.vault_cat_email
    PasswordCategory.BANKING -> R.string.vault_cat_banking
    PasswordCategory.WORK -> R.string.vault_cat_work
    PasswordCategory.SHOPPING -> R.string.vault_cat_shopping
    PasswordCategory.ENTERTAINMENT -> R.string.vault_cat_entertainment
    else -> R.string.vault_cat_other
}

@Composable
fun PasswordListScreen(
    passwordViewModel: PasswordViewModel,
    onPasswordClick: (PasswordEntry) -> Unit,
    onAddClick: () -> Unit,
    onLockClick: () -> Unit
) {
    val listState by passwordViewModel.listState.collectAsState()
    val searchQuery by passwordViewModel.searchQuery.collectAsState()
    val selectedCategory by passwordViewModel.selectedCategory.collectAsState()
    var localFilter by remember { mutableStateOf<ListFilter>(ListFilter.All) }
    val filter = selectedCategory?.let { ListFilter.Category(it) } ?: localFilter

    LaunchedEffect(Unit) {
        passwordViewModel.loadPasswords()
    }

    val onFilter: (ListFilter) -> Unit = { picked ->
        when (picked) {
            is ListFilter.Category -> passwordViewModel.filterByCategory(picked.key)
            else -> {
                passwordViewModel.filterByCategory(null)
                localFilter = picked
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = AppSpacing.md, end = AppSpacing.sm, top = AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VaultSearchField(
                    query = searchQuery,
                    onQueryChange = { passwordViewModel.search(it) },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onLockClick) {
                    Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.vault_lock), tint = AppColors.textSecondary)
                }
            }

            FilterRow(selected = filter, onSelect = onFilter)

            when (val state = listState) {
                is PasswordListUiState.Loading -> AppSkeletonList(
                    itemCount = 5,
                    modifier = Modifier.padding(AppSpacing.md)
                )

                is PasswordListUiState.Error -> AppErrorState(
                    message = state.message.asString(),
                    onRetry = { passwordViewModel.loadPasswords() },
                    retryText = stringResource(R.string.retry)
                )

                is PasswordListUiState.Success -> {
                    val nothingStored = state.passwords.isEmpty() && searchQuery.isBlank() && selectedCategory == null
                    if (nothingStored) {
                        AppEmptyState(
                            icon = Icons.Default.Key,
                            title = stringResource(R.string.no_passwords_yet),
                            message = stringResource(R.string.tap_to_add_your_first_password),
                            actionText = stringResource(R.string.add_password),
                            onAction = onAddClick
                        )
                    } else {
                        PasswordList(
                            passwords = state.passwords,
                            filter = filter,
                            onPasswordClick = onPasswordClick,
                            onFavoriteClick = { passwordViewModel.toggleFavorite(it.id) },
                            onDelete = { passwordViewModel.deletePassword(it.id) }
                        )
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onAddClick,
            containerColor = AppColors.primary,
            contentColor = AppColors.onPrimary,
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.vault_add), style = AppTypography.labelLarge) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(AppSpacing.md)
        )
    }
}

@Composable
private fun PasswordList(
    passwords: List<PasswordEntry>,
    filter: ListFilter,
    onPasswordClick: (PasswordEntry) -> Unit,
    onFavoriteClick: (PasswordEntry) -> Unit,
    onDelete: (PasswordEntry) -> Unit
) {
    val shown = remember(passwords, filter) {
        when (filter) {
            ListFilter.Favorites -> passwords.filter { it.isFavorite }
            ListFilter.Weak -> passwords.filter { it.strengthBucket() == StrengthBucket.Weak }
            else -> passwords
        }.sortedWith(compareByDescending<PasswordEntry> { it.isFavorite }.thenBy { it.site.lowercase() })
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = AppSpacing.md, end = AppSpacing.md, top = AppSpacing.sm, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        if (filter == ListFilter.All && passwords.isNotEmpty()) {
            item { VaultSecurityDashboard(passwords = passwords, modifier = Modifier.padding(bottom = AppSpacing.sm)) }
        }
        if (shown.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.vault_no_match),
                    style = AppTypography.bodyMedium,
                    color = AppColors.textTertiary,
                    modifier = Modifier.padding(vertical = AppSpacing.lg)
                )
            }
        }
        items(shown, key = { it.id }) { entry ->
            PasswordCard(
                entry = entry,
                onClick = { onPasswordClick(entry) },
                onFavoriteClick = { onFavoriteClick(entry) },
                onDelete = { onDelete(entry) }
            )
        }
    }
}

@Composable
private fun VaultSearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        singleLine = true,
        placeholder = { Text(stringResource(R.string.search_passwords), color = AppColors.textMuted) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.textTertiary) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear), tint = AppColors.textTertiary)
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
private fun FilterRow(selected: ListFilter, onSelect: (ListFilter) -> Unit) {
    val options: List<Pair<ListFilter, Int>> = listOf(
        ListFilter.All to R.string.vault_filter_all,
        ListFilter.Favorites to R.string.vault_filter_favorites,
        ListFilter.Weak to R.string.vault_filter_weak
    ) + PasswordCategory.ALL.map { ListFilter.Category(it) to categoryLabel(it) }

    LazyRow(
        contentPadding = PaddingValues(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        items(options) { (option, label) ->
            val isSelected = option == selected
            val shape = RoundedCornerShape(50)
            Text(
                text = stringResource(label),
                style = AppTypography.labelMedium,
                color = if (isSelected) AppColors.onPrimary else AppColors.textSecondary,
                modifier = Modifier
                    .clip(shape)
                    .background(if (isSelected) AppColors.primary else AppColors.surface)
                    .border(1.dp, if (isSelected) AppColors.primary else AppColors.surfaceVariant, shape)
                    .clickable(role = Role.Tab) { onSelect(option) }
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PasswordCard(
    entry: PasswordEntry,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val bucket = entry.strengthBucket()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.large))
            .background(AppColors.surface)
            .combinedClickable(onClick = onClick, onLongClick = { showDeleteDialog = true })
            .padding(start = AppSpacing.md, top = AppSpacing.sm, bottom = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(AppCorners.medium))
                .background(AppColors.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.site.firstOrNull()?.uppercase() ?: "?",
                style = AppTypography.titleMedium,
                color = AppColors.primary
            )
        }
        Spacer(modifier = Modifier.width(AppSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.site,
                style = AppTypography.bodyMedium,
                color = AppColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = entry.username,
                style = AppTypography.labelMedium,
                color = AppColors.textTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        val label = entry.strengthLabel()
        if (bucket != null && label != null) {
            Spacer(modifier = Modifier.width(AppSpacing.sm))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(bucket.color.copy(alpha = 0.14f))
                    .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(6.dp).background(bucket.color, CircleShape))
                Spacer(modifier = Modifier.width(AppSpacing.xs))
                Text(text = stringResource(label), style = AppTypography.labelMedium, color = bucket.color)
            }
        }
        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (entry.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = stringResource(
                    if (entry.isFavorite) R.string.vault_favorite_remove else R.string.vault_favorite_add
                ),
                tint = if (entry.isFavorite) AppColors.warning else AppColors.textMuted
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = AppColors.surface,
            title = { Text(stringResource(R.string.delete_password_2), color = AppColors.textPrimary) },
            text = { Text(stringResource(R.string.vault_delete_confirm, entry.site), color = AppColors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.error, contentColor = AppColors.textPrimary)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel), color = AppColors.textSecondary)
                }
            }
        )
    }
}
