package tn.esprit.dam.features.darkweb.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import tn.esprit.dam.features.darkweb.DarkWebViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreachDetailScreen(
    navController: NavController,
    breachId: String,
    viewModel: DarkWebViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val breach = uiState.breaches.find { it._id == breachId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(breach?.source ?: "Breach Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (breach == null) {
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                Text("Breach not found", modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Status Header
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (breach.isResolved) Color.Green.copy(alpha = 0.1f) else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text(
                            text = if (breach.isResolved) "RESOLVED" else "ACTION REQUIRED",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (breach.isResolved) Color.Green else MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Breach Date: ${breach.breachDate}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // Exposed Data
                Text("Exposed Data", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    breach.dataClasses.forEach { item ->
                        SuggestionChip(onClick = {}, label = { Text(item) })
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // AI Explanation (Placeholder logic for visualization - assumes we might add real AI call here later)
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("AI Security Insight", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "This breach exposes high-risk data including passwords. Attackers could use this for credential stuffing. Recommendation: Change your password immediately on ${breach.source} and enable 2FA.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (!breach.isResolved) {
                    Button(
                        onClick = { viewModel.resolveBreach(breach._id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark as Resolved")
                    }
                }
            }
        }
    }
}
