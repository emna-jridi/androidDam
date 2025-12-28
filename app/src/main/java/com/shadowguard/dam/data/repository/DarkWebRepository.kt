package com.shadowguard.dam.data.repository

import com.shadowguard.dam.data.model.Breach
import com.shadowguard.dam.data.remote.darkweb.DarkWebApi
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

    suspend fun checkEmail(email: String): Result<List<Map<String, Any>>> {
        return api.checkEmail(email)
    }

    suspend fun checkPassword(prefix: String): Result<Int> {
        val result = api.checkPassword(prefix)
        return if (result.isSuccess) {
            val count = result.getOrNull()?.get("count") ?: 0
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
