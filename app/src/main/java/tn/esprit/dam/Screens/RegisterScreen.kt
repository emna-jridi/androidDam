package tn.esprit.dam.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import tn.esprit.dam.data.ApiClient
import tn.esprit.dam.data.RegisterRequest

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Validation errors
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    fun validateForm(): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            nameError = "Nom requis"
            isValid = false
        } else {
            nameError = null
        }

        if (email.isEmpty()) {
            emailError = "Email requis"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Email invalide"
            isValid = false
        } else {
            emailError = null
        }

        if (phone.isEmpty()) {
            phoneError = "Téléphone requis"
            isValid = false
        } else if (phone.length < 8) {
            phoneError = "Téléphone invalide"
            isValid = false
        } else {
            phoneError = null
        }

        if (address.isEmpty()) {
            addressError = "Adresse requise"
            isValid = false
        } else {
            addressError = null
        }

        if (password.isEmpty()) {
            passwordError = "Mot de passe requis"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "Minimum 6 caractères"
            isValid = false
        } else {
            passwordError = null
        }

        return isValid
    }

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
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo et titre
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF7C3AED)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ShadowGuard",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = Color.White
            )

            Text(
                text = "Créez votre compte sécurisé",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB4B4C6)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Card d'inscription
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E2139)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Inscription",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Nom complet
                    Text(
                        text = "Nom complet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (nameError != null && it.isNotEmpty()) nameError = null
                        },
                        placeholder = { Text("Votre nom", color = Color(0xFF6B7280)) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameError != null,
                        supportingText = nameError?.let { { Text(it, color = Color(0xFFEF4444)) } },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedContainerColor = Color(0xFF2D3250),
                            unfocusedContainerColor = Color(0xFF2D3250),
                            cursorColor = Color(0xFF7C3AED)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (emailError != null && it.isNotEmpty()) emailError = null
                        },
                        placeholder = { Text("votre@email.com", color = Color(0xFF6B7280)) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Email,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = emailError != null,
                        supportingText = emailError?.let { { Text(it, color = Color(0xFFEF4444)) } },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedContainerColor = Color(0xFF2D3250),
                            unfocusedContainerColor = Color(0xFF2D3250),
                            cursorColor = Color(0xFF7C3AED)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Téléphone
                    Text(
                        text = "Téléphone",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            if (phoneError != null && it.isNotEmpty()) phoneError = null
                        },
                        placeholder = { Text("+216 XX XXX XXX", color = Color(0xFF6B7280)) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Phone,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = phoneError != null,
                        supportingText = phoneError?.let { { Text(it, color = Color(0xFFEF4444)) } },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedContainerColor = Color(0xFF2D3250),
                            unfocusedContainerColor = Color(0xFF2D3250),
                            cursorColor = Color(0xFF7C3AED)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Adresse
                    Text(
                        text = "Adresse",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = {
                            address = it
                            if (addressError != null && it.isNotEmpty()) addressError = null
                        },
                        placeholder = { Text("Votre adresse", color = Color(0xFF6B7280)) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Home,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = addressError != null,
                        supportingText = addressError?.let { { Text(it, color = Color(0xFFEF4444)) } },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedContainerColor = Color(0xFF2D3250),
                            unfocusedContainerColor = Color(0xFF2D3250),
                            cursorColor = Color(0xFF7C3AED)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mot de passe
                    Text(
                        text = "Mot de passe",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (passwordError != null && it.isNotEmpty()) passwordError = null
                        },
                        placeholder = { Text("••••••••", color = Color(0xFF6B7280)) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordError != null,
                        supportingText = passwordError?.let { { Text(it, color = Color(0xFFEF4444)) } },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedContainerColor = Color(0xFF2D3250),
                            unfocusedContainerColor = Color(0xFF2D3250),
                            cursorColor = Color(0xFF7C3AED)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Bouton d'inscription
                    Button(
                        onClick = {
                            if (validateForm()) {
                                scope.launch {
                                    try {
                                        isLoading = true
                                        errorMessage = null
                                        ApiClient.register(RegisterRequest(email, password, name, phone, address))
                                        successMessage = "Inscription réussie!"
                                    } catch (e: Exception) {
                                        errorMessage = e.message ?: "Inscription échouée"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7C3AED),
                            disabledContainerColor = Color(0xFF5B21B6)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                "S'inscrire",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color.White
                            )
                        }
                    }

                    successMessage?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF10B981).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = it,
                                    color = Color(0xFF10B981),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(2000)
                            onRegisterSuccess()
                        }
                    }

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = it,
                                    color = Color(0xFFEF4444),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Déjà un compte
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Déjà un compte ? ",
                    color = Color(0xFFB4B4C6),
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        "Se connecter",
                        color = Color(0xFF7C3AED),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer sécurité
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Vos données sont sécurisées et chiffrées",
                    color = Color(0xFF6B7280),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(
        onRegisterSuccess = {},
        onNavigateToLogin = {}
    )
}