package citu.edu.stathis.mobile.features.vitals.domain.usecase

import citu.edu.stathis.mobile.features.vitals.data.healthconnect.HealthConnectManager
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns // Correct model import
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class MonitorRealTimeVitalsUseCase @Inject constructor(
    private val healthConnectManager: HealthConnectManager
) {
    // The HealthConnectManager already exposes vitalSigns as a StateFlow.
    // This use case can either directly return that or add more logic if needed.
    operator fun invoke(): StateFlow<VitalSigns?> {
        return healthConnectManager.vitalSigns
    }

    // Optional: If manual refresh is needed and not handled by collecting the StateFlow trigger.
    suspend fun refreshVitals() {
        healthConnectManager.fetchLatestVitals()
    }

    // Ensure HealthConnectManager is initialized with userId before invoking this flow
    fun setUserIdForMonitoring(userId: String) {
        healthConnectManager.setUserId(userId)
    }

    suspend fun ensureConnectionAndPermissions() {
        if (healthConnectManager.connectionState.value != HealthConnectManager.ConnectionState.CONNECTED) {
            healthConnectManager.connect()
        }
    }
}
