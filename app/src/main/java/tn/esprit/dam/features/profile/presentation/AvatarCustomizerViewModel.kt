package tn.esprit.dam.features.profile.presentation
import tn.esprit.dam.core.ui.toUiTextOr
import tn.esprit.dam.core.ui.UiText
import tn.esprit.dam.R
import timber.log.Timber
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.update
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
    data class Error(val message: UiText) : AvatarCustomizerUiState()
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
                Timber.w(e, "AvatarCustomizerViewModel.loadAvatar failed")
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
                    e.message.toUiTextOr(R.string.profile_error_update)
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
                    e.message.toUiTextOr(R.string.avatar_error_generate)
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
                    e.message.toUiTextOr(R.string.avatar_error_generate)
                )
            }
        }
    }

    fun updateHairColor(color: String) {
        _currentConfig.update { it.copy(hairColor = color) }
    }

    fun updateTopType(type: String) {
        _currentConfig.update { it.copy(topType = type) }
    }

    fun updateClotheType(type: String) {
        _currentConfig.update { it.copy(clotheType = type) }
    }

    fun updateClotheColor(color: String) {
        _currentConfig.update { it.copy(clotheColor = color) }
    }

    fun updateSkinColor(color: String) {
        _currentConfig.update { it.copy(skinColor = color) }
    }

    fun updateEyeType(type: String) {
        _currentConfig.update { it.copy(eyeType = type) }
    }

    fun updateMouthType(type: String) {
        _currentConfig.update { it.copy(mouthType = type) }
    }
}
