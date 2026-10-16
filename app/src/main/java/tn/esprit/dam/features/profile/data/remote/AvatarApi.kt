package tn.esprit.dam.features.profile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import tn.esprit.dam.core.network.ApiEnvelope
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.core.network.safeApiCall
import tn.esprit.dam.core.network.unwrapEnvelope
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

    suspend fun getAvatar(userHash: String): ApiResult<Avatar> =
        safeApiCall<ApiEnvelope<Avatar>> { client.get("$AVATAR_URL/$userHash") }.unwrapEnvelope()

    suspend fun updateAvatar(userHash: String, updateDto: UpdateAvatarDto): ApiResult<AvatarResponse> =
        safeApiCall<ApiEnvelope<AvatarResponse>> {
            client.put("$AVATAR_URL/$userHash") { setBody(updateDto) }
        }.unwrapEnvelope()

    suspend fun generateRandomAvatar(userHash: String): ApiResult<AvatarResponse> =
        safeApiCall<ApiEnvelope<AvatarResponse>> { client.post("$AVATAR_URL/random/$userHash") }.unwrapEnvelope()

    suspend fun generateConsistentAvatar(userHash: String): ApiResult<AvatarResponse> =
        safeApiCall<ApiEnvelope<AvatarResponse>> { client.post("$AVATAR_URL/consistent/$userHash") }.unwrapEnvelope()
}
