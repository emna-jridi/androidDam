package tn.esprit.dam.features.scan.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import java.util.Locale
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography

/** Example queries shown before the first search. App names, so not translated. */
private val POPULAR_QUERIES = listOf("TikTok", "WhatsApp", "Instagram", "Snapchat", "Facebook", "Telegram")

@Composable
fun AppSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    focusRequester: FocusRequester
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppSpacing.md)
            .focusRequester(focusRequester),
        singleLine = true,
        placeholder = { Text(stringResource(R.string.search_hint), color = AppColors.textMuted) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.primary) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.search_clear), tint = AppColors.textTertiary)
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        shape = RoundedCornerShape(AppCorners.large),
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

/** Before the first search: what this screen is for, and one-tap example queries. */
@Composable
fun SearchPlaceholder(onPick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppCorners.xlarge))
                .background(AppColors.surface)
                .padding(AppSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AppColors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ManageSearch, contentDescription = null, tint = AppColors.primary, modifier = Modifier.size(32.dp))
            }
            Text(
                text = stringResource(R.string.search_intro_title),
                style = AppTypography.titleMedium,
                color = AppColors.textPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.search_intro_desc),
                style = AppTypography.bodyMedium,
                color = AppColors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = stringResource(R.string.search_popular).uppercase(),
            style = AppTypography.labelMedium,
            color = AppColors.textTertiary
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            POPULAR_QUERIES.forEach { name ->
                val shape = RoundedCornerShape(50)
                Text(
                    text = name,
                    style = AppTypography.labelMedium,
                    color = AppColors.textSecondary,
                    modifier = Modifier
                        .clip(shape)
                        .background(AppColors.surface)
                        .border(1.dp, AppColors.surfaceVariant, shape)
                        .clickable { onPick(name) }
                        .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
                )
            }
        }
    }
}

@Composable
fun SearchResults(
    results: List<SearchAppUi>,
    onAppClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = AppSpacing.md, end = AppSpacing.md, bottom = AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        item {
            Text(
                text = pluralStringResource(R.plurals.search_results_n, results.size, results.size),
                style = AppTypography.labelMedium,
                color = AppColors.textTertiary
            )
        }
        items(results, key = { it.packageName }) { result ->
            SearchResultCard(result = result, onClick = { onAppClick(result.packageName) })
        }
    }
}

@Composable
fun SearchResultCard(result: SearchAppUi, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.large))
            .background(AppColors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm + AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StoreIcon(name = result.appName, url = result.iconUrl)
        Spacer(modifier = Modifier.width(AppSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.appName,
                style = AppTypography.bodyMedium,
                color = AppColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                result.rating?.let { rating ->
                    Icon(Icons.Default.Star, contentDescription = null, tint = AppColors.warning, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(AppSpacing.xs))
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f", rating),
                        style = AppTypography.labelMedium,
                        color = AppColors.textSecondary
                    )
                    if (result.developer != null) {
                        Text(text = "  ·  ", style = AppTypography.labelMedium, color = AppColors.textMuted)
                    }
                }
                result.developer?.let {
                    Text(
                        text = it,
                        style = AppTypography.labelMedium,
                        color = AppColors.textTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(AppSpacing.sm))
        Text(text = stringResource(R.string.search_analyze), style = AppTypography.labelMedium, color = AppColors.primary)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.primary)
    }
}

@Composable
private fun StoreIcon(name: String, url: String) {
    val letter: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(text = name.take(1).uppercase(), style = AppTypography.titleMedium, color = AppColors.textPrimary)
        }
    }
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(AppCorners.medium))
    ) {
        if (url.isBlank()) {
            letter()
        } else {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { letter() },
                error = { letter() }
            )
        }
    }
}
