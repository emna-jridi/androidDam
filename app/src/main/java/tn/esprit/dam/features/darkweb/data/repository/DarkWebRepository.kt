package tn.esprit.dam.features.darkweb.data.repository

import tn.esprit.dam.features.darkweb.data.model.Breach
import tn.esprit.dam.features.darkweb.data.model.ManualBreachResult
import kotlinx.coroutines.flow.Flow

interface DarkWebRepository {
    suspend fun getBreaches(): Result<List<Breach>>

    suspend fun checkNow(): Result<Unit>

    suspend fun checkEmail(email: String): Result<List<ManualBreachResult>>

    /** How many times the password with this SHA-1 [prefix] + [suffix] appears in leaks (0 = not found). Only the prefix is sent. */
    suspend fun checkPassword(prefix: String, suffix: String): Result<Int>

    suspend fun resolveBreach(breachId: String): Result<Unit>

    fun monitorBreaches(): Flow<List<Breach>>
}
