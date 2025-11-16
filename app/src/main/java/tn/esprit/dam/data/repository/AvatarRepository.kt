package tn.esprit.dam.data.repository

import android.content.Context
import tn.esprit.dam.data.TokenManager
import tn.esprit.dam.data.model.Avatar
import tn.esprit.dam.data.model.AvatarResponse
import tn.esprit.dam.data.model.UpdateAvatarDto
import tn.esprit.dam.data.remote.AvatarApiService

class AvatarRepository(private val context: Context) {

    suspend fun getAvatar(userHash: String): Avatar {
        val token = TokenManager.getAccessToken(context)
            ?: throw Exception("No access token")
        return AvatarApiService.getAvatar(token, userHash)
    }

    suspend fun updateAvatar(
        userHash: String,
        updateDto: UpdateAvatarDto
    ): AvatarResponse {
        val token = TokenManager.getAccessToken(context)
            ?: throw Exception("No access token")
        return AvatarApiService.updateAvatar(token, userHash, updateDto)
    }

    suspend fun generateRandomAvatar(userHash: String): AvatarResponse {
        val token = TokenManager.getAccessToken(context)
            ?: throw Exception("No access token")
        return AvatarApiService.generateRandomAvatar(token, userHash)
    }

    suspend fun generateConsistentAvatar(userHash: String): AvatarResponse {
        val token = TokenManager.getAccessToken(context)
            ?: throw Exception("No access token")
        return AvatarApiService.generateConsistentAvatar(token, userHash)
    }
}