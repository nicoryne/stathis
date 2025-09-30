package citu.edu.stathis.mobile.features.vitals.domain.usecase

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import citu.edu.stathis.mobile.features.vitals.domain.repository.VitalsRepository
import citu.edu.stathis.mobile.features.exercise.domain.usecase.GetCurrentUserIdUseCase
import javax.inject.Inject

class SaveVitalsUseCase @Inject constructor(
    private val vitalsRepository: VitalsRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    suspend operator fun invoke(
        vitalSignsFromHealthConnect: VitalSigns,
        classroomId: String? = null,
        taskId: String? = null,
        isPreActivity: Boolean? = null,
        isPostActivity: Boolean? = null
    ): ClientResponse<Unit> {
        val currentUserId = getCurrentUserIdUseCase()
            ?: return ClientResponse(success = false, message = "User not identified. Cannot save vitals.", data = null)

        val vitalsToSave = vitalSignsFromHealthConnect.copy(
            userId = currentUserId,
            classroomId = classroomId ?: vitalSignsFromHealthConnect.classroomId,
            taskId = taskId ?: vitalSignsFromHealthConnect.taskId,
            isPreActivity = isPreActivity ?: vitalSignsFromHealthConnect.isPreActivity,
            isPostActivity = isPostActivity ?: vitalSignsFromHealthConnect.isPostActivity,
            heartRate = vitalSignsFromHealthConnect.heartRate,
            oxygenSaturation = vitalSignsFromHealthConnect.oxygenSaturation
        )
        return vitalsRepository.saveVitals(vitalsToSave)
    }
}
