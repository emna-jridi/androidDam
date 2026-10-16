package tn.esprit.dam.features.profile.data.repository

import tn.esprit.dam.core.network.getOrThrow
import tn.esprit.dam.features.profile.data.model.Avatar
import tn.esprit.dam.features.profile.data.model.AvatarResponse
import tn.esprit.dam.features.profile.data.model.UpdateAvatarDto
import tn.esprit.dam.features.profile.data.remote.AvatarApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvatarRepository @Inject constructor(
    private val avatarApi: AvatarApi
) {
    suspend fun getAvatar(userHash: String): Avatar {
        return avatarApi.getAvatar(userHash).getOrThrow()
    }

    suspend fun updateAvatar(
        userHash: String,
        updateDto: UpdateAvatarDto
    ): AvatarResponse {
        return avatarApi.updateAvatar(userHash, updateDto).getOrThrow()
    }

    suspend fun generateRandomAvatar(userHash: String): AvatarResponse {
        return avatarApi.generateRandomAvatar(userHash).getOrThrow()
    }

    suspend fun generateConsistentAvatar(userHash: String): AvatarResponse {
        return avatarApi.generateConsistentAvatar(userHash).getOrThrow()
    }
}
