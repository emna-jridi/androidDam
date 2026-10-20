package tn.esprit.dam.features.profile.presentation

import tn.esprit.dam.core.ui.toUiTextOr
import tn.esprit.dam.core.ui.UiText
import tn.esprit.dam.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.esprit.dam.features.profile.data.local.AvatarStorage
import tn.esprit.dam.features.profile.data.repository.UserRepository
import javax.inject.Inject
import tn.esprit.dam.features.auth.data.repository.AuthRepository

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository,
    private val authRepository: AuthRepository,
    private val avatarStorage: AvatarStorage
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            try {
                _uiState.value = ProfileUiState.Loading

                val user = repository.getProfile()

                val localAvatarPath = if (user.avatarFileName != null) {
                    avatarStorage.cacheAvatar(
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
                        it.contains("Session expired") -> UiText.Res(R.string.profile_session_expired)
                        it.contains("Unauthorized") -> UiText.Res(R.string.profile_access_denied)
                        else -> UiText.Raw(it)
                    }
                } ?: UiText.Res(R.string.profile_error_load)
                _uiState.value = ProfileUiState.Error(errorMessage)
            }
        }
    }

    fun updateProfile(name: String?, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val updatedUser = repository.updateName(name ?: "")

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
                    e.message.toUiTextOr(R.string.profile_error_update)
                )
            }
        }
    }

    fun updateAvatarAfterCustomization(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val updatedUser = repository.getProfile()

                val localAvatarPath = if (updatedUser.avatarFileName != null) {
                    avatarStorage.cacheAvatar(
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
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(
                    e.message.toUiTextOr(R.string.profile_error_avatar_update)
                )
            }
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            avatarStorage.clearAll()
            onLogoutComplete()
        }
    }

    fun showEditDialog() { _showEditDialog.value = true }
    fun hideEditDialog() { _showEditDialog.value = false }
}
