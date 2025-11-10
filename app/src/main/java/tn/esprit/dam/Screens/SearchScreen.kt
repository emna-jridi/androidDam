package tn.esprit.dam.Screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.launch
import tn.esprit.dam.data.AppDetails
import tn.esprit.dam.data.api.ShadowGuardApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    api: ShadowGuardApi,
    onBack: () -> Unit,
    onAppDetails: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<AppDetails>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun performSearch() {
        if (query.isEmpty()) return

        scope.launch {
            isLoading = true
            error = null
            searchResults = emptyList()

            try {
                val response = api.searchApp(query, limit = 20)
                searchResults = response.results

                if (searchResults.isEmpty()) {
                    error = "Aucune application trouvée"
                }
            } catch (e: Exception) {
                error = "Erreur: ${e.message ?: "Impossible de rechercher"}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rechercher une App") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Barre de recherche
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Nom d'app ou package (ex: tiktok)") },
                leadingIcon = { Icon(Icons.Default.Search, "Rechercher") },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            query = ""
                            searchResults = emptyList()
                            error = null
                        }) {
                            Icon(Icons.Default.Clear, "Effacer")
                        }
                    }
                },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { performSearch() })
            )

            Button(
                onClick = { performSearch() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                enabled = query.isNotEmpty() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isLoading) "Recherche..." else "Rechercher")
            }

            Text(
                text = "💡 Astuce: Entrez le nom ou le package (ex: com.tiktok.app)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Zone de résultats
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> SearchLoadingState()
                    error != null -> SearchErrorState(error!!) { performSearch() }
                    searchResults.isEmpty() && query.isEmpty() -> SearchEmptyState()
                    searchResults.isEmpty() -> SearchNoResultsState(query)
                    else -> SearchResultsList(searchResults, onAppDetails)
                }
            }
        }
    }
}

// ============================================
// Composants d'état
// ============================================

@Composable
private fun SearchLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Recherche en cours...")
        }
    }
}

@Composable
private fun SearchErrorState(error: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Erreur",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = error, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, "Réessayer")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Réessayer")
            }
        }
    }
}

@Composable
private fun SearchEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Rechercher une application",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Entrez le nom d'une app pour vérifier sa sécurité",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchNoResultsState(query: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Aucun résultat pour \"$query\"",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SearchResultsList(
    results: List<AppDetails>,
    onAppClick: (String) -> Unit
) {
    Column {
        Text(
            text = "${results.size} résultat(s)",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(results) { app ->
                AppCard(app = app, onClick = { onAppClick(app.packageName) })
            }
        }
    }
}

// ============================================
// Composants de carte
// ============================================

@Composable
fun AppCard(app: AppDetails, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text = app.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (app.developer.isNotEmpty()) {
                    Text(
                        text = app.developer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (app.category.isNotEmpty()) {
                    Text(
                        text = "📱 ${app.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                app.trackers?.let { trackers ->
                    Text(
                        text = "🕵️ ${trackers.total} tracker(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (trackers.total > 5) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))
            PrivacyScoreChip(score = app.privacyScore, riskLevel = app.riskLevel)
        }
    }
}

@Composable
fun PrivacyScoreChip(score: Int, riskLevel: String) {
    val (color, emoji) = when (riskLevel) {
        "LOW" -> Color(0xFF2ECC71) to "✅"
        "MEDIUM" -> Color(0xFFF39C12) to "⚠️"
        "HIGH" -> Color(0xFFE67E22) to "🔶"
        "CRITICAL" -> Color(0xFFE74C3C) to "🔴"
        else -> Color.Gray to "⚪"
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}