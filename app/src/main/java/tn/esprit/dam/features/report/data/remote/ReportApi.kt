package tn.esprit.dam.features.report.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.core.network.safeApiCall
import tn.esprit.dam.features.report.data.model.AppSafetyReport
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportApi @Inject constructor(
    private val client: HttpClient
) {
    suspend fun getSafetyReport(packageName: String): ApiResult<AppSafetyReport> =
        safeApiCall { client.get("/api/report/$packageName") }
}
