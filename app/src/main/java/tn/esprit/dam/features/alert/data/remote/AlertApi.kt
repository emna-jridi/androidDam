package tn.esprit.dam.features.alert.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.core.network.safeApiCall
import tn.esprit.dam.features.alert.data.model.Alert
import tn.esprit.dam.features.alert.data.model.AlertEventDto
import tn.esprit.dam.features.alert.data.model.DeviceTokenDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertApi @Inject constructor(
    private val client: HttpClient
) {
    companion object {
        private const val ALERTS_URL = "/api/alerts"
    }

    suspend fun getAlerts(): ApiResult<List<Alert>> =
        safeApiCall { client.get(ALERTS_URL) }

    suspend fun markAsRead(alertId: String): ApiResult<Unit> =
        safeApiCall { client.patch("$ALERTS_URL/$alertId/read") }

    suspend fun markAllAsRead(): ApiResult<Unit> =
        safeApiCall { client.patch("$ALERTS_URL/read-all") }

    suspend fun sendEvent(event: AlertEventDto): ApiResult<Unit> =
        safeApiCall { client.post("$ALERTS_URL/event") { setBody(event) } }

    suspend fun registerDeviceToken(body: DeviceTokenDto): ApiResult<Unit> =
        safeApiCall { client.post("$ALERTS_URL/device-token") { setBody(body) } }
}
