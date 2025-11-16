package tn.esprit.dam.screens
/*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import kotlinx.coroutines.launch
import tn.esprit.dam.data.model.AppDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppsScreen(
    api: ShadowGuardApi,
    onBack: () -> Unit,
    onAppDetails: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var safeApps by remember { mutableStateOf<List<AppDetails>>(emptyList()) }
    var dangerousApps by remember { mutableStateOf<List<AppDetails>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                safeApps = api.getTopSafeApps()
                dangerousApps = api.getTopDangerousApps()
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Top Apps") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Safe Apps") },
                    icon = { Icon(Icons.Default.Shield, "Safe") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Dangerous Apps") },
                    icon = { Icon(Icons.Default.Warning, "Dangerous") }
                )
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> ErrorCard(error = error!!)
                else -> {
                    val apps = if (selectedTab == 0) safeApps else dangerousApps
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      /*  items(apps) { app ->
                            AppCard(app = app, onClick = { onAppDetails(app.packageName) })
                        }*/
                    }
                }
            }
        }
    }
}
*/