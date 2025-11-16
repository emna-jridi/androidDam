package tn.esprit.dam.screens.profile
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.data.model.*
import tn.esprit.dam.data.repository.AvatarRepository

sealed class AvatarCustomizerUiState {
    object Loading : AvatarCustomizerUiState()
    data class Success(val avatar: Avatar) : AvatarCustomizerUiState()
    data class Error(val message: String) : AvatarCustomizerUiState()
}

class AvatarCustomizerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AvatarCustomizerUiState>(
        AvatarCustomizerUiState.Loading
    )
    val uiState: StateFlow<AvatarCustomizerUiState> = _uiState.asStateFlow()

    private val _currentConfig = MutableStateFlow(AvatarConfig())
    val currentConfig: StateFlow<AvatarConfig> = _currentConfig.asStateFlow()

    /**
     * ✅ Charger l'avatar existant
     */
    fun loadAvatar(context: Context, userHash: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AvatarCustomizerUiState.Loading

                val repository = AvatarRepository(context)
                val avatar = repository.getAvatar(userHash)

                _currentConfig.value = avatar.config
                _uiState.value = AvatarCustomizerUiState.Success(avatar)
            } catch (e: Exception) {
                // Si l'avatar n'existe pas, générer un avatar cohérent
                generateConsistentAvatar(context, userHash)
            }
        }
    }

    /**
     * ✅ Mettre à jour l'avatar avec la config personnalisée
     * Note: Le backend retourne le nouveau avatar avec fileName
     */
    fun updateAvatarConfig(
        context: Context,
        userHash: String,
        updateDto: UpdateAvatarDto,
        onSuccess: (String) -> Unit = {} // ✅ Callback avec fileName
    ) {
        viewModelScope.launch {
            try {
                val repository = AvatarRepository(context)
                val response = repository.updateAvatar(userHash, updateDto)

                _currentConfig.value = response.avatar.config
                _uiState.value = AvatarCustomizerUiState.Success(response.avatar)

                // ✅ Passer le fileName au callback
                onSuccess(response.avatar.fileName)
            } catch (e: Exception) {
                _uiState.value = AvatarCustomizerUiState.Error(
                    e.message ?: "Erreur de mise à jour"
                )
            }
        }
    }

    /**
     * ✅ Générer un avatar aléatoire
     */
    fun generateRandomAvatar(
        context: Context,
        userHash: String,
        onSuccess: (String) -> Unit = {} // ✅ Callback avec fileName
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = AvatarCustomizerUiState.Loading

                val repository = AvatarRepository(context)
                val response = repository.generateRandomAvatar(userHash)

                _currentConfig.value = response.avatar.config
                _uiState.value = AvatarCustomizerUiState.Success(response.avatar)

                // ✅ Passer le fileName au callback
                onSuccess(response.avatar.fileName)
            } catch (e: Exception) {
                _uiState.value = AvatarCustomizerUiState.Error(
                    e.message ?: "Erreur de génération"
                )
            }
        }
    }

    /**
     * ✅ Générer un avatar cohérent (déterministe)
     */
    fun generateConsistentAvatar(
        context: Context,
        userHash: String,
        onSuccess: (String) -> Unit = {} // ✅ Callback avec fileName
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = AvatarCustomizerUiState.Loading

                val repository = AvatarRepository(context)
                val response = repository.generateConsistentAvatar(userHash)

                _currentConfig.value = response.avatar.config
                _uiState.value = AvatarCustomizerUiState.Success(response.avatar)

                // ✅ Passer le fileName au callback
                onSuccess(response.avatar.fileName)
            } catch (e: Exception) {
                _uiState.value = AvatarCustomizerUiState.Error(
                    e.message ?: "Erreur de génération"
                )
            }
        }
    }

    // Méthodes pour modifier individuellement (Preview en temps réel)
    fun updateHairColor(color: String) {
        _currentConfig.value = _currentConfig.value.copy(hairColor = color)
    }

    fun updateTopType(type: String) {
        _currentConfig.value = _currentConfig.value.copy(topType = type)
    }

    fun updateClotheType(type: String) {
        _currentConfig.value = _currentConfig.value.copy(clotheType = type)
    }

    fun updateClotheColor(color: String) {
        _currentConfig.value = _currentConfig.value.copy(clotheColor = color)
    }

    fun updateSkinColor(color: String) {
        _currentConfig.value = _currentConfig.value.copy(skinColor = color)
    }

    fun updateEyeType(type: String) {
        _currentConfig.value = _currentConfig.value.copy(eyeType = type)
    }

    fun updateMouthType(type: String) {
        _currentConfig.value = _currentConfig.value.copy(mouthType = type)
    }
}