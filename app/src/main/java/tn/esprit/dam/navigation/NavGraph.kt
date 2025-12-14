package tn.esprit.dam.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.util.Base64
import androidx.compose.runtime.collectAsState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.decodeFromString
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import kotlinx.coroutines.runBlocking
import tn.esprit.dam.R
import tn.esprit.dam.data.TokenManager
import tn.esprit.dam.features.auth.forgotpassword.ForgotPasswordScreen
import tn.esprit.dam.features.auth.forgotpassword.ForgotPasswordViewModel
import tn.esprit.dam.features.auth.forgotpassword.NewPasswordScreen
import tn.esprit.dam.features.auth.forgotpassword.PasswordResetSuccessScreen
import tn.esprit.dam.features.auth.forgotpassword.ResetPasswordOTPScreen
import tn.esprit.dam.features.auth.login.LoginScreen
import tn.esprit.dam.features.auth.register.RegisterScreen
import tn.esprit.dam.features.auth.verification.EmailVerificationScreen
import tn.esprit.dam.features.auth.verification.VerificationSuccessScreen
import tn.esprit.dam.features.components.AppBottomNavBar
import tn.esprit.dam.features.components.AppTopBar
import tn.esprit.dam.features.components.BottomNavItem
import tn.esprit.dam.features.components.NavigationScreen
import tn.esprit.dam.features.profile.ProfileScreen
import tn.esprit.dam.features.scan.presentation.HomeScreen
import tn.esprit.dam.features.scan.presentation.ScanScreen
import com.shadowguard.dam.data.remote.api.VaultApi
import com.shadowguard.dam.data.remote.ai.OllamaPasswordAdvisor
import com.shadowguard.dam.data.repository.VaultRepository
import com.shadowguard.dam.ui.vault.viewmodel.VaultViewModel
import com.shadowguard.dam.ui.vault.viewmodel.PasswordViewModel
import com.shadowguard.dam.ui.vault.viewmodel.VaultUiState
import com.shadowguard.dam.ui.vault.screens.CreateMasterPasswordScreen
import com.shadowguard.dam.ui.vault.screens.VaultUnlockScreen
import com.shadowguard.dam.ui.vault.screens.PasswordListScreen
import tn.esprit.dam.data.api.KtorClient

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screens.Login.route,
) {
    val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel()
    val context = LocalContext.current

    // Create singleton VaultRepository for all vault screens
    val vaultClient = remember { KtorClient.getInstance(context, TokenManager) }
    val vaultRepository = remember { VaultRepository(VaultApi(vaultClient)) }
    val ollamaAdvisor = remember { OllamaPasswordAdvisor(vaultRepository) }
    
    // Shared ViewModels
    val passwordViewModel = remember { PasswordViewModel(vaultRepository, ollamaAdvisor) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    val isLoggedIn = currentRoute != Screens.Login.route &&
                     currentRoute != Screens.Register.route &&
                     !currentRoute.contains("email_verification") &&
                     !currentRoute.contains("verification_success") &&
                     !currentRoute.contains("forgot_password") &&
                     !currentRoute.contains("reset_password_otp") &&
                     !currentRoute.contains("new_password") &&
                     !currentRoute.contains("password_reset_success")

    // Get current screen for TopBar
    val currentScreen = when {
        currentRoute.contains(Screens.Home.route) -> NavigationScreen.Home
        currentRoute.contains(Screens.Scan.route) -> NavigationScreen.Scan
        currentRoute.contains(Screens.ScanHistory.route) -> NavigationScreen.History
        currentRoute.contains(Screens.Profile.route) -> NavigationScreen.Profile
        currentRoute.contains(Screens.Vault.route) -> NavigationScreen.Vault
        currentRoute.contains(Screens.VaultAddPassword.route) -> NavigationScreen.Vault
        else -> NavigationScreen.Home
    }

    Scaffold(
        topBar = {
            if (isLoggedIn) {
                AppTopBar(
                    currentScreen = currentScreen,
                    onBackClick = if (currentScreen != NavigationScreen.Home) {
                        { navController.popBackStack() }
                    } else null
                )
            }
        },
        bottomBar = {
            if (isLoggedIn) {
                AppBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screens.Home.route) { saveState = true }
                            restoreState = true
                        }
                    },
                    isLoggedIn = isLoggedIn
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screens.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screens.Home.route) {
                            popUpTo(Screens.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screens.Register.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screens.ForgotPassword.route) }
                )
            }

            composable(Screens.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = { email ->
                        navController.navigate(Screens.EmailVerification.createRoute(email)) {
                            popUpTo(Screens.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable(
                route = Screens.EmailVerification.route,
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""

                EmailVerificationScreen(
                    email = email,
                    onVerificationSuccess = {
                        navController.navigate(Screens.VerificationSuccess.route) {
                            popUpTo(Screens.Register.route) { inclusive = true }
                        }
                    },
                    onBackToLogin = {
                        navController.navigate(Screens.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screens.VerificationSuccess.route) {
                VerificationSuccessScreen(
                    userName = "Utilisateur",
                    onContinue = {
                        navController.navigate(Screens.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screens.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onNavigateToOTP = {
                        val email = forgotPasswordViewModel.uiState.value.email
                        navController.navigate(Screens.ResetPasswordOTP.createRoute(email))
                    },
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = forgotPasswordViewModel
                )
            }

            composable(
                route = Screens.ResetPasswordOTP.route,
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""

                ResetPasswordOTPScreen(
                    email = email,
                    onOTPVerified = {
                        navController.navigate(Screens.NewPassword.route)
                    },
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = forgotPasswordViewModel
                )
            }

            composable(Screens.NewPassword.route) {
                NewPasswordScreen(
                    onPasswordResetSuccess = {
                        navController.navigate(Screens.PasswordResetSuccess.route) {
                            popUpTo(Screens.ForgotPassword.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                    viewModel = forgotPasswordViewModel
                )
            }

            composable(Screens.PasswordResetSuccess.route) {
                PasswordResetSuccessScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screens.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screens.Home.route) {
                HomeScreen(
                    onNavigateToScan = { navController.navigate(Screens.Scan.route) },
                    onNavigateToHistory = { navController.navigate(Screens.ScanHistory.route) },
                    onNavigateToAppSearch = { navController.navigate(Screens.AppSearch.route) },
                    onNavigateToProfile = { navController.navigate(Screens.Profile.route) },
                    onNavigateToAppDetails = { packageName ->
                        navController.navigate(Screens.AppDetails.createRoute(packageName))
                    },
                    onNavigateToVault = { navController.navigate(Screens.Vault.route) },
                    onLogout = {
                        runBlocking { TokenManager.clearAll(context) }
                        navController.navigate(Screens.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screens.Scan.route) {
                val context = LocalContext.current
                    var userId by remember { mutableStateOf<String?>(null) }
                    var deviceId by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    val user = TokenManager.getUser(context)
                    val token = TokenManager.getAccessToken(context)
                    val decodedId = token?.let { decodeUserIdFromToken(it) }
                    userId = user?.id ?: decodedId ?: "unknown"

                    deviceId = android.provider.Settings.Secure.getString(
                        context.contentResolver,
                        android.provider.Settings.Secure.ANDROID_ID
                    ) ?: "unknown"
                }

                    // Only render ScanScreen when userId is loaded
                    if (userId != null && deviceId != null) {
                        ScanScreen(
                            userId = userId!!,
                            deviceId = deviceId!!,
                    onNavigateToHome = {
                        navController.navigate(Screens.Home.route) {
                            popUpTo(Screens.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateToAppDetails = { packageName ->
                        navController.navigate(Screens.AppDetails.createRoute(packageName))
                    }
                )
                } else {
                    // Loading state while fetching userId
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            composable(
                route = Screens.AppDetails.route,
                arguments = listOf(navArgument("packageName") { type = NavType.StringType })
            ) { backStackEntry ->
                val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
                tn.esprit.dam.features.scan.presentation.AppDetailScreen(
                    packageName = packageName,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screens.ScanHistory.route) {
                tn.esprit.dam.features.scan.presentation.ScanHistoryScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screens.AppSearch.route) {
                tn.esprit.dam.features.scan.presentation.SearchAppScreen(
                    onAppClick = { packageName ->
                        navController.navigate(Screens.AppDetails.createRoute(packageName))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screens.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        runBlocking { TokenManager.clearAll(context) }
                        navController.navigate(Screens.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onOpenVault = { navController.navigate(Screens.Vault.route) },
                    onCreateVault = { navController.navigate("${Screens.Vault.route}?intent=create") }
                )
            }



            // ShadowVault flow
            composable(Screens.Vault.route) {
                val vaultViewModel = remember { VaultViewModel(vaultRepository) }
                // Use shared passwordViewModel


                val uiState = vaultViewModel.uiState.collectAsState().value
                val createResult = vaultViewModel.createVaultState.collectAsState().value
                val createError = createResult?.exceptionOrNull()?.message
                val isLoading = uiState is VaultUiState.Loading

                when (uiState) {
                    is VaultUiState.NoVault -> {
                        CreateMasterPasswordScreen(
                            onCreateVault = { master, confirm -> vaultViewModel.createVault(master, confirm) },
                            isLoading = isLoading,
                            error = createError
                        )
                    }
                    is VaultUiState.Unlocked -> {
                        PasswordListScreen(
                            passwordViewModel = passwordViewModel,
                            onPasswordClick = { entry ->
                                passwordViewModel.selectPassword(entry)
                                navController.navigate(Screens.VaultDetail.route)
                            },
                            onAddClick = { navController.navigate(Screens.VaultAddPassword.route) },
                            onLockClick = {
                                vaultViewModel.lockVault()
                                navController.popBackStack()
                            }
                        )
                    }
                    else -> {
                        VaultUnlockScreen(
                            vaultViewModel = vaultViewModel,
                            onUnlocked = { /* no-op: UI will switch to list */ }
                        )
                    }
                }
            }
            // Optional intent query: vault?intent={intent}
            composable(
                route = "${Screens.Vault.route}?intent={intent}",
                arguments = listOf(navArgument("intent") { type = NavType.StringType; nullable = true })
            ) { backStackEntry ->
                val intent = backStackEntry.arguments?.getString("intent")
                val vaultViewModel = remember { VaultViewModel(vaultRepository) }
                // Used shared passwordViewModel
                
                // Clear selection when entering list if needed, or keep state
                // LaunchedEffect(Unit) { passwordViewModel.clearSelection() } 

                val uiState = vaultViewModel.uiState.collectAsState().value
                val createResult = vaultViewModel.createVaultState.collectAsState().value
                val createError = createResult?.exceptionOrNull()?.message
                val isLoading = uiState is VaultUiState.Loading

                // If intent=create and vault exists locked, still show unlock; if no vault, show create
                if (intent == "create" && uiState !is VaultUiState.Unlocked) {
                    CreateMasterPasswordScreen(
                        onCreateVault = { master, confirm -> vaultViewModel.createVault(master, confirm) },
                        isLoading = isLoading,
                        error = createError
                    )
                } else {
                    when (uiState) {
                        is VaultUiState.NoVault -> {
                            CreateMasterPasswordScreen(
                                onCreateVault = { master, confirm -> vaultViewModel.createVault(master, confirm) },
                                isLoading = isLoading,
                                error = createError
                            )
                        }
                        is VaultUiState.Unlocked -> {
                            PasswordListScreen(
                                passwordViewModel = passwordViewModel,
                                onPasswordClick = { entry ->
                                    passwordViewModel.selectPassword(entry)
                                    navController.navigate(Screens.VaultDetail.route)
                                },
                                onAddClick = { navController.navigate(Screens.VaultAddPassword.route) },
                                onLockClick = {
                                    vaultViewModel.lockVault()
                                    navController.popBackStack()
                                }
                            )
                        }
                        else -> {
                            VaultUnlockScreen(
                                vaultViewModel = vaultViewModel,
                                onUnlocked = { /* no-op */ }
                            )
                        }
                    }
                }
            }

            // Add Password Screen
            composable(Screens.VaultAddPassword.route) {
                // Use shared passwordViewModel
                val saveState by passwordViewModel.saveState.collectAsState()

                // Navigate back on successful save
                LaunchedEffect(saveState) {
                    if (saveState?.isSuccess == true) {
                        passwordViewModel.clearSaveState()
                        navController.popBackStack()
                    }
                }

                com.shadowguard.dam.ui.vault.screens.AddPasswordScreen(
                    viewModel = passwordViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            
            // Password Detail Screen
            composable(Screens.VaultDetail.route) {
                val selectedPassword by passwordViewModel.selectedPassword.collectAsState()
                val passwordState = selectedPassword
                
                if (passwordState != null) {
                    com.shadowguard.dam.ui.vault.screens.PasswordDetailScreen(
                        data = passwordState,
                        onBack = { navController.popBackStack() },
                        onDelete = {
                            passwordViewModel.deletePassword(passwordState.entry.id)
                            navController.popBackStack()
                        }
                    )
                } else {
                    // Fallback if state lost (shouldn't happen with shared VM unless process death)
                    LaunchedEffect(Unit) { navController.popBackStack() }
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                         CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

private fun decodeUserIdFromToken(token: String): String? {
    val parts = token.split(".")
    if (parts.size < 2) return null
    return runCatching {
        val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
        val json = Json { ignoreUnknownKeys = true }
        val element = json.decodeFromString<JsonElement>(payload)
        val node = element.jsonObject
        node["sub"]?.jsonPrimitive?.contentOrNull
    }.getOrNull()
}

@Composable
private fun SimplePlaceholderScreen(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
    }
}
