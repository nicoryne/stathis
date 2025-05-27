package citu.edu.stathis.mobile.features.vitals.domain.usecase

import androidx.activity.result.contract.ActivityResultContract
import citu.edu.stathis.mobile.features.vitals.data.healthconnect.HealthConnectManager
import javax.inject.Inject

class RequestHealthConnectPermissionsUseCase @Inject constructor(
    private val healthConnectManager: HealthConnectManager
) {
    fun getPermissionsSet(): Set<String> {
        return healthConnectManager.permissions.map { it.toString() }.toSet()
    }

    fun createPermissionRequestContract(): ActivityResultContract<Set<String>, Set<String>> {
        return healthConnectManager.createPermissionRequestContract()
    }
}