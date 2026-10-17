package tn.esprit.dam.features.scan.presentation.scan

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tn.esprit.dam.R
import tn.esprit.dam.core.ui.theme.AppColors
import tn.esprit.dam.core.ui.theme.AppCorners
import tn.esprit.dam.core.ui.theme.AppSpacing
import tn.esprit.dam.core.ui.theme.AppTypography
import tn.esprit.dam.features.scan.domain.RiskLevel
import tn.esprit.dam.features.scan.presentation.home.HomeShieldHero
import tn.esprit.dam.features.scan.presentation.home.RiskCountsRow
import tn.esprit.dam.features.scan.presentation.home.RiskPill
import tn.esprit.dam.features.scan.presentation.model.LocalAppInfo
import tn.esprit.dam.features.scan.presentation.model.ScanState

/** Result filters, matched on the app's risk level (not on its display label). */
private enum class ResultFilter(@StringRes val label: Int) {
    High(R.string.home_count_high),
    Medium(R.string.home_count_medium),
    Safe(R.string.home_count_safe),
    All(R.string.scan_filter_all);

    fun matches(app: LocalAppInfo): Boolean {
        val level = app.riskResult?.riskLevel
        return when (this) {
            High -> level == RiskLevel.HIGH || level == RiskLevel.CRITICAL
            Medium -> level == RiskLevel.MEDIUM
            Safe -> level == RiskLevel.LOW || level == RiskLevel.SAFE
            All -> true
        }
    }
}

@Composable
fun ResultsContent(
    scanState: ScanState,
    analysisNote: String?,
    onViewDetails: (String) -> Unit,
    onNewScan: () -> Unit,
    onImportApk: () -> Unit,
    modifier: Modifier = Modifier
) {
    val apps = scanState.selectedApps
    var filter by rememberSaveable {
        mutableStateOf(if (apps.any { ResultFilter.High.matches(it) }) ResultFilter.High else ResultFilter.All)
    }
    val filteredApps = remember(apps, filter) {
        apps.filter { filter.matches(it) }.sortedBy { it.riskResult?.score ?: Int.MAX_VALUE }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.background),
        contentPadding = PaddingValues(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        item {
            HomeShieldHero(
                score = scanState.averageScore.toInt(),
                appsToCheck = scanState.highRiskCount,
                caption = stringResource(R.string.scan_results_caption, apps.size),
                actionText = stringResource(R.string.scan_new_scan),
                onScanClick = onNewScan
            )
        }

        item {
            RiskCountsRow(
                high = scanState.highRiskCount,
                medium = scanState.mediumRiskCount,
                safe = scanState.lowRiskCount
            )
        }

        if (scanState.mlRecommendations.isNotEmpty() || analysisNote != null) {
            item {
                MLRecommendationsCard(
                    recommendations = scanState.mlRecommendations,
                    riskFactors = scanState.mlRiskFactors,
                    note = analysisNote
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                ResultFilter.entries.forEach { option ->
                    FilterChip(
                        label = stringResource(option.label),
                        count = apps.count { option.matches(it) },
                        selected = option == filter,
                        onClick = { filter = option }
                    )
                }
            }
        }

        if (filteredApps.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.scan_empty_filter),
                    style = AppTypography.bodyMedium,
                    color = AppColors.textTertiary,
                    modifier = Modifier.padding(vertical = AppSpacing.md)
                )
            }
        }

        items(filteredApps, key = { it.packageName }) { app ->
            AppResultRow(app = app, onClick = { onViewDetails(app.packageName) })
        }
    }
}

@Composable
private fun FilterChip(label: String, count: Int, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Text(
        text = "$label $count",
        style = AppTypography.labelMedium,
        color = if (selected) AppColors.onPrimary else AppColors.textSecondary,
        modifier = Modifier
            .clip(shape)
            .background(if (selected) AppColors.primary else AppColors.surface)
            .border(1.dp, if (selected) AppColors.primary else AppColors.surfaceVariant, shape)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
    )
}

@Composable
private fun AppResultRow(app: LocalAppInfo, onClick: () -> Unit) {
    val risk = app.riskResult
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.large))
            .background(AppColors.surface)
            .clickable(onClick = onClick)
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
                text = stringResource(R.string.scan_app_meta, risk?.score ?: 0, app.trackers.size),
                style = AppTypography.labelMedium,
                color = AppColors.textTertiary
            )
        }
        if (risk != null) {
            Spacer(modifier = Modifier.width(AppSpacing.sm))
            RiskPill(score = risk.score.toFloat())
        }
    }
}

data class AppRiskData(val label: String, val color: Color, val score: Int)

fun deriveAppRisk(app: LocalAppInfo): AppRiskData {
    val risk = app.riskResult ?: return AppRiskData(RiskLevel.SAFE.label, AppColors.textMuted, 0)
    val color = when (risk.riskLevel) {
        RiskLevel.CRITICAL, RiskLevel.HIGH -> AppColors.riskHigh
        RiskLevel.MEDIUM -> AppColors.riskMedium
        RiskLevel.LOW, RiskLevel.SAFE -> AppColors.riskLow
    }
    return AppRiskData(label = risk.riskLevel.label, color = color, score = risk.score)
}
