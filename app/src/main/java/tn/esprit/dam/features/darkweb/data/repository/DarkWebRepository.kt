package tn.esprit.dam.features.darkweb.data.repository

import tn.esprit.dam.core.network.map
import tn.esprit.dam.core.network.toResult
import tn.esprit.dam.features.darkweb.data.model.Breach
import tn.esprit.dam.features.darkweb.data.remote.DarkWebApi
import tn.esprit.dam.features.darkweb.data.remote.ManualBreachResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class DarkWebRepository @Inject constructor(
    private val api: DarkWebApi
) {
    suspend fun getBreaches(): Result<List<Breach>> {
        return api.getBreaches().toResult()
    }

    suspend fun checkNow(): Result<Unit> {
        return api.checkNow().toResult()
    }

    suspend fun checkEmail(email: String): Result<List<ManualBreachResult>> {
        return api.checkEmail(email).toResult()
    }

    suspend fun checkPassword(prefix: String): Result<Int> {
        return api.checkPassword(prefix).map { it.count }.toResult()
    }

    suspend fun resolveBreach(breachId: String): Result<Unit> {
        return api.resolveBreach(breachId).toResult()
    }

    fun monitorBreaches(): Flow<List<Breach>> = flow {
        emit(getBreaches().getOrDefault(emptyList()))
    }
}
