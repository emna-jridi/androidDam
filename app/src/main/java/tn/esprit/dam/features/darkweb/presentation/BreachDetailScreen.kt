package tn.esprit.dam.features.darkweb.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.components.AppEmptyState
import tn.esprit.dam.core.ui.components.AppPrimaryButton
import tn.esprit.dam.core.ui.components.AppSkeletonList
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.darkweb.data.model.Breach

@Composable
fun BreachDetailScreen(
    onBackClick: () -> Unit,
    breachId: String,
    viewModel: DarkWebViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val breach = uiState.breaches.find { it._id == breachId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = AppColors.textPrimary)
            }
            Text(
                text = breach?.source.orEmpty(),
                style = AppTypography.titleMedium,
                color = AppColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        when {
            breach != null -> BreachDetailContent(breach = breach, onResolve = { viewModel.resolveBreach(breach._id) })
            // The list loads on open; don't flash "not found" while it does.
            uiState.isLoading ->
                AppSkeletonList(itemCount = 3, modifier = Modifier.padding(AppSpacing.md))
            else -> AppEmptyState(
                icon = Icons.Default.VerifiedUser,
                title = stringResource(R.string.breach_not_found),
                message = "",
                actionText = stringResource(R.string.back),
                onAction = onBackClick
            )
        }
    }
}

@Composable
private fun BreachDetailContent(breach: Breach, onResolve: () -> Unit) {
    val date = formatBreachDate(breach.breachDate, "d MMMM yyyy") ?: stringResource(R.string.darkweb_unknown_date)
    val dataClasses = breach.dataClasses.sortedByDescending { it.isSensitiveData() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = AppSpacing.md, end = AppSpacing.md, bottom = AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppCorners.xlarge))
                .background(AppColors.surface)
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BreachAvatar(source = breach.source, resolved = breach.isResolved, size = 56.dp)
            Spacer(modifier = Modifier.width(AppSpacing.md))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(text = breach.source, style = AppTypography.titleLarge, color = AppColors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                breach.domain?.takeIf { it.isNotBlank() }?.let {
                    Text(text = it, style = AppTypography.labelMedium, color = AppColors.textTertiary)
                }
                Text(text = stringResource(R.string.darkweb_breach_date, date), style = AppTypography.labelMedium, color = AppColors.textTertiary)
                BreachStatusPill(resolved = breach.isResolved)
            }
        }

        breach.description?.takeIf { it.isNotBlank() }?.let {
            Text(text = it, style = AppTypography.bodyMedium, color = AppColors.textSecondary)
        }

        Section(title = pluralStringResource(R.plurals.darkweb_exposed_data, dataClasses.size, dataClasses.size)) {
            DataClassChips(dataClasses)
            Text(text = stringResource(insightFor(dataClasses)), style = AppTypography.bodyMedium, color = AppColors.textSecondary)
        }

        Section(title = stringResource(R.string.darkweb_what_to_do)) {
            todosFor(dataClasses).forEach { (res, needsSource) ->
                TodoLine(
                    text = if (needsSource) stringResource(res, breach.source) else stringResource(res),
                    done = breach.isResolved
                )
            }
        }

        if (breach.isResolved) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppCorners.medium))
                    .background(AppColors.riskLow.copy(alpha = 0.12f))
                    .padding(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = AppColors.riskLow)
                Spacer(modifier = Modifier.width(AppSpacing.sm))
                Text(stringResource(R.string.darkweb_resolved_banner), style = AppTypography.bodyMedium, color = AppColors.textSecondary)
            }
        } else {
            AppPrimaryButton(text = stringResource(R.string.mark_as_resolved), onClick = onResolve)
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.large))
            .background(AppColors.surface)
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Text(text = title, style = AppTypography.titleMedium, color = AppColors.textPrimary)
        content()
    }
}

@Composable
private fun TodoLine(text: String, done: Boolean) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (done) AppColors.riskLow else AppColors.textMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(AppSpacing.sm))
        Box(modifier = Modifier.weight(1f)) {
            Text(text = text, style = AppTypography.bodyMedium, color = AppColors.textSecondary)
        }
    }
}

@StringRes
private fun insightFor(dataClasses: List<String>): Int {
    fun has(word: String) = dataClasses.any { it.contains(word, ignoreCase = true) }
    return when {
        has("password") -> R.string.darkweb_insight_passwords
        has("credit") || has("bank") || has("card") -> R.string.darkweb_insight_financial
        has("email") || has("phone") -> R.string.darkweb_insight_contact
        else -> R.string.darkweb_insight_generic
    }
}

/** Steps for this breach, most important first. The flag says whether the string takes the source name. */
private fun todosFor(dataClasses: List<String>): List<Pair<Int, Boolean>> {
    fun has(word: String) = dataClasses.any { it.contains(word, ignoreCase = true) }
    return buildList {
        if (has("password")) add(R.string.darkweb_todo_password to false)
        if (has("email")) add(R.string.darkweb_todo_email to true)
        if (has("phone")) add(R.string.darkweb_todo_phone to false)
        add(R.string.darkweb_todo_2fa to false)
        add(R.string.darkweb_todo_monitor to false)
    }.take(4)
}
