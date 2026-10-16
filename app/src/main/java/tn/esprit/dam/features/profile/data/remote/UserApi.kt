package tn.esprit.dam.features.profile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import tn.esprit.dam.core.model.User
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.core.network.safeApiCall
import tn.esprit.dam.features.profile.data.model.UpdateUserRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserApi @Inject constructor(
    private val client: HttpClient
) {
    companion object {
        private const val USERS_URL = "/api/users"
    }

    suspend fun getProfile(): ApiResult<User> =
        safeApiCall { client.get("$USERS_URL/profile") }

    suspend fun updateMe(request: UpdateUserRequest): ApiResult<User> =
        safeApiCall { client.patch("$USERS_URL/me") { setBody(request) } }
}
