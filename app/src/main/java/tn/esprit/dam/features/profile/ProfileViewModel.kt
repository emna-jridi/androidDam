package tn.esprit.dam.features.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.data.local.AvatarCache
import tn.esprit.dam.data.ApiClient
import tn.esprit.dam.data.repository.UserRepository
import javax.inject.Inject


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    private val _showAvatarCustomizer = MutableStateFlow(false)
    val showAvatarCustomizer: StateFlow<Boolean> = _showAvatarCustomizer.asStateFlow()

    /**
     * Charger le profil utilisateur
     */
    fun loadProfile() {
        viewModelScope.launch {
            try {
                _uiState.value = ProfileUiState.Loading

                val user = repository.getProfile()

                // TÃ©lÃ©charger et mettre en cache l'avatar
                val localAvatarPath = if (user.avatarFileName != null) {
                    AvatarCache.cacheAvatar(
                        context = context,
                        userId = user.id ?: "default",
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
                val errorMessage = e.message?.let {
                    when {
                        it.contains("Session expired") -> "Votre session a expirÃ©. Veuillez vous reconnecter."
                        it.contains("Unauthorized") -> "AccÃ¨s refusÃ©. Veuillez vous reconnecter."
                        else -> it
                    }
                } ?: "Erreur de chargement du profil"
                _uiState.value = ProfileUiState.Error(errorMessage)
            }
        }
    }

    /**
     * Mettre Ã  jour le nom de l'utilisateur
     */
    fun updateProfile(name: String?, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
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
                    e.message ?: "Erreur de mise Ã  jour"
                )
            }
        }
    }

    /**
     * Mettre Ã  jour l'avatar aprÃ¨s personnalisation
     * Cette fonction recharge simplement le profil pour obtenir le nouveau avatarFileName
     */
    fun updateAvatarAfterCustomization(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                // Recharger le profil pour obtenir le nouveau avatarFileName
                val updatedUser = repository.getProfile()

                // TÃ©lÃ©charger le nouvel avatar
                val localAvatarPath = if (updatedUser.avatarFileName != null) {
                    AvatarCache.cacheAvatar(
                        context = context,
                        userId = updatedUser.id ?: "default",
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
                    e.message ?: "Erreur de mise Ã  jour de l'avatar"
                )
            }
        }
    }

    /**
     * DÃ©connexion
     */
    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            repository.getCachedUser() // touch repository to keep DI used

            // Logout via ApiClient singleton
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
