package tn.esprit.dam.features.alert.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.core.network.Config
import tn.esprit.dam.core.network.getOrNull
import tn.esprit.dam.core.network.safeApiCall
import tn.esprit.dam.features.alert.data.model.Alert
import tn.esprit.dam.features.alert.data.model.AlertEventDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepository @Inject constructor(
    private val httpClient: HttpClient
) {
    companion object {
        private const val ENDPOINT_ALERTS = "${Config.API_URL}/alerts"
    }

    suspend fun getAlertHistory(): List<Alert> {
        return safeApiCall<List<Alert>> { httpClient.get(ENDPOINT_ALERTS) }.getOrNull().orEmpty()
    }

    suspend fun markAsRead(alertId: String): Boolean {
        return safeApiCall<Unit> { httpClient.patch("$ENDPOINT_ALERTS/$alertId/read") } is ApiResult.Success
    }

    suspend fun markAllAsRead(): Boolean {
        return safeApiCall<Unit> { httpClient.patch("$ENDPOINT_ALERTS/read-all") } is ApiResult.Success
    }

    suspend fun sendAlertEvent(packageName: String, event: String, details: Map<String, String>) {
        safeApiCall<Unit> {
            httpClient.post("$ENDPOINT_ALERTS/event") {
                setBody(
                    AlertEventDto(
                        packageName = packageName,
                        event = event,
                        timestamp = System.currentTimeMillis(),
                        details = details
                    )
                )
            }
        }
    }
}
