package citu.edu.stathis.mobile.features.vitals.domain.usecase

import androidx.activity.result.contract.ActivityResultContract
import citu.edu.stathis.mobile.features.vitals.data.HealthConnectManager
import javax.inject.Inject

class RequestHealthConnectPermissionsUseCase @Inject constructor(
    private val healthConnectManager: HealthConnectManager
) {
    fun getPermissionsSet(): Set<String> {
        return healthConnectManager.permissions
    }

    fun createPermissionRequestContract(): ActivityResultContract<Set<String>, Set<String>> {
        return healthConnectManager.createPermissionRequestContract()
    }
}