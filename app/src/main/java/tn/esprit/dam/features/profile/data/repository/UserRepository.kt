package tn.esprit.dam.features.profile.data.repository

import tn.esprit.dam.core.model.User
import tn.esprit.dam.features.profile.data.model.UpdateUserRequest

interface UserRepository {
    suspend fun getProfile(): User

    suspend fun updateProfile(request: UpdateUserRequest): User

    suspend fun updateName(name: String): User

    suspend fun getCachedUser(): User?
}
