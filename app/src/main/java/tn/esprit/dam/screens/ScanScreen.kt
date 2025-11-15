// Screens/ScanScreen.kt
package tn.esprit.dam.screens

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

// Modern Color Scheme
object ScanTheme {
    val PrimaryGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF6366F1), Color(0xFF9333EA))
    )
    val DarkBg = Color(0xFF0F172A)
    val CardBg = Color(0xFF1E293B)
    val CardHover = Color(0xFF334155)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF94A3B8)
    val BorderColor = Color(0xFF334155)

    val CriticalBg = Color(0x1AEF4444)
    val CriticalText = Color(0xFFEF4444)
    val HighBg = Color(0x1AFB923C)
    val HighText = Color(0xFFFB923C)
    val MediumBg = Color(0x1AFBBF24)
    val MediumText = Color(0xFFFBBF24)
    val LowBg = Color(0x1A10B981)
    val LowText = Color(0xFF10B981)
}

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

    suspend fun getInstalledApps(): List<InstalledAppDto> = withContext(Dispatchers.IO) {
        try {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            Log.d("ScanScreen", "Total installed packages: ${packages.size}")

            packages
                .filter { appInfo ->
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScanTheme.DarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Modern Header
            ModernTopBar(onBack = onBack)

            // Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    isLoading -> LoadingState(totalApps = totalApps)
                    error != null -> ErrorState(
                        error = error!!,
                        onDismiss = { error = null }
                    )
                    scanResults.isEmpty() -> EmptyState1()
                    else -> ResultsList(
                        results = scanResults,
                        totalApps = totalApps,
                        onAppClick = onAppDetails
                    )
                }
            }
        }

        // Floating Action Button
        ModernFAB(
            isScanning = isScanning,
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
                            withTimeout(180_000) {
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
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        )
    }
}

@Composable
fun ModernTopBar(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ScanTheme.DarkBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ScanTheme.CardBg)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = ScanTheme.TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ScanTheme.PrimaryGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Analyse Complète",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ScanTheme.TextPrimary
                        )
                    )
                    Text(
                        text = "Scanner les applications",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ScanTheme.TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingState(totalApps: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(ScanTheme.PrimaryGradient),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(56.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (totalApps > 0) "Analyse de $totalApps apps..." else "Collecte des applications...",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = ScanTheme.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cela peut prendre quelques minutes...",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ScanTheme.TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF6366F1),
                trackColor = ScanTheme.CardBg
            )
        }
    }
}

@Composable
fun ErrorState(error: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = ScanTheme.CardBg
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(ScanTheme.CriticalBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = ScanTheme.CriticalText,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Erreur de Scan",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = ScanTheme.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ScanTheme.TextSecondary
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ScanTheme.PrimaryGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Réessayer", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState1() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(ScanTheme.PrimaryGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Aucun Scan Effectué",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = ScanTheme.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Appuyez sur le bouton ci-dessous pour\nanalyser vos applications installées",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = ScanTheme.TextSecondary
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ResultsList(
    results: List<ScanResult>,
    totalApps: Int,
    onAppClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Stats Card
        item {
            StatsCard(totalApps = totalApps, results = results)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Results
        items(results) { result ->
            ModernScanResultCard(result = result, onClick = { onAppClick(result.packageName) })
        }

        // Bottom spacing for FAB
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun StatsCard(totalApps: Int, results: List<ScanResult>) {
    val criticalApps = results.count { it.riskLevel.lowercase() == "high" || it.riskLevel.lowercase() == "critical" }
    val mediumApps = results.count { it.riskLevel.lowercase() == "medium" }
    val safeApps = results.count { it.riskLevel.lowercase() == "low" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ScanTheme.CardBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ScanTheme.LowText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Scan Terminé",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = ScanTheme.TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "$totalApps applications analysées",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ScanTheme.TextSecondary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip(
                    count = criticalApps,
                    label = "À risque",
                    color = ScanTheme.CriticalText,
                    bgColor = ScanTheme.CriticalBg,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    count = mediumApps,
                    label = "Modéré",
                    color = ScanTheme.MediumText,
                    bgColor = ScanTheme.MediumBg,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    count = safeApps,
                    label = "Sûres",
                    color = ScanTheme.LowText,
                    bgColor = ScanTheme.LowBg,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatChip(
    count: Int,
    label: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = color.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
fun ModernScanResultCard(result: ScanResult, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ScanTheme.CardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.appName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = ScanTheme.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = result.packageName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ScanTheme.TextSecondary,
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ScanTheme.CardHover
                    ) {
                        Text(
                            text = "${result.trackers.size} trackers",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ScanTheme.TextSecondary,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ScanTheme.CardHover
                    ) {
                        Text(
                            text = "${result.permissions.total} permissions",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ScanTheme.TextSecondary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(horizontalAlignment = Alignment.End) {
                ModernPrivacyScore(score = result.privacyScore)
                Spacer(modifier = Modifier.height(8.dp))
                ModernRiskChip(riskLevel = result.riskLevel)
            }
        }
    }
}

@Composable
fun ModernPrivacyScore(score: Int) {
    val (color, bgColor) = when {
        score >= 70 -> ScanTheme.LowText to ScanTheme.LowBg
        score >= 40 -> ScanTheme.MediumText to ScanTheme.MediumBg
        else -> ScanTheme.CriticalText to ScanTheme.CriticalBg
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = "$score/100",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun ModernRiskChip(riskLevel: String) {
    val (color, bgColor, label) = when (riskLevel.lowercase()) {
        "low" -> Triple(ScanTheme.LowText, ScanTheme.LowBg, "Faible")
        "medium" -> Triple(ScanTheme.MediumText, ScanTheme.MediumBg, "Moyen")
        "high", "critical" -> Triple(ScanTheme.CriticalText, ScanTheme.CriticalBg, "Élevé")
        else -> Triple(ScanTheme.TextSecondary, ScanTheme.CardHover, "Inconnu")
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun ModernFAB(
    isScanning: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(68.dp),
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.Transparent,
        contentColor = Color.White
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScanTheme.PrimaryGradient),
            contentAlignment = Alignment.Center
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Start Scan",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}



@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun EmptyStatePreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScanTheme.DarkBg)
        ) {
            Column {
                ModernTopBar(onBack = {})
                EmptyState1()
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun LoadingStatePreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScanTheme.DarkBg)
        ) {
            Column {
                ModernTopBar(onBack = {})
                LoadingState(totalApps = 24)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun ErrorStatePreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScanTheme.DarkBg)
        ) {
            Column {
                ModernTopBar(onBack = {})
                ErrorState(
                    error = "Cannot connect to server. Check:\n• Backend is running\n• IP address is correct\n• Same WiFi network",
                    onDismiss = {}
                )
            }
        }
    }
}