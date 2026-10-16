package tn.esprit.dam.features.scan.presentation.detail

import tn.esprit.dam.core.ui.asString
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.features.scan.domain.RiskLevel
import tn.esprit.dam.features.scan.domain.SecurityUtils
import tn.esprit.dam.features.scan.presentation.ScanErrorState
import tn.esprit.dam.features.scan.presentation.ScanLoadingState

@Composable
fun AppDetailScreen(
    packageName: String,
    viewModel: AppDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(packageName) {
        viewModel.loadAppDetails(packageName)
    }

    when {
        uiState.isLoading -> {
            ScanLoadingState("Loading details...")
        }
        uiState.error != null -> {
            ScanErrorState(
                error = uiState.error!!.asString(),
                onRetry = { viewModel.refresh(packageName) },
                textAlign = TextAlign.Center
            )
        }
        uiState.app != null -> {
            AppDetailContent(
                app = uiState.app!!,
                history = uiState.history,
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
private fun AppDetailContent(
    app: AppDetailUi,
    history: List<AppScanHistoryUi>,
    onBackClick: () -> Unit
) {
    val riskResult = remember(app) {
        SecurityUtils.calculateAppRisk(
            permissions = app.permissions,
            trackers = app.trackers.map { it.name },
            isSystemApp = false
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            DetailHeader(onBackClick = onBackClick)
        }

        item {
            AppHeaderWithRiskCard(app = app, riskResult = riskResult)
        }

        if (riskResult.riskLevel != RiskLevel.SAFE && riskResult.riskLevel != RiskLevel.LOW) {
            item {
                WhyFlaggedSection(app = app, riskResult = riskResult)
            }
        }

        item {
            PermissionsAnalysisSection(app = app)
        }

        item {
            DetectedTrackersSection(app = app)
        }

        item {
            RecommendationsSection(app = app, riskResult = riskResult)
        }

        if (history.isNotEmpty()) {
            item {
                AppHistorySection(history = history)
            }
        }
    }
}
