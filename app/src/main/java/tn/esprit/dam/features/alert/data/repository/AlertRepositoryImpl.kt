package tn.esprit.dam.features.alert.data.repository

import tn.esprit.dam.core.network.ApiResult
import tn.esprit.dam.core.network.toResult
import tn.esprit.dam.features.alert.data.model.Alert
import tn.esprit.dam.features.alert.data.model.AlertEventDto
import tn.esprit.dam.features.alert.data.model.DeviceTokenDto
import tn.esprit.dam.features.alert.data.remote.AlertApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepositoryImpl @Inject constructor(
    private val api: AlertApi
) : AlertRepository {

    // Result, not an empty list: a failed load must not look like "no threats".
    override suspend fun getAlertHistory(): Result<List<Alert>> =
        api.getAlerts().toResult()

    override suspend fun markAsRead(alertId: String): Boolean =
        api.markAsRead(alertId) is ApiResult.Success

    override suspend fun markAllAsRead(): Boolean =
        api.markAllAsRead() is ApiResult.Success

    override suspend fun sendAlertEvent(packageName: String, event: String, details: Map<String, String>) {
        api.sendEvent(
            AlertEventDto(
                packageName = packageName,
                event = event,
                timestamp = System.currentTimeMillis(),
                details = details
            )
        )
    }

    override suspend fun registerDeviceToken(token: String): Boolean =
        api.registerDeviceToken(DeviceTokenDto(token = token, platform = "android")) is ApiResult.Success
}
