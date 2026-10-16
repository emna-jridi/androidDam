package tn.esprit.dam.features.report.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import tn.esprit.dam.core.network.getOrNull
import tn.esprit.dam.core.network.safeApiCall
import tn.esprit.dam.features.report.data.model.AppSafetyReport
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val httpClient: HttpClient
) {
    suspend fun getAppSafetyReport(packageName: String): AppSafetyReport? {
        return safeApiCall<AppSafetyReport> { httpClient.get("/api/report/$packageName") }.getOrNull()
    }
}
