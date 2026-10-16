package tn.esprit.dam.features.profile.data.repository

import tn.esprit.dam.core.model.User
import tn.esprit.dam.core.network.getOrNull
import tn.esprit.dam.core.network.getOrThrow
import tn.esprit.dam.features.profile.data.model.UpdateUserRequest
import tn.esprit.dam.features.profile.data.remote.UserApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: UserApi
) : UserRepository {

    override suspend fun getProfile(): User =
        api.getProfile().getOrThrow()

    override suspend fun updateProfile(request: UpdateUserRequest): User =
        api.updateMe(request).getOrThrow()

    override suspend fun updateName(name: String): User =
        updateProfile(UpdateUserRequest(name = name))

    override suspend fun getCachedUser(): User? =
        api.getProfile().getOrNull()
}
