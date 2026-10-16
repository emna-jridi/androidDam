package tn.esprit.dam.core.session

import kotlinx.coroutines.flow.Flow
import tn.esprit.dam.core.model.User

interface TokenRepository {
    suspend fun saveTokens(accessToken: String, refreshToken: String)

    suspend fun getAccessToken(): String?

    suspend fun getRefreshToken(): String?

    fun observeAccessToken(): Flow<String?>

    suspend fun clearTokens()

    suspend fun clearAll() {
        clearTokens()
    }

    suspend fun hasTokens(): Boolean

    suspend fun saveUser(user: User)

    suspend fun saveUserData(userData: String)

    suspend fun getUser(): User?

    suspend fun getUserData(): String?

    fun getUserFlow(): Flow<User?>

    suspend fun setLoggedIn(isLoggedIn: Boolean)

    suspend fun isLoggedIn(): Boolean

    fun isLoggedInFlow(): Flow<Boolean>

    suspend fun getUserHash(): String? {
        return getUser()?.userHash
    }
}
