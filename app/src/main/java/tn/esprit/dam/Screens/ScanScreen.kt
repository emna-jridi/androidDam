// Screens/ScanScreen.kt
package tn.esprit.dam.Screens

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import tn.esprit.dam.data.AnalyzeInstalledAppsDto
import tn.esprit.dam.data.InstalledAppDto
import tn.esprit.dam.data.ScanResult
import tn.esprit.dam.data.api.ApiProvider
import tn.esprit.dam.data.appName
import tn.esprit.dam.data.privacyScore
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onAppDetails: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { ApiProvider.getApi() }

    var scanResults by remember { mutableStateOf<List<ScanResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var totalApps by remember { mutableStateOf(0) }
    var isScanning by remember { mutableStateOf(false) }

    // ✅ Fonction pour récupérer les apps dans un thread IO
    suspend fun getInstalledApps(): List<InstalledAppDto> = withContext(Dispatchers.IO) {
        try {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

            Log.d("ScanScreen", "Total installed packages: ${packages.size}")

            packages
                .filter { appInfo ->
                    // Filtrer uniquement les apps utilisateur
                    (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                }
                .mapNotNull { appInfo ->
                    try {
                        val packageInfo = pm.getPackageInfo(appInfo.packageName, 0)
                        val permissionInfo = try {
                            pm.getPackageInfo(appInfo.packageName, PackageManager.GET_PERMISSIONS)
                        } catch (e: Exception) {
                            null
                        }

                        InstalledAppDto(
                            packageName = appInfo.packageName,
                            appName = pm.getApplicationLabel(appInfo).toString(),
                            versionName = packageInfo.versionName ?: "Unknown",
                            versionCode = packageInfo.versionCode,
                            permissions = permissionInfo?.requestedPermissions?.toList() ?: emptyList()
                        )
                    } catch (e: Exception) {
                        Log.e("ScanScreen", "Error processing ${appInfo.packageName}", e)
                        null
                    }
                }
        } catch (e: Exception) {
            Log.e("ScanScreen", "Error getting installed apps", e)
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Apps") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    scope.launch {
                        isLoading = true
                        isScanning = true
                        error = null

                        try {
                            Log.d("ScanScreen", "🔍 Starting scan...")

                            val installedApps = getInstalledApps()
                            totalApps = installedApps.size
                            Log.d("ScanScreen", "✅ Found ${installedApps.size} user apps")

                            if (installedApps.isEmpty()) {
                                error = "No user apps found to scan"
                                return@launch
                            }

                            val userHash = UUID.randomUUID().toString()
                            val analyzeDto = AnalyzeInstalledAppsDto(
                                userHash = userHash,
                                apps = installedApps
                            )
                            Log.d("ScanScreen", "📤 Sending ${installedApps.size} apps to backend...")

                            val response = withContext(Dispatchers.IO) {
                                withTimeout(180_000) { // 3 minutes
                                    api.analyzeInstalledApps(analyzeDto)
                                }
                            }

                            scanResults = response.results
                            Log.d("ScanScreen", "✅ Received ${response.results.size} results")

                        } catch (e: TimeoutCancellationException) {
                            error = "Request timeout - Backend took too long to respond (>3 min)"
                            Log.e("ScanScreen", "❌ Timeout error", e)
                        } catch (e: io.ktor.client.network.sockets.ConnectTimeoutException) {
                            error = "Cannot connect to server. Check:\n• Backend is running\n• IP address is correct (${ApiProvider.BASE_URL})\n• Same WiFi network"
                            Log.e("ScanScreen", "❌ Connection timeout", e)
                        } catch (e: java.net.ConnectException) {
                            error = "Connection refused. Backend may not be running on the expected address."
                            Log.e("ScanScreen", "❌ Connection refused", e)
                        } catch (e: io.ktor.serialization.JsonConvertException) {
                            error = "Invalid response format from server. Check backend logs."
                            Log.e("ScanScreen", "❌ JSON parsing error", e)
                        } catch (e: Exception) {
                            error = "Error: ${e.message ?: "Unknown error"}\n\nCheck Logcat for details."
                            Log.e("ScanScreen", "❌ Unexpected error", e)
                            e.printStackTrace()
                        } finally {
                            isLoading = false
                            isScanning = false
                        }
                    }
                },
                icon = { Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Scan") },
                text = { Text(text = if (isScanning) "Scanning..." else "Start Scan") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Afficher le nombre d'apps trouvées
            if (totalApps > 0 && !isLoading) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Scan Complete",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Analyzed $totalApps user apps",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = if (totalApps > 0) "Analyzing $totalApps apps..." else "Collecting installed apps...",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This may take a few minutes...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(0.7f)
                        )
                    }
                }
            } else if (error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { error = null },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dismiss")
                        }
                    }
                }
            } else if (scanResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No scans yet",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the button below to start scanning your installed apps for privacy issues",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(scanResults) { result ->
                        ScanResultCard(
                            result = result,
                            onClick = { onAppDetails(result.packageName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScanResultCard(result: ScanResult, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.appName,  // ✅ Utilise l'extension property
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = result.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    // ✅ Utilise les nouvelles propriétés
                    text = "${result.trackers.size} trackers • ${result.permissions.total} permissions",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                PrivacyScoreChip(score = result.privacyScore)  // ✅ Utilise l'extension property
                Spacer(modifier = Modifier.height(4.dp))
                RiskLevelChip(riskLevel = result.riskLevel)
            }
        }
    }
}
@Composable
fun PrivacyScoreChip(score: Int) {
    val color = when {
        score >= 70 -> Color(0xFF4CAF50)
        score >= 40 -> Color(0xFFFFA726)
        else -> Color(0xFFEF5350)
    }

    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = "$score/100",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun RiskLevelChip(riskLevel: String) {
    val color = when (riskLevel.lowercase()) {
        "low" -> Color(0xFF4CAF50)
        "medium" -> Color(0xFFFFA726)
        "high" -> Color(0xFFEF5350)
        else -> Color.Gray
    }

    AssistChip(
        onClick = { },
        label = { Text(riskLevel) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.2f),
            labelColor = color
        )
    )
}