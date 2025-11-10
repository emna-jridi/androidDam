package tn.esprit.dam.Screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import tn.esprit.dam.data.AppDetails
import tn.esprit.dam.data.api.ShadowGuardApi

// Modern Theme Colors
object SearchTheme {
    val PrimaryGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF6366F1), Color(0xFF9333EA))
    )
    val DarkBg = Color(0xFF0F172A)
    val CardBg = Color(0xFF1E293B)
    val CardHover = Color(0xFF334155)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF94A3B8)
    val BorderColor = Color(0xFF334155)
    val InputBg = Color(0xFF1E293B)

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SearchTheme.DarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Modern Header
            ModernSearchHeader(onBack = onBack)

            // Search Input Section
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                ModernSearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    onClear = {
                        query = ""
                        searchResults = emptyList()
                        error = null
                    },
                    onSearch = { performSearch() },
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(12.dp))

                ModernSearchButton(
                    onClick = { performSearch() },
                    enabled = query.isNotEmpty() && !isLoading,
                    isLoading = isLoading
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Hint Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SearchTheme.CardBg.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = SearchTheme.TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Entrez le nom ou le package (ex: tiktok, com.tiktok.app)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SearchTheme.TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Results Section
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    isLoading -> ModernLoadingState()
                    error != null -> ModernErrorState(error!!) { performSearch() }
                    searchResults.isEmpty() && query.isEmpty() -> ModernEmptyState()
                    searchResults.isEmpty() -> ModernNoResultsState(query)
                    else -> ModernResultsList(searchResults, onAppDetails)
                }
            }
        }
    }
}

@Composable
fun ModernSearchHeader(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SearchTheme.DarkBg
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
                    .background(SearchTheme.CardBg)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = SearchTheme.TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SearchTheme.PrimaryGradient),
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
                        text = "Recherche",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SearchTheme.TextPrimary
                        )
                    )
                    Text(
                        text = "Vérifier une application",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SearchTheme.TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ModernSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                "Nom d'app ou package...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SearchTheme.TextSecondary
                )
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = SearchTheme.TextSecondary
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = SearchTheme.TextSecondary
                    )
                }
            }
        },
        singleLine = true,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = SearchTheme.TextPrimary,
            unfocusedTextColor = SearchTheme.TextPrimary,
            disabledTextColor = SearchTheme.TextSecondary,
            focusedContainerColor = SearchTheme.InputBg,
            unfocusedContainerColor = SearchTheme.InputBg,
            disabledContainerColor = SearchTheme.InputBg.copy(alpha = 0.5f),
            focusedBorderColor = Color(0xFF6366F1),
            unfocusedBorderColor = SearchTheme.BorderColor,
            cursorColor = Color(0xFF6366F1)
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() })
    )
}

@Composable
fun ModernSearchButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = SearchTheme.CardBg
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled) SearchTheme.PrimaryGradient
                    else Brush.horizontalGradient(
                        colors = listOf(
                            SearchTheme.CardBg,
                            SearchTheme.CardBg
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = if (isLoading) "Recherche en cours..." else "Rechercher",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (enabled) Color.White else SearchTheme.TextSecondary
                    )
                )
            }
        }
    }
}

@Composable
fun ModernLoadingState() {
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
                    .background(SearchTheme.PrimaryGradient),
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
                text = "Recherche en cours...",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = SearchTheme.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Analyse des applications",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SearchTheme.TextSecondary
                )
            )
        }
    }
}

@Composable
fun ModernErrorState(error: String, onRetry: () -> Unit) {
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
                containerColor = SearchTheme.CardBg
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
                        .background(SearchTheme.CriticalBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = SearchTheme.CriticalText,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Erreur de Recherche",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = SearchTheme.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SearchTheme.TextSecondary
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRetry,
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
                            .background(SearchTheme.PrimaryGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Réessayer",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernEmptyState() {
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
                    .background(SearchTheme.PrimaryGradient),
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
                text = "Rechercher une App",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = SearchTheme.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Entrez le nom d'une application pour\nvérifier son niveau de sécurité",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SearchTheme.TextSecondary
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ModernNoResultsState(query: String) {
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
                    .size(100.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(SearchTheme.CardBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = SearchTheme.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Aucun résultat",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = SearchTheme.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Aucune application trouvée pour \"$query\"",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SearchTheme.TextSecondary
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ModernResultsList(
    results: List<AppDetails>,
    onAppClick: (String) -> Unit
) {
    Column {
        // Results Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SearchTheme.DarkBg
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SearchTheme.LowBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = results.size.toString(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = SearchTheme.LowText,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "résultat${if (results.size > 1) "s" else ""} trouvé${if (results.size > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SearchTheme.TextSecondary
                    )
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(results) { app ->
                ModernAppCard(app = app, onClick = { onAppClick(app.packageName) })
            }
        }
    }
}

@Composable
fun ModernAppCard(app: AppDetails, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SearchTheme.CardBg
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = SearchTheme.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    if (app.developer.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = app.developer,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SearchTheme.TextSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
                    Spacer(modifier = Modifier.width(12.dp))

                    ModernPrivacyScoreChip(
                        score = app.privacyScore,
                        riskLevel = app.riskLevel
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (app.category.isNotEmpty()) {
                        InfoChip(
                            icon = Icons.Default.Category,
                            text = app.category
                        )
                    }

                    app.trackers?.let { trackers ->
                        val (color, bgColor) = when {
                            trackers.total > 10 -> SearchTheme.CriticalText to SearchTheme.CriticalBg
                            trackers.total > 5 -> SearchTheme.HighText to SearchTheme.HighBg
                            trackers.total > 0 -> SearchTheme.MediumText to SearchTheme.MediumBg
                            else -> SearchTheme.LowText to SearchTheme.LowBg
                        }

                        InfoChipColored(
                            icon = Icons.Default.Security,
                            text = "${trackers.total} trackers",
                            color = color,
                            bgColor = bgColor
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SearchTheme.CardHover
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = SearchTheme.TextSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SearchTheme.TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }

    @Composable
    fun InfoChipColored(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        text: String,
        color: Color,
        bgColor: Color
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = bgColor
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = color
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }

    @Composable
    fun ModernPrivacyScoreChip(score: Int, riskLevel: String) {
        val (color, bgColor) = when (riskLevel.uppercase()) {
            "LOW" -> SearchTheme.LowText to SearchTheme.LowBg
            "MEDIUM" -> SearchTheme.MediumText to SearchTheme.MediumBg
            "HIGH" -> SearchTheme.HighText to SearchTheme.HighBg
            "CRITICAL" -> SearchTheme.CriticalText to SearchTheme.CriticalBg
            else -> SearchTheme.TextSecondary to SearchTheme.CardHover
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = bgColor
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "/100",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = color.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }

