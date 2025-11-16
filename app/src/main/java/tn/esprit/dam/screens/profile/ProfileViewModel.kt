package tn.esprit.dam.screens.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.data.ApiClient
import tn.esprit.dam.data.local.AvatarCache
import tn.esprit.dam.data.model.User
import tn.esprit.dam.data.repository.UserRepository


class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    private val _showAvatarCustomizer = MutableStateFlow(false)
    val showAvatarCustomizer: StateFlow<Boolean> = _showAvatarCustomizer.asStateFlow()

    /**
     * ✅ Charger le profil utilisateur
     */
    fun loadProfile(context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = ProfileUiState.Loading

                val repository = UserRepository(context)
                val user = repository.getProfile()

                // ✅ Télécharger et mettre en cache l'avatar
                val localAvatarPath = if (user.avatarFileName != null) {
                    AvatarCache.cacheAvatar(
                        context = context,
                        userId =user.id ?: "default",
                        avatarFileName = user.avatarFileName
                    )
                } else {
                    null
                }

                _uiState.value = ProfileUiState.Success(
                    user = user,
                    localAvatarPath = localAvatarPath
                )
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(
                    e.message ?: "Erreur de chargement du profil"
                )
            }
        }
    }

    /**
     * ✅ Mettre à jour le nom de l'utilisateur
     */
    fun updateProfile(
        context: Context,
        name: String?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val repository = UserRepository(context)
                val updatedUser = repository.updateName(name ?: "")

                // Conserver le chemin local existant
                val currentState = _uiState.value
                val localAvatarPath = if (currentState is ProfileUiState.Success) {
                    currentState.localAvatarPath
                } else {
                    null
                }

                _uiState.value = ProfileUiState.Success(
                    user = updatedUser,
                    localAvatarPath = localAvatarPath
                )
                _showEditDialog.value = false
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(
                    e.message ?: "Erreur de mise à jour"
                )
            }
        }
    }

    /**
     * ✅ Mettre à jour l'avatar après personnalisation
     * Cette fonction recharge simplement le profil pour obtenir le nouveau avatarFileName
     */
    fun updateAvatarAfterCustomization(
        context: Context,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                // Recharger le profil pour obtenir le nouveau avatarFileName
                val repository = UserRepository(context)
                val updatedUser = repository.getProfile()

                // ✅ Télécharger le nouvel avatar
                val localAvatarPath = if (updatedUser.avatarFileName != null) {
                    AvatarCache.cacheAvatar(
                        context = context,
                        userId =updatedUser.id ?: "default",
                        avatarFileName = updatedUser.avatarFileName
                    )
                } else {
                    null
                }

                _uiState.value = ProfileUiState.Success(
                    user = updatedUser,
                    localAvatarPath = localAvatarPath
                )
                _showAvatarCustomizer.value = false
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(
                    e.message ?: "Erreur de mise à jour de l'avatar"
                )
            }
        }
    }

    /**
     * ✅ Déconnexion
     */
    fun logout(context: Context, onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            val apiClient = ApiClient.getInstance(context)
            apiClient.logout()

            // Nettoyer le cache des avatars
            AvatarCache.clearAllCache(context)

            onLogoutComplete()
        }
    }

    fun showEditDialog() { _showEditDialog.value = true }
    fun hideEditDialog() { _showEditDialog.value = false }

    fun showAvatarCustomizer() { _showAvatarCustomizer.value = true }
    fun hideAvatarCustomizer() { _showAvatarCustomizer.value = false }
}