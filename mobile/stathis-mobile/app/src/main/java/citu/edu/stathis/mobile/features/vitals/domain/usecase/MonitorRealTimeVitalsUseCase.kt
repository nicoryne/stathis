package citu.edu.stathis.mobile.features.vitals.domain.usecase

import citu.edu.stathis.mobile.features.vitals.data.HealthConnectManager
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class MonitorRealTimeVitalsUseCase @Inject constructor(
    private val healthConnectManager: HealthConnectManager
) {
    operator fun invoke(): StateFlow<VitalSigns?> {
        return healthConnectManager.vitalSigns
    }

    suspend fun refreshVitals() {
        healthConnectManager.fetchLatestVitals()
    }

    fun setUserIdForMonitoring(userId: String) {
        healthConnectManager.setUserId(userId)
    }

    suspend fun ensureConnectionAndPermissions() {
        if (healthConnectManager.connectionState.value != HealthConnectManager.ConnectionState.CONNECTED) {
            healthConnectManager.connect()
        }
    }
}