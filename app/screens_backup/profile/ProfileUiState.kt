package tn.esprit.dam.screens.profile


import tn.esprit.dam.data.model.User



sealed class ProfileUiState {
    object Loading : ProfileUiState()

    data class Success(
        val user: User,
        val localAvatarPath: String? = null
    ) : ProfileUiState()

    data class Error(val message: String) : ProfileUiState()
}