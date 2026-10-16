package tn.esprit.dam.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import tn.esprit.dam.features.vault.presentation.passwords.PasswordViewModel
import tn.esprit.dam.features.vault.presentation.access.VaultViewModel
import tn.esprit.dam.features.vault.presentation.access.VaultUiState
import tn.esprit.dam.features.vault.presentation.access.CreateMasterPasswordScreen
import tn.esprit.dam.features.vault.presentation.access.VaultUnlockScreen
import tn.esprit.dam.features.vault.presentation.passwords.PasswordListScreen
import tn.esprit.dam.features.vault.presentation.passwords.PasswordDetailScreen
import tn.esprit.dam.features.vault.presentation.access.VaultAddPasswordScreen

fun NavGraphBuilder.vaultNavGraph(
    navController: NavHostController,
    passwordViewModel: PasswordViewModel? = null
) {
    composable(Screens.Vault.route) {
        VaultEntry(navController, passwordViewModel, forceCreate = false)
    }

    composable(
        route = "${Screens.Vault.route}?intent={intent}",
        arguments = listOf(navArgument("intent") { type = NavType.StringType; nullable = true })
    ) { backStackEntry ->
        val intent = backStackEntry.arguments?.getString("intent")
        VaultEntry(navController, passwordViewModel, forceCreate = intent == "create")
    }

    composable(Screens.VaultAddPassword.route) {
        val vaultViewModel: VaultViewModel = hiltViewModel()
        val pwdVm: PasswordViewModel = passwordViewModel ?: hiltViewModel()
        VaultAddPasswordScreen(
            passwordViewModel = pwdVm,
            vaultViewModel = vaultViewModel,
            onBackClick = { navController.popBackStack() },
            onPasswordAdded = {
                navController.popBackStack()
            }
        )
    }

    composable(Screens.VaultDetail.route) {
        val pwdVm: PasswordViewModel = passwordViewModel ?: hiltViewModel()
        val selectedPassword by pwdVm.selectedPassword.collectAsState()
        val passwordState = selectedPassword

        if (passwordState != null) {
            PasswordDetailScreen(
                data = passwordState,
                onBack = { navController.popBackStack() },
                onDelete = {
                    pwdVm.deletePassword(passwordState.entry.id)
                    navController.popBackStack()
                }
            )
        } else {
            androidx.compose.runtime.LaunchedEffect(Unit) { navController.popBackStack() }
        }
    }
}

@Composable
private fun VaultEntry(
    navController: NavHostController,
    passwordViewModel: PasswordViewModel?,
    forceCreate: Boolean
) {
    val vaultViewModel: VaultViewModel = hiltViewModel()
    val pwdVm: PasswordViewModel = passwordViewModel ?: hiltViewModel()

    val uiState = vaultViewModel.uiState.collectAsState().value
    val createResult = vaultViewModel.createVaultState.collectAsState().value
    val createError = createResult?.exceptionOrNull()?.message
    val isLoading = uiState is VaultUiState.Loading

    when {
        uiState is VaultUiState.NoVault || (forceCreate && uiState !is VaultUiState.Unlocked) -> {
            CreateMasterPasswordScreen(
                onCreateVault = { master, confirm -> vaultViewModel.createVault(master, confirm) },
                isLoading = isLoading,
                error = createError
            )
        }
        uiState is VaultUiState.Unlocked -> {
            PasswordListScreen(
                passwordViewModel = pwdVm,
                onPasswordClick = { entry ->
                    pwdVm.selectPassword(entry)
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
                onUnlocked = { }
            )
        }
    }
}
