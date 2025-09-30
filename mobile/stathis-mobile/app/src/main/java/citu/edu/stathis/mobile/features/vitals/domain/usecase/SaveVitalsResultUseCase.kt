package citu.edu.stathis.mobile.features.vitals.domain.usecase

import citu.edu.stathis.mobile.features.common.domain.Result
import citu.edu.stathis.mobile.features.common.domain.toResult
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import citu.edu.stathis.mobile.features.vitals.domain.repository.VitalsRepository
import citu.edu.stathis.mobile.features.exercise.domain.usecase.GetCurrentUserIdUseCase
import javax.inject.Inject

class SaveVitalsResultUseCase @Inject constructor(
    private val vitalsRepository: VitalsRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    suspend operator fun invoke(
        vitalSignsFromHealthConnect: VitalSigns,
        classroomId: String? = null,
        taskId: String? = null,
        isPreActivity: Boolean? = null,
        isPostActivity: Boolean? = null
    ): Result<Unit> {
        val currentUserId = getCurrentUserIdUseCase()
            ?: return Result.Error("User not identified. Cannot save vitals.")

        val vitalsToSave = vitalSignsFromHealthConnect.copy(
            userId = currentUserId,
            classroomId = classroomId ?: vitalSignsFromHealthConnect.classroomId,
            taskId = taskId ?: vitalSignsFromHealthConnect.taskId,
            isPreActivity = isPreActivity ?: vitalSignsFromHealthConnect.isPreActivity,
            isPostActivity = isPostActivity ?: vitalSignsFromHealthConnect.isPostActivity,
            heartRate = vitalSignsFromHealthConnect.heartRate,
            oxygenSaturation = vitalSignsFromHealthConnect.oxygenSaturation
        )
        return vitalsRepository.saveVitals(vitalsToSave).toResult()
    }
}


