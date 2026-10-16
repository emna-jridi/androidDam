package tn.esprit.dam.features.report.data.repository

import tn.esprit.dam.features.report.data.model.AppSafetyReport

interface ReportRepository {
    suspend fun getAppSafetyReport(packageName: String): AppSafetyReport?
}
