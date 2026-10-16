package tn.esprit.dam.features.darkweb.data.repository

import tn.esprit.dam.features.darkweb.data.model.Breach
import tn.esprit.dam.features.darkweb.data.model.ManualBreachResult
import kotlinx.coroutines.flow.Flow

interface DarkWebRepository {
    suspend fun getBreaches(): Result<List<Breach>>

    suspend fun checkNow(): Result<Unit>

    suspend fun checkEmail(email: String): Result<List<ManualBreachResult>>

    suspend fun checkPassword(prefix: String): Result<Int>

    suspend fun resolveBreach(breachId: String): Result<Unit>

    fun monitorBreaches(): Flow<List<Breach>>
}
