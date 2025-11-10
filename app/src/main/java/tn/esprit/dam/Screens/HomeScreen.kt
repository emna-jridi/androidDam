package tn.esprit.dam.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToScan: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToTopApps: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    currentRoute: String = "home"
) {
    var showMenu by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "ShadowGuard",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF7C3AED),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF1E2139))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Profile", color = Color.White) },
                            onClick = {
                                showMenu = false
                                onNavigateToProfile()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = Color(0xFF7C3AED)
                                )
                            }
                        )
                        HorizontalDivider(color = Color(0xFF374151))
                        DropdownMenuItem(
                            text = { Text("Déconnexion", color = Color(0xFFEF4444)) },
                            onClick = {
                                showMenu = false
                                onLogout()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.ExitToApp,
                                    contentDescription = "Logout",
                                    tint = Color(0xFFEF4444)
                                )
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E2139),
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                    },
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Accueil") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF7C3AED),
                        selectedTextColor = Color(0xFF7C3AED),
                        unselectedIconColor = Color(0xFF9CA3AF),
                        unselectedTextColor = Color(0xFF9CA3AF),
                        indicatorColor = Color(0xFF7C3AED).copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        onNavigateToScan()
                    },
                    icon = {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = "Scan"
                        )
                    },
                    label = { Text("Scan") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF7C3AED),
                        selectedTextColor = Color(0xFF7C3AED),
                        unselectedIconColor = Color(0xFF9CA3AF),
                        unselectedTextColor = Color(0xFF9CA3AF),
                        indicatorColor = Color(0xFF7C3AED).copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        onNavigateToSearch()
                    },
                    icon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    label = { Text("Recherche") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF7C3AED),
                        selectedTextColor = Color(0xFF7C3AED),
                        unselectedIconColor = Color(0xFF9CA3AF),
                        unselectedTextColor = Color(0xFF9CA3AF),
                        indicatorColor = Color(0xFF7C3AED).copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        onNavigateToHistory()
                    },
                    icon = {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "History"
                        )
                    },
                    label = { Text("Historique") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF7C3AED),
                        selectedTextColor = Color(0xFF7C3AED),
                        unselectedIconColor = Color(0xFF9CA3AF),
                        unselectedTextColor = Color(0xFF9CA3AF),
                        indicatorColor = Color(0xFF7C3AED).copy(alpha = 0.2f)
                    )
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0E27),
                            Color(0xFF1A1F3A)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp)
            ) {
                // Welcome Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
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
                                .background(
                                    Color(0xFF7C3AED),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Bienvenue sur ShadowGuard",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Protégez votre appareil des menaces",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFB4B4C6)
                            )
                        }
                    }
                }

                // Section Title
                Text(
                    text = "Fonctionnalités",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Feature Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        FeatureCard(
                            title = "Scan Apps",
                            icon = Icons.Default.Security,
                            description = "Analyser les apps",
                            onClick = onNavigateToScan,
                            gradientColors = listOf(Color(0xFF7C3AED), Color(0xFF9333EA))
                        )
                    }
                    item {
                        FeatureCard(
                            title = "Recherche",
                            icon = Icons.Default.Search,
                            description = "Rechercher une app",
                            onClick = onNavigateToSearch,
                            gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB))
                        )
                    }
                    item {
                        FeatureCard(
                            title = "Historique",
                            icon = Icons.Default.History,
                            description = "Voir l'historique",
                            onClick = onNavigateToHistory,
                            gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669))
                        )
                    }
                    item {
                        FeatureCard(
                            title = "Top Apps",
                            icon = Icons.Default.Star,
                            description = "Meilleures apps",
                            onClick = onNavigateToTopApps,
                            gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                        )
                    }
                }
            }
        }
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