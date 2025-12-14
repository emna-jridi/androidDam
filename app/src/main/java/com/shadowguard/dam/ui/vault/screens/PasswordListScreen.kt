package com.shadowguard.dam.ui.vault.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shadowguard.dam.data.model.PasswordCategory
import com.shadowguard.dam.data.model.PasswordEntry
import com.shadowguard.dam.ui.vault.viewmodel.PasswordListUiState
import com.shadowguard.dam.ui.vault.viewmodel.PasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordListScreen(
    passwordViewModel: PasswordViewModel,
    onPasswordClick: (PasswordEntry) -> Unit,
    onAddClick: () -> Unit,
    onLockClick: () -> Unit
) {
    val listState by passwordViewModel.listState.collectAsState()
    val searchQuery by passwordViewModel.searchQuery.collectAsState()
    val selectedCategory by passwordViewModel.selectedCategory.collectAsState()
    
    var showSearchBar by remember { mutableStateOf(false) }
    var showCategoryFilter by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Password Vault") },
                    actions = {
                        IconButton(onClick = { showSearchBar = !showSearchBar }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showCategoryFilter = !showCategoryFilter }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                        IconButton(onClick = onLockClick) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock Vault")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                
                // Search bar
                if (showSearchBar) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { passwordViewModel.search(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Search passwords...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { passwordViewModel.search("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true
                    )
                }
                
                // Category filter chips
                if (showCategoryFilter) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { passwordViewModel.filterByCategory(null) },
                            label = { Text("All") }
                        )
                        PasswordCategory.ALL.forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { passwordViewModel.filterByCategory(category) },
                                label = { Text(category.replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Password")
            }
        }
    ) { paddingValues ->
        when (val state = listState) {
            is PasswordListUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is PasswordListUiState.Success -> {
                if (state.passwords.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No passwords yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap + to add your first password",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.passwords) { entry ->
                            PasswordCard(
                                entry = entry,
                                onClick = { onPasswordClick(entry) },
                                onFavoriteClick = { passwordViewModel.toggleFavorite(entry.id) }
                            )
                        }
                    }
                }
            }
            
            is PasswordListUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { passwordViewModel.loadPasswords() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordCard(
    entry: PasswordEntry,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.site,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (entry.isFavorite) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorite",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.username,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (entry.strengthLevel != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = when (entry.strengthLevel) {
                                "VERY_WEAK", "WEAK" -> MaterialTheme.colorScheme.errorContainer
                                "FAIR" -> MaterialTheme.colorScheme.tertiaryContainer
                                "GOOD" -> MaterialTheme.colorScheme.primaryContainer
                                "STRONG" -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = entry.strengthLevel.replace("_", " "),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = when (entry.strengthLevel) {
                                    "VERY_WEAK", "WEAK" -> MaterialTheme.colorScheme.onErrorContainer
                                    "FAIR" -> MaterialTheme.colorScheme.onTertiaryContainer
                                    "GOOD" -> MaterialTheme.colorScheme.onPrimaryContainer
                                    "STRONG" -> MaterialTheme.colorScheme.onSecondaryContainer
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = entry.category.replaceFirstChar { it.uppercase() },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
            
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (entry.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (entry.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (entry.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
