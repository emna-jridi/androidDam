package tn.esprit.dam.features.vault.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import tn.esprit.dam.features.vault.viewmodel.BiometricOperationState
import tn.esprit.dam.features.vault.viewmodel.VaultUiState
import tn.esprit.dam.features.vault.viewmodel.VaultViewModel
import tn.esprit.dam.features.vault.utils.BiometricHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun VaultUnlockScreen(
    vaultViewModel: VaultViewModel,
    onUnlocked: () -> Unit
) {
    val uiState by vaultViewModel.uiState.collectAsState()
    val biometricEnabled by vaultViewModel.biometricEnabled.collectAsState()
    val biometricRegistered by vaultViewModel.biometricRegistered.collectAsState()
    val biometricOperationState by vaultViewModel.biometricOperationState.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    
    var masterPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var attemptFailed by remember { mutableStateOf(false) }
    var showBiometricSuggestion by remember { mutableStateOf(true) }
    var showBiometricSetupDialog by remember { mutableStateOf(false) }
    
    // Shake animation on error
    val shakeOffset by animateFloatAsState(
        targetValue = if (attemptFailed) 0f else 0f,
        animationSpec = if (attemptFailed) {
            keyframes {
                durationMillis = 400
                0f at 0
                -10f at 50
                10f at 100
                -10f at 150
                10f at 200
                -5f at 250
                5f at 300
                0f at 400
            }
        } else spring(),
        finishedListener = { attemptFailed = false }
    )
    
    // Icon pulse animation
    val iconScale by animateFloatAsState(
        targetValue = if (uiState is VaultUiState.Loading) 0.9f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Navigate when unlocked
    LaunchedEffect(uiState) {
        if (uiState is VaultUiState.Unlocked) {
            onUnlocked()
        } else if (uiState is VaultUiState.Error) {
            attemptFailed = true
        }
    }
    
    // Get activity reference early
    val activity = remember(context) { context as? FragmentActivity }
    
    // Biometric authentication on screen load (if registered with cryptographic biometric)
    LaunchedEffect(Unit) {
        if (biometricRegistered && activity != null && vaultViewModel.isBiometricAvailable()) {
            // Use the new cryptographic biometric authentication
            vaultViewModel.authenticateWithCryptoBiometric(activity)
        } else if (biometricEnabled && activity != null) {
            // Fallback to cached password biometric (legacy behavior)
            if (BiometricHelper.isBiometricAvailable(activity)) {
                BiometricHelper.authenticateWithBiometric(
                    activity = activity,
                    title = "Unlock Vault",
                    subtitle = "Use your fingerprint or face to unlock",
                    onSuccess = {
                        vaultViewModel.unlockVaultWithBiometric()
                    },
                    onError = { /* User can still use master password */ }
                )
            }
        }
    }
    
    // Show snackbar for biometric operation results
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(biometricOperationState) {
        when (biometricOperationState) {
            is BiometricOperationState.Success -> {
                snackbarHostState.showSnackbar(
                    (biometricOperationState as BiometricOperationState.Success).message
                )
                vaultViewModel.clearBiometricOperationState()
            }
            is BiometricOperationState.Error -> {
                snackbarHostState.showSnackbar(
                    (biometricOperationState as BiometricOperationState.Error).message
                )
                vaultViewModel.clearBiometricOperationState()
            }
            else -> {}
        }
    }
    
    // Biometric setup dialog
    if (showBiometricSetupDialog && activity != null) {
        AlertDialog(
            onDismissRequest = { showBiometricSetupDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Enable Biometric Login") },
            text = {
                Text(
                    "Would you like to enable fingerprint or face unlock? " +
                    "This creates a secure cryptographic key on your device for quick access."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBiometricSetupDialog = false
                        vaultViewModel.registerBiometricDevice(activity)
                    }
                ) {
                    Text("Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBiometricSetupDialog = false }) {
                    Text("Not Now")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Security Icon
            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .scale(iconScale),
                shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (uiState is VaultUiState.Loading) Icons.Outlined.LockClock else Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Unlock Your Vault",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Enter your master password to decrypt and access your passwords.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Check biometric status
        val biometricManager = remember(context) { 
            androidx.biometric.BiometricManager.from(context) 
        }
        val canAuthenticate = remember(context) {
            biometricManager.canAuthenticate(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
            )
        }
        
        // Debug/Status card - always show to indicate biometric status
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = when (canAuthenticate) {
                    androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> 
                        MaterialTheme.colorScheme.primaryContainer
                    androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> 
                        MaterialTheme.colorScheme.secondaryContainer
                    else -> 
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (canAuthenticate) {
                            androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> Icons.Filled.Fingerprint
                            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Icons.Outlined.Fingerprint
                            else -> Icons.Outlined.Error
                        },
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = when (canAuthenticate) {
                            androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> 
                                MaterialTheme.colorScheme.primary
                            androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> 
                                MaterialTheme.colorScheme.secondary
                            else -> 
                                MaterialTheme.colorScheme.error
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (canAuthenticate) {
                                androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> {
                                    if (biometricRegistered) "✓ Secure Biometric Ready"
                                    else if (biometricEnabled) "✓ Biometric Unlock Ready" 
                                    else "🔓 Fingerprint Available"
                                }
                                androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> 
                                    "Set Up Fingerprint"
                                androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> 
                                    "No Biometric Hardware"
                                androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> 
                                    "Hardware Unavailable"
                                androidx.biometric.BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> 
                                    "Security Update Required"
                                else -> "Biometric Unavailable"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = when (canAuthenticate) {
                                androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> 
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> 
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else -> 
                                    MaterialTheme.colorScheme.onErrorContainer
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (canAuthenticate) {
                                androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> {
                                    if (biometricRegistered) "Cryptographic biometric login enabled - most secure option"
                                    else if (biometricEnabled) "Tap the button below to unlock with fingerprint" 
                                    else "Enable secure biometric login below for quick vault access"
                                }
                                androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> 
                                    "Your device supports fingerprint. Add your fingerprint in Settings for faster vault access."
                                else -> 
                                    "Biometric authentication is not available on this device"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when (canAuthenticate) {
                                androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS -> 
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> 
                                    MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                else -> 
                                    MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            }
                        )
                    }
                    
                    // Settings icon button for fingerprint available
                    if (canAuthenticate == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
                        IconButton(
                            onClick = { BiometricHelper.openBiometricSettings(context) }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Fingerprint Settings",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // Show enrollment button if not enrolled
                if (canAuthenticate == androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED && activity != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    FilledTonalButton(
                        onClick = { BiometricHelper.openBiometricSettings(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Settings to Add Fingerprint")
                    }
                }
                
                // Show "Enable Secure Biometric" button INSIDE the card if biometric available but not registered
                if (canAuthenticate == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS && 
                    !biometricRegistered && !biometricEnabled && activity != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    FilledTonalButton(
                        onClick = { showBiometricSetupDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enable Secure Biometric Login")
                    }
                }
            }
        }
        
        // Biometric unlock button (if enrolled and registered)
        if (canAuthenticate == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS && 
            (biometricRegistered || biometricEnabled) && activity != null) {
            FilledTonalButton(
                onClick = {
                    if (biometricRegistered) {
                        // Use cryptographic biometric authentication
                        vaultViewModel.authenticateWithCryptoBiometric(activity)
                    } else {
                        // Fallback to cached password biometric
                        BiometricHelper.authenticateWithBiometric(
                            activity = activity,
                            title = "Unlock Vault",
                            subtitle = "Use your fingerprint or face to unlock",
                            onSuccess = {
                                vaultViewModel.unlockVaultWithBiometric()
                            },
                            onError = { error ->
                                // Show error snackbar or toast
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = biometricOperationState !is BiometricOperationState.Loading,
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (biometricOperationState is BiometricOperationState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (biometricRegistered) "Unlock with Secure Biometric" else "Unlock with Biometric",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "OR",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Master password field with shake effect
        OutlinedTextField(
            value = masterPassword,
            onValueChange = { 
                masterPassword = it
                attemptFailed = false
            },
            label = { Text("Master Password") },
            placeholder = { Text("Enter your master password") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(translationX = shakeOffset),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (masterPassword.isNotBlank()) {
                        vaultViewModel.unlockVault(masterPassword)
                    }
                }
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (showPassword) "Hide password" else "Show password"
                    )
                }
            },
            isError = uiState is VaultUiState.Error,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Unlock button with elevation
        Button(
            onClick = { 
                focusManager.clearFocus()
                vaultViewModel.unlockVault(masterPassword)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = masterPassword.isNotBlank() && uiState !is VaultUiState.Loading,
            shape = MaterialTheme.shapes.large,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 3.dp,
                pressedElevation = 6.dp
            )
        ) {
            AnimatedContent(
                targetState = uiState is VaultUiState.Loading,
                transitionSpec = {
                    fadeIn() + scaleIn() with fadeOut() + scaleOut()
                }
            ) { isLoading ->
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Unlocking...", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LockOpen, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Unlock Vault", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        
        // Animated error message
        AnimatedVisibility(
            visible = uiState is VaultUiState.Error,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = (uiState as? VaultUiState.Error)?.message ?: "Authentication failed",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Security reminder
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Your passwords are encrypted with zero-knowledge architecture. We never store your master password.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        }
    }
}
