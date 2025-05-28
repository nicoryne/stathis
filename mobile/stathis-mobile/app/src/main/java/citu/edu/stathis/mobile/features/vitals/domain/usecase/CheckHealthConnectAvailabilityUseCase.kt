package citu.edu.stathis.mobile.features.vitals.domain.usecase

import citu.edu.stathis.mobile.features.vitals.data.HealthConnectManager
import javax.inject.Inject

data class HealthConnectAvailabilityStatus(
    val isClientAvailable: Boolean,
    val hasAllPermissions: Boolean
)

class CheckHealthConnectAvailabilityUseCase @Inject constructor(
    private val healthConnectManager: HealthConnectManager
) {
    suspend operator fun invoke(): HealthConnectAvailabilityStatus {
        val isClientAvailable = healthConnectManager.isHealthConnectAvailable()
        val hasPermissions = if (isClientAvailable) healthConnectManager.hasAllPermissions() else false
        return HealthConnectAvailabilityStatus(isClientAvailable, hasPermissions)
    }
}