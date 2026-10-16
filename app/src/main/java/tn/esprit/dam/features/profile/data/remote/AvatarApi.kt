package tn.esprit.dam.features.profile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.core.network.safeApiCall
import tn.esprit.dam.features.profile.data.model.Avatar
import tn.esprit.dam.features.profile.data.model.AvatarResponse
import tn.esprit.dam.features.profile.data.model.UpdateAvatarDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvatarApi @Inject constructor(
    private val client: HttpClient
) {
    companion object {
        private const val AVATAR_URL = "/api/v1/avatar"
    }

    // GET returns the avatar object directly; PUT/random/consistent return { success, avatar }
    suspend fun getAvatar(userHash: String): ApiResult<Avatar> =
        safeApiCall { client.get("$AVATAR_URL/$userHash") }

    suspend fun updateAvatar(userHash: String, updateDto: UpdateAvatarDto): ApiResult<AvatarResponse> =
        safeApiCall { client.put("$AVATAR_URL/$userHash") { setBody(updateDto) } }

    suspend fun generateRandomAvatar(userHash: String): ApiResult<AvatarResponse> =
        safeApiCall { client.post("$AVATAR_URL/random/$userHash") }

    suspend fun generateConsistentAvatar(userHash: String): ApiResult<AvatarResponse> =
        safeApiCall { client.post("$AVATAR_URL/consistent/$userHash") }
}
