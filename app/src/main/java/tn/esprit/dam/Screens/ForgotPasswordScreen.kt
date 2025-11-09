package tn.esprit.dam.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tn.esprit.dam.data.ApiClient

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Mot de passe oublié",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (!isSuccess) {
            // Formulaire d'envoi d'email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Adresse e-mail") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        scope.launch {
                            try {
                                isLoading = true
                                message = null

                                val response = ApiClient.forgotPassword(email)

                                // Affiche le message de succès
                                message = response.message
                                isSuccess = true

                            } catch (e: Exception) {
                                message = e.message ?: "Erreur lors de l'envoi"
                                isSuccess = false
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        message = "Veuillez entrer votre e-mail"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Envoyer le lien de réinitialisation")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateBack) {
                Text("Retour à la connexion")
            }

            message?.let {
                if (!isSuccess) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        } else {
            // Écran de confirmation après envoi réussi
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Email,
                contentDescription = "Email envoyé",
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Email envoyé !",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = message ?: "Un lien de réinitialisation a été envoyé à votre adresse e-mail.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Veuillez vérifier votre boîte de réception et cliquer sur le lien pour réinitialiser votre mot de passe.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Retour à la connexion")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    // Réinitialiser pour permettre un nouvel envoi
                    isSuccess = false
                    email = ""
                    message = null
                }
            ) {
                Text("Renvoyer l'email")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreview() {
    MaterialTheme {
        ForgotPasswordScreen(
            onNavigateBack = {}
        )
    }
}