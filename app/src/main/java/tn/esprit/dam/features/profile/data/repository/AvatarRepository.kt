package tn.esprit.dam.features.profile.data.repository

import tn.esprit.dam.features.profile.data.model.Avatar
import tn.esprit.dam.features.profile.data.model.AvatarResponse
import tn.esprit.dam.features.profile.data.model.UpdateAvatarDto

interface AvatarRepository {
    suspend fun getAvatar(userHash: String): Avatar

    suspend fun updateAvatar(
        userHash: String,
        updateDto: UpdateAvatarDto
    ): AvatarResponse

    suspend fun generateRandomAvatar(userHash: String): AvatarResponse

    suspend fun generateConsistentAvatar(userHash: String): AvatarResponse
}
