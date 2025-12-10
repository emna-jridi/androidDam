package tn.esprit.dam.screens.scan

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tn.esprit.dam.data.model.ScanApkResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanApkScreen(
    onBack: () -> Unit,
    viewModel: ScanApkViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.value
    val context = LocalContext.current

    val pickApkLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.handleApkUri(context, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan APK") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Button(
                onClick = { pickApkLauncher.launch("application/vnd.android.package-archive") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Choisir un APK à analyser")
            }

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }

                uiState.error != null -> {
                    Text(
                        "Erreur : ${uiState.error}",
                        color = Color.Red
                    )
                }

                uiState.result != null -> {
                    ScanApkResultCard(uiState.result!!)
                }
            }
        }
    }
}

@Composable
fun ScanApkResultCard(result: ScanApkResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2139)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Package: ${result.packageName ?: "Inconnu"}", color = Color.White)
            Spacer(Modifier.height(10.dp))
            Text("Score sécurité: ${result.analysis?.score ?: 0}", color = Color.White)
            Spacer(Modifier.height(10.dp))

            Text("📌 Permissions dangereuses:", color = Color.Red)
            result.analysis?.dangerousPermissions?.forEach {
                Text("- $it", color = Color.Red)
            }

            Spacer(Modifier.height(10.dp))
            Text("🔧 Alertes:", color = Color(0xFFFFD54F))
            result.analysis?.alerts?.forEach {
                Text("- $it", color = Color(0xFFFFD54F))
            }
        }
    }
}
