package tn.esprit.dam.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import kotlinx.coroutines.launch
import tn.esprit.dam.data.AppDetails
import tn.esprit.dam.data.api.ShadowGuardApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailsScreen(
    packageName: String,
    api: ShadowGuardApi,
    onBack: () -> Unit
) {
    var appDetails by remember { mutableStateOf<AppDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(packageName) {
        scope.launch {
            try {
                appDetails = api.getAppDetails(packageName)
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
                title = { Text("App Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    ErrorCard(error = error!!)
                }
            }
            appDetails != null -> {
                AppDetailsContent(
                    app = appDetails!!,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
fun AppDetailsContent(app: AppDetails, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.name,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = app.developer,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Version: ${app.version}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        PrivacyScoreChip(score = app.privacyScore)
                    }

                    if (app.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = app.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Stats Card
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Statistics",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    StatRow(
                        icon = Icons.Default.Security,
                        label = "Trackers",
                        value = app.trackers.size.toString()
                    )
                    StatRow(
                        icon = Icons.Default.Lock,
                        label = "Permissions",
                        value = app.permissions.size.toString()
                    )
                    StatRow(
                        icon = Icons.Default.Category,
                        label = "Category",
                        value = app.category
                    )
                    if (app.isDebuggable) {
                        StatRow(
                            icon = Icons.Default.Warning,
                            label = "Debuggable",
                            value = "Yes"
                        )
                    }
                }
            }
        }

        // Trackers Section
        if (app.trackers.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Trackers (${app.trackers.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            items(app.trackers) { tracker ->
                ListItem(
                    headlineContent = { Text(tracker) },
                    leadingContent = {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
                HorizontalDivider()
            }
        }

        // Permissions Section
        if (app.permissions.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Permissions (${app.permissions.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            items(app.permissions) { permission ->
                ListItem(
                    headlineContent = { Text(permission.split(".").last()) },
                    supportingContent = { Text(permission, style = MaterialTheme.typography.bodySmall) },
                    leadingContent = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null
                        )
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun StatRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label)
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall
        )
    }
}