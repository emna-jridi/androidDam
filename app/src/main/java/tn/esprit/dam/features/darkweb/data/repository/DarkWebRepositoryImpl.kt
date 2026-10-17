package tn.esprit.dam.features.darkweb.data.repository

import tn.esprit.dam.core.network.map
import tn.esprit.dam.core.network.toResult
import tn.esprit.dam.features.darkweb.data.model.Breach
import tn.esprit.dam.features.darkweb.data.remote.DarkWebApi
import tn.esprit.dam.features.darkweb.data.model.ManualBreachResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class DarkWebRepositoryImpl @Inject constructor(
    private val api: DarkWebApi
) : DarkWebRepository {
    override suspend fun getBreaches(): Result<List<Breach>> {
        return api.getBreaches().toResult()
    }

    override suspend fun checkNow(): Result<Unit> {
        return api.checkNow().toResult()
    }

    override suspend fun checkEmail(email: String): Result<List<ManualBreachResult>> {
        return api.checkEmail(email).toResult()
    }

    override suspend fun checkPassword(prefix: String, suffix: String): Result<Int> {
        // The suffix is compared here, on the device; the server only ever receives the prefix.
        return api.checkPassword(prefix)
            .map { result -> result.suffixes.firstOrNull { it.suffix.equals(suffix, ignoreCase = true) }?.count ?: 0 }
            .toResult()
    }

    override suspend fun resolveBreach(breachId: String): Result<Unit> {
        return api.resolveBreach(breachId).toResult()
    }

    override fun monitorBreaches(): Flow<List<Breach>> = flow {
        emit(getBreaches().getOrDefault(emptyList()))
    }
}
