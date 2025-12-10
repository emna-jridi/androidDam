package tn.esprit.dam.screens.topapps

import tn.esprit.dam.ui.components.scan.ScanResultCard
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.screens.scan.ScanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppsScreen(
    onBack: () -> Unit,
    onAppDetails: (String) -> Unit
) {
    val topAppsVM: TopAppsViewModel = hiltViewModel()
    val scanVM: ScanViewModel = hiltViewModel()

    // Attach ScanViewModel only once
    LaunchedEffect(Unit) {
        topAppsVM.attachScanViewModel(scanVM)
    }

    val uiState by topAppsVM.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Top Apps") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(Modifier.padding(padding)) {

            // ---------------- TABS ----------------
            TabRow(
                selectedTabIndex = if (uiState.selectedTab == TopAppsTab.SAFE) 0 else 1
            ) {

                Tab(
                    selected = uiState.selectedTab == TopAppsTab.SAFE,
                    onClick = { topAppsVM.selectTab(TopAppsTab.SAFE) },
                    text = { Text("Safe Apps") },
                    icon = { Icon(Icons.Default.Shield, null) }
                )

                Tab(
                    selected = uiState.selectedTab == TopAppsTab.DANGEROUS,
                    onClick = { topAppsVM.selectTab(TopAppsTab.DANGEROUS) },
                    text = { Text("Dangerous Apps") },
                    icon = { Icon(Icons.Default.Warning, null) }
                )
            }

            // ---------------- CONTENT ----------------
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    Text(
                        uiState.error!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                else -> {
                    val apps = when (uiState.selectedTab) {
                        TopAppsTab.SAFE -> uiState.safeApps
                        TopAppsTab.DANGEROUS -> uiState.dangerousApps
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(apps) { app ->
                            ScanResultCard(
                                result = app,
                                onClick = { onAppDetails(app.packageName) }
                            )
                        }
                    }
                }
            }
        }
    }
}
