package tn.esprit.dam.features.profile.presentation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.features.profile.data.model.*
import tn.esprit.dam.features.profile.data.repository.AvatarRepository

sealed class AvatarCustomizerUiState {
    object Loading : AvatarCustomizerUiState()
    data class Success(val avatar: Avatar) : AvatarCustomizerUiState()
    data class Error(val message: String) : AvatarCustomizerUiState()
}

@HiltViewModel
class AvatarCustomizerViewModel @Inject constructor(
    private val repository: AvatarRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<AvatarCustomizerUiState>(
        AvatarCustomizerUiState.Loading
    )
    val uiState: StateFlow<AvatarCustomizerUiState> = _uiState.asStateFlow()

    private val _currentConfig = MutableStateFlow(AvatarConfig())
    val currentConfig: StateFlow<AvatarConfig> = _currentConfig.asStateFlow()

    fun loadAvatar(userHash: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AvatarCustomizerUiState.Loading

                val avatar = repository.getAvatar(userHash)

                _currentConfig.value = avatar.config
                _uiState.value = AvatarCustomizerUiState.Success(avatar)
            } catch (e: Exception) {
                generateConsistentAvatar(userHash)
            }
        }
    }

    fun updateAvatarConfig(
        userHash: String,
        updateDto: UpdateAvatarDto,
        onSuccess: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val response = repository.updateAvatar(userHash, updateDto)

                _currentConfig.value = response.avatar.config
                _uiState.value = AvatarCustomizerUiState.Success(response.avatar)

                onSuccess(response.avatar.fileName)
            } catch (e: Exception) {
                _uiState.value = AvatarCustomizerUiState.Error(
                    e.message ?: "Erreur de mise à jour"
                )
            }
        }
    }

    fun generateRandomAvatar(
        userHash: String,
        onSuccess: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = AvatarCustomizerUiState.Loading

                val response = repository.generateRandomAvatar(userHash)

                _currentConfig.value = response.avatar.config
                _uiState.value = AvatarCustomizerUiState.Success(response.avatar)

                onSuccess(response.avatar.fileName)
            } catch (e: Exception) {
                _uiState.value = AvatarCustomizerUiState.Error(
                    e.message ?: "Erreur de génération"
                )
            }
        }
    }

    fun generateConsistentAvatar(
        userHash: String,
        onSuccess: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = AvatarCustomizerUiState.Loading

                val response = repository.generateConsistentAvatar(userHash)

                _currentConfig.value = response.avatar.config
                _uiState.value = AvatarCustomizerUiState.Success(response.avatar)

                onSuccess(response.avatar.fileName)
            } catch (e: Exception) {
                _uiState.value = AvatarCustomizerUiState.Error(
                    e.message ?: "Erreur de génération"
                )
            }
        }
    }

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
