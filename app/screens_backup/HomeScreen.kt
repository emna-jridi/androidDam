package tn.esprit.dam.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.data.TokenManager
import tn.esprit.dam.screens.scan.ScanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToTopApps: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToScanApk: () -> Unit,
    onNavigateToShadowGuard: () -> Unit, // 🔐 AJOUTÉ: Navigation vers ShadowGuard
    onLogout: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    val scanViewModel: ScanViewModel = hiltViewModel()
    val stats by scanViewModel.stats.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val userHash = TokenManager.getUserHash(context)
        if (userHash != null) {
            scanViewModel.loadLastScan(userHash)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "ShadowGuard",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF1E2139))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Déconnexion", color = Color(0xFFEF4444)) },
                            onClick = {
                                showMenu = false
                                showLogoutDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color(0xFFEF4444))
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF7C3AED),
                    titleContentColor = Color.White
                )
            )
        },

        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E2139),
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Accueil") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        onNavigateToScan()
                    },
                    icon = { Icon(Icons.Default.Security, contentDescription = null) },
                    label = { Text("Scan") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        onNavigateToHistory()
                    },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("Historique") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        onNavigateToProfile()
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profil") }
                )
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0A0E27), Color(0xFF1A1F3A))
                    )
                )
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // 1️⃣ Score Section
            item {
                ScoreSection(score = stats.avgScore)
            }

            // 2️⃣ Welcome Card
          /*  item {
                HomeWelcomeCard()
            }*/

            // 3️⃣ Title
            item {
                Text(
                    text = "Fonctionnalités",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            // 4️⃣ Feature Grid
            item {
                FeatureGrid(
                    onScan = onNavigateToScan,
                    onSearch = onNavigateToSearch,
                    onHistory = onNavigateToHistory,
                    onTopApps = onNavigateToTopApps,
                    onScanApk = onNavigateToScanApk,
                    onShadowGuard = onNavigateToShadowGuard // 🔐 AJOUTÉ: Navigation vers ShadowGuard
                )
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Déconnexion") },
            text = { Text("Voulez-vous vous déconnecter ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) { Text("Déconnexion", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
@Composable
fun FeatureCard(
    title: String,
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    gradientColors: List<Color>
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E2139)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            brush = Brush.linearGradient(gradientColors),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB4B4C6)
                )
            }
        }
    }
}

@Composable
fun HomeWelcomeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF7C3AED).copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFF7C3AED), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text = "Bienvenue sur ShadowGuard",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Votre sécurité est analysée en continu",
                    color = Color(0xFFB4B4C6)
                )
            }
        }
    }
}

@Composable
fun FeatureGrid(
    onScan: () -> Unit,
    onSearch: () -> Unit,
    onHistory: () -> Unit,
    onTopApps: () -> Unit,
    onScanApk: () -> Unit,
    onShadowGuard: () -> Unit // 🔐 AJOUTÉ: Callback pour ShadowGuard
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(540.dp) // INCREASED HEIGHT FOR 5 CARDS (3 rows)
    ) {
        item {
            FeatureCard(
                title = "Scan Apps",
                icon = Icons.Default.Security,
                description = "Analyser les apps",
                onClick = onScan,
                gradientColors = listOf(Color(0xFF7C3AED), Color(0xFF9333EA))
            )
        }

        item {
            FeatureCard(
                title = "Recherche",
                icon = Icons.Default.Search,
                description = "Rechercher une app",
                onClick = onSearch,
                gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB))
            )
        }

        item {
            FeatureCard(
                title = "Historique",
                icon = Icons.Default.History,
                description = "Voir l'historique",
                onClick = onHistory,
                gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669))
            )
        }

     /*   item {
            FeatureCard(
                title = "Top Apps",
                icon = Icons.Default.Star,
                description = "Meilleures apps",
                onClick = onTopApps,
                gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
            )
        }*/
        item {
            FeatureCard(
                title = "Scan APK",
                icon = Icons.Default.Android,
                description = "Analyser un fichier APK",
                onClick = onScanApk,
                gradientColors = listOf(Color(0xFF22C55E), Color(0xFF16A34A))
            )
        }

        // 🔐 SHADOWGUARD PASSWORD MANAGER
        item {
            FeatureCard(
                title = "ShadowGuard",
                icon = Icons.Default.Lock,
                description = "Gestionnaire de mots de passe",
                onClick = onShadowGuard,
                gradientColors = listOf(Color(0xFF6C63FF), Color(0xFF00D9FF)) // Purple to Cyan
            )
        }

    }
}


@Composable
fun ScoreSection(score: Int) {

    // Animation du score (0 → score)
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 1200)
    )

    // Animation du cercle
    val progress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(1200)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 26.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF15182D)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ---- TITLE ----
            Text(
                text = "Votre Score de Sécurité",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = Color.White
            )

            Spacer(Modifier.height(26.dp))

            // ---- BIG SCORE WITH BIG GLOW ----
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {

                // Glow élargi
                Box(
                    Modifier
                        .size(190.dp)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color(0xFF7C3AED).copy(alpha = 0.55f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(120.dp)
                        )
                )

                // Circle
                CircularProgressIndicator(
                    progress = progress,
                    strokeWidth = 14.dp,
                    color = Color(0xFF7C3AED),
                    trackColor = Color(0xFF2A2E4B)
                )

                // Big animated score
                Text(
                    text = animatedScore.toInt().toString(),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 48.sp, // 🔥 score 2x bigger
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.6f),
                            blurRadius = 14f
                        )
                    ),
                    color = Color.White
                )
            }

            Spacer(Modifier.height(22.dp))

            // ---- Score message ----
            Text(
                text = when (score) {
                    in 0..40 -> "⚠️ Score faible — plusieurs risques importants détectés."
                    in 41..70 -> "🟠 Score moyen — quelques risques à surveiller."
                    else -> "🟢 Très bon score — votre appareil est globalement sécurisé."
                },
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            // ---- Description ----
            Text(
                text = "Analyse basée sur les permissions sensibles, les trackers, le niveau de risque et le comportement global de vos applications.",
                color = Color(0xFFA3A5BE),
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(26.dp))

            // ---- TAGS MULTILIGNE ----
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ScoreTag("Permissions", Color(0xFFF43F5E))
                    ScoreTag("Trackers", Color(0xFFF59E0B))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ScoreTag("Risque global", Color(0xFF10B981))
                }
            }
        }
    }
}

@Composable
fun ScoreTag(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(
                color.copy(alpha = 0.22f),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}
