package tn.esprit.dam.data.repository

import android.content.Context
import tn.esprit.dam.data.ApiClient
import tn.esprit.dam.data.model.UpdateUserRequest
import tn.esprit.dam.data.model.User

class UserRepository(private val context: Context) {

    private val apiClient = ApiClient.getInstance(context)


    suspend fun getProfile(): User {
        return apiClient.getUserProfile()
    }

    suspend fun updateProfile(request: UpdateUserRequest): User {
        return apiClient.updateUserProfile(request)
    }

    suspend fun updateName(name: String): User {
        return apiClient.updateUserProfile(
            UpdateUserRequest(name = name)
        )
    }


    suspend fun getCachedUser(): User? {
        return apiClient.getCurrentUser()
    }
}