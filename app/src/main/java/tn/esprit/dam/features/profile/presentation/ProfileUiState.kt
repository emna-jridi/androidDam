package tn.esprit.dam.features.profile.presentation

import tn.esprit.dam.core.ui.UiText
import tn.esprit.dam.core.model.User

sealed class ProfileUiState {
    object Loading : ProfileUiState()

    data class Success(
        val user: User,
        val localAvatarPath: String? = null
    ) : ProfileUiState()

    data class Error(val message: UiText) : ProfileUiState()
}
