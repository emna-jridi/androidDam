package tn.esprit.dam.features.report.data.repository

import tn.esprit.dam.core.network.getOrNull
import tn.esprit.dam.features.report.data.model.AppSafetyReport
import tn.esprit.dam.features.report.data.remote.ReportApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val api: ReportApi
) : ReportRepository {
    override suspend fun getAppSafetyReport(packageName: String): AppSafetyReport? =
        api.getSafetyReport(packageName).getOrNull()
}
