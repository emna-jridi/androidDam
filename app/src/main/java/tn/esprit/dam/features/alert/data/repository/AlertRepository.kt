package tn.esprit.dam.features.alert.data.repository

import tn.esprit.dam.features.alert.data.model.Alert

interface AlertRepository {
    suspend fun getAlertHistory(): Result<List<Alert>>

    suspend fun markAsRead(alertId: String): Boolean

    suspend fun markAllAsRead(): Boolean

    suspend fun sendAlertEvent(packageName: String, event: String, details: Map<String, String>)

    /** Tells the backend where to send push notifications for the logged-in user. */
    suspend fun registerDeviceToken(token: String): Boolean
}
