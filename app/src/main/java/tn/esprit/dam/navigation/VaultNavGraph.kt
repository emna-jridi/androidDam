package tn.esprit.dam.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import tn.esprit.dam.data.remote.api.VaultApi
import tn.esprit.dam.data.remote.ai.OllamaPasswordAdvisor
import tn.esprit.dam.data.repository.VaultRepository
import tn.esprit.dam.features.vault.viewmodel.PasswordViewModel
import tn.esprit.dam.features.vault.viewmodel.VaultViewModel
import tn.esprit.dam.features.vault.viewmodel.VaultUiState
import tn.esprit.dam.features.vault.screens.CreateMasterPasswordScreen
import tn.esprit.dam.features.vault.screens.VaultUnlockScreen
import tn.esprit.dam.features.vault.screens.PasswordListScreen
import tn.esprit.dam.features.vault.screens.VaultDetailScreen
import tn.esprit.dam.features.vault.screens.VaultAddPasswordScreen
import tn.esprit.dam.data.remote.KtorHttpClient
import android.content.Context
import androidx.compose.ui.platform.LocalContext

/**
 * Vault feature routes
 * Handles secure password storage and management
 */
fun NavGraphBuilder.vaultNavGraph(
    navController: NavHostController,
    vaultRepository: VaultRepository,
    passwordViewModel: PasswordViewModel
) {
    composable(Screens.Vault.route) {
        val vaultViewModel = remember { VaultViewModel(vaultRepository) }

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
                    onUnlocked = { /* UI will switch automatically */ }
                )
            }
        }
    }

    // Vault with optional intent parameter
    composable(
        route = "${Screens.Vault.route}?intent={intent}",
        arguments = listOf(navArgument("intent") { type = NavType.StringType; nullable = true })
    ) { backStackEntry ->
        val intent = backStackEntry.arguments?.getString("intent")
        val vaultViewModel = remember { VaultViewModel(vaultRepository) }

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

    // Add Password
    composable(Screens.VaultAddPassword.route) {
        val vaultViewModel = remember { VaultViewModel(vaultRepository) }
        VaultAddPasswordScreen(
            passwordViewModel = passwordViewModel,
            vaultViewModel = vaultViewModel,
            onBackClick = { navController.popBackStack() },
            onPasswordAdded = {
                navController.popBackStack()
                // Refresh passwords after adding
            }
        )
    }

    // Password Detail
    composable(Screens.VaultDetail.route) {
        VaultDetailScreen(
            passwordViewModel = passwordViewModel,
            onBackClick = { navController.popBackStack() }
        )
    }
}
