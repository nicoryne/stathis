package citu.edu.stathis.mobile.features.vitals.domain.usecase

import citu.edu.stathis.mobile.features.vitals.data.healthconnect.HealthConnectManager
import javax.inject.Inject

class ConnectToHealthConnectUseCase @Inject constructor(
    private val healthConnectManager: HealthConnectManager
) {
    suspend operator fun invoke() {
        // HealthConnectManager's connect() already updates its internal state.
        // This use case ensures the action is triggered.
        // The ViewModel will observe healthConnectManager.connectionState directly or via another use case.
        healthConnectManager.connect()
    }
}