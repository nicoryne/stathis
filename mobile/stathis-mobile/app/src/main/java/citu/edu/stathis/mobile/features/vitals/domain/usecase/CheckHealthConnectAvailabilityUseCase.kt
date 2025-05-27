package citu.edu.stathis.mobile.features.vitals.domain.usecase

import citu.edu.stathis.mobile.features.vitals.data.healthconnect.HealthConnectManager
import javax.inject.Inject

// Define a simple result class for this use case
data class HealthConnectAvailabilityStatus(
    val isAvailable: Boolean,
    val hasAllPermissions: Boolean
)

class CheckHealthConnectAvailabilityUseCase @Inject constructor(
    private val healthConnectManager: HealthConnectManager
) {
    suspend operator fun invoke(): HealthConnectAvailabilityStatus {
        val isAvailable = healthConnectManager.isHealthConnectAvailable()
        val hasPermissions = if (isAvailable) healthConnectManager.hasAllPermissions() else false
        return HealthConnectAvailabilityStatus(isAvailable, hasPermissions)
    }
}