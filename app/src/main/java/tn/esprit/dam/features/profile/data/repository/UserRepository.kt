package tn.esprit.dam.features.profile.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import tn.esprit.dam.core.model.User
import tn.esprit.dam.core.network.getOrNull
import tn.esprit.dam.core.network.getOrThrow
import tn.esprit.dam.core.network.safeApiCall
import tn.esprit.dam.features.auth.data.model.UpdateUserRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val httpClient: HttpClient
) {
    companion object {
        private const val ENDPOINT_PROFILE = "/api/users/profile"
        private const val ENDPOINT_UPDATE_ME = "/api/users/me"
    }

    suspend fun getProfile(): User {
        return safeApiCall<User> { httpClient.get(ENDPOINT_PROFILE) }.getOrThrow()
    }

    suspend fun updateProfile(request: UpdateUserRequest): User {
        return safeApiCall<User> {
            httpClient.patch(ENDPOINT_UPDATE_ME) { setBody(request) }
        }.getOrThrow()
    }

    suspend fun updateName(name: String): User {
        return updateProfile(UpdateUserRequest(name = name))
    }

    suspend fun getCachedUser(): User? {
        return safeApiCall<User> { httpClient.get(ENDPOINT_PROFILE) }.getOrNull()
    }
}
