package tn.esprit.dam.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.dam.data.model.SearchItem
import kotlin.math.max
import kotlin.math.min

// -------------------------------------------------------
// 🔍 SEARCH BUTTON
// -------------------------------------------------------
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

// -------------------------------------------------------
// 🔍 LIST OF RESULTS
// -------------------------------------------------------
@Composable
fun ModernResultsList(
    results: List<SearchItem>,
    onAppClick: (String) -> Unit
) {
    Column {
        // Header
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
                        color = SearchTheme.LowText,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (results.size > 1)
                        "${results.size} résultats trouvés"
                    else "1 résultat trouvé",
                    color = SearchTheme.TextSecondary
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(results) { item ->
                ModernAppCard(
                    item = item,
                    onClick = { onAppClick(item.packageName) }
                )
            }
        }
    }
}

// -------------------------------------------------------
// 📱 APP CARD (using SearchItem, not AppDetails)
// -------------------------------------------------------
@Composable
fun ModernAppCard(item: SearchItem, onClick: () -> Unit) {

    val riskLevel = when {
        item.privacyScore >= 75 -> "LOW"
        item.privacyScore >= 50 -> "MEDIUM"
        item.privacyScore >= 25 -> "HIGH"
        else -> "CRITICAL"
    }

    val (riskColor, riskBg) = when (riskLevel) {
        "LOW" -> SearchTheme.LowText to SearchTheme.LowBg
        "MEDIUM" -> SearchTheme.MediumText to SearchTheme.MediumBg
        "HIGH" -> SearchTheme.HighText to SearchTheme.HighBg
        "CRITICAL" -> SearchTheme.CriticalText to SearchTheme.CriticalBg
        else -> SearchTheme.TextSecondary to SearchTheme.CardBg
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SearchTheme.CardBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SearchTheme.TextPrimary
                        )
                    )

                    if (!item.developer.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.developer,
                            color = SearchTheme.TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Privacy Score Chip
                PrivacyScoreChip(item.privacyScore, riskColor, riskBg)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                if (!item.category.isNullOrEmpty()) {
                    InfoChip(Icons.Default.Category, item.category!!)
                }

                // Trackers Chip
                val trackersCount = item.trackers?.size ?: 0
                if (trackersCount > 0) {
                    InfoChipColored(
                        icon = Icons.Default.Security,
                        text = "$trackersCount trackers",
                        color = riskColor,
                        bgColor = riskBg
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------
// SCORE CHIP
// -------------------------------------------------------
@Composable
fun PrivacyScoreChip(score: Int, color: Color, bgColor: Color) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = score.toString(),
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 18.sp
            )
            Text(
                "/100",
                color = color.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        }
    }
}

// -------------------------------------------------------
// CHIPS
// -------------------------------------------------------
@Composable
fun InfoChip(icon: ImageVector, text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SearchTheme.CardHover
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = SearchTheme.TextSecondary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, color = SearchTheme.TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
fun InfoChipColored(icon: ImageVector, text: String, color: Color, bgColor: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, color = color, fontSize = 11.sp)
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
                color = SearchTheme.TextSecondary
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
        keyboardActions = KeyboardActions { onSearch() }
    )
}
