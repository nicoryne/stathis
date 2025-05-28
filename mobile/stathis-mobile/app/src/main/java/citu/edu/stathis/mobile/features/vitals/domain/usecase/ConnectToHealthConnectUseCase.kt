package citu.edu.stathis.mobile.features.vitals.domain.usecase

import citu.edu.stathis.mobile.features.vitals.data.HealthConnectManager
import javax.inject.Inject

class ConnectToHealthConnectUseCase @Inject constructor(
    private val healthConnectManager: HealthConnectManager
) {
    suspend operator fun invoke(): HealthConnectManager.ConnectionState {
        healthConnectManager.connect()
        return healthConnectManager.connectionState.value
    }
}