package tn.esprit.dam.features.darkweb.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import tn.esprit.dam.data.model.Breach
import tn.esprit.dam.features.darkweb.DarkWebViewModel
import tn.esprit.dam.features.darkweb.DarkWebUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkWebMonitoringScreen(
    navController: NavController,
    viewModel: DarkWebViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("My Breaches", "Manual Check")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Dark Web Monitoring") },
                    actions = {
                        IconButton(onClick = { viewModel.checkNow() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Scan Now")
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (selectedTab == 0) {
                // ... My Breaches Content (Existing)
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.breaches.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Safe",
                            tint = Color.Green,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No breaches detected for your accounts.")
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.breaches) { breach ->
                            BreachCard(breach = breach, onClick = {
                                navController.navigate("breach_detail/${breach._id}")
                            })
                        }
                    }
                }
            } else {
                // Reset manual state when switching to this tab
                LaunchedEffect(selectedTab) {
                    if (selectedTab == 1) {
                        viewModel.resetManualState()
                    }
                }
               ManualCheckContent(viewModel, uiState)
            }
        }
    }
}

@Composable
fun ManualCheckContent(viewModel: DarkWebViewModel, uiState: DarkWebUiState) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Email Check Section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Check Email Address", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Enter Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { 
                        viewModel.manualCheckEmail(email)
                        // Clear input after search
                        email = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = email.isNotBlank() && !uiState.isLoading
                ) {
                    Text("Check Email")
                }

                // Show loading or results
                if (uiState.isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Checking...", style = MaterialTheme.typography.bodyMedium)
                    }
                } else if (uiState.manualEmailResult != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (uiState.manualEmailResult.isEmpty()) {
                         Text("✅ No breaches found for this email.", color = Color.Green)
                    } else {
                        Text("⚠️ Found ${uiState.manualEmailResult.size} breaches!", color = MaterialTheme.colorScheme.error)
                        uiState.manualEmailResult.forEach { breach ->
                            Text("- ${breach["Name"]} (${breach["BreachDate"]})", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Password Check Section (K-Anonymity)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Check Password Safety", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("We check securely using k-anonymity. Only the first 5 characters of the hash are sent.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; viewModel.resetManualState() },
                    label = { Text("Enter Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.manualCheckPassword(password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = password.isNotBlank() && !uiState.isLoading
                ) {
                    Text("Check Password")
                }

                if (uiState.passwordCheckPerformed) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val count = uiState.manualPasswordCount ?: 0
                    if (count == 0) {
                         Text("✅ Password not found in known breaches.", color = Color.Green)
                    } else {
                        Text("⚠️ This password has been exposed $count times!", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        Text("Do not use this password.", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun BreachCard(breach: Breach, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (breach.isResolved) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (breach.isResolved) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (breach.isResolved) Color.Green else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = breach.source,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = breach.breachDate ?: "Unknown Date",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = breach.description?.take(100) + "...",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                breach.dataClasses.take(3).forEach { dataClass ->
                    AssistChip(
                        onClick = { },
                        label = { Text(dataClass, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }
    }
}
