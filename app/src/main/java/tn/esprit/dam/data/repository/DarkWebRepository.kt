package tn.esprit.dam.data.repository

import tn.esprit.dam.data.model.Breach
import tn.esprit.dam.data.remote.darkweb.DarkWebApi
import tn.esprit.dam.data.remote.darkweb.ManualBreachResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class DarkWebRepository @Inject constructor(
    private val api: DarkWebApi
) {
    suspend fun getBreaches(): Result<List<Breach>> {
        return api.getBreaches()
    }

    suspend fun checkNow(): Result<Unit> {
        return api.checkNow()
    }

    suspend fun checkEmail(email: String): Result<List<ManualBreachResult>> {
        return api.checkEmail(email)
    }

    suspend fun checkPassword(prefix: String): Result<Int> {
        val result = api.checkPassword(prefix)
        return if (result.isSuccess) {
            val count = result.getOrNull()?.count ?: 0
            Result.success(count)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
        }
    }

    suspend fun resolveBreach(breachId: String): Result<Unit> {
        return api.resolveBreach(breachId)
    }

    // Optional: flow-based observation if we add local caching later
    fun monitorBreaches(): Flow<List<Breach>> = flow {
        val result = getBreaches()
        if (result.isSuccess) {
            emit(result.getOrDefault(emptyList()))
        } else {
             emit(emptyList()) 
        }
    }
}
