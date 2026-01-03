package tn.esprit.dam.data.repository

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import tn.esprit.dam.data.Config
import tn.esprit.dam.data.model.Alert
import tn.esprit.dam.data.security.TokenRepository
import javax.inject.Inject

/**
 * AlertRepository - Handles alert and security monitoring data
 * Manages API calls for alerts, breaches, and threat notifications
 */
class AlertRepository @Inject constructor(
    private val httpClient: HttpClient,
    private val tokenRepository: TokenRepository
) {
    
    companion object {
        private const val TAG = "AlertRepository"
        private const val ENDPOINT_ALERTS = "${Config.API_ROOT}/alerts"
    }
    
    /**
     * Get alert history for the user
     */
    suspend fun getAlertHistory(): List<Alert> {
        return try {
            Log.d(TAG, "Fetching alert history")
            
            val response = httpClient.get("$ENDPOINT_ALERTS/history")
            
            return when (response.status) {
                HttpStatusCode.OK -> {
                    val alerts = response.body<List<Alert>>()
                    Log.d(TAG, "Got ${alerts.size} alerts")
                    alerts
                }
                HttpStatusCode.Unauthorized -> {
                    Log.w(TAG, "Unauthorized to fetch alerts")
                    tokenRepository.clearTokens()
                    emptyList()
                }
                else -> {
                    Log.e(TAG, "Failed to fetch alerts: ${response.status}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching alerts", e)
            emptyList()
        }
    }
    
    /**
     * Get active alerts only
     */
    suspend fun getActiveAlerts(): List<Alert> {
        return try {
            Log.d(TAG, "Fetching active alerts")
            
            val response = httpClient.get("$ENDPOINT_ALERTS/active")
            
            return when (response.status) {
                HttpStatusCode.OK -> response.body<List<Alert>>()
                else -> emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching active alerts", e)
            emptyList()
        }
    }
    
    /**
     * Mark alert as read
     */
    suspend fun markAsRead(alertId: String): Boolean {
        return try {
            Log.d(TAG, "Marking alert $alertId as read")
            
            val response = httpClient.get("$ENDPOINT_ALERTS/$alertId/read")
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            Log.e(TAG, "Error marking alert as read", e)
            false
        }
    }
    
    /**
     * Dismiss alert
     */
    suspend fun dismissAlert(alertId: String): Boolean {
        return try {
            Log.d(TAG, "Dismissing alert $alertId")
            
            val response = httpClient.get("$ENDPOINT_ALERTS/$alertId/dismiss")
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            Log.e(TAG, "Error dismissing alert", e)
            false
        }
    }
}
