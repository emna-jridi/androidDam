package tn.esprit.dam.features.vault.data.repository

import tn.esprit.dam.features.vault.data.model.BiometricStatusResponse
import tn.esprit.dam.features.vault.data.model.DevicesListResponse
import androidx.fragment.app.FragmentActivity

interface BiometricAuthRepository {
    sealed class BiometricAuthResult {
        data class Success(
            val accessToken: String,
            val refreshToken: String?,
            val userEmail: String
        ) : BiometricAuthResult()

        data class Error(val message: String) : BiometricAuthResult()
        object Cancelled : BiometricAuthResult()
        object NotRegistered : BiometricAuthResult()
        object BiometricNotAvailable : BiometricAuthResult()
    }

    sealed class RegistrationResult {
        data class Success(val message: String) : RegistrationResult()
        data class Error(val message: String) : RegistrationResult()
        object BiometricNotAvailable : RegistrationResult()
        object Cancelled : RegistrationResult()
    }

    fun isBiometricAvailable(): Boolean

    fun needsEnrollment(): Boolean

    fun isDeviceRegistered(): Boolean

    fun getDeviceId(): String

    fun getDeviceName(): String

    /** [deviceName] defaults to the device's manufacturer + model. */
    suspend fun registerDevice(
        activity: FragmentActivity,
        deviceName: String? = null
    ): RegistrationResult

    suspend fun authenticateWithBiometric(
        activity: FragmentActivity
    ): BiometricAuthResult

    suspend fun removeDevice(removeFromBackend: Boolean = true): Boolean

    suspend fun getDevices(): Result<DevicesListResponse>

    suspend fun getBiometricStatus(): Result<BiometricStatusResponse>
}
